# Provizclient Communication Manager

The communication manager for the proviz client. Works on the
Raspberry Pi and BeagleBone boards.

The com manager responsibilities:

1. Start/stop sensor application.
2. Provide controls in the form of systray icon, when click has control menu.
3. Forward sensor app messages that are in the correct format(json) to server.
4. Handle WiFi, bluetooth communication.
5. Receive new sensor application from server.

The com manager reads all settings from the **.config.properties** file in the
**.proviz** directory. All changes to the com manager behavior should be made
through the properties file.
