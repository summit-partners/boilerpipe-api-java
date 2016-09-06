FROM maven:3.3.9-jdk-8-onbuild

ENV PORT 3000

HEALTHCHECK --interval=15s --timeout=3s \
  CMD curl -f http://localhost:${PORT}/ || exit 1

CMD ["java", "-jar", "target/rest-api.jar"]