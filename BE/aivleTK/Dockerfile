FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

#wget 없으면 먼저 다운로드
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# 에이전트 다운로드
RUN wget -q -O applicationinsights-agent.jar https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.6.2/applicationinsights-agent-3.6.2.jar

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:applicationinsights-agent.jar", "-jar", "/app.jar"]
