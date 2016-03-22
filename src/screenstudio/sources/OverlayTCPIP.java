/*
 * Use FFMPEG to procude a video file:
 * ffmpeg -re -r 10 -s 400x480 -f rawvideo -pix_fmt bgr24 -i /tmp/tempfile.bgr24 test.flv
 */
package screenstudio.sources;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import screenstudio.gui.overlays.PanelWebcam;

/**
 *
 * @author patrick
 */
public class OverlayTCPIP implements Runnable {

    private ServerSocket server = null;
    private PanelWebcam mPanel = null;
    private long mFPS = 10;
    private boolean stopMe = false;
    private boolean mIsRunning = false;
    private boolean mIsDrawing = false;

    public OverlayTCPIP(PanelWebcam panel, long fps) throws IOException, InterruptedException {
        mPanel = panel;
        mFPS = fps;
        server = new ServerSocket(8000 + new Random().nextInt(1000));
        server.setSoTimeout(2000);
        new Thread(this).start();
    }

    public int getPort() {
        if (server != null) {
            return server.getLocalPort();
        } else {
            return -1;
        }

    }

    @Override
    public void run() {
        mIsRunning = true;
        stopMe = false;
        java.io.OutputStream output = null;

        try {
            // Pipe created. so we need to paint
            // the panel in the fifo each x ms seconds..
            long nextTimeStamp;
            long delay;
            // Use a BGR 24 bits images as ffmpeg will read  -pix_format BGR24
            BufferedImage img = new BufferedImage(mPanel.getWidth(), mPanel.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            //waiting for connection...
            while (!stopMe && output == null) {
                try {
                    System.out.println("Waiting for connection...");
                    Socket s = server.accept();
                    System.out.println("Got connection...");
                    output = s.getOutputStream();
                } catch (java.net.SocketTimeoutException ex) {
                    //do nothing...
                }
            }
            mPanel.doLayout();
            Graphics g = img.getGraphics();
            while (!stopMe && output != null) {
                nextTimeStamp = System.currentTimeMillis() + (1000 / (mFPS));
                try {
                    if (!mPanel.IsUpdating()){
                        mIsDrawing = true;
                        mPanel.paint(g);
                        mIsDrawing = false;
                    }
                } catch (Exception e) {
                    //Do nothing if painting failed...
                    System.err.println("Error painting overlay..." + e.getMessage());
                }
                byte[] imageBytes = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                output.write(imageBytes);
                //Try to sleep just what is needed to keep a constant fps
                delay = nextTimeStamp - System.currentTimeMillis();
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            }
            g.dispose();
            if (output != null) {
                output.close();
            }
        } catch (IOException ex) {
            //Logger.getLogger(OverlayTCPIP.class.getName()).log(Level.SEVERE, null, ex);
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex1) {
                    Logger.getLogger(OverlayTCPIP.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(OverlayTCPIP.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (server != null) {
            try {
                server.close();
            } catch (IOException ex) {
                Logger.getLogger(OverlayTCPIP.class.getName()).log(Level.SEVERE, null, ex);
            }
            server = null;
        }
        mIsRunning = false;
    }

    public void stop() {
        stopMe = true;
    }

    public boolean isRunning() {
        return mIsRunning;
    }
}