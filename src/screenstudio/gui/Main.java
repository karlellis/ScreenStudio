/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenstudio.gui;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import screenstudio.Version;
import screenstudio.encoder.FFMpeg;
import screenstudio.gui.overlays.PanelWebcam;
import screenstudio.sources.Microphone;
import screenstudio.sources.Overlay;
import screenstudio.sources.Screen;
import screenstudio.sources.Webcam;
import screenstudio.targets.SIZES;
import screenstudio.targets.Targets;
import screenstudio.targets.Targets.FORMATS;

/**
 *
 * @author patrick
 */
public class Main extends javax.swing.JFrame implements ItemListener {

    private Targets target = new Targets();
    private Overlay runningOverlay = null;
    private TrayIcon trayIcon = null;
    private long recordingTimestamp = 0;

    private boolean isLoading = false;

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        isLoading = true;
        try {
            target.loadDefault();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        initControls();
        updateCurrentConfigurationStatus();
        this.pack();

        isLoading = false;
    }

    private void initControls() {
        this.setTitle("ScreenStudio " + Version.MAIN);
        try {
            this.setIconImage(javax.imageio.ImageIO.read(this.getClass().getResource("images/logo.png")));
        } catch (IOException ex) {
            lblMessages.setText("Error when loading icons: " + ex.getMessage());
        }

        cboTargets.removeAllItems();
        for (FORMATS f : FORMATS.values()) {
            cboTargets.addItem(f);
            if (f.name().equals(target.format)) {
                cboTargets.setSelectedItem(f);
            }
        }
        cboProfiles.removeAllItems();
        for (SIZES s : SIZES.values()) {
            cboProfiles.addItem(s);
            if (s.name().equals(target.size)) {
                cboProfiles.setSelectedItem(s);
            }
        }
        try {
            cboDisplays.removeAllItems();
            for (Screen s : Screen.getSources()) {
                cboDisplays.addItem(s);
                if (s.toString().equals(target.mainSource)) {
                    cboDisplays.setSelectedItem(s);
                    int x, y, w, h;
                    if (target.captureX.length() > 0) {
                        x = Integer.parseInt(target.captureX);
                        y = Integer.parseInt(target.captureY);
                        w = Integer.parseInt(target.captureWidth);
                        h = Integer.parseInt(target.captureHeight);
                        s.setFps(Integer.parseInt(target.framerate));
                        s.setSize(new Rectangle(x, y, w, h));
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            lblMessages.setText("Error when loading displays: " + ex.getMessage());
        }
        try {
            cboWebcams.removeAllItems();
            for (Webcam o : Webcam.getSources()) {
                cboWebcams.addItem(o);
                if (o.toString().equals(target.webcamDevice)) {
                    cboWebcams.setSelectedItem(o);
                    o.setWidth(Integer.parseInt(target.webcamWidth));
                    o.setHeight(Integer.parseInt(target.webcamHeight));
                    o.setOffset(Double.parseDouble(target.webcamOffset));
                }
            }

        } catch (IOException | InterruptedException ex) {
            lblMessages.setText("Error when loading webcams: " + ex.getMessage());
        }
        try {
            cboAudiosMicrophone.removeAllItems();
            cboAudiosInternal.removeAllItems();
            cboAudiosMicrophone.addItem(new Microphone());
            cboAudiosInternal.addItem(new Microphone());
            for (Microphone o : Microphone.getSources()) {
                if (o.getDescription().toLowerCase().contains("monitor")) {
                    cboAudiosInternal.addItem(o);
                } else {
                    cboAudiosMicrophone.addItem(o);
                }
                if (o.toString().equals(target.mainAudio)) {
                    cboAudiosMicrophone.setSelectedItem(o);
                }
                if (o.toString().equals(target.secondAudio)) {
                    cboAudiosInternal.setSelectedItem(o);
                }
            }
        } catch (IOException | InterruptedException ex) {
            lblMessages.setText("Error when loading audios: " + ex.getMessage());
        }
        cboOverlays.removeAllItems();
        popTrayIconPanelContent.removeAll();
        try {
            for (Object o : Overlay.getOverlays()) {
                cboOverlays.addItem(o);
                java.awt.CheckboxMenuItem menu = new CheckboxMenuItem(o.toString());
                menu.setActionCommand(((File) o).getAbsolutePath());
                menu.addItemListener(this);
                popTrayIconPanelContent.add(menu);
                if (o.toString().equals(target.mainOverlay)) {
                    cboOverlays.setSelectedItem(o);
                    menu.setState(true);
                }
            }
        } catch (Exception ex) {
            lblMessages.setText("Error when loading overlays: " + ex.getMessage());
        }
        if (target.mainOverlayWidth.length() > 0) {
            try {
                spinPanelWidth.setValue(new Integer(target.mainOverlayWidth));
            } catch (Exception ex) {
                lblMessages.setText("Error when loading overlays: " + ex.getMessage());
            }
        }
        if (SystemTray.isSupported() && trayIcon == null) {
            SystemTray tray = SystemTray.getSystemTray();
            try {
                BufferedImage img = new BufferedImage((int) tray.getTrayIconSize().getWidth(), (int) tray.getTrayIconSize().getHeight(), BufferedImage.OPAQUE);
                trayIcon = new TrayIcon(img, this.getTitle(), popTrayIcon);
                trayIcon.setImageAutoSize(false);
                tray.add(trayIcon);

                updateTrayIcon();
            } catch (AWTException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (trayIcon == null) {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        if (target.showDuration.length() > 0) {
            try {
                spinShowDurationTime.setValue(new Integer(target.showDuration));
            } catch (Exception ex) {
                lblMessages.setText("Error when parsing duration time: " + ex.getMessage());
            }
        }
    }

    private void setPanelContent(File file) {
        if (runningOverlay != null) {
            runningOverlay.setContent(file);
        }
    }

    private void updateTrayIcon() {
        if (trayIcon != null) {
            long delta = (System.currentTimeMillis() - recordingTimestamp) / 60000; //In minutes
            Image img = this.createImage((int) trayIcon.getSize().getWidth(), (int) trayIcon.getSize().getHeight());
            Graphics2D g = (Graphics2D) img.getGraphics();
            if (this.processRunning) {
                g.setBackground(Color.GREEN);
                g.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g.clearRect(0, 0, img.getWidth(null), img.getHeight(null));
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(2));
                g.drawRect(1, 1, img.getWidth(null) - 2, img.getHeight(null) - 2);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, img.getHeight(null) / 3));
                String time = (delta) + "";
                int x = (img.getWidth(null) / 2) - (g.getFontMetrics(g.getFont()).stringWidth(time) / 2);
                g.drawString(time, x, (img.getHeight(null) / 2));
                time = "MIN";
                x = (img.getWidth(null) / 2) - (g.getFontMetrics(g.getFont()).stringWidth(time) / 2);
                g.drawString(time, x, img.getHeight(null) - 4);
                g.dispose();
                trayIcon.setImage(img);
                this.trayIcon.setToolTip("Recording Time: " + delta + " minutes...");
            } else {
                g.drawImage(this.getIconImage().getScaledInstance(img.getWidth(null), img.getHeight(null), Image.SCALE_FAST), 0, 0, null);
                trayIcon.setImage(img);
            }
        }
    }

    private void updateCurrentConfigurationStatus() {
        String text = "<HTML>";
        if (cboTargets.getSelectedItem() != null && cboTargets.getSelectedItem() instanceof FORMATS) {
            btnSetTarget.setEnabled(Targets.isRTMP((FORMATS) cboTargets.getSelectedItem()));
            text += "<B>Format:</B> " + cboTargets.getSelectedItem().toString() + "<BR>";
            if (Targets.isRTMP((FORMATS) cboTargets.getSelectedItem())) {
                text += "<B>RTMP Server:</B> " + target.server + "<BR>";
                if (target.rtmpKey.length() == 0) {
                    text += "<font color=red><B>Warning:</B> Secret key not set</font><BR>";
                }
            }
        }
        if (cboProfiles.getSelectedItem() != null) {
            text += "<B>Size:</B> " + cboProfiles.getSelectedItem().toString() + " (bitrate: " + target.outputVideoBitrate + ")<BR>";
        }
        if (cboDisplays.getSelectedItem() != null && cboDisplays.getSelectedItem() instanceof Screen) {
            text += "<B>Display:</B> " + ((Screen) cboDisplays.getSelectedItem()).getDetailledLabel() + "<BR>";
        }
        if (cboWebcams.getSelectedItem() != null) {
            text += "<B>Webcam:</B> " + cboWebcams.getSelectedItem().toString() + "<BR>";
        }
        if (cboAudiosMicrophone.getSelectedItem() != null) {
            text += "<B>Microphone:</B> " + cboAudiosMicrophone.getSelectedItem().toString() + "<BR>";
        }
        if (cboAudiosInternal.getSelectedItem() != null) {
            text += "<B>Internal:</B> " + cboAudiosInternal.getSelectedItem().toString() + "<BR>";
        }
        if (cboOverlays.getSelectedItem() != null) {
            text += "<B>Panel:</B> " + cboOverlays.getSelectedItem().toString() + "<BR>";
        }
        text += "</HTML>";
        lblCurrentTargetConfiguration.setText(text);

    }

    private FFMpeg getCommand() throws IOException, InterruptedException {
        FFMpeg command = new FFMpeg();
        Microphone m = (Microphone) cboAudiosMicrophone.getSelectedItem();
        Microphone i = (Microphone) cboAudiosInternal.getSelectedItem();
        if (m.getDescription().equals("None")){
            m = null;
        }
        if (i.getDescription().equals("None")){
            i = null;
        }
        command.setAudio(FFMpeg.AudioRate.Audio44K, Microphone.getVirtualAudio(m, i), FFMpeg.AudioFormat.pulse);
        Screen s = (Screen) cboDisplays.getSelectedItem();
        command.setCaptureFormat(FFMpeg.CaptureFormat.Desktop, s.getId(), (int) s.getSize().getX(), (int) s.getSize().getY());
        command.setFramerate(s.getFps());
        command.setOutputFormat((FORMATS) cboTargets.getSelectedItem(), target);
        command.setPreset(FFMpeg.Presets.ultrafast);
        if (cboOverlays.getSelectedIndex() > 0) {
            File content = (File) cboOverlays.getSelectedItem();
            if (cboWebcams.getSelectedIndex() > 0) {
                Webcam w = (Webcam) cboWebcams.getSelectedItem();
                runningOverlay = new Overlay(content, (Integer) spinPanelWidth.getValue(), (int) s.getSize().getHeight(), s.getFps(), w, (Integer) spinShowDurationTime.getValue());
            } else {
                runningOverlay = new Overlay(content, (Integer) spinPanelWidth.getValue(), (int) s.getSize().getHeight(), s.getFps(), null, (Integer) spinShowDurationTime.getValue());
            }
            command.setOverlay(runningOverlay);
        }
        command.setOutputSize((int) s.getSize().getWidth(), (int) s.getSize().getHeight(), (SIZES) cboProfiles.getSelectedItem());

        return command;
    }

    private Process streamProcess = null;
    private boolean processRunning = false;

    private void startProcess(String command) {
        btnCapture.setText("Stop");
        popTrayIconRecord.setLabel("Stop recording");
        processRunning = true;
        System.out.println("Screen Studio 2");
        System.out.println("-----------------------");
        System.out.println("Started");
        recordingTimestamp = System.currentTimeMillis();
        try {
            //updateControls(true);
            streamProcess = Runtime.getRuntime().exec(command);
            new Thread(this::updateStatus).start();
            new Thread(this::monitorProcess).start();
        } catch (IOException ex) {
            lblMessages.setText(ex.getMessage());
        }

    }

    private void stopStream(String message) {
        btnCapture.setText("Capture");
        popTrayIconRecord.setLabel(btnCapture.getText());
        if (streamProcess != null) {
            try {
                try (OutputStream out = streamProcess.getOutputStream()) {
                    out.write("q".getBytes());
                    out.flush();
                }
                streamProcess.waitFor(30, TimeUnit.SECONDS);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            streamProcess.destroy();
            streamProcess = null;

        }
        if (runningOverlay != null) {
            runningOverlay.stop();
            runningOverlay = null;
        }
        while (processRunning) {
            //Waiting for the process to completly stop...
            try {
                Thread.sleep(100);
                Thread.yield();
            } catch (InterruptedException ex) {
            }
        }
        try {
            //unloading any virtual audio...
            Microphone.getVirtualAudio(null, null);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(message);
        lblMessages.setText(message);
        if (trayIcon == null) {
            this.setExtendedState(JFrame.NORMAL);
        } else {
            this.setVisible(true);
        }

    }

    private void updateStatus() {
        if (streamProcess != null) {
            BufferedReader reader = null;
            try {
                String line;
                reader = new BufferedReader(new InputStreamReader(streamProcess.getErrorStream()));
                line = reader.readLine();
                recordingTimestamp = System.currentTimeMillis();
                while (line != null) {
                    lblMessages.setText(line);
                    System.out.println(line);
                    if (streamProcess != null) {
                        line = reader.readLine();
                    } else {
                        line = null;
                    }
                }
                recordingTimestamp = 0;
            } catch (IOException ex) {

            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        //
                    }
                }
            }

        }
        processRunning = false;
        recordingTimestamp = 0;
        updateTrayIcon();
    }

    private void monitorProcess() {
        while (streamProcess != null) {
            try {
                lblMessages.setText("Recording...");
                updateTrayIcon();
                System.out.println("Exit Code: " + streamProcess.exitValue());
                stopStream("An error occured...");
            } catch (Exception ex) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex1) {
                    //Logger.getLogger(ScreenStudio.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popTrayIcon = new java.awt.PopupMenu();
        popTrayIconPanelContent = new java.awt.Menu();
        popTrayIconRecord = new java.awt.MenuItem();
        popTrayIconExit = new java.awt.MenuItem();
        tabs = new javax.swing.JTabbedPane();
        panCapture = new javax.swing.JPanel();
        cboTargets = new javax.swing.JComboBox();
        lblTargets = new javax.swing.JLabel();
        lblProfiles = new javax.swing.JLabel();
        cboProfiles = new javax.swing.JComboBox();
        btnCapture = new javax.swing.JButton();
        btnSetTarget = new javax.swing.JButton();
        btnSetProfile = new javax.swing.JButton();
        lblCurrentTargetConfiguration = new javax.swing.JLabel();
        panSources = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cboDisplays = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        cboWebcams = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cboAudiosMicrophone = new javax.swing.JComboBox();
        btnSetDisplay = new javax.swing.JButton();
        btnSetWebcam = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cboOverlays = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        spinPanelWidth = new javax.swing.JSpinner();
        chkDebugMode = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        cboAudiosInternal = new javax.swing.JComboBox();
        btnPreviewPanelContent = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        spinShowDurationTime = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        panStatusBar = new javax.swing.JPanel();
        lblMessages = new javax.swing.JLabel();

        popTrayIcon.setLabel("ScreenStudio");

        popTrayIconPanelContent.setLabel("Panel Content");
        popTrayIcon.add(popTrayIconPanelContent);
        popTrayIcon.addSeparator();
        popTrayIconRecord.setActionCommand("Capture");
        popTrayIconRecord.setLabel("Capture");
        popTrayIconRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popTrayIconRecordActionPerformed(evt);
            }
        });
        popTrayIcon.add(popTrayIconRecord);

        popTrayIconExit.setLabel("Exit");
        popTrayIconExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popTrayIconExitActionPerformed(evt);
            }
        });
        popTrayIcon.add(popTrayIconExit);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFont(new java.awt.Font("Nimbus Roman No9 L", 0, 10)); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cboTargets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboTargets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTargetsActionPerformed(evt);
            }
        });

        lblTargets.setText("Target");

        lblProfiles.setText("Profile");

        cboProfiles.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboProfiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboProfilesActionPerformed(evt);
            }
        });

        btnCapture.setText("Capture");
        btnCapture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCaptureActionPerformed(evt);
            }
        });

        btnSetTarget.setText("...");
        btnSetTarget.setToolTipText("Set RTMP server and secret key");
        btnSetTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetTargetActionPerformed(evt);
            }
        });

        btnSetProfile.setText("...");
        btnSetProfile.setToolTipText("Set bitrate and encording preset");
        btnSetProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetProfileActionPerformed(evt);
            }
        });

        lblCurrentTargetConfiguration.setText("None");
        lblCurrentTargetConfiguration.setAutoscrolls(true);
        lblCurrentTargetConfiguration.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuration"));

        javax.swing.GroupLayout panCaptureLayout = new javax.swing.GroupLayout(panCapture);
        panCapture.setLayout(panCaptureLayout);
        panCaptureLayout.setHorizontalGroup(
            panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panCaptureLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCurrentTargetConfiguration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panCaptureLayout.createSequentialGroup()
                        .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTargets)
                            .addComponent(lblProfiles))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboProfiles, 0, 265, Short.MAX_VALUE)
                            .addComponent(cboTargets, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSetProfile)
                            .addComponent(btnSetTarget, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panCaptureLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCapture)))
                .addContainerGap())
        );
        panCaptureLayout.setVerticalGroup(
            panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panCaptureLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cboTargets)
                        .addComponent(lblTargets))
                    .addComponent(btnSetTarget, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panCaptureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfiles)
                    .addComponent(cboProfiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetProfile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCurrentTargetConfiguration, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCapture)
                .addContainerGap())
        );

        tabs.addTab("Targets", panCapture);

        jLabel2.setText("Display");

        cboDisplays.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboDisplays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboDisplaysActionPerformed(evt);
            }
        });

        jLabel3.setText("Webcam");

        cboWebcams.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboWebcams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboWebcamsActionPerformed(evt);
            }
        });

        jLabel4.setText("Microphone");

        cboAudiosMicrophone.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnSetDisplay.setText("...");
        btnSetDisplay.setToolTipText("Set framerate capture");
        btnSetDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetDisplayActionPerformed(evt);
            }
        });

        btnSetWebcam.setText("...");
        btnSetWebcam.setToolTipText("Set webcam capture size");
        btnSetWebcam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetWebcamActionPerformed(evt);
            }
        });

        jLabel5.setText("Panel");

        cboOverlays.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboOverlays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboOverlaysActionPerformed(evt);
            }
        });

        jLabel6.setText("Panel Width");

        spinPanelWidth.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(320), null, null, Integer.valueOf(10)));
        spinPanelWidth.setEditor(new javax.swing.JSpinner.NumberEditor(spinPanelWidth, ""));

        chkDebugMode.setText("Debug Mode");

        jLabel7.setText("Internal");

        cboAudiosInternal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboAudiosInternal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboAudiosInternalActionPerformed(evt);
            }
        });

        btnPreviewPanelContent.setText("...");
        btnPreviewPanelContent.setToolTipText("Preview panel...");
        btnPreviewPanelContent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewPanelContentActionPerformed(evt);
            }
        });

        jLabel1.setText("Duration");

        spinShowDurationTime.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(30), Integer.valueOf(0), null, Integer.valueOf(15)));

        jLabel8.setText("minutes");

        javax.swing.GroupLayout panSourcesLayout = new javax.swing.GroupLayout(panSources);
        panSources.setLayout(panSourcesLayout);
        panSourcesLayout.setHorizontalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panSourcesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(chkDebugMode))
                    .addGroup(panSourcesLayout.createSequentialGroup()
                        .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panSourcesLayout.createSequentialGroup()
                                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboOverlays, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cboAudiosMicrophone, 0, 215, Short.MAX_VALUE)
                                    .addComponent(cboAudiosInternal, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cboDisplays, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cboWebcams, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnSetWebcam, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnSetDisplay, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnPreviewPanelContent)))
                            .addGroup(panSourcesLayout.createSequentialGroup()
                                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(spinPanelWidth, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                    .addComponent(spinShowDurationTime))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        panSourcesLayout.setVerticalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cboDisplays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetDisplay))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cboWebcams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetWebcam))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cboAudiosMicrophone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cboAudiosInternal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cboOverlays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPreviewPanelContent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(spinPanelWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinShowDurationTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkDebugMode)
                .addContainerGap())
        );

        tabs.addTab("Sources", panSources);

        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);

        panStatusBar.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panStatusBar.setLayout(new javax.swing.BoxLayout(panStatusBar, javax.swing.BoxLayout.LINE_AXIS));

        lblMessages.setText("Welcome");
        panStatusBar.add(lblMessages);

        getContentPane().add(panStatusBar, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCaptureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCaptureActionPerformed

        if (processRunning) {
            stopStream("Stopped...");
        } else {
            try {
                if (trayIcon == null) {
                    this.setExtendedState(JFrame.ICONIFIED);
                } else {
                    this.setVisible(false);
                }

                FFMpeg command = getCommand();
                startProcess(command.getCommand(chkDebugMode.isSelected()));
            } catch (IOException | InterruptedException ex) {
                lblMessages.setText("An error occured: " + ex.getMessage());
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnCaptureActionPerformed

    private void cboTargetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTargetsActionPerformed
        if (!isLoading && cboTargets.getSelectedItem() != null && Targets.isRTMP((FORMATS) cboTargets.getSelectedItem())) {
            btnSetTargetActionPerformed(evt);
        }
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_cboTargetsActionPerformed

    private void cboProfilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboProfilesActionPerformed
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_cboProfilesActionPerformed

    private void cboDisplaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboDisplaysActionPerformed
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_cboDisplaysActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            target.format = cboTargets.getSelectedItem().toString();
            Screen s = (Screen) cboDisplays.getSelectedItem();
            target.captureX = "" + (int) s.getSize().getX();
            target.captureY = "" + (int) s.getSize().getY();
            target.captureWidth = "" + (int) s.getSize().getWidth();
            target.captureHeight = "" + (int) s.getSize().getHeight();
            target.framerate = "" + s.getFps();
            target.size = ((SIZES) cboProfiles.getSelectedItem()).name();
            target.mainSource = cboDisplays.getSelectedItem().toString();
            target.mainAudio = cboAudiosMicrophone.getSelectedItem().toString();
            target.showDuration = spinShowDurationTime.getValue().toString();
            if (cboWebcams.getSelectedIndex() > 0) {
                Webcam w = (Webcam) cboWebcams.getSelectedItem();
                target.webcamDevice = w.toString();
                target.webcamWidth = "" + w.getWidth();
                target.webcamHeight = "" + w.getHeight();
                target.webcamOffset = "" + w.getOffset();
            } else {
                target.webcamDevice = "";
            }
            if (cboOverlays.getSelectedItem() != null) {
                target.mainOverlay = cboOverlays.getSelectedItem().toString();
                target.mainOverlayWidth = spinPanelWidth.getValue().toString();
            }
            target.saveDefault();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowClosing

    private void cboWebcamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboWebcamsActionPerformed
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_cboWebcamsActionPerformed

    private void btnSetWebcamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetWebcamActionPerformed
        SetupWebcam dlg = new SetupWebcam((Webcam) cboWebcams.getSelectedItem(), this, true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        dlg.dispose();
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_btnSetWebcamActionPerformed

    private void btnSetDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetDisplayActionPerformed
        SetupDisplay frm = new SetupDisplay((Screen) cboDisplays.getSelectedItem(), this, true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
        frm.dispose();
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_btnSetDisplayActionPerformed

    private void popTrayIconExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popTrayIconExitActionPerformed
        stopStream("Exiting");
        this.dispose();
        System.exit(0);
    }//GEN-LAST:event_popTrayIconExitActionPerformed

    private void popTrayIconRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popTrayIconRecordActionPerformed
        btnCapture.doClick();
    }//GEN-LAST:event_popTrayIconRecordActionPerformed

    private void btnSetTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetTargetActionPerformed
        if (cboTargets.getSelectedItem() != null) {
            FORMATS f = (FORMATS) cboTargets.getSelectedItem();
            SetupRTMP frm = new SetupRTMP(f, target, this, true);
            frm.setLocationRelativeTo(this);
            frm.setVisible(true);
            frm.dispose();
        }
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_btnSetTargetActionPerformed

    private void btnSetProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetProfileActionPerformed
        if (cboTargets.getSelectedItem() != null) {
            FORMATS f = (FORMATS) cboTargets.getSelectedItem();
            SetupProfile frm = new SetupProfile(target, this, true);
            frm.setLocationRelativeTo(this);
            frm.setVisible(true);
            frm.dispose();
        }
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_btnSetProfileActionPerformed

    private void cboAudiosInternalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboAudiosInternalActionPerformed
        updateCurrentConfigurationStatus();
    }//GEN-LAST:event_cboAudiosInternalActionPerformed

    private void cboOverlaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboOverlaysActionPerformed
        updateCurrentConfigurationStatus();
        btnPreviewPanelContent.setEnabled(cboOverlays.getSelectedIndex() > 0);
    }//GEN-LAST:event_cboOverlaysActionPerformed

    private void btnPreviewPanelContentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewPanelContentActionPerformed
        try {
            Screen s = (Screen) cboDisplays.getSelectedItem();
            JDialog d = new JDialog(this, "ScreenStudio Panel Preview", true);
            d.setLayout(new BorderLayout());
            d.setSize((Integer) spinPanelWidth.getValue(), (int) s.getSize().getHeight());
            d.setLocation((int) s.getSize().getWidth() - (int) spinPanelWidth.getValue(), 0);

            Webcam device = null;
            if (cboWebcams.getSelectedIndex() > 0) {
                device = (Webcam) cboWebcams.getSelectedItem();
            }
            PanelWebcam w = new PanelWebcam(device, (Integer) spinPanelWidth.getValue(), (int) s.getSize().getHeight(), (Integer) spinShowDurationTime.getValue());
            d.add(w, BorderLayout.CENTER);
            File content = (File) cboOverlays.getSelectedItem();
            InputStream in = content.toURI().toURL().openStream();
            byte[] data = new byte[(int) content.length()];
            in.read(data);
            in.close();
            if (content.getName().endsWith("html")) {
                //Reading content from a local html file
                w.setText(new String(data));
            } else if (content.getName().endsWith("url")) {
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
                in.close();
                w.setText(html.toString());
            } else {
                //Reading raw content from a text file
                w.setText("<html>" + new String(data).replaceAll("\n", "<br>") + "</html>");
            }
            d.setVisible(true);
            w.stop();
            d.dispose();
        } catch (IOException | URISyntaxException ex) {
            System.out.println(ex.getMessage());
        }
    }//GEN-LAST:event_btnPreviewPanelContentActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapture;
    private javax.swing.JButton btnPreviewPanelContent;
    private javax.swing.JButton btnSetDisplay;
    private javax.swing.JButton btnSetProfile;
    private javax.swing.JButton btnSetTarget;
    private javax.swing.JButton btnSetWebcam;
    private javax.swing.JComboBox cboAudiosInternal;
    private javax.swing.JComboBox cboAudiosMicrophone;
    private javax.swing.JComboBox cboDisplays;
    private javax.swing.JComboBox cboOverlays;
    private javax.swing.JComboBox cboProfiles;
    private javax.swing.JComboBox cboTargets;
    private javax.swing.JComboBox cboWebcams;
    private javax.swing.JCheckBox chkDebugMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel lblCurrentTargetConfiguration;
    private javax.swing.JLabel lblMessages;
    private javax.swing.JLabel lblProfiles;
    private javax.swing.JLabel lblTargets;
    private javax.swing.JPanel panCapture;
    private javax.swing.JPanel panSources;
    private javax.swing.JPanel panStatusBar;
    private java.awt.PopupMenu popTrayIcon;
    private java.awt.MenuItem popTrayIconExit;
    private java.awt.Menu popTrayIconPanelContent;
    private java.awt.MenuItem popTrayIconRecord;
    private javax.swing.JSpinner spinPanelWidth;
    private javax.swing.JSpinner spinShowDurationTime;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables

    @Override
    public void itemStateChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.SELECTED) {
            setPanelContent(new File(((CheckboxMenuItem) e.getSource()).getActionCommand()));
            for (int i = 0; i < popTrayIconPanelContent.getItemCount(); i++) {
                CheckboxMenuItem item = (CheckboxMenuItem) popTrayIconPanelContent.getItem(i);
                if (!item.equals(e.getSource())) {
                    item.setState(false);
                }
            }
        }
    }
}
