# project-aviation — CLAUDE.md

## 프로젝트 개요

- **프로젝트명**: project-aviation (항공 예약 시스템)
- **디자인 기조**: 파란색 (항공/하늘 컨셉)
- **Git**: https://github.com/dev-parkynh/project-aviation

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.4 · Java 17 · Gradle · PostgreSQL · Spring Data JPA |
| Frontend | React · TypeScript · Vite · Tailwind CSS (파란 배경 기조) |
| 인증 | JWT · Spring Security (카카오 OAuth는 추후 추가) |
| 인프라 | Docker · docker-compose · GitLab CI/CD |
| 배포 | AWS EC2 예정 |

---

## 개발 Phase

| Phase | 내용 | 상태 |
|-------|------|:----:|
| Phase 1 | 프로젝트 세팅 | ✅ 완료 |
| Phase 2 | 인증 API (JWT + Role) | ✅ 완료 |
| Phase 3 | 항공편/좌석 API | ✅ 완료 |
| Phase 4 | 예약 API (Transaction + 동시성 제어) | ✅ 완료 |
| Phase 5 | 관리자 API + 통계 | ✅ 완료 |
| Phase 6-A | 배치/스케줄러 (미결제 만료 + 월별 정산) | ✅ 완료 |
| Phase 6-B | MSA 구조 분리 (Eureka + Gateway + 4개 서비스) | ✅ 완료 |
| Phase 7 | GitLab CI/CD + 배포 | 🔲 진행 예정 |
| Phase 8 | README + 포트폴리오 정리 | 🔲 진행 예정 |

> 계획은 차후 변경/추가될 수 있음

---

## 향후 추가 예정

- 카카오 OAuth 소셜 로그인
- 결제 시스템 연동
- K8s 배포 전환
- 프론트엔드 React 구현

---

## MSA 서비스 구조 (Phase 6-B)

| 서비스 | 포트 | 역할 |
|--------|------|------|
| eureka-server | 8761 | 서비스 등록/관리 |
| gateway-service | 8080 | 라우팅 + JWT 검증 필터 |
| auth-service | 8081 | 회원가입/로그인, JWT 발급 |
| flight-service | 8082 | 항공편/좌석 + 내부 예약/반환 API |
| booking-service | 8083 | 예약/결제 + 스케줄러 + 통계 |

### 서비스 간 통신
- booking-service → flight-service: **Feign Client** (Eureka 기반 로드밸런싱)
- 내부 API: `POST /internal/seats/{id}/reserve`, `POST /internal/seats/{id}/release`
- 예약 시 flight 정보(flightNumber, origin, destination 등)를 **스냅샷**으로 저장 → cross-service 조회 최소화

## 패키지 구조 (모놀리스 - Phase 6-B 이전 참고용)

```
src/main/java/com/aviation/reservation
├── config/       # SecurityConfig, GlobalExceptionHandler, BatchConfig
├── controller/   # REST 컨트롤러
├── scheduler/    # ReservationScheduler (@Scheduled)
├── service/      # 비즈니스 로직
├── repository/   # Spring Data JPA
├── entity/       # JPA 엔티티 (MonthlyStat 포함)
└── dto/          # Request / Response (inner static class 패턴)
```

---

## 로컬 개발 환경

```bash
# PostgreSQL 컨테이너 시작
docker compose up -d postgres

# 각 서비스 실행 (로컬 프로파일: ddl-auto=create-drop)
./gradlew :eureka-server:bootRun
./gradlew :gateway-service:bootRun
./gradlew :auth-service:bootRun --args='--spring.profiles.active=local'
./gradlew :flight-service:bootRun --args='--spring.profiles.active=local'
./gradlew :booking-service:bootRun --args='--spring.profiles.active=local'

# 전체 도커 빌드 및 실행 (JAR 먼저 빌드 필요)
./gradlew build -x test
docker compose up -d
```

| 항목 | 값 |
|------|----|
| DB Host | localhost:5432 |
| DB Name | aviation_db |
| DB User | aviation |
| DB Password | 1234 |
| Eureka | http://localhost:8761 |
| Gateway | http://localhost:8080 |
| auth-service | http://localhost:8081 |
| flight-service | http://localhost:8082 |
| booking-service | http://localhost:8083 |
