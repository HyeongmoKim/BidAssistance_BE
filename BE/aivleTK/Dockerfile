FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Application Insights Java Agent 다운로드
ADD https://github.com/microsoft/ApplicationInsights-java/releases/latest/download/applicationinsights-agent.jar /agent/applicationinsights-agent.jar

# Spring Boot JAR 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080

# javaagent 활성화
ENTRYPOINT ["java","-javaagent:/agent/applicationinsights-agent.jar","-jar","app.jar"]
