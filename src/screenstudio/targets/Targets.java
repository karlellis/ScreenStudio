/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenstudio.targets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author patrick
 */
public class Targets {
// <editor-fold defaultstate="collapsed" desc="Contants">

    public enum FORMATS {

        FLV,
        MP4,
        MOV,
        OGG,
        HITBOX,
        TWITCH,
        USTREAM,
        VAUGHNLIVE,
        YOUTUBE,
        RTMP
    }

    public static boolean isRTMP(FORMATS f) {
        switch (f) {
            case HITBOX:
            case RTMP:
            case TWITCH:
            case USTREAM:
            case VAUGHNLIVE:
            case YOUTUBE:
                return true;
            default:
                return false;
        }
    }


    public static String[] getServerList(FORMATS format) {
        String[] list = new String[0];
        switch (format) {
            case HITBOX:
                list = new String[]{
                    "Default;rtmp://live.hitbox.tv/push",
                    "EU-East;rtmp://live.vie.hitbox.tv/push",
                    "EU-Central;rtmp://live.nbg.hitbox.tv/push",
                    "EU-West;rtmp://live.fra.hitbox.tv/push",
                    "EU-North;rtmp://live.ams.hitbox.tv/push",
                    "US-East;rtmp://live.vgn.hitbox.tv/push",
                    "US-West;rtmp://live.lax.hitbox.tv/push",
                    "South America;rtmp://live.gru.hitbox.tv/push",
                    "South Korea;rtmp://live.icn.hitbox.tv/push",
                    "United Kingdom;rtmp://live.lhr.hitbox.tv/push",
                    "South America;rtmp://live.gru.hitbox.tv/push"
                };
                break;
            case TWITCH:
                list = new String[]{
                    "Amsterdam, NL;rtmp://live-ams.twitch.tv/app",
                    "Stockholm, SE;rtmp://live-arn.justin.tv/app",
                    "Paris, FR;rtmp://live-cdg.twitch.tv/app",
                    "Dallas, TX;rtmp://live-dfw.twitch.tv/app",
                    "Frankfurt, Germany;rtmp://live-fra.twitch.tv/app",
                    "Ashburn, VA;rtmp://live-iad.twitch.tv/app",
                    "New York, NY;rtmp://live-jfk.twitch.tv/app",
                    "Los Angeles, CA;rtmp://live-lax.twitch.tv/app",
                    "London, UK;rtmp://live-lhr.twitch.tv/app",
                    "Miami, FL;rtmp://live-mia.twitch.tv/app",
                    "Chicago, IL;rtmp://live-ord.twitch.tv/app",
                    "Prague, CZ;rtmp://live-prg.twitch.tv/ap",
                    "Singapore;rtmp://live-sin-backup.twitch.tv/app",
                    "San Francisco, CA;rtmp://live.twitch.tv/app",};
                break;
            case USTREAM:
                break;
            case VAUGHNLIVE:
                list = new String[]{
                    "Primary;rtmp://live.vaughnsoft.net:443/live",
                    "Virginia, USA;rtmp://live-iad.vaughnsoft.net:443/live",
                    "New-York, USA;rtmp://live-nyc.vaughnsoft.net:443/live",
                    "New York #2, USA;rtmp://live-nyc2.vaughnsoft.net:443/live",
                    "Amsterdam, Netherlands;rtmp://live-nl.vaughnsoft.net:443/live",
                    "Frankfurt, Germany;rtmp://live-de.vaughnsoft.net:443/live"
                };
                break;
            case YOUTUBE:
                list = new String[]{
                    "Primary;rtmp://a.rtmp.youtube.com/live2",
                    "Backup;rtmp://b.rtmp.youtube.com/live2?backup=1",};
                break;
        }
        return list;
    }
    // </editor-fold >
// <editor-fold defaultstate="collapsed" desc="Members">
    public String format = "";
    public String size = "";
    public String server = "";
    public String rtmpKey = "";
    public String mainSource = "";
    public String mainAudio = "";
    public String secondAudio = "";
    public String mainOverlay = "";
    public String mainOverlayWidth = "320";
    public String framerate = "";
    public String captureX = "";
    public String captureY = "";
    public String captureWidth = "";
    public String captureHeight = "";
    public String webcamDevice = "";
    public String webcamWidth= "320";
    public String webcamHeight = "240";
    public String webcamOffset = "0.0";
    public String outputPreset = "ultrafast";
    public String outputVideoBitrate = "9000";
    public String showDuration = "60";
// </editor-fold>

    public void saveDefault() throws IOException {
        java.util.Properties props = new java.util.Properties();
        FileWriter out = new FileWriter("screenstudio.properties");
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (f.get(this) != null) {
                    props.setProperty(f.getName(), f.get(this).toString());
                } else {
                    props.setProperty(f.getName(), "");
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Targets.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        props.store(out, "ScreenStudio default settings");
        out.close();
    }

    public void loadDefault() throws FileNotFoundException, IOException {
        if (new File("screenstudio.properties").exists()) {
            FileReader in = new FileReader("screenstudio.properties");
            java.util.Properties props = new java.util.Properties();
            props.load(in);
            for (Field f : this.getClass().getDeclaredFields()) {
                try {
                    if (props.getProperty(f.getName()) != null) {
                        f.set(this, props.getProperty(f.getName()));
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Targets.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            in.close();
        }
    }
}
