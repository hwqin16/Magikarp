FROM gradle

COPY .  /Magikarp

WORKDIR /Magikarp

RUN cd /Magikarp

COPY magikarp.json /tmp/magikarp.json
ENTRYPOINT ["/Magikarp/gradlew"]
CMD ["run"]

