# ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy project
COPY . .

# Give permission to wrapper
RUN chmod +x ./gradlew

# Build using wrapper (IMPORTANT FIX)
RUN ./gradlew clean build -x test

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]