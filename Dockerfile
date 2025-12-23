FROM eclipse-temurin:11-jre
WORKDIR /app
COPY src /app/src
RUN apt-get update && apt-get install -y openjdk-11-jdk && rm -rf /var/lib/apt/lists/*
RUN mkdir bin
RUN javac -d bin src/*.java
EXPOSE 5900
CMD ["java", "-cp", "bin", "RelayServer"]
