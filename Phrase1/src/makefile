# Makefile

JFLAGS = -g
JC = javac -sourcepath . -classpath .:../lib/jeromq-0.5.3.jar:../lib/jnacl-1.0.0.jar:../lib/forms_rt-7.0.3.jar -encoding UTF-8 # For Windows, use -classpath .;../lib\jeromq-0.5.3.jar;../lib\jnacl-1.0.0.jar
JAR = jar cmf
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./networking/InterleaveJobAllocator.java \
	./networking/MusicStreamer.java \
	./networking/P2PMusicStreaming.java \
	./networking/TrackerServer.java \
	./music/MusicManager.java \
	./music/MusicPlayer.java \
	./music/MusicProperty.java \
	./music/MusicReader.java \
	SongProperty.java \
	MusicPlayerDashboard.java \
	LrcFileReader.java \
	InterleavingBitmaps.java \
	MusicDecoding.java

# MAIN_CLASS = InterleavingBitmaps
# MAIN_CLASS = networking/TrackerServer
MAIN_CLASS = MusicPlayerDashboard
MANIFEST = manifest.txt

default: classes

classes: $(CLASSES:.java=.class)

jar: $(MAIN_CLASS).class
	echo "Main-Class: $(MAIN_CLASS)" > $(MANIFEST)
	$(JAR) $(MANIFEST) $(MAIN_CLASS).jar *.class

run:
	java -classpath .:../lib/jeromq-0.5.3.jar:../lib/jnacl-1.0.0.jar:../lib/forms_rt-7.0.3.jar $(MAIN_CLASS) # For Windows, use -classpath .;../lib\jeromq-0.5.3.jar;../lib\jnacl-1.0.0.jar

clean:
	$(RM) *.class $(MAIN_CLASS).jar $(MANIFEST)
