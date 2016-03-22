VERSION=$( grep "MAIN" src/screenstudio/Version.java | cut -d= -f 2 | cut -d'"' -f 2 )
echo "ScreenStudio - Build a new version..."
read -e -p "Enter new version: " -i "$VERSION" VERSION
sed "s/MAIN = \".*\"/MAIN = \"$VERSION\"/g" src/screenstudio/Version.java>src/screenstudio/Version.java.temp
rm src/screenstudio/Version.java
mv src/screenstudio/Version.java.temp src/screenstudio/Version.java
ant -Dnb.internal.action.name=rebuild clean
rm Capture/*.*
tar -zcvf "../ScreenStudio-Ubuntu-$VERSION-src.tar.gz" .
ant -Dnb.internal.action.name=jar
echo "Building Ubuntu app"
echo "Removing previous build..."
rm -r ScreenStudio.Ubuntu
rm ScreenStudio-Ubuntu*.tar.gz
echo "Creating new folder app..."
mkdir ScreenStudio.Ubuntu
mkdir ScreenStudio.Ubuntu/Overlays
mkdir ScreenStudio.Ubuntu/Capture
echo "Copying ScreenStudio archive..."
cp dist/ScreenStudio.jar ScreenStudio.Ubuntu/ScreenStudio.jar
echo "Copying logo file..."
cp apps/Ubuntu/logo.png ScreenStudio.Ubuntu/logo.png
cp apps/default.html ScreenStudio.Ubuntu/Overlays/default.html
cp apps/logo.png ScreenStudio.Ubuntu/Overlays/logo.png
cp apps/Ubuntu/ScreenStudio.sh ScreenStudio.Ubuntu/ScreenStudio.sh
cp apps/README.txt ScreenStudio.Ubuntu/README.txt
sed "s/@VERSION/$VERSION/g" apps/Ubuntu/createDesktopIcon.sh>ScreenStudio.Ubuntu/createDesktopIcon.sh
chmod +x ScreenStudio.Ubuntu/createDesktopIcon.sh
tar -zcvf "../ScreenStudio-Ubuntu-$VERSION-bin.tar.gz" ScreenStudio.Ubuntu
rm -r ScreenStudio.Ubuntu




