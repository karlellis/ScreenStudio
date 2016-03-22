/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenstudio.sources;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class WebcamViewer extends javax.swing.JPanel implements Runnable{

    private final File mDevice ;
    private final int mWidth;
    private final int mHeight;
    private BufferedImage buffer;
    private boolean stopMe = false;
    /**
     * Creates new form WebcamViewer
     * @param device
     * @param width
     * @param height
     */
    public WebcamViewer(File device,int width, int height) {
        initComponents();
        mDevice = device;
        mWidth = width;
        mHeight = height;
        this.setSize(width, height);
        buffer = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
    }

    public void stop(){
        stopMe = true;
    }
    @Override
    public void paint(Graphics g){
        g.drawImage(buffer, 0, 0, this);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        try {
            stopMe = false;
            Process p = Runtime.getRuntime().exec("ffmpeg -nostats -loglevel 0 -r 10 -video_size " +mWidth+"x" +mHeight+ " -f video4linux2 -i " + mDevice + " -f rawvideo -pix_fmt bgr24 -");
            java.io.DataInputStream in = new java.io.DataInputStream(p.getInputStream());
            byte[] imageBytes = ((DataBufferByte) buffer.getRaster().getDataBuffer()).getData();
            while(!stopMe){
                in.readFully(imageBytes);
                this.repaint();
            }
            in.close();
            p.destroy();
        } catch (IOException ex) {
            Logger.getLogger(WebcamViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
