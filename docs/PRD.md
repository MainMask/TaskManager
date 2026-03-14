# PRD: Task Manager API (MVP)

## Overview
Build a RESTful Task Manager API using Spring Boot.
No UI, no authentication at this stage. The app will be
extended in future iterations.

## Tech Stack
- **Framework:** Spring Boot (latest stable)
- **Build tool:** Maven
- **JDK:** 25
- **Database:** PostgreSQL (via Docker)
- **API Docs:** Springdoc OpenAPI (Swagger UI)
- **Testing:** Postman (manual)

## Project Setup

### Docker
Provide a `docker-compose.yml` that spins up a PostgreSQL container with the following defaults:
- Image: `postgres:16`
- DB name: `taskmanager`
- User: `admin`
- Password: `admin`
- Port: `5432`

### application.properties
Configure datasource to connect to the Docker PostgreSQL instance.
Enable automatic schema creation via JPA (`spring.jpa.hibernate.ddl-auto=update`).

## Data Models

### User
| Field      | Type          | Constraints        |
|------------|---------------|--------------------|
| id         | Long          | PK, auto-generated |
| name       | String        | not null           |
| email      | String        | not null, unique   |
| created_at | LocalDateTime | auto               |

### Category
| Field | Type   | Constraints        |
|-------|--------|--------------------|
| id    | Long   | PK, auto-generated |
| name  | String | not null, unique   |

Seed the following default categories on startup:
`Home`, `Work`, `Education`, `Health`, `Other`

### Task
| Field       | Type          | Constraints             |
|-------------|---------------|-------------------------|
| id          | Long          | PK, auto-generated      |
| title       | String        | not null                |
| description | String        | nullable                |
| status      | Enum          | TODO, IN_PROGRESS, DONE |
| user        | User          | ManyToOne, not null     |
| category    | Category      | ManyToOne, not null     |
| created_at  | LocalDateTime | auto                    |
| updated_at  | LocalDateTime | auto                    |

## API Endpoints

### Users — `/api/users`
- `POST   /api/users`      — create user
- `GET    /api/users`      — get all users
- `GET    /api/users/{id}` — get user by ID
- `PUT    /api/users/{id}` — update user
- `DELETE /api/users/{id}` — delete user

### Categories — `/api/categories`
- `POST   /api/categories`      — create category
- `GET    /api/categories`      — get all categories
- `GET    /api/categories/{id}` — get category by ID
- `PUT    /api/categories/{id}` — update category
- `DELETE /api/categories/{id}` — delete category

### Tasks — `/api/tasks`
- `POST   /api/tasks`              — create task
- `GET    /api/tasks`              — get all tasks
- `GET    /api/tasks/{id}`         — get task by ID
- `PUT    /api/tasks/{id}`         — update task
- `DELETE /api/tasks/{id}`         — delete task
- `GET    /api/tasks?categoryId={id}` — filter tasks by category
- `GET    /api/tasks?userId={id}`     — filter tasks by user

## Request / Response Format
Use JSON. All responses must follow this structure:

```json
{
  "data": { },
  "message": "Success"
}
```

For errors, return appropriate HTTP status codes (400, 404, 500)
with a meaningful `message` field.

## Swagger

- Dependency: `springdoc-openapi-starter-webmvc-ui`
- Swagger UI available at: `http://localhost:8080/swagger-ui.html`
- All controllers must use `@Tag` annotation
- All endpoints must use `@Operation` annotation


## Project Structure

Follow standard layered architecture:

```
src/main/java/com/example/taskmanager/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
└── config/
```

Use DTOs for all request/response objects.
Do not expose JPA entities directly in API responses.

## Out of Scope (current iteration)

- Authentication \& Authorization
- Frontend / UI
- Unit \& integration tests
- Pagination


## Future Iterations

- JWT-based authentication
- Role-based access control (ADMIN / USER)
- Task deadlines and reminders
- Pagination and sorting
- Full Docker Compose (app + db)