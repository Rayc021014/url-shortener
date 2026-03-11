# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install Node.js for the frontend build
RUN apk add --no-cache nodejs npm

# Copy everything
COPY . .

# Build frontend then backend in one step
RUN cd frontend && npm install && npm run build && \
    cp -r dist/* ../src/main/resources/static/ && \
    cd .. && ./mvnw clean package -DskipTests

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the built jar — keeps the image small (~200MB vs ~800MB)
COPY --from=build /app/target/url-shortener-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
