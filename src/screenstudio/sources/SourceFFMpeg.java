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
package screenstudio.sources;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import screenstudio.encoder.FFMpeg;
import screenstudio.targets.Layout.SourceType;

/**
 *
 * @author patrick
 */
public class SourceFFMpeg extends Source{

    private FFMpeg mFFMpeg;
    private Process mProcess;
    private DataInputStream mInputData;
    private final String mInput;
    private int mFPS;
    private final Rectangle mCaptureSize;
    private Monitor monitor = null;
    
    protected enum DEVICES{
        Desktop,
        Webcam,
        File,
        Stream
    }
    
    public SourceFFMpeg(Rectangle captureSize,Rectangle outputSize, int fps, String input,SourceType type,String id) {
        super(outputSize, 1, 1, 0,id);
        mInput = input;
        mFPS = fps;
        mCaptureSize = captureSize;
        mImageType = BufferedImage.TYPE_3BYTE_BGR;
        mType = type;
    }

    public void setFPS(int fps){
        mFPS = fps;
    }
    @Override
    protected void getData(byte[] buffer) throws IOException {
        mInputData.readFully(buffer);
    }

    @Override
    protected void initStream() throws IOException {
        mFFMpeg = new FFMpeg(null);
        String command = mFFMpeg.getBin() + " " + mInput + " " + "-s " + mBounds.width + "x" + mBounds.height + " -r " + mFPS + " -f rawvideo -pix_fmt bgr24 -";
        mProcess = Runtime.getRuntime().exec(command);
        monitor = new Monitor(mProcess.getErrorStream());
        System.out.println(command);
        mInputData = new DataInputStream(mProcess.getInputStream());
    }

    @Override
    protected void disposeStream() throws IOException {
        mInputData.close();
        mProcess.destroy();
        monitor.stop();
        monitor = null;
        mProcess = null;
        mInputData = null;
    }
    
    protected String getInput(String source, DEVICES type){
        String input = "";
        
        switch (type){
            case Desktop:
                input = " -f " + mFFMpeg.getDesktopFormat() + " -video_size " + mCaptureSize.width + "x" + mCaptureSize.height + " -i " + source;
                break;
            case File:
                input = " -i " + source;
                break;
            case Stream:
                input = " -i " + source;
                 break;
            case Webcam:
                input = " -f " + mFFMpeg.getWebcamFormat() + " -i " + source;
                break;
        }
        return input;
    }
    
    public static SourceFFMpeg getDesktopInstance(Screen display,int fps){
        String  input = " -f " + new FFMpeg(null).getDesktopFormat() + " -video_size " + display.getWidth() + "x" + display.getHeight() + " -i " + display.getId();
        return new SourceFFMpeg(display.getSize(),new Rectangle(display.getSize()),fps,input,SourceType.Desktop,display.getId());
    }
    public static SourceFFMpeg getWebcamInstance(Webcam webcam, int fps){
        String input = " -f " + new FFMpeg(null).getWebcamFormat() + " -i " + webcam.getDevice();
        return new SourceFFMpeg(webcam.getSize(),new Rectangle(webcam.getSize()),fps,input,SourceType.Webcam,webcam.getDevice());
    }
    public static SourceFFMpeg getFileInstance(Rectangle bounds,java.io.File file, int fps){
        return new SourceFFMpeg(bounds,bounds,fps,"-i " + file.getAbsolutePath(),SourceType.Video,file.getAbsolutePath());
    }
    public static SourceFFMpeg getStreamInstance(Rectangle bounds,String url, int fps){
        return new SourceFFMpeg(bounds,bounds,fps,"-i " + url,SourceType.Stream,url);
    }
}

//To read the output and make sure FFMPEG does not stop...
class Monitor implements Runnable {

    private InputStream mIn;
    private boolean mStopMe = false;

    protected Monitor(InputStream in) {
        mIn = in;
        new Thread(this).start();
    }

    protected void stop() {
        mStopMe = true;
    }

    @Override
    public void run() {
        int count = 0;
        
        byte[] buffer = new byte[65525];
        while (!mStopMe) {
            try {
                count = mIn.read(buffer);
            } catch (IOException ex) {
                //log nothing...
            }
//            if (count > 0) {
//                System.out.println(new String(buffer, 0, count));
//            }
        }
        try {
            mIn.close();
        } catch (IOException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
