#!/usr/bin/env bash

#/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/bin/java -jar out/artifacts/provizclient_com_manager_jar provizclient.com.manager.jar
java -Dlog4j.configuration=file:/home/pi/provizclient.com.manager/src/main/resources/log4j.properties -jar /home/pi/provizclient.com.manager/out/artifacts/provizclient_com_manager_jar/provizclient.com.manager.jar

#/home/pi/provizclient.com.manager/src/main/resources
