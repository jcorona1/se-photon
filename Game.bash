#!/bin/bash

# Launch Server.bash in a new terminal window
xterm -geometry 120x24+1500+800 -hold -e "./Server.bash" &

# Launch traffic.py in another new terminal window
xterm -geometry 120x24+1500+0 -hold -e "python3 python_trafficgenarator_v2.py" &

set -e
rm *.class
javac *.java
java Game
