VERSION=$( grep "MAIN" src/screenstudio/Version.java | cut -d= -f 2 | cut -d'"' -f 2 )
echo "ScreenStudio - Build a new version..."
read -e -p "Enter new version: " -i "$VERSION" VERSION
sed "s/MAIN = \".*\"/MAIN = \"$VERSION\"/g" src/screenstudio/Version.java>src/screenstudio/Version.java.temp
rm src/screenstudio/Version.java
mv src/screenstudio/Version.java.temp src/screenstudio/Version.java
ant -Dnb.internal.action.name=rebuild clean
rm Capture/*.*
tar -zcvf "../ScreenStudio-OSX-$VERSION-src.tar.gz" .
ant -Dnb.internal.action.name=jar
echo "Building OSX app"
echo "Removing previous build..."
echo "Creating new folder app..."
cp -r apps/OSX/ ./ScreenStudio.app
mkdir ScreenStudio.app/Contents/MacOS/Overlays
mkdir ScreenStudio.app/Contents/MacOS/Capture
mkdir ScreenStudio.app/Contents/MacOS/RTMP
mkdir ScreenStudio.app/Contents/MacOS/FFMPEG
echo "Copying ScreenStudio archive..."
cp dist/ScreenStudio.jar ScreenStudio.app/Contents/MacOS/ScreenStudio.jar
echo "Copying logo file..."
cp apps/default.html ScreenStudio.app/Contents/MacOS/Overlays/default.html
cp apps/logo.png ScreenStudio.app/Contents/MacOS/Overlays/logo.png
cp apps/README.txt ScreenStudio.app/Contents/MacOS/README.txt
cp RTMP/* ScreenStudio.app/Contents/MacOS/RTMP
cp FFMPEG/* ScreenStudio.app/Contents/MacOS/FFMPEG
tar -zcvf "../ScreenStudio-OSX-$VERSION-bin.tar.gz" ScreenStudio.app
rm -r ScreenStudio.app



