# ✈️ Aviation Reservation System (항공 예약 시스템)

> Spring Boot 백엔드 포트폴리오를 위한 항공 예약 REST API 서버입니다.
> 항공편 검색, 승객 등록, 좌석 예약/취소, JWT 인증까지 구현한 항공 예약 관리 서비스입니다.

---

## 👨‍💻 프로젝트 개요

- **프로젝트명**: Aviation Reservation System
- **개발자**: 박용희
- **개발 목적**
  - Spring Boot 백엔드 포트폴리오
  - Spring Security + JWT 인증 실습
  - JPA 연관관계 매핑 및 트랜잭션 처리 실전 경험
  - Docker 기반 개발 환경 구성 실습

---

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.4.1
- Gradle
- Spring Data JPA / Hibernate
- Spring Security + JWT
- Bean Validation
- Lombok

### Database
- PostgreSQL 16

### 인프라
- Docker / Docker Compose

---

## 🚀 개발 진행 단계

### ✅ Phase 1. 프로젝트 초기 세팅
- Spring Boot 3.4.1 + Java 17 + Gradle 프로젝트 구성
- 패키지 구조 설계 (config / controller / service / repository / entity / dto)
- .gitignore, application.yml 설정

### ✅ Phase 2. Docker PostgreSQL 환경 구성
- Docker Compose로 PostgreSQL 16 컨테이너 구성
- aviation_db / aviation / 1234 설정
- 로컬(local) 프로파일 분리 (ddl-auto: create-drop)

### ✅ Phase 3. 엔티티 설계 및 JPA 매핑
- Flight, Passenger, Reservation 엔티티 설계
- @ManyToOne / @OneToMany 연관관계 매핑
- FlightStatus, ReservationStatus Enum 정의

### ✅ Phase 4. 항공편 / 승객 / 예약 CRUD API 구현
- 항공편 등록 / 조회 / 검색 / 상태 변경
- 승객 등록 / 조회
- 예약 생성 / 조회 / 취소 (좌석 수 동기화 트랜잭션 처리)
- GlobalExceptionHandler 전역 예외 처리

### 🔜 Phase 5. JWT 인증 전환
- Spring Security HTTP Basic → JWT Bearer Token 방식 전환
- 로그인 API 구현 (Access Token 발급)
- JwtFilter, JwtProvider 구현
- 인증/인가 예외 처리 (401, 403)

### 🔜 Phase 6. API 응답 표준화 및 예외 처리 고도화
- 공통 응답 포맷 (ApiResponse wrapper) 적용
- 에러 코드 Enum 정의
- Validation 에러 응답 정비

### 🔜 Phase 7. 단위 테스트 / 통합 테스트 작성
- JUnit5 + Mockito 단위 테스트
- @SpringBootTest 통합 테스트
- 예약 생성 / 취소 시나리오 테스트

### 🔜 Phase 8. 배포 환경 구성
- 운영(prod) 프로파일 분리
- GitHub Actions CI/CD 파이프라인 구성
- AWS EC2 또는 Railway 배포

---

## 📡 API 목록

### 🛫 항공편 (Flights)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/flights | 항공편 등록 | ✅ |
| GET | /api/flights | 전체 항공편 조회 | ✅ |
| GET | /api/flights/{id} | 항공편 단건 조회 | ❌ |
| GET | /api/flights/search?origin=&destination=&departureTime= | 항공편 검색 | ❌ |
| PATCH | /api/flights/{id}/status?status= | 항공편 상태 변경 | ✅ |

**FlightStatus:** `SCHEDULED` · `DELAYED` · `CANCELLED` · `DEPARTED` · `ARRIVED`

---

### 👤 승객 (Passengers)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/passengers | 승객 등록 | ✅ |
| GET | /api/passengers | 전체 승객 조회 | ✅ |
| GET | /api/passengers/{id} | 승객 단건 조회 | ✅ |

---

### 🎫 예약 (Reservations)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/reservations | 예약 생성 | ✅ |
| GET | /api/reservations/{reservationCode} | 예약 단건 조회 | ✅ |
| GET | /api/reservations/passenger/{passengerId} | 승객별 예약 목록 | ✅ |
| DELETE | /api/reservations/{reservationCode} | 예약 취소 | ✅ |

**ReservationStatus:** `CONFIRMED` · `PENDING` · `CANCELLED`

---

### 🔐 인증 (현재: HTTP Basic → Phase 5에서 JWT 전환 예정)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/auth/login | 로그인 (JWT 발급) — Phase 5 |
| POST | /api/auth/register | 회원가입 — Phase 5 |

---

## 📁 폴더 구조

```
src/main/java/com/aviation/reservation/
├── ReservationApplication.java
├── config/
│   ├── SecurityConfig.java           # Spring Security 설정
│   └── GlobalExceptionHandler.java   # 전역 예외 처리
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
│   ├── Flight.java                   # FlightStatus Enum 포함
│   ├── Passenger.java
│   └── Reservation.java              # ReservationStatus Enum 포함
└── dto/
    ├── FlightDto.java                # inner static Request / Response
    ├── PassengerDto.java
    └── ReservationDto.java
```

---

## ⚙️ 실행 방법

### 1) 사전 요구사항
- Java 17+
- Docker Desktop

### 2) 저장소 클론

```bash
git clone https://github.com/dev-parkynh/project-aviation.git
cd project-aviation
```

### 3) PostgreSQL 실행 (Docker)

```bash
docker compose up -d
```

| 항목 | 값 |
|------|----|
| Host | localhost:5432 |
| Database | aviation_db |
| Username | aviation |
| Password | 1234 |

### 4) Spring Boot 실행

```bash
# 로컬 프로파일 (ddl-auto: create-drop)
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버 기동 확인: `http://localhost:8080`

### 5) 환경변수 오버라이드 (선택)

```env
DB_USERNAME=aviation
DB_PASSWORD=1234
```

### 6) 컨테이너 종료

```bash
docker compose down      # 데이터 보존
docker compose down -v   # 데이터 초기화
```

---

## 🗄 ERD

```
┌──────────────┐       ┌─────────────────────┐       ┌──────────────┐
│   flights    │       │     reservations     │       │  passengers  │
├──────────────┤       ├─────────────────────┤       ├──────────────┤
│ id (PK)      │◄──┐   │ id (PK)             │   ┌──►│ id (PK)      │
│ flightNumber │   └───│ flight_id (FK)       │   │   │ firstName    │
│ origin       │       │ passenger_id (FK)    │───┘   │ lastName     │
│ destination  │       │ reservationCode      │       │ email        │
│ departureTime│       │ seatNumber           │       │ phone        │
│ arrivalTime  │       │ status               │       │ passportNum  │
│ totalSeats   │       │ reservedAt           │       └──────────────┘
│ availSeats   │       └─────────────────────┘
│ price        │
│ status       │
└──────────────┘
```

---

## 📚 학습 포인트

- RESTful API 설계 / HTTP 상태 코드 활용
- Spring Data JPA / @ManyToOne, @OneToMany 연관관계 매핑
- @Transactional 트랜잭션 처리 / readOnly 최적화
- DTO 패턴 (inner static class Request / Response 분리)
- Bean Validation (@NotBlank, @Email, @Future 등)
- Spring Security HTTP Basic / Stateless 세션 설정
- GlobalExceptionHandler / @RestControllerAdvice 전역 예외 처리
- Docker Compose PostgreSQL 개발 환경 구성
- application.yml 프로파일 분리 (local / prod)
- JWT Bearer Token 인증 (Phase 5 예정)

---

## ✨ 한 줄 소개

**Aviation Reservation System은 Spring Boot 3.4 + JPA + JWT 인증 + Docker를 활용한 항공 예약 관리 백엔드 포트폴리오 프로젝트입니다.**
