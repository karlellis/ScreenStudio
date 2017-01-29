# screenstudio
Streaming, made easy!

ScreenStudio 3
-----------------------------------------------------------------
INSTALLATION:

FOR UBUNTU:
- Copy the content of the archive into any folder
- Execute the script "createDesktopIcon.sh" to create a shortcut on your desktop
- Make sure that Java JRE 8 is installed
- Make sure that "FFMPEG" is installed (ffmpeg version 2.7.6-0ubuntu0.15.10.1)
- Make sure that Pulseaudio is installed as it is required for audio mixing.

To install dependencies on Ubuntu:
sudo apt-get install ffmpeg openjdk-8-jre

NOTE:  ScreenStudio is relying heavily on FFMpeg provided with Ubuntu 16.04.  Any other distros or custom builds of FFMPEG
may not be supported.  If ScreenStudio does not work on your distro (or custom build), download the source code of
ScreenStudio to adjust the proper command to use with FFMpeg.

FOR OSX:
- Install Java 8 JRE from http://java.com
- Uncompress the archive
- An Application folder will be available
- Right-click on ScreenStudio app and click "Open"
- OS X will ask you to confirm since the origin of the application cannot be validated.
- Answer yes and ScreenStudio will start.

FOR WINDOWS:
- Install Java 8 JRE from http://java.com
- Uncompress the archive
- A folder will be available
- In the new sub-folder, double-click on ScreenStudio.jar
- You can copy/move this sub-folder where you want

Binaries available at: http://screenstudio.crombz.com

-----------------------------------------------------------------
USAGE:

- Output
Select the output size that you want to record.  By default, the size of your main display will be used.
Then select the output format.  Some settings will be available according to the selected format.
> FLV, TS, MP4, MOV are to record into a local file
> TWITCH, YOUTUBE, USTREAM, HITBOX are for streaming to their respective RTMP server.
> For RTMP, you can select a server and enter your secret key.

If your computer is not really fast and powerful, use a 10 frame per second and a lower output size for better
performances. 

- SOURCES
This is where you can add video sources.  Supported formats are:
> Webcams (V4L2)
> Desktop (screen capture)
> Images (PNG, JPG, BMP, GIF, animated GIFS)
> Text (Raw text of basic HTML)

For each source, you have to set the proper size for each source.  The top most sources will be displayed above the
others.  You can see a preview at the bottom.  You can use your mouse to move the selected source around.

You can save the current layout by using the File menu.  This will generate an XML file that can be used to reload all
your sources and settings the next time you launch ScreenStudio.

- OPTIONS

ScreenStudio is relying on Pulseaudio for audio recording.  When two audio input are selected (Mic+Internal), ScreenStudio will
add a virtual audio input that will mix both Mic and Internal audio input.  This virtual audio input will only exists while
ScreenStudio is capturing and will be remove once the capture is completed.

To adjust the audio levels, use the default audio mixer or install "pavucontrol" for more options.

You can also select the output folder where ScreenStudio will save the video files.

- HTML Text label

HTML file are the best of showing content in your recording/live streaming.  Use any text editor to create your own overlay
using basic HTML tags.  Javascript is not supported.  For more dynamic content, your can use a URL file to load content
from a web server.

The HTML rendering do support some basic styles CSS but do not expect a full HTML5 support.  Here's an example:

<html>
<body bgcolor=white color=black with=320 height=800>
<H1>ScreenStudio is amazing!</H1>
<font color=red>Download now!</font>
</body
</html>

In the "BODY" tag, set the background and foreground color.  To ensure that the overlay will use all the available space,
set the width and height also in the body tag.

See http://www.w3schools.com/tags/ for a list of tags to use...

- TAGS

In the text/html content, some tags are supported to udate the text content with values like the current date and time.

	@CURRENTDATE (Current date)
	@CURRENTTIME (Current time)
 	@RECORDINGTIME (Recording time in minutes)
	@STARTTIME (Time when the recording started)
	@REMAININGTIME (Time remaining in minutes)
	@TEXT (Custom text from the text entry in the Panel tab...)
        @COMMAND (Custom text from a command output...)

-----------------------------------------------------------------
CONTACT:

Main website: http://screenstudio.crombz.com
Twitter: http://twitter.com/patrickballeux
G+ Community: https://plus.google.com/communities/107164189448403990139

Keep in mind that ScreenStudio is free and that I work on this project in my spare time.

Have fun!

Patrick

