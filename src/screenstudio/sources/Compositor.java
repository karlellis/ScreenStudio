/*
 * Copyright (C) 2016 Patrick Balleux (Twitter: @patrickballeux)
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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import screenstudio.gui.LabelText;
import screenstudio.targets.Layout;
import screenstudio.targets.Layout.SourceType;

/**
 *
 * @author patrick
 */
public class Compositor {

    private final java.util.List<Source> mSources;
    private final int mFPS;
    private final Rectangle mOutputSize;
    private final byte[] mData;
    private boolean mIsReady = false;
    private final Graphics2D g;

    public Compositor(java.util.List<Source> sources, Rectangle outputSize, int fps) {
        sources.sort((a, b) -> Integer.compare(b.getZOrder(), a.getZOrder()));
        mSources = sources;
        mOutputSize = outputSize;
        mFPS = fps;
        for (Source s : mSources) {
            s.start();
            while (s.getImage() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Compositor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        BufferedImage img = new BufferedImage(mOutputSize.width, mOutputSize.height, BufferedImage.TYPE_3BYTE_BGR);
        g = img.createGraphics();
        mData = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        mIsReady = true;
    }

    public boolean isReady() {
        return mIsReady;
    }

    public byte[] getData() {
        java.util.Arrays.fill(mData, (byte) 0);
        for (Source s : mSources) {
            g.setComposite(s.getAlpha());
            g.drawImage(s.getImage(), s.getBounds().x, s.getBounds().y, null);
        }
        return mData;
    }

    public int getFPS() {
        return mFPS;
    }

    public int getWidth() {
        return mOutputSize.width;
    }

    public int getHeight() {
        return mOutputSize.height;
    }

    public void stop() {
        System.out.println("Compositor is stopping");
        for (Source s : mSources) {
            s.stop();
        }
    }

    public static List<Source> getSources(JTable sources, int fps) {
        java.util.ArrayList<Source> list = new java.util.ArrayList();
        for (int i = sources.getRowCount() - 1; i >= 0; i--) {
            if ((Boolean) sources.getValueAt(i, 0)) {
                int sx = (int) sources.getValueAt(i, 3);
                int sy = (int) sources.getValueAt(i, 4);
                int sw = (int) sources.getValueAt(i, 5);
                int sh = (int) sources.getValueAt(i, 6);
                float alpha = new Float(sources.getValueAt(i, 7).toString());
                Object source = sources.getValueAt(i, 2);
                // Detect type of source...
                if (source instanceof Screen) {
                    Screen screen = (Screen) source;
                    SourceFFMpeg s = SourceFFMpeg.getDesktopInstance(screen, new Rectangle(sx, sy, sw, sh), fps);
                    s.setAlpha(alpha);
                    s.setZOrder(i);
                    s.setFPS(fps);
                    list.add(s);
                } else if (source instanceof Webcam) {
                    Webcam webcam = (Webcam) source;
                    SourceFFMpeg s = SourceFFMpeg.getWebcamInstance(webcam, fps);
                    s.getBounds().setBounds(new Rectangle(sx, sy, sw, sh));
                    s.setAlpha(alpha);
                    s.setZOrder(i);
                    s.setFPS(fps);
                    list.add(s);
                } else if (source instanceof File) {
                    switch ((Layout.SourceType) sources.getValueAt(i, 1)) {
                        case Image:
                            list.add(new SourceImage(new Rectangle(sx, sy, sw, sh), i, alpha, (File) source));
                            break;
                        case Video:
                            SourceFFMpeg s = SourceFFMpeg.getFileInstance(new Rectangle(sx, sy, sw, sh), ((File) source), fps);
                            s.setAlpha(alpha);
                            s.setZOrder(i);
                            list.add(s);
                            break;
                    }
                } else if (source instanceof LabelText) {
                    list.add(new SourceLabel(new Rectangle(sx, sy, sw, sh), i, alpha, ((LabelText) source)));
                }
            }
        }
        return list;
    }

}
