#!/usr/bin/env bash

USERNAME=$(whoami)

java -Dlog4j.configuration=file:/home/$USERNAME/.proviz/src/log4j.properties -jar /home/$USERNAME/.proviz/src/provizclient.com.manager.jar

#/home/pi/provizclient.com.manager/src/main/resources
