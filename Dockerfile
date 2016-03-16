FROM maven:3.3.3-jdk8-onbuild

CMD ["mvn", "jetty:run"]