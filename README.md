# Task Manager — Microservices

REST API для управления задачами, пользователями и категориями. Архитектура разбита на два независимых микросервиса, связанных через Apache Kafka.

---

## Технологический стек

| | |
|---|---|
| **Java 25 / Spring Boot 4.0.3** | Основной фреймворк |
| **PostgreSQL 16** | Два независимых экземпляра БД (task + notification) |
| **Redis 7** | Кэширование (с fallback на in-memory при отключении) |
| **Apache Kafka 7.7 (KRaft)** | Асинхронный обмен событиями между сервисами |
| **Liquibase** | Версионирование схемы БД в обоих сервисах |
| **MapStruct** | Автоматический маппинг Entity ↔ DTO |
| **Lombok** | Снижение шаблонного кода |
| **Springdoc OpenAPI** | Swagger UI |
| **Testcontainers** | Интеграционные тесты с реальными контейнерами |

---

## Архитектура

```
┌──────────────────────────────────────────────────────────────┐
│                        Docker Compose                         │
│                                                               │
│  ┌──────────────────┐       Kafka        ┌─────────────────┐ │
│  │   task-service   │ ─── task-events ──▶│notification-    │ │
│  │   :8080          │                    │service :8081    │ │
│  │                  │                    │                 │ │
│  │  PostgreSQL :5432│                    │PostgreSQL :5433 │ │
│  │  Redis :6379     │                    │                 │ │
│  └──────────────────┘                    └─────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

**task-service** — принимает REST-запросы, управляет задачами/пользователями/категориями, публикует события в Kafka.

**notification-service** — слушает топик `task-events`, сохраняет записи о событиях в отдельную БД.

---

## Быстрый старт

### Требования

- **Docker** и **Docker Compose**
- **Java 25** (JDK)
- **Maven** (или используйте `./mvnw`)

### 1. Запуск инфраструктуры

```bash
docker-compose up -d
```

Запустятся четыре контейнера:

| Контейнер | Порт | Назначение |
|---|---|---|
| `taskmanager-postgres` | 5432 | БД для task-service |
| `notification-postgres` | 5433 | БД для notification-service |
| `taskmanager-redis` | 6379 | Redis-кэш |
| `taskmanager-kafka` | 9092 | Kafka (KRaft, без Zookeeper) |

Дождаться состояния `healthy`:
```bash
docker-compose ps
```

### 2. Сборка проекта

```bash
mvn clean package -DskipTests
```

### 3. Запуск task-service (порт 8080)

```bash
java -jar task-service/target/task-service-0.0.1-SNAPSHOT.jar
```

Или через Maven:
```bash
mvn spring-boot:run -pl task-service
```

Liquibase автоматически создаст таблицы и наполнит базу стартовыми данными (5 категорий).

### 4. Запуск notification-service (порт 8081)

В **отдельном** терминале:
```bash
java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
```

Или:
```bash
mvn spring-boot:run -pl notification-service
```

---

## API — task-service

Swagger UI: **http://localhost:8080/swagger-ui.html**

Все ответы обёрнуты в единый формат:
```json
{
  "data": { ... },
  "message": "Success"
}
```

Ошибки:
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Task not found with id: 999",
  "status": 404,
  "timestamp": "2026-04-18T10:00:00Z"
}
```

### Пользователи — `/api/users`

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@example.com"}'
```

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/users` | Список всех пользователей |
| GET | `/api/users/{id}` | Пользователь по ID |
| POST | `/api/users` | Создать пользователя |
| PUT | `/api/users/{id}` | Обновить данные |
| DELETE | `/api/users/{id}` | Удалить |

### Категории — `/api/categories`

При первом запуске автоматически создаются: Home, Work, Education, Health, Other.

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/categories` | Список всех категорий |
| GET | `/api/categories/{id}` | Категория по ID |
| POST | `/api/categories` | Создать |
| PUT | `/api/categories/{id}` | Переименовать |
| DELETE | `/api/categories/{id}` | Удалить |

### Задачи — `/api/tasks`

```bash
# Создать задачу
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Купить молоко", "description": "В магазине", "status": "TODO", "userId": 1, "categoryId": 1}'

# Получить список с пагинацией и фильтром
curl "http://localhost:8080/api/tasks?page=0&size=5&sort=createdAt,desc&status=TODO"
```

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/tasks` | Список задач с пагинацией и фильтрами |
| GET | `/api/tasks/{id}` | Задача по ID |
| POST | `/api/tasks` | Создать задачу |
| PUT | `/api/tasks/{id}` | Обновить задачу |
| DELETE | `/api/tasks/{id}` | Удалить задачу |

**Query-параметры для `GET /api/tasks`:**

| Параметр | Тип | Пример | Описание |
|---|---|---|---|
| `page` | int | `0` | Номер страницы (с нуля) |
| `size` | int | `10` | Размер страницы (по умолчанию 10, макс 100) |
| `sort` | string | `createdAt,desc` | Поле и направление сортировки |
| `status` | enum | `TODO` | Фильтр: `TODO`, `IN_PROGRESS`, `DONE` |
| `from` | ISO datetime | `2026-01-01T00:00:00` | Нижняя граница даты создания |
| `to` | ISO datetime | `2026-12-31T23:59:59` | Верхняя граница даты создания |

Ответ с метаданными пагинации:
```json
{
  "data": {
    "content": [ ... ],
    "totalElements": 42,
    "totalPages": 5,
    "number": 0,
    "size": 10
  },
  "message": "Success"
}
```

---

## Kafka — события задач

После каждой операции с задачей task-service публикует событие в топик **`task-events`**.

| Операция | `eventType` |
|---|---|
| Создание | `TASK_CREATED` |
| Обновление | `TASK_UPDATED` |
| Изменение статуса | `TASK_STATUS_CHANGED` |
| Удаление | `TASK_DELETED` |

notification-service принимает эти события и сохраняет их в таблицу `notification_records` базы `notificationdb`.

Посмотреть сохранённые события:
```bash
docker exec -it notification-postgres psql -U admin -d notificationdb \
  -c "SELECT id, task_id, event_type, dispatch_strategy, processed_at FROM notification_records ORDER BY processed_at DESC LIMIT 10;"
```

---

## Кэширование Redis

По умолчанию Redis включён (`app.cache.redis.enabled=true`).

При отключении (`app.cache.redis.enabled=false`) автоматически активируется **in-memory fallback** (SimpleCacheManager) — приложение продолжает работать без изменений в API.

Кэшируются: списки задач, пользователей, категорий и отдельные сущности по ID. Кэш сбрасывается при create/update/delete.

---

## Приоритизация задач (Strategy Pattern)

При каждом создании или обновлении задачи вычисляется приоритет. Стратегия задаётся в `application.properties`:

```properties
# Варианты: statusBasedPriorityStrategy | agePriorityStrategy
app.task.priority.strategy=statusBasedPriorityStrategy
```

| Стратегия | Логика |
|---|---|
| `statusBasedPriorityStrategy` | TODO=10, IN_PROGRESS=20, DONE=0 |
| `agePriorityStrategy` | Возраст задачи в днях × 2, максимум 100 |

Рассчитанный приоритет выводится в лог при каждой операции.

---

## Тесты

### Unit-тесты (без Docker)

```bash
mvn test -pl task-service -Dtest=TaskServiceTest
```

6 тестов: создание задачи, 404, удаление, смена статуса (с/без Kafka-события), пагинация.

### Интеграционные тесты (Testcontainers поднимает Docker-контейнеры автоматически)

```bash
# task-service: MockMvc + PostgreSQL + Kafka
mvn test -pl task-service -Dtest=TaskControllerIT

# notification-service: Kafka consumer + PostgreSQL + Awaitility
mvn test -pl notification-service -Dtest=TaskEventConsumerIT

# Все тесты
mvn test
```

---

## Конфигурация

### task-service — `application.properties`

```properties
# БД
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager

# Redis (false = in-memory fallback)
app.cache.redis.enabled=true

# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Стратегия приоритизации
app.task.priority.strategy=statusBasedPriorityStrategy
```

### notification-service — `application.properties`

```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5433/notificationdb
spring.kafka.bootstrap-servers=localhost:9092
```

---

## Остановка

```bash
# Остановить Spring Boot: Ctrl+C в каждом терминале

# Остановить Docker (данные сохранятся)
docker-compose down

# Полный сброс с удалением данных
docker-compose down -v
```

---

## Структура проекта

```
TaskManager/
├── pom.xml                          ← родительский POM (multi-module)
├── docker-compose.yml
├── task-service/                    ← основной сервис (порт 8080)
│   └── src/main/java/.../taskservice/
│       ├── config/                  CacheConfig, KafkaProducerConfig
│       ├── controller/              TaskController, UserController, CategoryController
│       ├── event/                   TaskCreatedEvent, TaskUpdatedEvent, ...
│       ├── producer/                TaskEventProducer
│       ├── service/
│       │   ├── TaskService.java
│       │   └── priority/            TaskPriorityStrategy, StatusBased, Age
│       └── repository/              TaskRepository (JPQL, фильтрация, пагинация)
└── notification-service/            ← сервис уведомлений (порт 8081)
    └── src/main/java/.../notificationservice/
        ├── consumer/                TaskEventConsumer
        ├── service/
        │   └── dispatch/            NotificationDispatchStrategy, Database, Log
        └── model/                   NotificationRecord
```
