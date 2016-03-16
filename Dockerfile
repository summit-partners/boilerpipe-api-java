FROM maven:3.3.3-jdk-8-onbuild

RUN mvn jetty:effective-web-xml

CMD ["mvn", "jetty:run"]