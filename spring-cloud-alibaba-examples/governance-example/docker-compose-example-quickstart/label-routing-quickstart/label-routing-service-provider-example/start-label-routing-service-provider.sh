#!/bin/sh

java -jar app.jar --spring.profiles.active=dockerA1 &

java -jar app.jar --spring.profiles.active=dockerA2 &

java -jar app.jar --spring.profiles.active=dockerA3 &

java -jar app.jar --spring.profiles.active=dockerA4