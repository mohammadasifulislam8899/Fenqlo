# Fenqlo

A scalable, real-time messaging backend built with Kotlin and Ktor. It features a unified conversation model for both 1-to-1 and group chats, utilizing MongoDB for data persistence, Redis for real-time pub/sub synchronization, and JWT for secure authentication.

## Tech Stack
- **Language:** Kotlin (JVM 21)
- **Framework:** Ktor 3.5.0 (Server & Client)
- **Real-Time Gateway:** Ktor WebSockets
- **Database:** MongoDB (using official sync driver)
- **Cache & Pub/Sub:** Redis (using Jedis client)
- **Authentication:** JWT (JSON Web Tokens)
- **Dependency Injection:** Koin
- **Serialization:** kotlinx.serialization (JSON)
- **Security:** jbcrypt (for password hashing)

## Architecture
This project is built using a **Feature-based Layered Monolith** architecture:
- Divided by business features (e.g., `auth`, `chat`).
- Layered internally (Routes, Services, Repositories) to keep database and network code decoupled from core logic.
- Redis Pub/Sub coordinates WebSocket messages across server instances for horizontal scaling.

## Building & Running
To run the server locally:
```bash
./gradlew run
```
To run the test suite:
```bash
./gradlew test
```
