#!/usr/bin/env bash

SERVER=localhost
TIMELIMIT=5

on_sigint() {
    echo "Shutting down";
    kill -9 $PID1 $PID2 $PID3;
}
trap on_sigint SIGINT

java -classpath ReversiServer Reversi $TIMELIMIT > /dev/null &
PID1=$!

sleep 1s

java -classpath ReversiRandom_Java RandomGuy $SERVER 1 /dev/null &
PID2=$!

sleep 1s

java -classpath ReversiAIChamp AIChamp $SERVER 2 &
PID3=$!

wait $PID1 $PID2 $PID3


