FROM gradle

COPY .  /Magikarp

WORKDIR /Magikarp

RUN cd /Magikarp

ENTRYPOINT ["/Magikarp/gradlew"]
CMD ["run"]

