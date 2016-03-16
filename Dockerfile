FROM maven:3.3.3-jdk-8-onbuild

RUN mvn dependency:resolve-plugins

CMD ["mvn", "jetty:run"]