version=$1

rm -rvf bin/ &&
rm -rvf lib/swt* &&
mkdir bin &&

# Create JAR for Linux 64bit
ln swt/swt-3.7.2-gtk-linux-x86_64.jar lib/ &&
xsbt clean assembly &&
cp target/IRCBalloon-assembly-$version.jar bin/IRCBallon-linux64-$version.jar &&

# Create JAR for Windows 64bit
ln swt/swt-3.7.2-win32-win32-x86_64.jar lib/ &&
xsbt clean assembly &&
cp target/IRCBalloon-assembly-$version.jar bin/IRCBallon-win64-$version.jar &&

# Create JAR for Windows 32bit
ln swt/swt-3.7.2-win32-win32-x86.jar lib/ &&
xsbt clean assembly &&
cp target/IRCBalloon-assembly-$version.jar bin/IRCBallon-win32-$version.jar &&

# Swtich to default SWT jar
rm -rvf lib/swt* &&
ln swt/swt-3.7.2-gtk-linux-x86_64.jar lib/
