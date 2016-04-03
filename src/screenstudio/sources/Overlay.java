/*
 * Copyright (C) 2014 Patrick Balleux
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package screenstudio.sources;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import screenstudio.encoder.FFMpeg;
import screenstudio.gui.overlays.PanelWebcam;

/**
 *
 * @author patrick
 */
public class Overlay implements Runnable {

    private File mContent;
    private String mUserTextContent;
    private final PanelWebcam htmlRenderer;
    private final int mFPS;
    private boolean stopME = false;
    private OverlayTCPIP mOutput = null;

    public Overlay(File content, int width, int height, int fps,screenstudio.sources.Webcam webcam,int showDurationTime,String userTextContent,String webcamTitle) throws IOException, InterruptedException {
        mContent = content;
        mUserTextContent = userTextContent;
        htmlRenderer = new PanelWebcam(webcam, width, height,showDurationTime,webcamTitle);
        htmlRenderer.setVisible(true);
        htmlRenderer.setSize(width, height);
        htmlRenderer.setOpaque(true);
        htmlRenderer.repaint();
        mFPS = fps;
        new Thread(this).start();
        mOutput = new OverlayTCPIP(htmlRenderer, mFPS);
    }

    public boolean isRunning(){
        return mOutput.isRunning();
    }
    public void setUserTextContent(String text){
        mUserTextContent = text;
    }
    public void setContent(File content){
        mContent = content;
    }
    public void stop() {
        stopME = true;
    }

    public Dimension getSize() {
        return htmlRenderer.getSize();
    }

    public String OutputURL() {
        if (mOutput == null) {
            return "";
        } else {
            return "tcp://127.0.0.1:" + mOutput.getPort();
        }
    }

    public static ArrayList<File> getOverlays() throws IOException {

        File home = new FFMpeg().getHome();
        File overlayFolder = new File(home,"Overlays");
        if (!overlayFolder.exists()) {
            overlayFolder.mkdir();
        }
        File[] list = overlayFolder.listFiles((File folder, String filename) -> filename.endsWith("html") || filename.endsWith("txt") || filename.endsWith("url"));
        ArrayList<File> newList = new ArrayList();
        newList.add(new ComboBoxFile("None"));
        
        for (File f : list) {
            newList.add(new ComboBoxFile(f.getAbsolutePath()));
        }
        if (list.length == 0){
            //No template found, add a default one...
            byte[] buffer = new byte[65000];
            java.io.InputStream in = Overlay.class.getResource("/screenstudio/sources/Default.html").openStream();
            int count = in.read(buffer);
            in.close();
            FileWriter out = new FileWriter(new File(overlayFolder,"Default.html"));
            out.write(new String(buffer,0,count));
            out.close();
            newList.add(new ComboBoxFile(new File(overlayFolder,"Default.html").getAbsolutePath()));
        }
        return newList;
    }
    //create overlay side by side
//ffmpeg -i capture-1454504589261.mp4 -i capture-1454504589261.mp4 -filter_complex "[0:v]pad=iw+100:ih[left];[left][1:v]overlay=w" test.flv

    @Override
    public void run() {
        stopME = false;
        try {
            htmlRenderer.setText("<html></html>","");
            htmlRenderer.repaint();
            while (!stopME) {
                // Read content into renderer...
                InputStream in = mContent.toURI().toURL().openStream();
                byte[] data = new byte[(int) mContent.length()];
                in.read(data);
                if (mContent.getName().endsWith("html")) {
                    //Reading content from a local html file
                    htmlRenderer.setText(new String(data),mUserTextContent);
                } else if (mContent.getName().endsWith("url")) {
                    //Reading content from a webpage...
                    data = new byte[65536];
                    String addr = new String(data);
                    in.close();
                    in = new java.net.URI(addr).toURL().openStream();
                    StringBuilder html = new StringBuilder();
                    int count = in.read(data);
                    while (count > 0) {
                        html.append(new String(data, 0, count));
                        count = in.read(data);
                    }
                    htmlRenderer.setText(html.toString(),mUserTextContent);
                } else {
                    //Reading raw content from a text file
                    htmlRenderer.setText("<html>" + new String(data).replaceAll("\n", "<br>") + "</html>",mUserTextContent);
                }
                htmlRenderer.repaint();
                in.close();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Overlay.class.getName()).log(Level.SEVERE, null, ex);
                }
                //System.out.println(htmlRenderer.getText());
            }
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(Overlay.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (mOutput != null) {
            mOutput.stop();
        }
        htmlRenderer.stop();
        System.out.println("Exiting Overlay rendering...");
    }
}

class ComboBoxFile extends File {

    public ComboBoxFile(String pathname) {
        super(pathname);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
