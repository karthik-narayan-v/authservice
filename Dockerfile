# ---------- Build Stage ----------
FROM gradle:8.7-jdk17 AS builder

WORKDIR /app

COPY . .

RUN gradle clean build -x test

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]