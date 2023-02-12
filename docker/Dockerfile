FROM appinair/jdk11-maven

RUN mkdir -p /usr/local/jar

WORKDIR /usr/local/jar

COPY ./sa-admin.jar app.jar
COPY ./sa-common.jar sa-common.jar

ENTRYPOINT ["sh","-c","java $PARAMS -noverify -XX:+AlwaysPreTouch -jar -Dspring.profiles.active=prod app.jar"]
