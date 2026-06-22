# ✈️ Aviation Reservation System

> Spring Boot 기반 항공 예약 관리 REST API 서버

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Build | Gradle |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security (HTTP Basic, Stateless) |
| Container | Docker / Docker Compose |
| Etc | Lombok, Bean Validation |

---

## 📁 폴더 구조

```
src/main/java/com/aviation/reservation/
├── config/
│   ├── SecurityConfig.java          # Spring Security 설정
│   └── GlobalExceptionHandler.java  # 전역 예외 처리
├── controller/
│   ├── FlightController.java
│   ├── PassengerController.java
│   └── ReservationController.java
├── service/
│   ├── FlightService.java
│   ├── PassengerService.java
│   └── ReservationService.java
├── repository/
│   ├── FlightRepository.java
│   ├── PassengerRepository.java
│   └── ReservationRepository.java
├── entity/
│   ├── Flight.java                  # 항공편 (FlightStatus Enum 포함)
│   ├── Passenger.java               # 승객
│   └── Reservation.java             # 예약 (ReservationStatus Enum 포함)
├── dto/
│   ├── FlightDto.java               # Request / Response inner class
│   ├── PassengerDto.java
│   └── ReservationDto.java
└── ReservationApplication.java
```

---

## 🚀 실행 방법

### 1. 사전 요구사항

- Java 17+
- Docker Desktop

### 2. 저장소 클론

```bash
git clone https://github.com/dev-parkynh/project-aviation.git
cd project-aviation
```

### 3. PostgreSQL 실행 (Docker)

```bash
docker compose up -d
```

| 항목 | 값 |
|------|----|
| Host | localhost:5432 |
| Database | aviation_db |
| Username | aviation |
| Password | 1234 |

### 4. Spring Boot 실행

```bash
# 로컬 프로파일 (ddl-auto: create-drop)
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버 기동 확인: `http://localhost:8080`

### 5. 컨테이너 종료

```bash
docker compose down        # 데이터 보존
docker compose down -v     # 데이터 초기화
```

---

## 📡 API 목록

### 🛫 항공편 (Flights)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| `POST` | `/api/flights` | 항공편 등록 | ✅ |
| `GET` | `/api/flights` | 전체 항공편 조회 | ✅ |
| `GET` | `/api/flights/{id}` | 항공편 단건 조회 | ❌ |
| `GET` | `/api/flights/search?origin=&destination=&departureTime=` | 항공편 검색 | ❌ |
| `PATCH` | `/api/flights/{id}/status?status=` | 항공편 상태 변경 | ✅ |

**FlightStatus:** `SCHEDULED` · `DELAYED` · `CANCELLED` · `DEPARTED` · `ARRIVED`

---

### 👤 승객 (Passengers)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| `POST` | `/api/passengers` | 승객 등록 | ✅ |
| `GET` | `/api/passengers` | 전체 승객 조회 | ✅ |
| `GET` | `/api/passengers/{id}` | 승객 단건 조회 | ✅ |

---

### 🎫 예약 (Reservations)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| `POST` | `/api/reservations` | 예약 생성 | ✅ |
| `GET` | `/api/reservations/{reservationCode}` | 예약 단건 조회 | ✅ |
| `GET` | `/api/reservations/passenger/{passengerId}` | 승객별 예약 목록 | ✅ |
| `DELETE` | `/api/reservations/{reservationCode}` | 예약 취소 | ✅ |

**ReservationStatus:** `CONFIRMED` · `PENDING` · `CANCELLED`

---

### 🔐 인증

HTTP Basic Authentication 사용

```
Username: admin
Password: admin
```

---

## 📋 API 요청 예시

<details>
<summary>항공편 등록</summary>

```json
POST /api/flights
{
  "flightNumber": "KE001",
  "origin": "Seoul",
  "destination": "Tokyo",
  "departureTime": "2026-08-01T09:00:00",
  "arrivalTime": "2026-08-01T11:30:00",
  "totalSeats": 200,
  "price": 150000
}
```
</details>

<details>
<summary>승객 등록</summary>

```json
POST /api/passengers
{
  "firstName": "길동",
  "lastName": "홍",
  "email": "hong@example.com",
  "phone": "01012345678",
  "passportNumber": "M12345678"
}
```
</details>

<details>
<summary>예약 생성</summary>

```json
POST /api/reservations
{
  "flightId": 1,
  "passengerId": 1,
  "seatNumber": "12A"
}
```
</details>

---

## 🗺 개발 진행 단계

| Phase | 내용 | 상태 |
|-------|------|:----:|
| **Phase 1** | 프로젝트 초기 세팅 (Spring Boot, Gradle, 폴더 구조) | ✅ 완료 |
| **Phase 2** | Docker Compose PostgreSQL 환경 구성 | ✅ 완료 |
| **Phase 3** | 엔티티 설계 및 JPA 연관관계 매핑 | ✅ 완료 |
| **Phase 4** | 항공편 / 승객 / 예약 CRUD API 구현 | ✅ 완료 |
| **Phase 5** | Spring Security 인증 적용 및 JWT 전환 | 🔲 예정 |
| **Phase 6** | 예외 처리 고도화 및 API 응답 표준화 | 🔲 예정 |
| **Phase 7** | 단위 테스트 / 통합 테스트 작성 | 🔲 예정 |
| **Phase 8** | 배포 환경 구성 (CI/CD, 운영 프로파일) | 🔲 예정 |

---

## 🗄 ERD

```
┌──────────────┐       ┌─────────────────┐       ┌──────────────┐
│    flights   │       │   reservations  │       │  passengers  │
├──────────────┤       ├─────────────────┤       ├──────────────┤
│ id (PK)      │◄──┐   │ id (PK)         │   ┌──►│ id (PK)      │
│ flightNumber │   └───│ flight_id (FK)  │   │   │ firstName    │
│ origin       │       │ passenger_id(FK)│───┘   │ lastName     │
│ destination  │       │ reservationCode │       │ email        │
│ departureTime│       │ seatNumber      │       │ phone        │
│ arrivalTime  │       │ status          │       │ passportNum  │
│ totalSeats   │       │ reservedAt      │       └──────────────┘
│ availSeats   │       └─────────────────┘
│ price        │
│ status       │
└──────────────┘
```
