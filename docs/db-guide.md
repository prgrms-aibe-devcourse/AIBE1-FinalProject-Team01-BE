# 💾Local Database Setting Guide

## 개발 환경 DB 구성

본 프로젝트는 **MySQL 8.0**과 **Redis**를 사용하며, Docker Compose를 통해 쉽게 개발 환경을 구성할 수 있습니다.

## 사전 요구사항

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

> 💡 Tip: Docker Desktop을 설치하면 Docker Compose가 함께 설치됩니다.
>

## 빠른 시작

### 1. DB 컨테이너 실행

프로젝트 루트 디렉토리에서 다음 명령어를 실행하세요:

```bash
# docker 폴더로 이동
cd docker

# DB 컨테이너 시작 (백그라운드 실행)
docker-compose up -d

```

**또는 루트 디렉토리에서 직접 실행:**

```bash
# 루트 디렉토리에서 docker-compose.yml 파일 경로 지정
docker-compose -f docker/docker-compose.yml up -d

```

### 2. 컨테이너 상태 확인

```bash
# docker 폴더에서 실행
cd docker
docker-compose ps

# 또는 루트 디렉토리에서
docker-compose -f docker/docker-compose.yml ps

# 로그 확인 (선택사항)
docker-compose logs -f

```

### 3. 애플리케이션 실행

이제 Spring Boot 애플리케이션을 실행하면 자동으로 DB에 연결됩니다.

## DB 연결 정보

### MySQL 정보

| 항목 | 값 |
| --- | --- |
| 호스트 | `localhost` |
| 포트 | `3306` |
| 데이터베이스 | `amatuers` |
| 사용자 | `admin` |
| 비밀번호 | `1234` |
| Root 비밀번호 | `1234` |

### Redis 정보

| 항목 | 값 |
| --- | --- |
| 호스트 | `localhost` |
| 포트 | `6379` |

## Spring Boot 설정

`application.yml` 설정 예시:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/amatuers
    username: admin
    password: 1234
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

  data:
    redis:
      host: localhost
      port: 6379

```

## 데이터 영속성

- **MySQL 데이터**: `mysql_data` Docker Volume에 저장됩니다
- **컨테이너 재시작**: 데이터가 보존됩니다
- **완전 삭제**: `docker-compose down -v` 명령어로 볼륨까지 삭제 가능

> ⚠️ 주의: docker-compose down -v 실행 시 모든 데이터가 삭제됩니다!
>

## 컨테이너 관리

### 기본 명령어

```bash
# docker 폴더에서 실행하는 경우
cd docker

# 컨테이너 시작
docker-compose up -d

# 컨테이너 중지
docker-compose stop

# 컨테이너 중지 및 삭제 (데이터 보존)
docker-compose down

# 컨테이너 및 볼륨 삭제 (데이터 완전 삭제)
docker-compose down -v

```

**루트 디렉토리에서 실행하는 경우:**

```bash
# 컨테이너 시작
docker-compose -f docker/docker-compose.yml up -d

# 컨테이너 중지
docker-compose -f docker/docker-compose.yml stop

# 컨테이너 중지 및 삭제 (데이터 보존)
docker-compose -f docker/docker-compose.yml down

# 컨테이너 및 볼륨 삭제 (데이터 완전 삭제)
docker-compose -f docker/docker-compose.yml down -v

```

### 개별 컨테이너 관리

```bash
# docker 폴더에서 실행
cd docker

# MySQL만 재시작
docker-compose restart mysql

# Redis만 재시작
docker-compose restart redis

# 특정 컨테이너 로그 확인
docker-compose logs mysql
docker-compose logs redis

```

**루트 디렉토리에서 실행:**

```bash
# MySQL만 재시작
docker-compose -f docker/docker-compose.yml restart mysql

# Redis만 재시작
docker-compose -f docker/docker-compose.yml restart redis

# 특정 컨테이너 로그 확인
docker-compose -f docker/docker-compose.yml logs mysql

```

## 직접 DB 접속

### MySQL 접속

**방법 1: Docker 컨테이너를 통한 접속**

```bash
docker exec -it mysql-container mysql -u admin -p amatuers

```

**방법 2: 로컬 MySQL 클라이언트 사용**

```bash
mysql -h localhost -P 3306 -u admin -p amatuers

```

### Redis 접속

**방법 1: Docker 컨테이너를 통한 접속**

```bash
docker exec -it redis-container redis-cli

```

**방법 2: 로컬 Redis 클라이언트 사용**

```bash
redis-cli -h localhost -p 6379

```

## 트러블슈팅

### 🚨 포트 충돌 문제

**증상**: `bind: address already in use` 에러 발생

**해결방법**:

```bash
# 포트 사용 프로세스 확인
lsof -i :3306  # MySQL
lsof -i :6379  # Redis

# 기존 프로세스 종료 후 다시 시도

```

### 🚨 컨테이너 시작 실패

**증상**: 컨테이너가 계속 재시작되거나 실행되지 않음

**해결방법**:

```bash
# 1. 로그 확인
docker-compose logs mysql
docker-compose logs redis

# 2. 기존 컨테이너 완전 삭제 후 재시작
docker-compose down -v
docker-compose up -d

```

### 🚨 MySQL 연결 오류

**증상**: `Access denied for user` 또는 `Unknown database` 에러

**해결방법**:

```bash
# 1. MySQL 컨테이너 재생성
docker-compose down
docker volume rm $(docker volume ls -q | grep mysql)
docker-compose up -d

# 2. 연결 정보 재확인
docker exec -it mysql-container mysql -u root -p

```

### 🚨 권한 문제

**증상**: Volume 마운트 권한 오류

**해결방법**:

```bash
# Docker 볼륨 권한 재설정
docker-compose down
docker system prune -a --volumes
docker-compose up -d

```

### 🚨 데이터 초기화

**개발 중 DB를 완전히 초기화하고 싶을 때**:

```bash
# 모든 데이터 삭제 후 재시작
docker-compose down -v
docker-compose up -d

```

---

## 📚 추가 자료

- [MySQL 8.0 공식 문서](https://dev.mysql.com/doc/refman/8.0/en/)
- [Redis 공식 문서](https://redis.io/documentation)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Spring Boot Data JPA 가이드](https://spring.io/guides/gs/accessing-data-jpa/)