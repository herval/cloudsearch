FROM williamyeh/scala:v2.11.5


RUN mkdir /app
WORKDIR /app
ADD . /app

RUN sbt compile stage

EXPOSE 8080

CMD ["target/universal/stage/bin/cloudsearch-api"]