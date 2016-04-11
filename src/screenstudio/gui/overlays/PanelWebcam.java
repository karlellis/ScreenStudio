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
package screenstudio.gui.overlays;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import screenstudio.sources.WebcamViewer;

/**
 *
 * @author patrick
 */
public final class PanelWebcam extends javax.swing.JPanel implements TextContent {

    private final WebcamViewer mViewer;
    private final long startingTime;
    private final long showEndTime;
    private boolean mIsUpdating = false;
    private boolean mStopMe = false;
    private String mText = "";
    public enum WebcamLocation{
        Top,
        Bottom,
    }

    /**
     * Creates new form PanelWebcam
     *
     * @param webcam
     * @param width
     * @param height
     * @param showDuration
     * @param webcamTitle
     */
    public PanelWebcam(screenstudio.sources.Webcam webcam, int width, int height, int showDuration, String webcamTitle) {
        initComponents();
        startingTime = System.currentTimeMillis();
        showEndTime = System.currentTimeMillis() + (showDuration * 60000);
        if (webcam != null) {
            mViewer = new WebcamViewer(new File(webcam.getDevice()), webcam.getWidth(), webcam.getHeight(), webcamTitle, webcam.getFps());
            mViewer.setOpaque(true);
            panWebcam.setOpaque(true);
            panWebcam.add(mViewer, BorderLayout.CENTER);
            setWebcamLocation(webcam.getLocation());
            new Thread(mViewer).start();
            System.out.println("Started webcam viewer");
        } else {
            mViewer = null;
            this.remove(panWebcam);
            this.revalidate();
            this.doLayout();
        }
        String tips = "<H1>Supported tags</H1>";
        tips += "<ul>";
        tips += "<li>@CURRENTDATE (Current date)</li>";
        tips += "<li>@CURRENTTIME (Current time)</li>";
        tips += "<li>@RECORDINGTIME (Recording time in minutes)</li>";
        tips += "<li>@STARTTIME (Time when the recording started)</li>";
        tips += "<li>@REMAININGTIME (Time remaining in minutes)</li>";
        tips += "<li>@TEXT (Custom text from the text entry in the Panel tab...)</li>";
        tips += "</ul>";
        this.setToolTipText("<html>" + tips + "</html>");

    }

    public void setWebcamLocation(WebcamLocation location){
        if (mViewer !=null){
            this.remove(panWebcam);
            switch(location){
                case Top:
                    this.add(panWebcam, BorderLayout.NORTH);
                    break;
                case Bottom:
                    this.add(panWebcam, BorderLayout.SOUTH);
                    break;
                default:
                    this.add(panWebcam, BorderLayout.NORTH);
                    break;
            }
        }
    }
    public boolean IsUpdating() {
        return mIsUpdating;
    }

    public void stop() {
        mStopMe = true;
        if (mViewer != null) {
            mViewer.stop();
        }
    }

    public void startPreview() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                mStopMe = false;
                while (!mStopMe) {
                    repaint();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PanelWebcam.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }).start();
    }

    @Override
    public void paint(Graphics g) {
        lblText.setText(replaceTags(mText));
        super.paint(g);
    }

    @Override
    public void setText(String text, String userTextContent) {
        mIsUpdating = true;
        mText = text.replaceAll("@TEXT", userTextContent);
        mIsUpdating = false;
    }

    private DateFormat formatDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    private DateFormat formatTime = DateFormat.getTimeInstance(DateFormat.LONG, Locale.getDefault());

    private String replaceTags(String text) {
        String retValue = text + "";
        retValue = retValue.replaceAll("@CURRENTDATE", formatDate.format(new Date()));
        retValue = retValue.replaceAll("@CURRENTTIME", formatTime.format(new Date()));
        retValue = retValue.replaceAll("@RECORDINGTIME", (System.currentTimeMillis() - startingTime) / 60000 + " min");
        retValue = retValue.replaceAll("@STARTTIME", formatTime.format(new Date(startingTime)));
        retValue = retValue.replaceAll("@REMAININGTIME", (((showEndTime - System.currentTimeMillis()) / 60000) + 1) + " min");
        return retValue;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panWebcam = new javax.swing.JPanel();
        lblText = new javax.swing.JLabel();

        setBackground(java.awt.Color.black);
        setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panWebcam.setBackground(new java.awt.Color(102, 102, 102));
        panWebcam.setBorder(null);
        panWebcam.setForeground(java.awt.Color.white);
        panWebcam.setPreferredSize(new java.awt.Dimension(320, 240));
        panWebcam.setLayout(new java.awt.BorderLayout());
        add(panWebcam, java.awt.BorderLayout.SOUTH);

        lblText.setBackground(java.awt.Color.black);
        lblText.setForeground(new java.awt.Color(19, 219, 19));
        lblText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblText.setText("<html>ScreenStudio</html>");
        lblText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblText.setBorder(null);
        lblText.setOpaque(true);
        lblText.setPreferredSize(new java.awt.Dimension(320, 24));
        add(lblText, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblText;
    private javax.swing.JPanel panWebcam;
    // End of variables declaration//GEN-END:variables
}
