run: ReversiAIChamp ReversiHuman ReversiServer ReversiRandom
	./launcher.sh

ReversiAIChamp: ReversiAIChamp/AIChamp.java
	bash -c "pushd ReversiAIChamp;	javac *.java;	popd"

ReversiHuman: ReversiHuman/Human.java
	bash -c "pushd ReversiHuman;	javac *.java;	popd"

ReversiServer: ReversiServer/Reversi.java
	bash -c "pushd ReversiServer; javac *.java; popd"

ReversiRandom: ReversiRandom_Java/RandomGuy.java
	bash -c "pushd ReversiRandom_Java; javac *.java;	popd"
