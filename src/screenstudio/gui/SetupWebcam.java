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
package screenstudio.gui;

import screenstudio.gui.overlays.Renderer;
import screenstudio.sources.Webcam;

/**
 *
 * @author patrick
 */
public class SetupWebcam extends javax.swing.JDialog {

    private final Webcam webcam;

    /**
     * Creates new form SetupWebcam
     *
     * @param webcam
     * @param parent
     * @param modal
     */
    public SetupWebcam(Webcam webcam, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        cboLocation.removeAllItems();
        for (Renderer.WebcamLocation l : Renderer.WebcamLocation.values()) {
            cboLocation.addItem(l);
            if (l == webcam.getLocation()) {
                cboLocation.setSelectedItem(l);
            }
        }
        this.webcam = webcam;
        lblWebcamName.setText(webcam.toString());
        this.setTitle(webcam.toString());
        spinWidth.setValue(webcam.getWidth());
        spinHeight.setValue(webcam.getHeight());
        chkGreenScreenMode.setSelected(webcam.isGreenScreen());
        sliderGreenScreenSensitivity.setValue(webcam.getGcreenSensitivity());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblWebcamName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        spinWidth = new javax.swing.JSpinner();
        spinHeight = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        cboLocation = new javax.swing.JComboBox<>();
        chkGreenScreenMode = new javax.swing.JCheckBox();
        sliderGreenScreenSensitivity = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblWebcamName.setFont(new java.awt.Font("Ubuntu", 1, 14)); // NOI18N
        lblWebcamName.setText("jLabel1");

        jLabel1.setText("Width");

        jLabel2.setText("Height");

        spinWidth.setModel(new javax.swing.SpinnerNumberModel(320, 100, 999, 1));
        spinWidth.setEditor(new javax.swing.JSpinner.NumberEditor(spinWidth, "#0"));
        spinWidth.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                spinWidthPropertyChange(evt);
            }
        });

        spinHeight.setModel(new javax.swing.SpinnerNumberModel(240, 100, 999, 1));
        spinHeight.setEditor(new javax.swing.JSpinner.NumberEditor(spinHeight, "#0"));
        spinHeight.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                spinHeightPropertyChange(evt);
            }
        });

        jLabel3.setText("Location");

        cboLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLocationActionPerformed(evt);
            }
        });

        chkGreenScreenMode.setText("Green Screen Mode");
        chkGreenScreenMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGreenScreenModeActionPerformed(evt);
            }
        });

        sliderGreenScreenSensitivity.setMajorTickSpacing(16);
        sliderGreenScreenSensitivity.setMaximum(64);
        sliderGreenScreenSensitivity.setMinorTickSpacing(4);
        sliderGreenScreenSensitivity.setPaintLabels(true);
        sliderGreenScreenSensitivity.setPaintTicks(true);
        sliderGreenScreenSensitivity.setToolTipText("Set sensitivity on Green Screen Mode");
        sliderGreenScreenSensitivity.setValue(5);
        sliderGreenScreenSensitivity.setName(""); // NOI18N
        sliderGreenScreenSensitivity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderGreenScreenSensitivityStateChanged(evt);
            }
        });

        jLabel4.setText("Sensitivity");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblWebcamName)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(spinHeight, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                        .addComponent(spinWidth)
                                        .addComponent(cboLocation, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(81, 81, 81)
                                .addComponent(chkGreenScreenMode))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(13, 13, 13)
                                .addComponent(sliderGreenScreenSensitivity, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWebcamName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cboLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkGreenScreenMode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(sliderGreenScreenSensitivity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void spinWidthPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_spinWidthPropertyChange
        if (webcam != null) {
            webcam.setWidth((Integer) spinWidth.getValue());
        }
    }//GEN-LAST:event_spinWidthPropertyChange

    private void spinHeightPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_spinHeightPropertyChange
        if (webcam != null) {
            webcam.setHeight((Integer) spinHeight.getValue());
        }
    }//GEN-LAST:event_spinHeightPropertyChange

    private void cboLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLocationActionPerformed
        if (webcam != null) {
            webcam.setLocation((Renderer.WebcamLocation) cboLocation.getSelectedItem());
        }

    }//GEN-LAST:event_cboLocationActionPerformed

    private void chkGreenScreenModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGreenScreenModeActionPerformed
        webcam.setGreenScreen(chkGreenScreenMode.isSelected());
    }//GEN-LAST:event_chkGreenScreenModeActionPerformed

    private void sliderGreenScreenSensitivityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderGreenScreenSensitivityStateChanged
        webcam.setGreenSensitivity(sliderGreenScreenSensitivity.getValue());
    }//GEN-LAST:event_sliderGreenScreenSensitivityStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Renderer.WebcamLocation> cboLocation;
    private javax.swing.JCheckBox chkGreenScreenMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblWebcamName;
    private javax.swing.JSlider sliderGreenScreenSensitivity;
    private javax.swing.JSpinner spinHeight;
    private javax.swing.JSpinner spinWidth;
    // End of variables declaration//GEN-END:variables
}
