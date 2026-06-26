# ✈️ Project Aviation — 항공 예약 시스템

> Spring Boot 기반 항공 예약 백엔드 포트폴리오입니다.  
> 모놀리식으로 시작해 MSA(마이크로서비스)로 직접 분리한 전 과정을 담았습니다.

---

## 목차

1. [프로젝트 소개](#1-프로젝트-소개)
2. [기술 스택](#2-기술-스택)
3. [시스템 아키텍처](#3-시스템-아키텍처)
4. [Phase별 구현 내용](#4-phase별-구현-내용)
5. [기술 선택 이유](#5-기술-선택-이유)
6. [로컬 실행 방법](#6-로컬-실행-방법)
7. [배포 가이드](#7-배포-가이드-gitlab-cicd--aws-ec2)
8. [API 엔드포인트](#8-api-엔드포인트)

---

## 1. 프로젝트 소개

**Project Aviation**은 항공편 검색·예약·결제 흐름을 구현한 백엔드 포트폴리오 프로젝트입니다.

단순 CRUD를 넘어 실무에서 자주 마주치는 문제들을 직접 해결하는 데 집중했습니다.

- **동시성 문제** — 같은 좌석에 두 명이 동시에 예약 요청을 보내면?
- **서비스 간 통신** — MSA 환경에서 예약 생성 시 항공편 서비스를 어떻게 호출하나?
- **트랜잭션 경계** — 좌석 점유와 예약 저장이 반드시 함께 성공/실패해야 한다면?
- **외부 장애 격리** — 날씨 API가 죽어도 항공편 조회는 정상 동작해야 한다면?

| 항목 | 내용 |
|------|------|
| 개발자 | 박용희 |
| 개발 기간 | 2025.11 ~ 2026.06 |
| 목적 | Spring Boot + MSA 백엔드 포트폴리오 |
| GitHub | https://github.com/dev-parkynh/project-aviation |

---

## 2. 기술 스택

### Backend

| 분류 | 기술 |
|------|------|
| 언어 / 플랫폼 | Java 17, Spring Boot 3.4.1, Gradle |
| 데이터 접근 | Spring Data JPA, Hibernate, JPQL |
| 보안 | Spring Security, JWT (jjwt 0.12.6) |
| 서비스 메시 | Spring Cloud 2024.0.0, Spring Cloud Gateway, Netflix Eureka |
| 서비스 간 통신 | OpenFeign |
| 비동기 / 외부 API | Spring WebFlux (WebClient) |
| 배치 | Spring Batch 5 |
| 이메일 | Spring Mail + Thymeleaf 템플릿 |
| 캐시 | Caffeine (인메모리) |
| 검증 | Bean Validation (Jakarta) |
| 유틸리티 | Lombok |

### Database / 인프라

| 분류 | 기술 |
|------|------|
| 데이터베이스 | PostgreSQL 16 |
| 컨테이너 | Docker, Docker Compose |
| 배포 (예정) | AWS EC2, GitLab CI/CD |

---

## 3. 시스템 아키텍처

### MSA 서비스 구성

```
클라이언트
    │
    ▼
┌─────────────────────────────────┐
│  gateway-service  :8080         │  ← JWT 검증 GlobalFilter
│  Spring Cloud Gateway           │    라우팅 (lb://서비스명)
└───────────┬─────────────────────┘
            │ Eureka 서비스 디스커버리
            ▼
┌────────────────────────────────────────────────────────┐
│              eureka-server  :8761                      │
│              Netflix Eureka Server                     │
└────────────────────────────────────────────────────────┘
            │
    ┌───────┼──────────────────────┐
    ▼       ▼                      ▼
┌────────┐ ┌─────────────────┐ ┌─────────────────────────────────┐
│ auth   │ │  flight-service │ │       booking-service           │
│ :8081  │ │  :8082          │ │       :8083                     │
│ 회원가 │ │  항공편 / 좌석  │ │  예약 / 통계 / 스케줄러 / 배치  │
│ 입/로그 │ │  외부 API 연동  │ │                                 │
│ JWT 발급│ │  내부 API 제공  │ │  ──── Feign ──────────────────► │
└────────┘ └─────────────────┘ └─────────────────────────────────┘
    │               │                        │
    └───────────────┴────────────────────────┘
                    │
              ┌─────▼─────┐
              │ PostgreSQL │
              │  :5432     │
              └───────────┘
```

### 서비스별 역할

| 서비스 | 포트 | 역할 | 핵심 의존성 |
|--------|------|------|-------------|
| eureka-server | 8761 | 서비스 등록 / 헬스체크 | spring-cloud-starter-netflix-eureka-server |
| gateway-service | 8080 | 라우팅 + JWT 검증 GlobalFilter | spring-cloud-starter-gateway |
| auth-service | 8081 | 회원가입·로그인 / JWT 발급 | Spring Security, jjwt |
| flight-service | 8082 | 항공편·좌석 CRUD / 외부 API / 내부 예약 API | JPA, WebFlux, Caffeine |
| booking-service | 8083 | 예약·취소 / 통계 / 스케줄러 / 월별 배치 | OpenFeign, Spring Batch, Spring Mail |

### 서비스 간 통신 흐름 (예약 생성)

```
클라이언트
  POST /api/reservations
      │
      ▼
gateway-service
  JWT 유효성 검증
  → X-User-Email, X-User-Role, X-User-Name 헤더 주입
      │
      ▼
booking-service
  JwtAuthenticationFilter → SecurityContext 세팅
  ReservationService.createReservation()
      │
      ▼ Feign 호출
flight-service
  POST /internal/seats/{seatId}/reserve
  SELECT FOR UPDATE (비관적 락)
  seat.isAvailable = false
  flight.availableSeats -= 1
  → 스냅샷 DTO 반환 (flightNumber, origin, seatNo ...)
      │
      ▼
booking-service
  Reservation 저장 (스냅샷 필드 포함)
  이메일 비동기 발송
  → 응답 반환
```

---

## 4. Phase별 구현 내용

### Phase 1 — 프로젝트 세팅

- Spring Boot 3.4.1 + Java 17 + Gradle 멀티모듈 구성
- PostgreSQL Docker Compose 환경
- `application.yml` / `application-local.yml` 프로파일 분리

---

### Phase 2 — JWT 인증

**구현 내용**

- 회원가입 / 로그인 API (Access Token 발급)
- `JwtTokenProvider` — 토큰 생성·파싱·검증
- `JwtAuthenticationFilter` — 매 요청마다 토큰 추출 후 `SecurityContextHolder` 세팅
- `ROLE_USER` / `ROLE_ADMIN` 권한 분리

**핵심 흐름**

```
요청 헤더: Authorization: Bearer <token>
    │
JwtAuthenticationFilter.doFilterInternal()
    │
JwtTokenProvider.validateToken()  →  parseClaims() (서명·만료 검증)
    │
getEmailFromToken() + getRoleFromToken()
    │
UsernamePasswordAuthenticationToken → SecurityContextHolder
    │
SecurityConfig.authorizeHttpRequests() 에서 권한 체크
```

**MSA 확장 시 변경점**  
auth-service JWT에 `name` claim 추가 → gateway가 downstream 헤더로 전달  
booking-service `JwtAuthenticationFilter`에서 `authentication.setDetails(name)`으로 저장

---

### Phase 3 — 항공편 / 좌석 API

**구현 내용**

- 항공편 등록 / 전체 조회 / 단건 조회 / 상태 변경
- 날짜·출발지·도착지 조건 검색 (JPQL)
- 좌석 목록 조회 (`SeatClass`: ECONOMY / BUSINESS / FIRST)
- `FlightStatus` Enum: `SCHEDULED` · `DELAYED` · `CANCELLED` · `DEPARTED` · `ARRIVED`

**JPQL 검색 쿼리**

```java
@Query("SELECT f FROM Flight f " +
       "WHERE f.origin = :origin AND f.destination = :destination " +
       "AND f.departureTime >= :start AND f.departureTime <= :end " +
       "AND f.availableSeats > 0 AND f.status = 'SCHEDULED'")
List<Flight> findAvailableFlights(...);
```

`availableSeats > 0` 조건으로 만석 항공편을 검색에서 자동 제외합니다.

---

### Phase 4 — 예약 API (동시성 제어)

**구현 내용**

- 예약 생성 / 내 예약 목록 / 단건 조회 / 취소
- 좌석 점유 + 예약 저장 단일 트랜잭션
- `ReservationStatus`: `PENDING` · `CONFIRMED` · `CANCELLED` · `EXPIRED`
- 탑승객(Passenger) 정보 함께 저장

**비관적 락으로 동시 예약 방지**

```java
// SeatRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.id = :id")
Optional<Seat> findByIdWithLock(@Param("id") Long id);
```

동일 좌석에 동시 요청이 들어오면 첫 번째 트랜잭션이 `SELECT FOR UPDATE`로 행을 잠급니다.  
두 번째 요청은 첫 번째가 커밋될 때까지 대기 → 커밋 후 `isAvailable = false` 확인 → 예외 발생.

> **낙관적 락이 아닌 비관적 락을 선택한 이유**  
> 항공 예약은 충돌 빈도가 높고(인기 노선, 특가 좌석), 충돌 시 재시도 비용이 큽니다.  
> 낙관적 락은 충돌이 드문 경우에 유리하므로, 좌석 예약처럼 경합이 잦은 시나리오에는 비관적 락이 적합합니다.

---

### Phase 5 — 관리자 API + 통계

**구현 내용**

- `ROLE_ADMIN` 전용 항공편 등록·수정
- 전체 예약 목록 조회 / 예약 상태 강제 변경
- 일별·월별·노선별 예약 수·매출 통계

**JPQL 집계 쿼리 예시**

```java
// 노선별 통계
@Query("SELECT r.origin, r.destination, COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
       "FROM Reservation r WHERE r.status = :status " +
       "GROUP BY r.origin, r.destination " +
       "ORDER BY COUNT(r) DESC")
List<Object[]> findRouteStats(@Param("status") ReservationStatus status);
```

---

### Phase 6-A — 이메일 알림 / 외부 API / 배치·스케줄러

**이메일 알림 (Spring Mail + Thymeleaf)**

- 예약 확정 시 Thymeleaf HTML 메일 비동기 발송 (`@Async`)
- Gmail SMTP (TLS 587)

**외부 API 연동 (WebClient)**

- 날씨 API — 항공편 목적지 현재 날씨 조회 (OpenWeatherMap)
- 환율 API — USD 기준 실시간 환율 (open.er-api.com), Caffeine 1시간 캐시

**스케줄러 (`@Scheduled`)**

- 10분마다 미결제(PENDING) 예약 자동 만료 처리 (30분 초과 시 EXPIRED)
- 좌석·항공편 잔여석 자동 복구

**Spring Batch (월별 정산)**

- `monthlyStatJob` — 전월 노선별 예약 수·매출 집계 → `monthly_stats` 테이블 upsert
- `@StepScope` + JobParameter로 특정 연월 재집계 가능
- chunk size 50, `spring.batch.job.enabled=false` (자동 실행 방지)

---

### Phase 6-B — MSA 구조 분리

**변경 사항**

- Gradle 단일 모듈 → 멀티모듈 (루트 BOM 관리)
- 모놀리스 5개 서비스로 분리

**Spring Cloud 구성**

| 컴포넌트 | 역할 |
|----------|------|
| Eureka Server | 서비스 등록·발견·헬스체크 |
| Spring Cloud Gateway | 단일 진입점, JWT 검증, `lb://` 로드밸런싱 |
| OpenFeign | booking → flight 동기 호출, Eureka 연동 |

**Reservation 엔티티 재설계**

모놀리스에서는 `@ManyToOne Flight`, `@ManyToOne Seat` 관계를 가졌지만,  
MSA에서는 서비스가 분리되어 JPA 관계를 유지할 수 없습니다.

```
모놀리스:  Reservation → @ManyToOne Flight, @ManyToOne Seat
MSA:       Reservation { flightNumber, origin, destination, seatNo, seatClass, ... }
```

예약 시점에 주요 필드를 **스냅샷**으로 저장합니다.  
→ 이후 항공편 정보가 변경되어도 예약 내역은 확정 시점 기준으로 유지됩니다.  
→ 예약 조회 시 flight-service 호출이 불필요해집니다.

**내부 API 설계 (flight-service)**

```
POST /internal/seats/{seatId}/reserve   # 좌석 점유 (비관적 락) + 스냅샷 반환
POST /internal/seats/{seatId}/release   # 좌석 반환 (취소·만료 시)
```

게이트웨이는 `/internal/**` 라우트를 외부에 노출하지 않습니다.  
booking-service는 Feign으로 직접 호출합니다.

---

## 5. 기술 선택 이유

### WebClient vs RestTemplate

| | WebClient | RestTemplate |
|--|-----------|--------------|
| I/O 모델 | 논블로킹 (Reactor) | 블로킹 |
| Spring 지원 | 현재 권장 | 6.x부터 유지보수 모드 |
| 스트리밍 | 지원 | 미지원 |

날씨·환율 외부 API는 응답이 느릴 수 있습니다. WebClient를 사용하면 해당 스레드를 블로킹하지 않고 다른 요청을 처리할 수 있습니다. RestTemplate은 Spring 6에서 유지보수 모드로 전환되어 신규 프로젝트에서는 WebClient를 선택했습니다.

### Caffeine vs Redis (캐시)

| | Caffeine | Redis |
|--|----------|-------|
| 설치 | 의존성만 추가 | 별도 인프라 필요 |
| 속도 | 인메모리, 매우 빠름 | 네트워크 I/O 발생 |
| 분산 | 단일 인스턴스 | 다중 인스턴스 공유 가능 |
| 용도 | 단일 서버 단순 캐시 | 분산 캐시, 세션 |

환율 데이터는 1시간 단위로 갱신되며, 전체 데이터 크기가 작습니다. 단일 서비스 인메모리 캐시로 충분하므로 운영 비용 없는 Caffeine을 선택했습니다. 서비스가 수평 확장되거나 인스턴스 간 캐시 공유가 필요해지면 Redis로 전환할 수 있습니다.

### 비관적 락 vs 낙관적 락 (동시성 제어)

| | 비관적 락 | 낙관적 락 |
|--|----------|----------|
| 방식 | SELECT FOR UPDATE | @Version 충돌 감지 |
| 성능 | 경합 없으면 오버헤드 | 경합 없으면 빠름 |
| 충돌 多 | 안전, 대기 후 처리 | 잦은 롤백·재시도 |
| 적합 케이스 | 좌석 예약, 재고 감소 | 수정 충돌이 드문 경우 |

항공 좌석은 특가 프로모션 시 초당 수백 건의 경합이 발생할 수 있습니다. 낙관적 락은 이 경우 대부분의 요청이 `OptimisticLockException`으로 실패·재시도를 반복합니다. 비관적 락은 대기 후 순차 처리되어 데이터 정합성을 보장합니다.

### MSA vs 모놀리식 (이 프로젝트에서의 선택)

이 프로젝트는 포트폴리오 목적으로 **일부러 모놀리식으로 시작해 MSA로 전환**하는 과정을 담았습니다.

실무에서 MSA는 항상 정답이 아닙니다.

| 상황 | 권장 |
|------|------|
| 초기 스타트업, 팀 소규모 | 모놀리식 |
| 서비스 규모 성장, 팀 분리 | MSA 고려 |
| 특정 도메인 독립 배포 필요 | MSA |
| 도메인 경계가 명확하지 않음 | 모듈러 모놀리식 |

이 프로젝트에서는 항공편·예약·인증 도메인이 명확히 분리되고, Spring Cloud 스택 경험을 쌓기 위해 MSA 전환을 진행했습니다.

---

## 6. 로컬 실행 방법

### 사전 요구사항

- Java 17+
- Docker Desktop

### 빠른 시작 (인프라만)

```bash
git clone https://github.com/dev-parkynh/project-aviation.git
cd project-aviation

# PostgreSQL 컨테이너 시작
docker compose up -d postgres
```

각 서비스를 IDE(IntelliJ IDEA) 또는 터미널에서 순서대로 실행합니다.

```bash
# 1. Eureka 먼저
./gradlew :eureka-server:bootRun

# 2. Gateway
./gradlew :gateway-service:bootRun

# 3. 나머지 (순서 무관)
./gradlew :auth-service:bootRun    --args='--spring.profiles.active=local'
./gradlew :flight-service:bootRun  --args='--spring.profiles.active=local'
./gradlew :booking-service:bootRun --args='--spring.profiles.active=local'
```

`local` 프로파일 적용 시 `ddl-auto: create-drop`으로 테이블을 자동 생성합니다.

### 전체 Docker Compose 실행 (JAR 빌드 후)

```bash
# 전체 빌드
./gradlew build -x test

# 전체 서비스 실행
docker compose up -d
```

### 포트 및 접속 정보

| 서비스 | URL |
|--------|-----|
| Eureka Dashboard | http://localhost:8761 |
| Gateway (API 진입점) | http://localhost:8080 |
| auth-service | http://localhost:8081 |
| flight-service | http://localhost:8082 |
| booking-service | http://localhost:8083 |
| PostgreSQL | localhost:5432 / aviation_db / aviation / 1234 |

### 환경변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `JWT_SECRET` | (Base64 키) | JWT 서명 키 (모든 서비스 동일) |
| `DB_USERNAME` | aviation | DB 사용자 |
| `DB_PASSWORD` | 1234 | DB 비밀번호 |
| `MAIL_USERNAME` | — | Gmail 계정 |
| `MAIL_PASSWORD` | — | Gmail 앱 비밀번호 |
| `WEATHER_API_KEY` | — | OpenWeatherMap API 키 |

---

## 7. 배포 가이드 (GitLab CI/CD + AWS EC2)

### 아키텍처

```
GitLab Push (main)
    │
    ▼
[build]  → Gradle 빌드, JAR 아티팩트 생성
    │
    ▼
[test]   → PostgreSQL 서비스 컨테이너로 통합 테스트
    │
    ▼
[docker-build] → 5개 서비스 Docker 이미지 빌드 & Docker Hub 푸시 (병렬)
    │
    ▼
[deploy] → EC2 SSH 접속 → docker compose pull & up
    │
    ▼
AWS EC2
  nginx:80 → gateway-service:8080 → MSA 서비스들
```

### GitLab CI/CD 변수 설정

GitLab 프로젝트 → Settings → CI/CD → Variables에서 아래 변수를 등록합니다.

| 변수 | 설명 | Masked |
|------|------|:------:|
| `DOCKERHUB_USERNAME` | Docker Hub 사용자명 | ❌ |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token | ✅ |
| `EC2_HOST` | EC2 퍼블릭 IP 또는 도메인 | ❌ |
| `EC2_USER` | EC2 접속 사용자 (기본값: `ubuntu`) | ❌ |
| `EC2_SSH_KEY` | EC2 접속용 PEM 키 내용 (전체) | ✅ |

### EC2 초기 설정

```bash
# EC2 접속 후 1회 실행
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
sudo mkdir -p /app
sudo chown ubuntu:ubuntu /app

# .env 파일 생성 (배포 전 필수)
cp .env.example /app/.env
vi /app/.env   # 실제 값으로 채워 넣기
```

### 배포 흐름

1. `main` 브랜치에 푸시하면 GitLab CI 파이프라인이 자동으로 시작됩니다.
2. `docker-build` 스테이지에서 5개 서비스가 병렬로 빌드·푸시됩니다.
   - 이미지 태그: `{commit SHA}` + `latest` 두 가지로 푸시
3. `deploy` 스테이지에서 `deploy.sh`가 EC2로 `docker-compose.prod.yml`을 전송하고 배포합니다.

### 수동 배포 (긴급 시)

```bash
# EC2에서 직접 실행
cd /app
export DOCKERHUB_USERNAME=your-username
export IMAGE_TAG=latest
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d --remove-orphans
```

### 주요 파일

| 파일 | 설명 |
|------|------|
| `.gitlab-ci.yml` | CI/CD 파이프라인 정의 (4 stage) |
| `docker-compose.prod.yml` | 프로덕션용 Compose (이미지 참조) |
| `deploy.sh` | EC2 배포 자동화 스크립트 |
| `nginx/nginx.conf` | Nginx 리버스 프록시 설정 |
| `.env.example` | 환경변수 템플릿 |

---

## 8. API 엔드포인트

모든 요청은 `gateway-service` (`:8080`)를 통해 들어옵니다.  
인증 필요 엔드포인트는 `Authorization: Bearer <token>` 헤더를 포함해야 합니다.

### 인증 (auth-service → `/api/auth`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| POST | `/api/auth/signup` | 회원가입 + JWT 발급 | ❌ |
| POST | `/api/auth/login` | 로그인 + JWT 발급 | ❌ |

**회원가입 요청 예시**

```json
POST /api/auth/signup
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

**응답**

```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

---

### 항공편 (flight-service → `/api/flights`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| GET | `/api/flights` | 전체 항공편 조회 | ❌ |
| GET | `/api/flights?departure=ICN&arrival=NRT&date=2026-07-01` | 날짜·노선 검색 | ❌ |
| GET | `/api/flights/{id}` | 항공편 단건 조회 | ❌ |
| GET | `/api/flights/{id}/seats` | 좌석 목록 조회 | ❌ |
| PATCH | `/api/flights/{id}/status?status=DELAYED` | 항공편 상태 변경 | ✅ |
| GET | `/api/flights/{id}/weather` | 목적지 현재 날씨 | ❌ |
| GET | `/api/flights/exchange-rate` | USD 기준 환율 (캐시 1h) | ❌ |

**FlightStatus** `SCHEDULED` · `DELAYED` · `CANCELLED` · `DEPARTED` · `ARRIVED`

---

### 예약 (booking-service → `/api/reservations`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| POST | `/api/reservations` | 예약 생성 | ✅ |
| GET | `/api/reservations` | 내 예약 목록 | ✅ |
| GET | `/api/reservations/{id}` | 예약 단건 조회 | ✅ |
| PATCH | `/api/reservations/{id}/cancel` | 예약 취소 | ✅ |

**예약 생성 요청 예시**

```json
POST /api/reservations
Authorization: Bearer <token>

{
  "flightId": 1,
  "seatId": 15,
  "passengers": [
    {
      "name": "홍길동",
      "passportNo": "M12345678",
      "nationality": "KR"
    }
  ]
}
```

**ReservationStatus** `PENDING` · `CONFIRMED` · `CANCELLED` · `EXPIRED`

---

### 관리자 — 항공편 (`/api/admin/flights`) `ROLE_ADMIN`

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| POST | `/api/admin/flights` | 항공편 등록 | ✅ ADMIN |
| PUT | `/api/admin/flights/{id}` | 항공편 수정 | ✅ ADMIN |

**항공편 등록 요청 예시**

```json
POST /api/admin/flights
{
  "flightNumber": "KE001",
  "origin": "ICN",
  "destination": "NRT",
  "departureTime": "2026-08-01T09:00:00",
  "arrivalTime": "2026-08-01T11:30:00",
  "totalSeats": 200,
  "price": 150000.00
}
```

---

### 관리자 — 예약 (`/api/admin/reservations`) `ROLE_ADMIN`

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| GET | `/api/admin/reservations` | 전체 예약 목록 | ✅ ADMIN |
| PATCH | `/api/admin/reservations/{id}/status?status=CANCELLED` | 예약 상태 강제 변경 | ✅ ADMIN |

---

### 관리자 — 통계 (`/api/admin/stats`) `ROLE_ADMIN`

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| GET | `/api/admin/stats/daily` | 일별 예약 수 / 매출 | ✅ ADMIN |
| GET | `/api/admin/stats/monthly` | 월별 예약 수 / 매출 | ✅ ADMIN |
| GET | `/api/admin/stats/route` | 노선별 예약 수 / 매출 | ✅ ADMIN |

**통계 응답 예시 (노선별)**

```json
[
  {
    "origin": "ICN",
    "destination": "NRT",
    "reservationCount": 342,
    "totalRevenue": 51300000.00
  }
]
```

---

## ERD

```
┌────────────────┐          ┌──────────────────────────────────────────┐
│     users      │          │              reservations                 │
├────────────────┤          ├──────────────────────────────────────────┤
│ id (PK)        │          │ id (PK)                                   │
│ email          │          │ user_email     ← 스냅샷                   │
│ password       │          │ user_name      ← 스냅샷                   │
│ name           │          │ flight_id                                 │
│ role           │          │ flight_number  ← 스냅샷                   │
└────────────────┘          │ origin         ← 스냅샷                   │
                            │ destination    ← 스냅샷                   │
┌────────────────┐          │ departure_time ← 스냅샷                   │
│    flights     │          │ seat_id                                   │
├────────────────┤          │ seat_no        ← 스냅샷                   │
│ id (PK)        │          │ seat_class     ← 스냅샷                   │
│ flight_number  │          │ status                                    │
│ origin         │          │ total_price                               │
│ destination    │          │ created_at                                │
│ departure_time │          └────────────────┬─────────────────────────┘
│ arrival_time   │                           │ 1:N
│ total_seats    │          ┌────────────────▼─────────┐
│ available_seats│          │        passengers         │
│ price          │          ├──────────────────────────┤
│ status         │          │ id (PK)                   │
└────────┬───────┘          │ reservation_id (FK)       │
         │ 1:N              │ name                      │
┌────────▼───────┐          │ passport_no               │
│     seats      │          │ nationality               │
├────────────────┤          └──────────────────────────┘
│ id (PK)        │
│ flight_id (FK) │          ┌──────────────────────────┐
│ seat_no        │          │       monthly_stats       │
│ seat_class     │          ├──────────────────────────┤
│ is_available   │          │ id (PK)                   │
└────────────────┘          │ stat_year                 │
                            │ stat_month                │
                            │ origin                    │
                            │ destination               │
                            │ reservation_count         │
                            │ total_revenue             │
                            │ calculated_at             │
                            └──────────────────────────┘
```

MSA 전환 후 `reservations` 테이블은 JPA 외래키 관계 대신 스냅샷 필드를 직접 저장합니다.  
예약 조회 시 flight-service / auth-service 호출 없이 자체 완결적으로 응답합니다.
