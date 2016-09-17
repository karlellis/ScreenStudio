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
package screenstudio.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import screenstudio.sources.Compositor;
import static screenstudio.sources.Compositor.getSources;
import screenstudio.sources.Source;
import screenstudio.targets.Layout.SourceType;

/**
 *
 * @author patrick
 */
public class SourceLayoutPreview extends javax.swing.JPanel {

    private final JTable mSources;
    private final Rectangle outputSize = new Rectangle(0, 0, 720, 480);
    private Compositor compositer = null;
    private int mFPS = 10;

    /**
     * Creates new form SourceLayoutPreview
     *
     * @param sources
     */
    public SourceLayoutPreview(JTable sources) {
        initComponents();
        this.setDoubleBuffered(true);
        mSources = sources;
    }

    public void setOutputWidth(int value) {
        outputSize.setSize(value, outputSize.height);
    }

    public void setOutputHeight(int value) {
        outputSize.setSize(outputSize.width, value);
    }

    public void setFPS(int value) {
        mFPS = value;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // Draw Output borders...
        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        if (outputSize != null) {
            w = outputSize.width;
            h = outputSize.height;
            double ratio = outputSize.getWidth() / outputSize.getHeight();
            if (h > getHeight() - 1) {
                h = getHeight() - 1;
                w = (int) (h * ratio);
            }
            if (w > getWidth()) {
                w = getWidth();
                h = (int) (w / ratio) - 1;
            }
            x = (getWidth() - w) / 2;
            if (compositer != null) {
                BufferedImage img = new BufferedImage(outputSize.width, outputSize.height, BufferedImage.TYPE_3BYTE_BGR);
                byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                System.arraycopy(compositer.getData(), 0, data, 0, data.length);
                g.drawImage(img.getScaledInstance(w, h, Image.SCALE_FAST), x, y, this);
            } else {
                int fontSize = h / 25;
                Font font = new Font(getFont().getFontName(), getFont().getStyle(), fontSize);
                g.setFont(font);
                g.setColor(Color.BLACK);
                g.fillRect(x, y, w, h);
                if (mSources != null) {
                    for (int i = mSources.getRowCount() - 1; i >= 0; i--) {
                        if ((Boolean) mSources.getValueAt(i, 0)) {
                            g.setFont(font);
                            int sx = (int) mSources.getValueAt(i, 3);
                            int sy = (int) mSources.getValueAt(i, 4);
                            int sw = (int) mSources.getValueAt(i, 5);
                            int sh = (int) mSources.getValueAt(i, 6);
                            sx = (int) (x + (sx * w / outputSize.getWidth()));
                            sy = (int) (y + (sy * h / outputSize.getHeight()));
                            sw = (int) ((sw * w / outputSize.getWidth()));
                            sh = (int) ((sh * h / outputSize.getHeight()));
                            if (sw + sx > x + w) {
                                sw = (x + w - sx);
                            }
                            if (sy + sh > y + h) {
                                sh = y + h - sy;
                            }
                            switch ((SourceType) mSources.getValueAt(i, 1)) {
                                case Desktop:
                                    g.setColor(Color.red);
                                    break;
                                case Webcam:
                                    g.setColor(Color.blue);
                                    break;
                                case Image:
                                    g.setColor(Color.ORANGE);
                                    break;
                                case LabelText:
                                    g.setColor(Color.darkGray);
                                    g.setFont(new Font(font.getFontName(), font.getStyle(),font.getSize()));
                                    break;
                                default:
                                    g.setColor(Color.gray);
                                    break;
                            }
                            g.fillRect(sx, sy, sw, sh);
                            if (i == mSources.getSelectedRow()) {
                                g.setColor(Color.green);
                                g.drawRect(sx, sy, sw, sh);
                            }
                            g.setColor(Color.white);
                            g.drawString(stripHTML(mSources.getValueAt(i, 2).toString()), sx + 5, sy + sh - 10);
                        }
                    }
                }
                g.setFont(font);
                //draw output format that will be used...
                g.setColor(Color.WHITE);
                g.drawString("Output : " + outputSize.width + "X" + outputSize.height, x + 5, y + 20);
            }
        }
    }

    private String stripHTML(String text) {
        String retValue = text.replaceAll("\\<[^>]*>", "");
        if (retValue.length() > 25) {
            retValue = "..." + retValue.substring(retValue.length() - 25, retValue.length());
        }
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

        popPreview = new javax.swing.JPopupMenu();
        popStartPreview = new javax.swing.JMenuItem();
        popStopPreview = new javax.swing.JMenuItem();

        popStartPreview.setText("Start Preview");
        popStartPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popStartPreviewActionPerformed(evt);
            }
        });
        popPreview.add(popStartPreview);

        popStopPreview.setText("Stop Preview");
        popStopPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popStopPreviewActionPerformed(evt);
            }
        });
        popPreview.add(popStopPreview);

        setBackground(new java.awt.Color(51, 51, 51));
        setComponentPopupMenu(popPreview);
        setPreferredSize(new java.awt.Dimension(320, 240));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 291, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 173, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void popStartPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popStartPreviewActionPerformed
        List<Source> list = getSources(mSources, mFPS);
        compositer = new Compositor(list, outputSize, 10);
        new Thread(compositer).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (compositer != null) {
                    repaint();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SourceLayoutPreview.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                repaint();
            }
        }).start();
    }//GEN-LAST:event_popStartPreviewActionPerformed

    private void popStopPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popStopPreviewActionPerformed
        compositer.stop();
        compositer = null;
        repaint();
    }//GEN-LAST:event_popStopPreviewActionPerformed

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        if (mSources.getSelectedRow() != -1) {
            int rowIndex = mSources.getSelectedRow();
            //int currentX = (Integer) mSources.getValueAt(rowIndex, 3);
            //int currentY = (Integer) mSources.getValueAt(rowIndex, 4);
            Point pos = getTranslatedPosition(evt.getX(), evt.getY());
            mSources.setValueAt(pos.x, rowIndex, 3);
            mSources.setValueAt(pos.y, rowIndex, 4);
            //System.out.println("Dragging " + mSources.getValueAt(rowIndex, 2).toString());
            mSources.repaint();
            repaint();
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_formMouseReleased

    private Point getTranslatedPosition(int mouseX, int mouseY) {
        int w = outputSize.width;
        int h = outputSize.height;
        int newX = 0;
        int newY = 0;

        double ratio = outputSize.getWidth() / outputSize.getHeight();
        if (h > getHeight() - 1) {
            h = getHeight() - 1;
            w = (int) (h * ratio);
        }
        if (w > getWidth()) {
            w = getWidth();
            h = (int) (w / ratio) - 1;
        }
        int x = (getWidth() - w) / 2;
        int y = 0;
        // inside the area...
        if (mouseX > x && mouseY > y && mouseX < x + w && mouseY < y + h) {
            newX = (int) ((mouseX - x) * (outputSize.getWidth() / (double) w));
            newY = (int) ((mouseY - y) * (outputSize.getHeight() / (double) h));
        }
        return new Point(newX - 10, newY - 10);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu popPreview;
    private javax.swing.JMenuItem popStartPreview;
    private javax.swing.JMenuItem popStopPreview;
    // End of variables declaration//GEN-END:variables
}
