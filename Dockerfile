FROM jetty:9.3-jre8

COPY . /app
WORKDIR /app

ENV MAVEN_VERSION 3.3.3

RUN mkdir -p /usr/share/maven \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

RUN mvn clean && \
  mvn package && \
  cp target/rest-api.war /var/lib/jetty/webapps/ROOT.war

RUN java -jar "$JETTY_HOME/start.jar" --add-to-start=logging,stats

CMD ["java", "-Djava.io.tmpdir=/tmp/jetty", "-jar", "/usr/local/jetty/start.jar", "jetty.threadPool.maxThreads=500"]