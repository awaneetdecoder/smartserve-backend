# SmartServe — Spring Boot Backend

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-brightgreen?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

> Spring Boot REST API for **SmartServe** — a digital queue management system that replaces physical token systems. Handles JWT authentication, role-based access control, queue token generation, and wait-time estimation.

**Flutter App Repo:** [smart_serve](https://github.com/awaneetdecoder/smart_serve)

---

## Why this exists

Physical token queues (hospitals, government offices, college departments) give no visibility into wait time and no way to manage the queue remotely. This backend exposes a REST API so any frontend — the Flutter app in this case — can let people join a queue digitally and let admins manage it live.

---

## What's actually working

- JWT authentication (register + login), passwords hashed with BCrypt
- Role-based access control (STUDENT / ADMIN) enforced via Spring Security
- Queue token generation with auto-incrementing token numbers (T-101, T-102...)
- **Wait-time estimation calculated from the average serving time per department** — not a static placeholder. See [How wait time is calculated](#how-wait-time-is-calculated) below.
- Soft delete on cancelled tokens (history preserved, not destroyed)
- CORS configured for the Flutter mobile client

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 4.0.2 | Framework |
| Spring Security | Latest | Auth & authorization |
| Spring Data JPA | Latest | Database ORM |
| JJWT | 0.12.3 | JWT generation & validation |
| MySQL | 8.0 | Database |
| Lombok | Latest | Boilerplate reduction |

---

## Project Structure

```
backend/src/main/java/com/smartserve/backend/
├── controller/
│   ├── AuthController.java      # /api/auth/register, /api/auth/login
│   └── QueueController.java     # All /api/queue/* endpoints
├── model/
│   ├── User.java                # id, email, password, fullName, role
│   └── QueueEntry.java          # id, tokenNumber, tokenType, status, user
├── repository/
│   ├── UserRepository.java      # findByEmail()
│   └── QueueRepository.java     # findByUserAndStatus() etc.
├── security/
│   ├── JwtUtil.java             # generateToken(), validateToken(), extractEmail()
│   ├── JwtFilter.java           # Intercepts every request, validates JWT
│   └── SecurityConfig.java      # Route permissions, CORS config
└── BackendApplication.java
```

---

## API Endpoints

### Auth — Public (no token required)

| Method | Endpoint | Body | Response |
|---|---|---|---|
| POST | `/api/auth/register` | `{fullName, email, password, role}` | `{token, userId, email, role, message}` |
| POST | `/api/auth/login` | `{email, password}` | `{token, userId, email, role, message}` |

### Queue — Protected (JWT required)

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/queue/join` | Any | Join queue, receive token |
| GET | `/api/queue/user/{userId}` | Any | Get user's tokens |
| GET | `/api/queue/user/{userId}/wait-time` | Any | Get wait time estimate |
| DELETE | `/api/queue/{id}` | Any | Cancel token (soft delete) |
| GET | `/api/queue/all` | **ADMIN** | Get all active tokens |
| PUT | `/api/queue/{id}/status?status=SERVING` | **ADMIN** | Update token status |

### Token Status Values
```
waiting → SERVING → DONE
waiting → ON_HOLD → waiting
waiting → CANCELLED (soft delete, isDeleted = true)
```

---

## How wait time is calculated

Wait time isn't hardcoded. The estimate is derived from the average serving time per department, computed from historical `queue_entries` records for that department, then multiplied by the number of people ahead of the requesting user in the active queue. This means estimates improve as more tokens get served and adapt per department rather than using one fixed number for every queue type.

---

## Database Schema

```sql
CREATE TABLE users (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255),
    email     VARCHAR(255) UNIQUE NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(50) DEFAULT 'STUDENT'
);

CREATE TABLE queue_entries (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_number VARCHAR(50),
    token_type   VARCHAR(255),
    status       VARCHAR(50) DEFAULT 'waiting',
    is_deleted   BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id      BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Getting Started

### Prerequisites
- Java 17
- MySQL 8.0
- IntelliJ IDEA (recommended)
- Maven (or use included `mvnw`)

### 1. Clone the Repository

```bash
git clone https://github.com/awaneetdecoder/smartserve-backend.git
cd smartserve-backend
```

### 2. Database Setup

```sql
CREATE DATABASE smartserve;
```

Create an admin user. Generate a BCrypt hash of your chosen password at [bcrypt-generator.com](https://bcrypt-generator.com), then run:

```sql
USE smartserve;
INSERT INTO users (full_name, email, password, role)
VALUES ('Admin', 'admin@smartserve.com', '$2a$10$yourBcryptHashHere', 'ADMIN');
```

> Never insert a plaintext password. Registration and login both go through BCrypt — always hash before inserting manually.

### 3. Configure Environment Variables

```bash
cp backend/src/main/resources/application.properties.example \
   backend/src/main/resources/application.properties
```

In IntelliJ → `Run → Edit Configurations → Environment Variables`:
```
JWT_SECRET=your_long_secret_key_here
DB_PASSWORD=your_mysql_root_password
```

### 4. Run

```bash
cd backend
./mvnw spring-boot:run
```

Wait for:
```
Started BackendApplication in X.XX seconds
Tomcat started on port 8080
```

---

## How JWT Works in This Project

```
1. Client sends POST /api/auth/login { email, password }
2. AuthController verifies credentials against MySQL (BCrypt comparison)
3. JwtUtil.generateToken() creates signed JWT:
   {
     "sub":    "user@email.com",
     "userId": 1,
     "role":   "STUDENT",
     "iat":    1703001234,
     "exp":    1703087634    ← expires in 24 hours
   }
4. JWT returned to client in response body
5. Client sends JWT with every future request:
   Authorization: Bearer eyJhbGci...
6. JwtFilter intercepts → validates signature → sets SecurityContext
7. SecurityConfig checks role for protected routes
```

---

## Common Issues

| Error | Cause | Fix |
|---|---|---|
| `Access denied for user 'root'` | Wrong DB password | Check `DB_PASSWORD` env variable |
| `Could not create connection` | MySQL not running | Start MySQL service |
| `401 Unauthorized` on all routes | JWT filter issue | Check `SecurityConfig.permitAll()` paths |
| `403 Forbidden` | Wrong role | Student calling ADMIN-only endpoint |

---

## Honest current limitations

- Tested locally and manually — not yet used by real users outside development.
- No automated test suite yet.
- No deployment / hosting set up yet — runs locally via Maven or IntelliJ.

---

## Roadmap

- [ ] JWT refresh tokens
- [ ] Email verification
- [ ] Multi-organisation support
- [ ] WebSocket for real-time queue updates
- [ ] Docker containerization
- [ ] Automated test suite

---

## Author

**Awaneet Mishra** — [@awaneetdecoder](https://github.com/awaneetdecoder)

---

## License

MIT License
