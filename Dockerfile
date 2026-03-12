# ── Stage 1: Build frontend ────────────────────────────────────────────────────
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# ── Stage 2: Build backend ─────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn/
RUN chmod +x mvnw

# Download dependencies (cache bust: v3)
RUN ./mvnw dependency:resolve dependency:resolve-plugins

# Copy source and built frontend
COPY src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static/

# Build jar
RUN ./mvnw clean package -DskipTests

# ── Stage 3: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/url-shortener-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
