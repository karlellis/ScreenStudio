/*
 * Copyright (C) 2016 patrick
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import screenstudio.sources.DesktopViewer;
import screenstudio.sources.NotificationListener;
import screenstudio.sources.Screen;
import screenstudio.sources.UDPNotifications;
import screenstudio.sources.WebcamViewer;

/**
 *
 * @author patrick
 */
public class Renderer implements NotificationListener {

    private WebcamViewer mWebcam;
    private DesktopViewer mDesktop;
    private long startingTime;
    private long showEndTime;
    private JLabel lblText;
    private boolean mIsUpdating = false;
    private boolean mStopMe = false;
    private String mText = "";
    private long nextTextUpdate = 0;
    private PanelLocation panelLocation;
    private WebcamLocation webcamLocation;
    private BufferedImage webcamGreenScreen = null;
    private final BufferedImage textBuffer;
    private UDPNotifications notifications;
    private long lastNotificationTime = 0;
    private JLabel notificationMessage = null;
    private String homeFolder = "";

    private int desktopX = 0;
    private int desktopY = 0;
    private int textX = 0;
    private int textY = 0;
    private int webcamX = 0;
    private int webcamY = 0;
    private int textSize = 0;
    private int mGreenScreenSensitivity = 1;

    private boolean mWebcamFocus = false;

    @Override
    public void received(String message) {
        lastNotificationTime = System.currentTimeMillis();
        notificationMessage.setText("<HTML><BODY width=" + notificationMessage.getWidth() + " height=" + notificationMessage.getHeight() + ">" + message + "</BODY></HTML>");
        notificationMessage.validate();
        System.out.println("Message received: " + message);
    }

    public enum WebcamLocation {

        Top,
        Bottom,
        Left,
        Right,
    }

    public enum PanelLocation {

        Top,
        Bottom,
        Left,
        Right,
    }

    public int getWidth() {
        switch (panelLocation) {
            case Top:
            case Bottom:
                return mDesktop.getImage().getWidth(null);
            case Left:
            case Right:
                return mDesktop.getImage().getWidth(null) + lblText.getWidth();
        }
        return mDesktop.getImage().getWidth(null);
    }

    public int getHeight() {
        switch (panelLocation) {
            case Top:
            case Bottom:
                return mDesktop.getImage().getHeight(null) + lblText.getHeight();
            case Left:
            case Right:
                return mDesktop.getImage().getHeight(null);
        }
        return mDesktop.getImage().getHeight(null);
    }

    public void setGreenScreenSensitivity(int s){
        mGreenScreenSensitivity = s;
    }
    public void setWebcamFocus(boolean focus) {
        mWebcamFocus = focus && mWebcam != null && !"WEBCAM".equals(mDesktop.getId());
    }

    public boolean isWebcamFocus() {
        return mWebcamFocus;
    }

    private void setPositions() {
        desktopX = 0;
        desktopY = 0;
        textX = 0;
        textY = 0;
        webcamX = 0;
        webcamY = 0;
        switch (panelLocation) {
            case Top:

                if (textBuffer != null) {
                    desktopY = textBuffer.getHeight();
                }
                if (mWebcam != null) {
                    switch (webcamLocation) {
                        case Top:
                        case Left:
                            textX = mWebcam.getImage().getWidth(null);
                            break;
                        case Bottom:
                        case Right:
                            if (textBuffer != null) {
                                webcamX = textBuffer.getWidth();
                            } else {
                                webcamX = mDesktop.getImage().getWidth(null) - mWebcam.getImage().getWidth(null);
                            }
                            break;
                    }
                }
                break;
            case Bottom:
                if (mWebcam != null) {
                    switch (webcamLocation) {
                        case Top:
                        case Left:
                            textX = mWebcam.getImage().getWidth(null);
                            textY = mWebcam.getImage().getHeight(null);
                            if (textBuffer != null) {
                                webcamY = mDesktop.getImage().getHeight(null);
                            } else {
                                webcamY = mDesktop.getImage().getHeight(null) - mWebcam.getImage().getHeight(null);
                            }
                            break;
                        case Bottom:
                        case Right:
                            textY = mDesktop.getImage().getHeight(null);
                            if (textBuffer != null) {
                                webcamX = textBuffer.getWidth();
                                webcamY = mDesktop.getImage().getHeight(null);
                            } else {
                                webcamX = mDesktop.getImage().getWidth(null) - mWebcam.getImage().getWidth(null);
                                webcamY = mDesktop.getImage().getHeight(null) - mWebcam.getImage().getHeight(null);
                            }

                            break;
                    }
                } else {
                    textY = mDesktop.getImage().getHeight(null);
                }
                break;
            case Left:
                if (textBuffer != null) {
                    desktopX = textBuffer.getWidth();
                }

                if (mWebcam != null) {
                    switch (webcamLocation) {
                        case Top:
                        case Left:
                            textX = mWebcam.getImage().getHeight(null);
                            break;
                        case Bottom:
                        case Right:
                            if (textBuffer != null) {
                                webcamY = textBuffer.getHeight();
                            } else {
                                webcamY = mDesktop.getImage().getHeight(null) - mWebcam.getImage().getHeight(null);
                            }

                            break;
                    }
                }
                break;
            case Right:
                if (mWebcam != null) {
                    switch (webcamLocation) {
                        case Top:
                        case Left:
                            textX = mDesktop.getImage().getWidth(null);
                            textY = mWebcam.getImage().getHeight(null);
                            if (textBuffer != null) {
                                webcamX = mDesktop.getImage().getWidth(null);
                            } else {
                                webcamX = mDesktop.getImage().getWidth(null) - mWebcam.getImage().getWidth(null);
                            }
                            break;
                        case Bottom:
                        case Right:
                            textX = mDesktop.getImage().getWidth(null);
                            if (textBuffer != null) {
                                webcamX = mDesktop.getImage().getWidth(null);
                                webcamY = textBuffer.getHeight();
                            } else {
                                webcamX = mDesktop.getImage().getWidth(null) - mWebcam.getImage().getWidth(null);
                                webcamY = mDesktop.getImage().getHeight(null) - mWebcam.getImage().getHeight(null);
                            }
                            break;
                    }
                } else {
                    textX = mDesktop.getImage().getWidth(null);
                }
                break;
        }
        if (mDesktop.getId().equals("WEBCAM")) {
            webcamX = desktopX;
            webcamY = desktopY;
        }
    }

    public Renderer(PanelLocation location, int panelSize, screenstudio.sources.Webcam webcam, Screen screen, int showDuration) {
        startingTime = System.currentTimeMillis();
        showEndTime = System.currentTimeMillis() + (showDuration * 60000);
        textSize = panelSize;
        notifications = new UDPNotifications(this);
        notificationMessage = new JLabel("<HTML><BODY></BODY></HTML>");
        if (screen.getId().equals("WEBCAM") && webcam != null) {
            screen.setSize(new Rectangle(webcam.getWidth(), webcam.getHeight()));
        }
        mDesktop = new DesktopViewer(screen);
        new Thread(mDesktop).start();
        mWebcam = null;
        if (webcam != null) {
            if (webcam.isGreenScreen()) {
                webcam.setGreenSensitivity(webcam.getGcreenSensitivity());
                webcamGreenScreen = new BufferedImage(webcam.getWidth(), webcam.getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            mWebcam = new WebcamViewer(screen, new File(webcam.getDevice()), webcam.getWidth(), webcam.getHeight(), webcam.getFps(), webcam.isGreenScreen());
            new Thread(mWebcam).start();
        }
        lblText = new JLabel();
        panelLocation = location;
        if (webcam != null) {
            webcamLocation = webcam.getLocation();
        }

        switch (location) {
            case Top:
            case Bottom:
                if (webcam != null && !screen.getId().equals("WEBCAM")) {
                    lblText.setPreferredSize(new Dimension(screen.getWidth() - webcam.getWidth(), webcam.getHeight()));
                } else {
                    lblText.setPreferredSize(new Dimension(screen.getWidth(), textSize));
                }
                break;
            case Left:
            case Right:
                if (webcam != null && !screen.getId().equals("WEBCAM")) {
                    lblText.setPreferredSize(new Dimension(webcam.getWidth(), screen.getHeight() - webcam.getHeight()));
                } else {
                    lblText.setPreferredSize(new Dimension(textSize, screen.getHeight()));
                }
                break;
        }
        lblText.setSize(lblText.getPreferredSize());
        lblText.setLocation(0, 0);
        if (panelSize > 0) {
            textBuffer = new BufferedImage((int) lblText.getPreferredSize().getWidth(), (int) lblText.getPreferredSize().getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        } else {
            textBuffer = null;
        }
        notificationMessage.setPreferredSize(new Dimension(screen.getWidth(), 150));
        notificationMessage.setVisible(true);
        notificationMessage.setSize(notificationMessage.getPreferredSize());
        notificationMessage.setLocation(0, 250);
        notificationMessage.setVerticalAlignment(JLabel.TOP);
        notificationMessage.setFont(new Font("Monospaced", Font.BOLD, 24));
    }

    public boolean IsUpdating() {
        return mIsUpdating;
    }

    public int getPort() {
        return notifications.getPort();
    }

    public void stop() {
        mStopMe = true;
        mDesktop.stop();
        if (mWebcam != null) {
            mWebcam.stop();
        }
    }

    public void paint(Graphics g) {
        if (System.currentTimeMillis() > nextTextUpdate) {
            setPositions();
            if (textBuffer != null) {
                lblText.setText(replaceTags(mText));
                lblText.revalidate();
            }
            nextTextUpdate = System.currentTimeMillis() + 1000;
        }
        if (!mDesktop.getId().equals("WEBCAM")) {
            Image desktop = mDesktop.getImage();
            g.drawImage(desktop, desktopX, desktopY, null);
        }
        if (textBuffer != null) {
            lblText.paint(textBuffer.getGraphics());
            g.drawImage(textBuffer, textX, textY, null);
        }
        if (mWebcam != null) {
            BufferedImage webcam = mWebcam.getImage();
            if (mWebcamFocus) {
                int w = webcam.getWidth(null) * 3;
                int h = webcam.getHeight(null) * 3;
                int x = desktopX + ((mDesktop.getImage().getWidth(null) - w) / 2);
                int y = desktopY + ((mDesktop.getImage().getHeight(null) - h) / 2);
                g.drawImage(webcam.getScaledInstance(w, h, Image.SCALE_FAST), x, y, null);
                if (textBuffer != null) {
                    g.setColor(Color.darkGray);
                    g.fillRect(webcamX, webcamY, webcam.getWidth(null), webcam.getHeight(null));
                }
            } else if (webcamGreenScreen != null) {
                webcamGreenScreen.createGraphics().drawImage(webcam, 0, 0, null);
                for (int x = 0; x < webcamGreenScreen.getWidth(); x++) {
                    for (int y = 0; y < webcamGreenScreen.getHeight(); y++) {
                        int c = webcamGreenScreen.getRGB(x, y);
                        int r = ((c & 0x00FF0000) >> 16) / 64;
                        int gr = ((c & 0x0000FF00) >> 8) / 64;
                        int b = (c & 0x000000FF) / 32;

                        if (r < (gr-mGreenScreenSensitivity) && b < (gr-mGreenScreenSensitivity)) {
                            webcamGreenScreen.setRGB(x, y, 0);
                        }
                    }
                }
                g.drawImage(webcamGreenScreen, webcamX, webcamY, null);
            } else {
                g.drawImage(webcam, webcamX, webcamY, null);
            }
        }
        if (System.currentTimeMillis() - lastNotificationTime <= 10000) {
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (10000 - (System.currentTimeMillis() - lastNotificationTime)) / 10000F));
            notificationMessage.paint(g);
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    public void setText(String text, String userTextContent, String command, String homeFolder) {
        mIsUpdating = true;
        this.homeFolder = homeFolder;
        mText = text.replaceAll("@TEXT", userTextContent);
        if (command.length() > 0) {
            String commandContent = getCommandContent(command);
            if (mText.toUpperCase().contains("<HTML")) {
                commandContent = commandContent.replaceAll("\n", "<BR>");
            }
            mText = mText.replaceAll("@COMMAND", commandContent);
        }
        if (mText.toUpperCase().contains("<HTML>")) {
            mText = mText.replaceFirst("<BODY", "<BODY width=" + (int) lblText.getPreferredSize().getWidth() + " height=" + (int) lblText.getPreferredSize().getHeight());
            mText = mText.replaceFirst("<body", "<body width=" + (int) lblText.getPreferredSize().getWidth() + " height=" + (int) lblText.getPreferredSize().getHeight());
        }
        mIsUpdating = false;
    }

    private String getCommandContent(String command) {
        String retValue = "";
        try {
            Process p = Runtime.getRuntime().exec(command);
            InputStream in = p.getInputStream();
            byte[] data = new byte[65000];
            int count = in.read(data);
            while (count > 0 && !mStopMe) {
                retValue += new String(data, 0, count);
                count = in.read(data);
            }
            retValue = retValue.trim();
        } catch (IOException ex) {
            Logger.getLogger(Renderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retValue;
    }

    private final DateFormat formatDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    private final DateFormat formatTime = DateFormat.getTimeInstance(DateFormat.LONG, Locale.getDefault());

    private String replaceTags(String text) {
        String retValue = text + "";
        retValue = retValue.replaceAll("@CURRENTDATE", formatDate.format(new Date()));
        retValue = retValue.replaceAll("@CURRENTTIME", formatTime.format(new Date()));
        retValue = retValue.replaceAll("@RECORDINGTIME", (System.currentTimeMillis() - startingTime) / 60000 + " min");
        retValue = retValue.replaceAll("@STARTTIME", formatTime.format(new Date(startingTime)));
        retValue = retValue.replaceAll("@REMAININGTIME", (((showEndTime - System.currentTimeMillis()) / 60000) + 1) + " min");
        if (!homeFolder.isEmpty()) {
            String parentPath = "file://" + homeFolder + "";
            parentPath = parentPath.replaceAll("\\.\\/", "");
            retValue = retValue.replaceAll("\\.\\/", parentPath + "/");
        }
        return retValue;
    }

}
