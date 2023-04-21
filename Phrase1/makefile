# Makefile

JFLAGS = -g
JC = javac -sourcepath ./src -classpath .:./lib/jeromq-0.5.3.jar:./lib/jnacl-1.0.0.jar:./lib/forms_rt-7.0.3.jar -encoding UTF-8 # For Windows, use -classpath .;./lib\jeromq-0.5.3.jar;./lib\jnacl-1.0.0.jar
JAR = jar cmf
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./src/networking/InterleaveJobAllocator.java \
	./src/networking/MusicStreamer.java \
	./src/networking/P2PMusicStreaming.java \
	./src/networking/TrackerServer.java \
	./src/music/MusicManager.java \
	./src/music/MusicPlayer.java \
	./src/music/MusicProperty.java \
	./src/music/MusicReader.java \
	./src/SongProperty.java \
	./src/MusicPlayerDashboard.java \
	./src/LrcFileReader.java \
	./src/InterleavingBitmaps.java \
	./src/MusicDecoding.java

MAIN_CLASS_IB = InterleavingBitmaps
MAIN_CLASS_TS = networking.TrackerServer
MAIN_CLASS_MP = MusicPlayerDashboard

MANIFEST = manifest.txt

default: classes

classes: $(CLASSES:.java=.class)

jar: classes
	echo "Main-Class: $(MAIN_CLASS_MP)" > $(MANIFEST)
	$(JAR) $(MANIFEST) MusicPlayerDashboard.jar -C src .

run:
	java -classpath ./src:./lib/jeromq-0.5.3.jar:./lib/jnacl-1.0.0.jar:./lib/forms_rt-7.0.3.jar $(MAIN_CLASS_MP) # For Windows, use -classpath .;./lib\jeromq-0.5.3.jar;./lib\jnacl-1.0.0.jar


run-ib:
	java -classpath ./src:./lib/jeromq-0.5.3.jar:./lib/jnacl-1.0.0.jar:./lib/forms_rt-7.0.3.jar $(MAIN_CLASS_IB) # For Windows, use -classpath .;./lib\jeromq-0.5.3.jar;./lib\jnacl-1.0.0.jar

run-ts:
	java -classpath ./src:./lib/jeromq-0.5.3.jar:./lib/jnacl-1.0.0.jar:./lib/forms_rt-7.0.3.jar $(MAIN_CLASS_TS) # For Windows, use -classpath .;./lib\jeromq-0.5.3.jar;./lib\jnacl-1.0.0.jar

run-mp:
	java -classpath ./src:./lib/jeromq-0.5.3.jar:./lib/jnacl-1.0.0.jar:./lib/forms_rt-7.0.3.jar $(MAIN_CLASS_MP) # For Windows, use -classpath .;./lib\jeromq-0.5.3.jar;./lib\jnacl-1.0.0.jar

clean:
	$(RM) src/*.class src/*/*.class MusicPlayerDashboard.jar $(MANIFEST)