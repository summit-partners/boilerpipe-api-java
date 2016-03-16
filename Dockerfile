FROM maven:3.3.3-jdk-8-onbuild

RUN mvn dependency:go-offline

CMD ["mvn", "jetty:run"]