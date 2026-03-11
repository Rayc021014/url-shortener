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

# Copy Maven wrapper and pom first (layer cache — only re-downloads deps when pom changes)
COPY mvnw pom.xml ./
COPY .mvn .mvn/
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -q

# Copy source
COPY src ./src

# Copy built frontend into static folder
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static/

# Build jar
RUN ./mvnw clean package -DskipTests -q

# ── Stage 3: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/url-shortener-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
