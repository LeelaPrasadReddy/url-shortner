# URL Shortener with Analytics

A production-ready URL shortening service built with Java, Spring Boot, Redis, and PostgreSQL.

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA, Spring Data Redis
- **Database:** PostgreSQL (persistent storage)
- **Cache:** Redis (fast redirects)
- **Containerization:** Docker, Docker Compose

## Features
- Shorten any URL to a 6-character code
- Fast redirects via Redis caching
- Click tracking and analytics
- REST API with validation
- Comprehensive JUnit tests

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/shorten` | Shorten a URL |
| GET | `/{shortCode}` | Redirect to original URL |
| GET | `/api/analytics/{shortCode}` | Get click analytics |
| GET | `/api/urls` | Get all URLs |
| GET | `/api/urls/top` | Get top clicked URLs |

## Running Locally

### Prerequisites
- Java 17+
- Maven
- Docker & Docker Compose

### Option 1 — Docker Compose (Recommended)
```bash
docker-compose up --build
```

### Option 2 — Run Locally
```bash
# Start PostgreSQL and Redis
docker-compose up postgres redis

# Run the app
mvn spring-boot:run
```

## Example Usage

### Shorten a URL
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.google.com"}'
```

### Response
```json
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.google.com",
  "clickCount": 0,
  "createdAt": "2026-04-13T10:00:00"
}
```

### Get Analytics
```bash
curl http://localhost:8080/api/analytics/abc123
```

## Running Tests
```bash
mvn test
```

## Project Structure
```
src/
├── main/java/com/leela/urlshortener/
│   ├── UrlShortenerApplication.java
│   ├── controller/
│   │   └── UrlShortenerController.java
│   ├── service/
│   │   └── UrlShortenerService.java
│   ├── repository/
│   │   └── UrlMappingRepository.java
│   ├── model/
│   │   └── UrlMapping.java
│   └── config/
│       └── RedisConfig.java
└── test/
    └── UrlShortenerServiceTest.java
```
