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
| Phase 6 | MSA 구조 분리 | 🔲 진행 예정 |
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

## 패키지 구조

```
com.aviation.reservation
├── config/       # SecurityConfig, GlobalExceptionHandler
├── controller/   # REST 컨트롤러
├── service/      # 비즈니스 로직
├── repository/   # Spring Data JPA
├── entity/       # JPA 엔티티
└── dto/          # Request / Response (inner static class 패턴)
```

---

## 로컬 개발 환경

```bash
# PostgreSQL 컨테이너 시작
docker compose up -d

# Spring Boot 실행 (ddl-auto: create-drop)
./gradlew bootRun --args='--spring.profiles.active=local'
```

| 항목 | 값 |
|------|----|
| DB Host | localhost:5432 |
| DB Name | aviation_db |
| DB User | aviation |
| DB Password | 1234 |
| 서버 포트 | 8080 |
