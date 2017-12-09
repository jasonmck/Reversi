#!/usr/bin/env bash

SERVER=localhost
TIMELIMIT=3

on_sigint() {
    echo "Shutting down";
    kill -9 $PID1 $PID2 $PID3;
}
trap on_sigint SIGINT

java -classpath ReversiServer Reversi $TIMELIMIT > /dev/null &
PID1=$!

sleep 2s

#java -classpath ReversiRandom_Java RandomGuy $SERVER 1 > /dev/null &
java -jar MCTS.jar localhost 1&
PID2=$!

sleep 1s

#java -classpath ReversiRandom_Java RandomGuy $SERVER 2 > /dev/null &
java -classpath ../out/production/Reversi AIChamp $SERVER 2 &
#java -classpath ReversiAIChamp_prev AIChamp $SERVER 2 &
#java -jar MCTS.jar localhost 2
PID3=$!

wait $PID1 $PID2 $PID3


