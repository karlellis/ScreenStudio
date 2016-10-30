/*
 * Copyright (C) 2016 Patrick Balleux (Twitter: @patrickballeux) Balleux
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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class Screen {

    private Rectangle size = new Rectangle(0, 0);
    private String id = "";
    private String name = "None";
    private int screenIndex = -1;
    private boolean mIsOSX = false;
    private boolean mFollowMouse = false;

    @Override
    public String toString() {
        return getDetailledLabel();
    }

    public int getWidth() {
        return size.width;
    }

    public int getHeight() {
        return size.height;
    }

    public boolean isFollowingMouse() {
        return mFollowMouse;
    }

    public void setFollowingMouse(boolean value) {
        mFollowMouse = value;
    }

    public String getDetailledLabel() {
        return getLabel() + " (" + (int) this.size.getWidth() + "X" + (int) this.size.getHeight() + ")";
    }

    public static Screen[] getSources() throws IOException, InterruptedException {
        java.util.ArrayList<Screen> list = new java.util.ArrayList<>();
        System.out.println("Screen List:");
        if (Screen.isOSX()) {
            list.addAll(getOSXDevices());

        } else {
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = g.getScreenDevices();
            int i = 1;
            double maxWidth = 0;
            double maxHeight = 9999;
            String currentDisplay = System.getenv("DISPLAY");
            if (currentDisplay != null) {
                if (currentDisplay.length() == 2) {
                    currentDisplay = currentDisplay + ".0";
                }
            } else {
                currentDisplay = "desktop";
            }
            for (GraphicsDevice d : devices) {
                Screen s = new Screen();
                s.mIsOSX = false;
                s.setId(currentDisplay);
                System.out.println(s.getId() + " " + d.getDefaultConfiguration().getBounds().toString().replaceAll("java.awt.Rectangle", ""));
                s.setScreenIndex(i++);
                s.setSize(d.getDefaultConfiguration().getBounds());
                //Assuming screens are side by side
                maxWidth += s.getSize().getWidth();
                //If we have one that has less height than the other
                if (maxHeight > s.getSize().getHeight()) {
                    maxHeight = s.getSize().getHeight();
                }
                list.add(s);
            }
            if (list.size() > 1) {
                //We have more than one screen
                // Creating full screen capture...
                Screen s = new Screen();
                s.mIsOSX = false;
                s.setId(list.get(0).getId());
                s.setScreenIndex(i++);
                s.setSize(new Rectangle(0, 0, (int) maxWidth, (int) maxHeight));
                list.add(s);
            }

        }
        return list.toArray(new Screen[list.size()]);
    }

    private void setName(String n) {
        name = n;
    }

    public static Rectangle captureWindowArea() throws IOException {
        Rectangle r = new Rectangle();
        System.out.println("Capture Window Area");
        Process p = Runtime.getRuntime().exec("xwininfo");
        InputStream in = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        int x = 0, y = 0, w = 0, h = 0;
        while (line != null) {
            System.out.println(line);
            if (line.trim().startsWith("Absolute upper-left X:")) {
                x = new Integer(line.trim().replaceAll(" ", "").split(":")[1]);
            } else if (line.trim().startsWith("Absolute upper-left Y:")) {
                y = new Integer(line.trim().replaceAll(" ", "").split(":")[1]);
            } else if (line.trim().startsWith("Width:")) {
                w = new Integer(line.trim().replaceAll(" ", "").split(":")[1]);
            } else if (line.trim().startsWith("Height:")) {
                h = new Integer(line.trim().replaceAll(" ", "").split(":")[1]);
            }
            line = reader.readLine();
        }
        r.setSize(w, h);
        r.setLocation(x, y);
        in.close();
        isr.close();
        reader.close();
        p.destroy();
        return r;
    }

    /**
     * @return the size
     */
    public Rectangle getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(Rectangle size) {
        this.size = size;
    }

    /**
     * @return the id
     */
    public String getId() {
        if (mIsOSX || Screen.isWindows()) {
            return id;
        } else {
            return id + "+" + size.x + "," + size.y;
        }
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the screenIndex
     */
    public int getScreenIndex() {
        return screenIndex;
    }

    /**
     * @param screenIndex the screenIndex to set
     */
    public void setScreenIndex(int screenIndex) {
        this.screenIndex = screenIndex;
        this.name = "Screen " + screenIndex;
    }

    public String getLabel() {
        if (screenIndex == -1) {
            return "None";
        } else {
            return "Screen " + screenIndex;
        }
    }

    public static boolean isOSX() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("mac os x");
    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("windows");
    }

    private static ArrayList<Screen> getOSXDevices() throws IOException, InterruptedException {
        ArrayList<Screen> list = new ArrayList<>();
        String command = "./FFMPEG/ffmpeg-osx -list_devices true -f avfoundation -i dummy";
        String line = "";
        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        InputStream in = p.getErrorStream();
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(isr);
        line = reader.readLine();
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();
        while (line != null) {
            if (line.endsWith("AVFoundation video devices:")) {
                // we have some audio sources
                line = reader.readLine();
                int index = 1;
                while (line != null && line.indexOf("input device") > 0 && !line.contains("audio devices")) {
                    if (line.contains("Capture screen")) {
                        Screen s = new Screen();
                        if (index < devices.length) {
                            GraphicsDevice d = devices[index - 1];
                            s.setSize(d.getDefaultConfiguration().getBounds());
                        } else {
                            GraphicsDevice d = devices[0];
                            s.setSize(d.getDefaultConfiguration().getBounds());
                        }
                        s.screenIndex = index;
                        s.id = "";
                        s.mIsOSX = true;
                        String[] parts = line.split(" ");
                        System.out.println(line);
                        for (int i = parts.length - 1; i >= 0; i--) {
                            if (parts[i].startsWith("[")) {
                                // reached device id
                                s.id = parts[i].substring(1, parts[i].length() - 1) + ":";
                                break;
                            }
                        }
                        s.setName("Screen " + index);
                        System.out.println(s.toString());
                        list.add(s);
                    }
                    line = reader.readLine();
                }
            } else {
                line = reader.readLine();
            }
        }
        reader.close();
        isr.close();
        in.close();
        p.destroy();

        return list;
    }

}
