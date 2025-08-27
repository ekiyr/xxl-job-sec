# FROM openjdk:8-jre-slim
FROM azul/zulu-openjdk:8u462-8.88
MAINTAINER xuxueli

ARG JAR_FILE
ENV PARAMS=""

ENV TZ=PRC
# RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD ${JAR_FILE} /admin-app.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /admin-app.jar $PARAMS"]