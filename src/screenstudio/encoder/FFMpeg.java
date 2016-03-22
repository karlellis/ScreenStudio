/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenstudio.encoder;

import java.awt.Rectangle;
import java.io.File;
import screenstudio.sources.Overlay;
import screenstudio.targets.SIZES;
import screenstudio.targets.Targets;
import screenstudio.targets.Targets.FORMATS;

/**
 *
 * @author patrick
 */
public class FFMpeg {

    //Enums
    public enum Presets {

        ultrafast,
        superfast,
        veryfast,
        faster,
        fast,
        medium,
        slow,
        slower,
        veryslow
    }

    public enum CaptureFormat {

        Desktop,
        Webcam
    }

    public enum AudioFormat {

        pulse,
        alsa
    }

    public enum AudioRate {

        Audio44K,
        Audio48K
    }

    private final String bin = "ffmpeg  ";
    private final String nonVerboseMode = " -nostats -loglevel 0 ";
    //Main input
    private String captureWidth = "720";
    private String captureHeight = "480";
    private String captureX = "0";
    private String captureY = "0";
    private String mainInput = ":0.0";
    private String mainFormat = "x11grab";
    //Overlay
    private String overlayInput = "";
    private final String overlayFormat = "rawvideo -pix_fmt bgr24";
    // Audio
    private String audioRate = "44100";
    private String audioInput = "default";
    private String audioFormat = "pulse";

    //Output
    private String framerate = "10";
    private String videoBitrate = "9000";
    private String audioBitrate = "128";
    private String videoEncoder = "libx264";
    private String audioEncoder = "aac";
    private String muxer = "mp4";
    private String preset = "ultrafast";
    private final String strictSetting = "-2";
    private String outputWidth = "720";
    private String outputHeight = "480";
    private File defaultCaptureFolder = new File(".");
    private String output = "Capture/capture.mp4";

    private Rectangle overlaySetting = new Rectangle(0, 0);

    public FFMpeg() {
        //Creating default folder for capturing videos...
        defaultCaptureFolder = new File("Capture");
        if (!defaultCaptureFolder.exists()) {
            defaultCaptureFolder.mkdir();
        }
        output = new File(defaultCaptureFolder, "capture.flv").getAbsolutePath();
    }

    public void setAudio(AudioRate rate, String input, AudioFormat format) {
        switch (rate) {
            case Audio44K:
                audioRate = "44100";
                break;
            case Audio48K:
                audioRate = "48000";
                break;
        }
        audioInput = input;
        audioFormat = format.name();
    }

    public void setOverlay(Overlay overlay) {
        if (overlay == null) {
            overlayInput = "";
        } else {
            overlayInput = overlay.OutputURL();
            overlaySetting = new Rectangle(0, 0, (int) overlay.getSize().getWidth(), (int) overlay.getSize().getHeight());
        }
    }

    public void setCaptureFormat(CaptureFormat format, String device, int capX, int capY) {
        switch (format) {
            case Desktop:
                mainFormat = "x11grab";
                mainInput = device;
                captureX = String.valueOf(capX);
                captureY = String.valueOf(capY);
                break;
            case Webcam:
                mainFormat = "video4linux2";
                mainInput = device;
                captureX = "";
                captureY = "";
                break;
        }
    }

    public void setOutputFormat(FORMATS format,Targets target) {
        switch (format) {
            case FLV:
                muxer = "flv";
                videoEncoder = "libx264";
                audioEncoder = "aac";
                output = new File(defaultCaptureFolder, generateRandomName() + ".flv").getAbsolutePath();
                break;
            case MP4:
                muxer = "mp4";
                videoEncoder = "libx264";
                audioEncoder = "aac";
                output = new File(defaultCaptureFolder, generateRandomName() + ".mp4").getAbsolutePath();
                break;
            case MOV:
                muxer = "mov";
                videoEncoder = "libx264";
                audioEncoder = "aac";
                output = new File(defaultCaptureFolder, generateRandomName() + ".mov").getAbsolutePath();
                break;
            case OGG:
                muxer = "ogg";
                videoEncoder = "libtheora";
                audioEncoder = "libvorbis";
                output = new File(defaultCaptureFolder, generateRandomName() + ".ogg").getAbsolutePath();
                break;
            case RTMP:
            case HITBOX:
            case TWITCH:
            case USTREAM:
            case VAUGHNLIVE:
            case YOUTUBE:
                muxer = "flv";
                videoEncoder = "libx264";
                audioEncoder = "aac";
                if (target.server.length() == 0){
                    output = target.rtmpKey;
                } else {
                    output = target.server + "/" + target.rtmpKey;
                }
                
                break;
        }
        preset = target.outputPreset;
        videoBitrate = target.outputVideoBitrate;
    }

    public void setAudioBitrate(int rate) {
        audioBitrate = String.valueOf(rate);
    }

    public void setVideoBitrate(int rate) {
        videoBitrate = String.valueOf(rate);
    }

    public void setFramerate(int rate) {
        framerate = String.valueOf(rate);
    }

    public void setPreset(Presets p) {
        preset = p.name();
    }

    public void setOutputSize(int capWidth, int capHeight, SIZES size) {
        captureWidth = String.valueOf(capWidth);
        captureHeight = String.valueOf(capHeight);
        if (overlayInput.length() > 0){
            capWidth += overlaySetting.getSize().getWidth();
        }
        int calculatedWidth = capWidth;
        switch (size) {
            case SOURCE:
                outputHeight = String.valueOf(capHeight);
                outputWidth = String.valueOf(capWidth);
                break;
            case OUT_240P:
                outputHeight = "240";
                calculatedWidth = (capWidth * 240 / capHeight);
                calculatedWidth += calculatedWidth % 2;
                outputWidth = String.valueOf(calculatedWidth);
                break;
            case OUT_360P:
                outputHeight = "360";
                calculatedWidth = (capWidth * 360 / capHeight);
                calculatedWidth += calculatedWidth % 2;
                outputWidth = String.valueOf(calculatedWidth);
                break;
            case OUT_480P:
                outputHeight = "480";
                calculatedWidth = (capWidth * 480 / capHeight);
                calculatedWidth += calculatedWidth % 2;
                outputWidth = String.valueOf(calculatedWidth);
                break;
            case OUT_720P:
                outputHeight = "720";
                calculatedWidth = (capWidth * 720 / capHeight);
                calculatedWidth += calculatedWidth % 2;
                outputWidth = String.valueOf(calculatedWidth);
                break;
            case OUT_1080P:
                outputHeight = "1080";
                calculatedWidth = (capWidth * 1080 / capHeight);
                calculatedWidth += calculatedWidth % 2;
                outputWidth = String.valueOf(calculatedWidth);
                break;
        }

    }

    public void setOutput(File out) {
        output = out.getAbsolutePath();
    }

    public String getOutput() {
        return output;
    }

    public String generateRandomName() {
        String name = "capture-" + System.currentTimeMillis();
        return name;
    }

    public String getCommand(boolean debugMode) {
        StringBuilder c = new StringBuilder();
        c.append(bin);
        if (!debugMode){
            c.append(nonVerboseMode);
        }
        if (overlayInput.length() > 0) {
            c.append(" -f ").append(overlayFormat);
            c.append(" -framerate ").append(framerate);
            int w = (int) overlaySetting.getWidth();
            int h = (int) overlaySetting.getHeight();
            c.append(" -video_size ").append(w).append("x").append(h);
            c.append(" -i ").append(overlayInput);
        }
        c.append(" -video_size ").append(captureWidth).append("x").append(captureHeight);
        c.append(" -framerate ").append(framerate);
        c.append(" -f ").append(mainFormat).append(" -i ").append(mainInput);
        if (captureX.length() > 0) {
            c.append("+").append(captureX).append(",").append(captureY);
        }
        
        if (overlayInput.length() > 0) {
            int x = (int) overlaySetting.getX();
            int y = (int) overlaySetting.getY();
            int w = (int) overlaySetting.getWidth();
            int h = (int) overlaySetting.getHeight();
            //ffmpeg -i capture-1454504589261.mp4 -i capture-1454504589261.mp4 -filter_complex "[0:v]pad=iw+100:ih[left];[left][1:v]overlay=w" test.flv
            c.append(" -filter_complex [1:v]pad=iw+").append(w).append(":ih[desk];[desk][0:v]overlay=main_w-overlay_w:0");
        }
        c.append(" ").append(" -f ").append(audioFormat).append(" -i ").append(audioInput);
        if (strictSetting.length() > 0) {
            c.append(" -strict ").append(strictSetting);
        }
        c.append(" -r ").append(framerate);
        c.append(" -s ").append(outputWidth).append("x").append(outputHeight);
        c.append(" -vb ").append(videoBitrate).append("k");
        c.append(" -ab ").append(audioBitrate).append("k").append(" -ar ").append(audioRate);
        c.append(" -vcodec ").append(videoEncoder);
        c.append(" -acodec ").append(audioEncoder);
        if (preset.length() > 0) {
            c.append(" -preset ").append(preset);
        }

        String buffer = " -g " + (new Integer(framerate) * 2);
        c.append(buffer).append(" -f ").append(muxer).append(" ");
        if (output.startsWith("rtmp")) {
            c.append(output);
        } else {
            c.append(" -y ").append(output);
        }
        System.out.println(c.toString());
        return c.toString();
    }
}
