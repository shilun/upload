FROM openjdk:8u232-jre-slim
MAINTAINER bsm

ENV PARAMS=""

ADD app.jar /app.jar

RUN sh -c 'touch /app.jar'

ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $PARAMS /app.jar" ]
