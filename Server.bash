#!/bin/bash
set -e
echo "Starting server..."
javac UdpServer.java
echo "Server started."
java UdpServer
