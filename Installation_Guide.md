# HOW TO INSTALL PROVIZ

1. Insert usb into device
2. Open terminal either on device desktop or through remote connection
3. CD into the usb installation directory. example:

```
    cd /media/pi/usb_name/proviz_installation
```

4. First run setup script using bash

```
    bash setup.sh
```

5. Then run install script as superuser. This step will also attempt to install
Java 8 and other dependencies so network connection is required. If the script does not
successfully connect to a network it will have to be re-run after a connection is:wa
established.

```
    sudo bash install.sh
```

6. If WiFi credentials are provided the install script will automatically configure the hardware.
For the beaglebone black in newer versions the use of `connmanctl` is required,
see instructions below on how to set it up.

7. *Optional* run the install_dependencies.sh script to install Java 8, bluetooth dependencies,
and for the beaglebone x11vnc or install them yourself. Internet access is required
before running.

### Java Alternatives

If Java 8 is not the default change with these commands.

`sudo update-alternatives --config javac`
`sudo update-alternatives --config java`

### Raspberry Pi Specific

 Since proviz uses the java swing library, to have proviz be able to automatically start on
 boot the raspberry pi must be changed to have the GUI launch at startup. To do this:

    Go to the Raspberry Pi Software Configuration Tool

```
    sudo raspi-config
        -> 3 Boot Options
        -> B1 Desktop / CLI
        -> B4 Desktop Autologin Desktop GUI
    Finish and exit
```
  Most of supported sensors work using I2C and SPI connection, so if I2C and SPI is not enabled, it should be enabled.
  To Enable I2C and SPI :

    Go to the Raspberry Pi Software Configuration Tool
```
    sudo raspi-config
        -> 5 Interfacing Options
        -> P4 SPI (Select Enable)
        -> P5 I2C (Select Enable)
    Finish and exit

The Pi will then restart with your new settings
