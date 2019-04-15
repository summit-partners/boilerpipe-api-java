FROM maven:3.3.9-jdk-8-onbuild

ENV PORT 3000

# See https://docs.datadoghq.com/agent/autodiscovery/?tab=docker#docker-example-nginx-dockerfile
# Date example: 2019-04-16 00:10:28,396
LABEL "com.datadoghq.ad.logs"='[{"source": "ecs", "service": "boilerpipe-api", "log_processing_rules": [{"type": "multi_line", "name":"new_log_start_with_date", "pattern" : "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}"}]}]'

CMD ["java", "-javaagent:newrelic/newrelic.jar", "-jar", "target/rest-api.jar"]