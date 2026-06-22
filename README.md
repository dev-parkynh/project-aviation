# ✈️ Aviation Reservation System (항공 예약 시스템)

> Spring Boot 백엔드 포트폴리오를 위한 항공 예약 REST API 서버입니다.
> 항공편 검색, 좌석 예약/취소, JWT 인증, 관리자 API, 통계 대시보드까지 구현한 항공 예약 관리 서비스입니다.

---

## 👨‍💻 프로젝트 개요

- **프로젝트명**: Aviation Reservation System
- **개발자**: 박용희
- **개발 목적**
  - Spring Boot 백엔드 포트폴리오
  - Spring Security + JWT 인증 실습
  - JPA 연관관계 매핑 및 트랜잭션 처리 실전 경험
  - 동시성 제어 (낙관적 락) 실습
  - 관리자 API 및 통계 집계 구현 경험
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

### ✅ Phase 2. 인증 API (JWT + Role)
- Spring Security + JWT Bearer Token 인증 구현
- 회원가입 / 로그인 API (Access Token 발급)
- JwtTokenProvider, JwtAuthenticationFilter 구현
- ROLE_USER / ROLE_ADMIN 권한 분리
- 인증/인가 예외 처리 (401, 403)

### ✅ Phase 3. 항공편 / 좌석 API
- 항공편 등록 / 조회 / 검색 / 상태 변경
- 좌석 자동 생성 및 좌석별 조회
- FlightStatus Enum 정의 (`SCHEDULED` · `DELAYED` · `CANCELLED` · `DEPARTED` · `ARRIVED`)
- GlobalExceptionHandler 전역 예외 처리

### ✅ Phase 4. 예약 API (Transaction + 동시성 제어)
- 예약 생성 / 조회 / 취소 (좌석 수 동기화 트랜잭션 처리)
- 낙관적 락(Optimistic Lock)을 활용한 좌석 중복 예약 방지
- ReservationStatus Enum 정의 (`CONFIRMED` · `PENDING` · `CANCELLED`)
- 예약 취소 시 좌석 반환 트랜잭션 처리

### ✅ Phase 5. 관리자 API + 통계
- 관리자 전용 항공편 등록 / 수정 API (`ROLE_ADMIN` 전용)
- 관리자 전용 전체 예약 조회 및 예약 상태 변경 API
- 일별 / 월별 / 노선별 예약 통계 집계 API
- JPQL 기반 통계 쿼리 구현

### 🔲 Phase 6. MSA 구조 분리
- 인증 / 항공편 / 예약 서비스 분리
- Spring Cloud Gateway, Eureka Service Registry 도입

### 🔲 Phase 7. GitLab CI/CD + 배포
- 운영(prod) 프로파일 분리
- GitLab CI/CD 파이프라인 구성
- AWS EC2 배포

### 🔲 Phase 8. README + 포트폴리오 정리
- API 문서화 (Swagger / Spring REST Docs)
- 포트폴리오 README 고도화

---

## 📡 API 목록

### 🔐 인증 (Auth)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/auth/signup | 회원가입 (JWT 발급) | ❌ |
| POST | /api/auth/login | 로그인 (JWT 발급) | ❌ |

---

### 🛫 항공편 (Flights)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | /api/flights | 전체 항공편 조회 | ❌ |
| GET | /api/flights/{id} | 항공편 단건 조회 | ❌ |
| GET | /api/flights/search?origin=&destination=&departureTime= | 항공편 검색 | ❌ |
| GET | /api/flights/{id}/seats | 항공편 좌석 목록 조회 | ✅ |
| PATCH | /api/flights/{id}/status?status= | 항공편 상태 변경 | ✅ |

**FlightStatus:** `SCHEDULED` · `DELAYED` · `CANCELLED` · `DEPARTED` · `ARRIVED`

---

### 🎫 예약 (Reservations)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/reservations | 예약 생성 | ✅ |
| GET | /api/reservations/{reservationCode} | 예약 단건 조회 | ✅ |
| GET | /api/reservations/my | 내 예약 목록 조회 | ✅ |
| DELETE | /api/reservations/{reservationCode} | 예약 취소 | ✅ |

**ReservationStatus:** `CONFIRMED` · `PENDING` · `CANCELLED`

---

### 🛡 관리자 - 항공편 (Admin Flights) `ROLE_ADMIN`

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/admin/flights | 항공편 등록 | ✅ ADMIN |
| PUT | /api/admin/flights/{id} | 항공편 수정 | ✅ ADMIN |

---

### 🛡 관리자 - 예약 (Admin Reservations) `ROLE_ADMIN`

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | /api/admin/reservations | 전체 예약 목록 조회 | ✅ ADMIN |
| PATCH | /api/admin/reservations/{id}/status?status= | 예약 상태 강제 변경 | ✅ ADMIN |

---

### 📊 관리자 - 통계 (Admin Stats) `ROLE_ADMIN`

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | /api/admin/stats/daily | 일별 예약 수 / 매출 통계 | ✅ ADMIN |
| GET | /api/admin/stats/monthly | 월별 예약 수 / 매출 통계 | ✅ ADMIN |
| GET | /api/admin/stats/route | 노선별 예약 수 / 매출 통계 | ✅ ADMIN |

**통계 응답 필드**

| 통계 유형 | 응답 필드 |
|-----------|-----------|
| 일별 (DailyStats) | `date`, `reservationCount`, `totalRevenue` |
| 월별 (MonthlyStats) | `year`, `month`, `reservationCount`, `totalRevenue` |
| 노선별 (RouteStats) | `origin`, `destination`, `reservationCount`, `totalRevenue` |

---

## 📁 폴더 구조

```
src/main/java/com/aviation/reservation/
├── ReservationApplication.java
├── config/
│   ├── SecurityConfig.java               # Spring Security + JWT 설정
│   ├── JwtTokenProvider.java             # JWT 생성 / 검증
│   ├── JwtAuthenticationFilter.java      # JWT 필터
│   └── GlobalExceptionHandler.java       # 전역 예외 처리
├── controller/
│   ├── AuthController.java               # 회원가입 / 로그인
│   ├── FlightController.java             # 항공편 조회 / 검색
│   ├── ReservationController.java        # 예약 생성 / 조회 / 취소
│   ├── AdminFlightController.java        # 관리자 항공편 등록 / 수정
│   └── AdminReservationController.java   # 관리자 예약 관리 / 통계
├── service/
│   ├── AuthService.java
│   ├── FlightService.java
│   ├── SeatService.java
│   ├── ReservationService.java
│   └── AdminService.java                 # 관리자 비즈니스 로직 + 통계 집계
├── repository/
│   ├── UserRepository.java
│   ├── FlightRepository.java
│   ├── SeatRepository.java
│   ├── PassengerRepository.java
│   └── ReservationRepository.java        # JPQL 통계 쿼리 포함
├── entity/
│   ├── User.java                         # ROLE_USER / ROLE_ADMIN
│   ├── Flight.java                       # FlightStatus Enum 포함
│   ├── Seat.java                         # 좌석 엔티티
│   ├── Passenger.java
│   └── Reservation.java                  # ReservationStatus Enum 포함
└── dto/
    ├── AuthDto.java                      # SignupRequest / LoginRequest / TokenResponse
    ├── FlightDto.java                    # Request / UpdateRequest / Response
    ├── SeatDto.java
    ├── PassengerDto.java
    ├── ReservationDto.java               # Request / Response
    └── StatsDto.java                     # DailyStats / MonthlyStats / RouteStats
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
│   flights    │       │     reservations     │       │    users     │
├──────────────┤       ├─────────────────────┤       ├──────────────┤
│ id (PK)      │◄──┐   │ id (PK)             │   ┌──►│ id (PK)      │
│ flightNumber │   └───│ flight_id (FK)       │   │   │ username     │
│ origin       │       │ user_id (FK)         │───┘   │ password     │
│ destination  │       │ seat_id (FK)         │──┐    │ email        │
│ departureTime│       │ reservationCode      │  │    │ role         │
│ arrivalTime  │       │ status               │  │    └──────────────┘
│ totalSeats   │       │ reservedAt           │  │
│ availSeats   │       └─────────────────────┘  │    ┌──────────────┐
│ price        │                                 └───►│    seats     │
│ status       │◄──────────────────────────────────── ├──────────────┤
└──────────────┘                                      │ id (PK)      │
                                                      │ flight_id(FK)│
                                                      │ seatNumber   │
                                                      │ isAvailable  │
                                                      └──────────────┘
```

---

## 📚 학습 포인트

- RESTful API 설계 / HTTP 상태 코드 활용
- Spring Data JPA / @ManyToOne, @OneToMany 연관관계 매핑
- @Transactional 트랜잭션 처리 / readOnly 최적화
- DTO 패턴 (inner static class Request / Response 분리)
- Bean Validation (@NotBlank, @Email, @Future 등)
- Spring Security + JWT Bearer Token 인증 / Stateless 세션
- ROLE_USER / ROLE_ADMIN 권한 분리 및 엔드포인트 접근 제어
- 낙관적 락(Optimistic Lock)을 활용한 동시성 제어 (좌석 중복 예약 방지)
- GlobalExceptionHandler / @RestControllerAdvice 전역 예외 처리
- JPQL 기반 집계 쿼리 (일별 / 월별 / 노선별 통계)
- Docker Compose PostgreSQL 개발 환경 구성
- application.yml 프로파일 분리 (local / prod)

---

## ✨ 한 줄 소개

**Aviation Reservation System은 Spring Boot 3.4 + JPA + JWT 인증 + 동시성 제어 + 관리자 통계 API를 구현한 항공 예약 관리 백엔드 포트폴리오 프로젝트입니다.**
