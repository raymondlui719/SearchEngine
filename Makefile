JCC = javac
CLASSPATH = -cp
.SUFFIXES: .java .class
DEPENDENCIES = jdbm-1.0.jar:htmlparser.jar:.
.java.class:
	$(JCC) $(CLASSPATH) $(DEPENDENCIES) $*.java

CLASSES = \
    Score.java \
	Posting.java \
	DataManager.java \
	Indexer.java \
	Page.java \
	PageAdapter.java \
	Spider.java \
	StopStem.java \
	TestProgram.java \
	./IRUtilities/Porter.java
	
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
