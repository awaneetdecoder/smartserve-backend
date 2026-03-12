# SmartServe — Spring Boot Backend

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-brightgreen?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

> The Spring Boot REST API backend for **SmartServe** — a digital queue management system. Handles authentication with JWT, role-based access control, and all queue operations.

**Flutter App Repo:** [smartserve-flutter](https://github.com/awaneetdecoder/smartserve-flutter)

---

## ✨ Features

- 🔑 JWT authentication (register + login → returns token)
- 🛡️ Spring Security with stateless sessions
- 👤 Role-based access control (STUDENT / ADMIN)
- 🎫 Queue token generation with auto-incrementing numbers (T-101, T-102...)
- ⏱️ Wait time estimation endpoint
- 🗑️ Soft delete for cancelled tokens (history preserved)
- 🌐 CORS configured for Flutter mobile client

---

## 🛠️ Tech Stack

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

## 📁 Project Structure

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

## 📡 API Endpoints

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

## 🗃️ Database Schema

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

## 🚀 Getting Started

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

-- Create admin user (required for admin dashboard)
USE smartserve;
INSERT INTO users (full_name, email, password, role)
VALUES ('Admin', 'admin@smartserve.com', 'admin123', 'ADMIN');
```

### 3. Configure Environment Variables

Copy the example config:
```bash
cp backend/src/main/resources/application.properties.example \
   backend/src/main/resources/application.properties
```

Set environment variables. In IntelliJ → `Run → Edit Configurations → Environment Variables`:
```
JWT_SECRET=mySecretKeyForSmartServeAppThatIsLongEnough123456
DB_PASSWORD=your_mysql_root_password
```

### 4. Run

**IntelliJ:** Click the green ▶ Run button.

**Terminal:**
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

## 🔐 How JWT Works in This Project

```
1. Client sends POST /api/auth/login { email, password }
2. AuthController verifies credentials against MySQL
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

## 🐛 Common Issues

| Error | Cause | Fix |
|---|---|---|
| `Access denied for user 'root'` | Wrong DB password | Check `DB_PASSWORD` env variable |
| `Could not create connection` | MySQL not running | Start MySQL service |
| `401 Unauthorized` on all routes | JWT filter issue | Check `SecurityConfig.permitAll()` paths |
| `403 Forbidden` | Wrong role | Student calling ADMIN-only endpoint |

---

## 🔮 Future Improvements

- [ ] BCrypt password hashing
- [ ] JWT refresh tokens
- [ ] Email verification
- [ ] Multi-organisation support
- [ ] WebSocket for real-time updates
- [ ] Docker containerization

---

## 👨‍💻 Author

**Awaneet Mishra** — [@awaneetdecoder](https://github.com/awaneetdecoder)

---

## 📄 License

MIT License
