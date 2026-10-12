FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY lib ./lib
COPY *.java ./
RUN javac -cp "lib/*" *.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/*.class ./
COPY --from=build /app/lib ./lib

EXPOSE 8091
CMD ["java", "-cp", ".:lib/*", "TeamBHttpServer"]