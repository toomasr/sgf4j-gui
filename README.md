SGF4J GUI
=========

I've been doing some serious [yak shaving](https://en.wiktionary.org/wiki/yak_shaving). Instead of solving [Go](https://en.wikipedia.org/wiki/Go_(game)) problems to improve my game I've been writing a [SGF](http://www.red-bean.com/sgf/) viewer to make it easier to go through my problem files.

As a side result of the viewer I have also implemented a [SGF Parser](https://github.com/toomasr/sgf4j) for Java.

The app is meant for me to go over a large number of SGF problems in a quick manner. I wasn't able to find a tool out there that would let you load SGFs without navigating some File explorer or similar.

![Screenshot](https://raw.githubusercontent.com/toomasr/sgf4j-gui/master/src/main/resources/screenshot-001.png)

Running It
==========

I haven't packaged the program into a executable. If you want to take it for a spin then clone the repository, issue a `mvn install` and run the JAR file `java -jar sgf4j-gui.jar`
