# URL Shortener Service

A production-style URL shortening REST API built with Java 17, Spring Boot 3, PostgreSQL, and Redis.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 3 |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Security | Spring Security + JWT |
| API Docs | SpringDoc / Swagger UI |
| Testing | JUnit 5 + Mockito |
| Frontend | Vue 3 + Vite |

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Start infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL on port `5432` and Redis on port `6379`.

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.  
Flyway will automatically run DB migrations on startup.

### 3. Open API docs

Visit `http://localhost:8080/swagger-ui.html`

## Project Structure

```
src/main/java/com/urlshortener/
├── controller/         # REST controllers (AuthController, UrlController, RedirectController)
├── service/            # Business logic (AuthService, UrlService, ClickService)
├── repository/         # JPA repositories
├── domain/             # JPA entities (User, Url, Click)
├── dto/
│   ├── request/        # Incoming request bodies
│   └── response/       # Outgoing response bodies
├── security/
│   ├── jwt/            # JwtService — token generation & validation
│   └── filter/         # JwtAuthFilter — intercepts requests
├── exception/          # Custom exceptions + GlobalExceptionHandler
├── config/             # SecurityConfig, RedisConfig, AppProperties
└── util/               # ShortCodeGenerator
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `JWT_SECRET` | *(change this!)* | HMAC-SHA256 signing key |
| `BASE_URL` | `http://localhost:8080` | Used to build short URLs |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |

## API Endpoints

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/auth/refresh` | Refresh access token |

### URLs (requires Bearer JWT)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/urls` | Create shortened URL |
| GET | `/api/urls` | List your URLs (paginated) |
| GET | `/api/urls/{id}` | Get URL details |
| PATCH | `/api/urls/{id}` | Update alias / expiry / status |
| DELETE | `/api/urls/{id}` | Delete a URL |
| GET | `/api/urls/{id}/analytics` | Click analytics |

### Public
| Method | Path | Description |
|--------|------|-------------|
| GET | `/{shortCode}` | Redirect to original URL |

## Next Steps (build order)

1. `AuthController` + `AuthService` — register, login, return JWT
2. `UrlController` + `UrlService` — CRUD for URLs, short code generation
3. `RedirectController` — Redis-cached redirect with async click tracking
4. `ClickService` — analytics aggregation
5. Vue 3 frontend dashboard
