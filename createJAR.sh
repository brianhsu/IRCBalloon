version=$1

rm -rvf bin/ &&
rm -rvf lib/swt* &&
mkdir bin &&

# Create JAR for Linux 64bit
cp swt/swt-3.7.2-gtk-linux-x86_64.jar lib/swt.jar &&
xsbt clean compile msgfmt assembly &&
cp target/IRCBalloon-assembly-$version.jar bin/IRCBalloon-linux64-$version.jar &&

# Create JAR for Windows 64bit
cp swt/swt-3.7.2-win32-win32-x86_64.jar lib/swt.jar &&
xsbt clean compile msgfmt assembly &&
cp target/IRCBalloon-assembly-$version.jar bin/IRCBalloon-win64-$version.jar &&

# Create JAR for Windows 32bit
cp swt/swt-3.7.2-win32-win32-x86.jar lib/swt.jar &&
xsbt clean compile msgfmt assembly &&
cp target/IRCBalloon-assembly-$version.jar bin/IRCBalloon-win32-$version.jar &&

# Swtich to default SWT jar
rm -rvf lib/swt* &&
cp swt/swt-3.7.2-gtk-linux-x86_64.jar lib/swt.jar
