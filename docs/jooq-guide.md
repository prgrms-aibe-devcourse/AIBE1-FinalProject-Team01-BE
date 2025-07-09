# jOOQ 사용법 가이드

> 팀원들을 위한 jOOQ 실무 사용법

## 📖 목차

1. [기본 개념](#1-기본-개념)
2. [핵심 클래스](#2-핵심-클래스)
3. [기본 쿼리 작성](#3-기본-쿼리-작성)
4. [데이터 조회 방법](#4-데이터-조회-방법)
5. [동적 쿼리](#5-동적-쿼리)
6. [페이징 처리](#6-페이징-처리)
7. [실무 예제](#7-실무-예제)
8. [유용한 메서드](#8-유용한-메서드)

---

## 1. 기본 개념

### jOOQ vs JPA
- **jOOQ**: SQL과 거의 동일한 문법, 컴파일 타임 체크
- **JPA**: 객체 중심, 런타임 체크

### 메서드 체이닝 순서
```
SELECT → FROM → JOIN → WHERE → ORDER BY → LIMIT → OFFSET → FETCH
```

---

## 2. 핵심 클래스

### DSLContext
```java
@Autowired
private DSLContext dsl;  // 모든 쿼리의 시작점
```

### 주요 인터페이스
```java
// 단계별 체이닝
SelectFromStep<Record> fromStep = dsl.select().from(USER);
SelectJoinStep<Record> joinStep = fromStep.join(POST).on(...);
SelectConditionStep<Record> whereStep = joinStep.where(...);
SelectOrderByStep<Record> orderStep = whereStep.orderBy(...);
```

### 결과 클래스
```java
Record record = dsl.selectFrom(USER).fetchOne();        // 단일 결과
Result<Record> result = dsl.selectFrom(USER).fetch();   // 여러 결과
List<User> users = result.into(User.class);             // 엔티티 변환
```

---

## 3. 기본 쿼리 작성

### SELECT
```java
// 모든 컬럼
dsl.selectFrom(USER)

// 특정 컬럼만
dsl.select(USER.ID, USER.NICKNAME, USER.EMAIL)
   .from(USER)

// 계산된 필드
dsl.select(
    USER.ID,
    DSL.count().as("post_count"),
    DSL.concat(USER.FIRST_NAME, " ", USER.LAST_NAME).as("full_name")
)
```

### JOIN
```java
dsl.select()
   .from(USER)
   .join(POST).on(USER.ID.eq(POST.USER_ID))
   .leftJoin(COMMENT).on(POST.ID.eq(COMMENT.POST_ID))
   .rightJoin(USER_TOPIC).on(USER.ID.eq(USER_TOPIC.USER_ID))
```

### WHERE 조건
```java
// 단일 조건
.where(USER.NICKNAME.eq("john_doe"))
.where(USER.ROLE.in("ADMIN", "USER"))
.where(USER.CREATED_AT.greaterThan(LocalDateTime.now().minusDays(7)))

// 복합 조건
.where(USER.NICKNAME.eq("john_doe")
   .and(USER.VERIFICATION_STATUS.eq("VERIFIED"))
   .or(USER.ROLE.eq("ADMIN")))

// LIKE 검색
.where(USER.NICKNAME.containsIgnoreCase(keyword))
.where(USER.EMAIL.like(concat("%", domain, "%")))
```

---

## 4. 데이터 조회 방법

### 기본 fetch 메서드
```java
// 여러 결과
Result<Record> result = query.fetch();
List<User> users = query.fetchInto(User.class);

// 단일 결과
Record record = query.fetchOne();              // null 가능
Record record = query.fetchSingle();           // null 불가 (예외 발생)
Optional<Record> opt = query.fetchOptional();  // Optional

// 첫 번째 결과
Record first = query.fetchAny();

// 개수 조회
int count = dsl.fetchCount(query);
boolean exists = dsl.fetchExists(query);
```

### 특정 컬럼만 조회
```java
// 단일 컬럼
List<String> nicknames = dsl.select(USER.NICKNAME)
    .from(USER)
    .fetch(USER.NICKNAME);

// 여러 컬럼을 Map으로
List<Map<String, Object>> maps = dsl.selectFrom(USER).fetchMaps();
Map<String, Object> map = dsl.selectFrom(USER).fetchOneMap();
```

### 엔티티 변환
```java
// 직접 변환
List<User> users = dsl.selectFrom(USER)
    .fetchInto(User.class);

// Record에서 변환
User user = record.into(User.class);

// 테이블별 분리 변환
UserRecord userRecord = record.into(USER);
PostRecord postRecord = record.into(POST);
```

---

## 5. 동적 쿼리

### Condition 사용
```java
public List<User> findUsers(String nickname, String email, Role role) {
    List<Condition> conditions = new ArrayList<>();
    
    if (StringUtils.hasText(nickname)) {
        conditions.add(USER.NICKNAME.containsIgnoreCase(nickname));
    }
    
    if (StringUtils.hasText(email)) {
        conditions.add(USER.EMAIL.eq(email));
    }
    
    if (role != null) {
        conditions.add(USER.ROLE.eq(role));
    }
    
    return dsl.selectFrom(USER)
        .where(conditions.isEmpty() ? DSL.noCondition() : DSL.and(conditions))
        .fetchInto(User.class);
}
```

### 조건부 JOIN
```java
SelectJoinStep<Record> query = dsl.select().from(USER);

if (includePostInfo) {
    query = query.join(POST).on(USER.ID.eq(POST.USER_ID))
                 .join(COMMENT).on(POST.ID.eq(COMMENT.POST_ID));
}

return query.fetchInto(User.class);
```

---

## 6. 페이징 처리

### Spring Pageable 활용
```java
public Page<User> findUsersWithPaging(String keyword, Pageable pageable) {
    
    // 기본 쿼리 구성
    SelectConditionStep<Record> query = dsl
        .select(USER.asterisk())
        .from(USER)
        .where(keyword != null ? 
            USER.NICKNAME.containsIgnoreCase(keyword)
                .or(USER.EMAIL.containsIgnoreCase(keyword)) 
            : DSL.noCondition());
    
    // 전체 개수 조회
    int totalCount = dsl.fetchCount(query);
    
    // 페이징 적용
    List<Record> records = query
        .orderBy(USER.CREATED_AT.desc())
        .limit(pageable.getPageSize())
        .offset((int) pageable.getOffset())
        .fetch();
    
    // 엔티티 변환
    List<User> users = records.stream()
        .map(record -> record.into(User.class))
        .toList();
    
    return new PageImpl<>(users, pageable, totalCount);
}
```

---

## 7. 실무 예제

### Repository 완전 구현
```java
@Repository
public class UserJooqRepository {
    
    private final DSLContext dsl;
    
    public UserJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }
    
    public Page<User> findAllByFilterOptions(
            String keyword, 
            Role role, 
            DevCourseTrack track,
            String batch,
            Pageable pageable) {
        
        // 기본 쿼리
        var query = dsl
            .select(USER.asterisk())
            .from(USER);
        
        // 동적 조건 추가
        List<Condition> conditions = new ArrayList<>();
        
        if (StringUtils.hasText(keyword)) {
            conditions.add(
                USER.NICKNAME.containsIgnoreCase(keyword)
                    .or(USER.EMAIL.containsIgnoreCase(keyword))
                    .or(USER.REAL_NAME.containsIgnoreCase(keyword))
            );
        }
        
        if (role != null) {
            conditions.add(USER.ROLE.eq(role));
        }
        
        if (track != null) {
            conditions.add(USER.DEVCOURSE_NAME.eq(track));
        }
        
        if (StringUtils.hasText(batch)) {
            conditions.add(USER.DEVCOURSE_BATCH.eq(batch));
        }
        
        // WHERE 절 적용
        var conditionQuery = conditions.isEmpty() 
            ? query.where(DSL.noCondition())
            : query.where(DSL.and(conditions));
        
        // 정렬
        var orderedQuery = conditionQuery.orderBy(USER.CREATED_AT.desc());
        
        // 총 개수
        int totalCount = dsl.fetchCount(orderedQuery);
        
        // 페이징 실행
        List<User> users = orderedQuery
            .limit(pageable.getPageSize())
            .offset((int) pageable.getOffset())
            .fetchInto(User.class);
        
        return new PageImpl<>(users, pageable, totalCount);
    }
    
    public List<User> findActiveUsersByTrack(DevCourseTrack track) {
        return dsl.selectFrom(USER)
            .where(USER.DEVCOURSE_NAME.eq(track))
            .and(USER.VERIFICATION_STATUS.eq(VerificationStatus.VERIFIED))
            .orderBy(USER.NICKNAME.asc())
            .fetchInto(User.class);
    }
    
    public Optional<User> findByEmail(String email) {
        return dsl.selectFrom(USER)
            .where(USER.EMAIL.eq(email))
            .fetchOptionalInto(User.class);
    }
    
    public boolean existsByNickname(String nickname) {
        return dsl.fetchExists(
            dsl.selectFrom(USER).where(USER.NICKNAME.eq(nickname))
        );
    }
    
    public int countByTrackAndBatch(DevCourseTrack track, String batch) {
        return dsl.fetchCount(
            dsl.selectFrom(USER)
                .where(USER.DEVCOURSE_NAME.eq(track))
                .and(USER.DEVCOURSE_BATCH.eq(batch))
        );
    }
}
```

---

## 8. 유용한 메서드

### 집계 함수
```java
// COUNT
dsl.select(DSL.count()).from(USER).fetchOne(0, int.class);

// 트랙별 사용자 수
dsl.select(USER.DEVCOURSE_NAME, DSL.count())
   .from(USER)
   .groupBy(USER.DEVCOURSE_NAME)
   .having(DSL.count().gt(10));

// 배치별 통계
dsl.select(
    USER.DEVCOURSE_BATCH,
    DSL.count().as("total_users"),
    DSL.countDistinct(USER.DEVCOURSE_NAME).as("track_count")
).from(USER)
.groupBy(USER.DEVCOURSE_BATCH);
```

### 서브쿼리
```java
// 게시글이 있는 사용자만
dsl.selectFrom(USER)
   .where(DSL.exists(
       dsl.selectOne().from(POST).where(POST.USER_ID.eq(USER.ID))
   ));

// 특정 조건의 사용자들
dsl.selectFrom(USER)
   .where(USER.ID.in(
       dsl.select(POST.USER_ID)
          .from(POST)
          .where(POST.LIKE_COUNT.gt(100))
   ));
```

### CASE WHEN
```java
dsl.select(
    USER.NICKNAME,
    DSL.case_(USER.VERIFICATION_STATUS)
       .when(VerificationStatus.VERIFIED, "인증완료")
       .when(VerificationStatus.PENDING, "인증대기")
       .otherwise("미인증")
       .as("verification_status_kor")
).from(USER);
```

### 날짜 함수
```java
// 오늘 가입한 사용자
dsl.selectFrom(USER)
   .where(USER.CREATED_AT.ge(LocalDate.now()));

// 최근 30일 활성 사용자
dsl.selectFrom(USER)
   .where(USER.LAST_LOGIN_AT.ge(LocalDateTime.now().minusDays(30)));

// 월별 가입자 수
dsl.select(
    DSL.extract(USER.CREATED_AT, DatePart.YEAR).as("year"),
    DSL.extract(USER.CREATED_AT, DatePart.MONTH).as("month"),
    DSL.count().as("signup_count")
)
.from(USER)
.groupBy(
    DSL.extract(USER.CREATED_AT, DatePart.YEAR),
    DSL.extract(USER.CREATED_AT, DatePart.MONTH)
)
.orderBy(1, 2);
```

### 유용한 조건문
```java
// NULL 체크
.where(USER.REAL_NAME.isNotNull())
.where(USER.LAST_LOGIN_AT.isNull())

// 범위 조건
.where(USER.AGE.between(20, 30))
.where(USER.CREATED_AT.between(startDate, endDate))

// 문자열 조건
.where(USER.NICKNAME.startsWith("admin"))
.where(USER.EMAIL.endsWith("@gmail.com"))
.where(USER.NICKNAME.contains("dev"))
.where(USER.REAL_NAME.containsIgnoreCase("김"))

// 컬렉션 조건
.where(USER.ROLE.in(Arrays.asList(Role.ADMIN, Role.USER)))
.where(USER.VERIFICATION_STATUS.notIn(
    VerificationStatus.BLOCKED, 
    VerificationStatus.DELETED
))

// 이메일 도메인 검색
.where(USER.EMAIL.like("%@programmers.co.kr"))

// 닉네임 길이 조건
.where(DSL.length(USER.NICKNAME).between(3, 20))
```

### 업데이트/삭제
```java
// 사용자 정보 업데이트
dsl.update(USER)
   .set(USER.LAST_LOGIN_AT, LocalDateTime.now())
   .set(USER.LOGIN_COUNT, USER.LOGIN_COUNT.plus(1))
   .where(USER.ID.eq(userId))
   .execute();

// 인증 상태 변경
dsl.update(USER)
   .set(USER.VERIFICATION_STATUS, VerificationStatus.VERIFIED)
   .where(USER.EMAIL.eq(email))
   .execute();

// 비활성 사용자 삭제 (30일 이상 미접속)
dsl.deleteFrom(USER)
   .where(USER.LAST_LOGIN_AT.lt(LocalDateTime.now().minusDays(30)))
   .and(USER.ROLE.eq(Role.USER))
   .execute();
```

---

## 💡 팁

### 1. 성능 최적화
- `fetchSize()` 설정으로 메모리 사용량 조절
- 필요한 컬럼만 SELECT
- INDEX가 있는 컬럼으로 WHERE 조건 작성
- 이메일이나 닉네임 검색 시 UNIQUE 인덱스 활용

### 2. 디버깅
```java
// SQL 확인
String sql = dsl.selectFrom(USER).getSQL();
System.out.println("Generated SQL: " + sql);

// 파라미터 포함 SQL 확인
String sqlWithParams = dsl.selectFrom(USER)
    .where(USER.NICKNAME.eq("john"))
    .getSQL(ParamType.INLINED);
```

### 3. 트랜잭션
```java
// jOOQ 트랜잭션
dsl.transaction(configuration -> {
    DSLContext tx = DSL.using(configuration);
    tx.insertInto(USER).values(...).execute();
    tx.update(USER).set(...).execute();
});

// Spring 트랜잭션과 함께 사용 시 그냥 @Transactional 사용
```

이 가이드로 jOOQ를 실무에서 바로 활용할 수 있습니다! 🚀