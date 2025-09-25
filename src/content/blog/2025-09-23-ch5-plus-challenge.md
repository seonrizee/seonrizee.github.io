---
title: "JPA 심화 프로젝트 도전 기능 회고"
summary: ""
date: "September 24 2025"
draft: false
tags:
- 회고
- 프로젝트
---

<!-- TOC -->

- [1. 과제 해결](#1-과제-해결)
  - [10. **QueryDSL 을 사용하여 검색 기능 만들기**](#10-querydsl-을-사용하여-검색-기능-만들기)
  - [11. **Transaction 심화**](#11-transaction-심화)
  - [12. **AWS 활용**](#12-aws-활용)
  - [13. **대용량 데이터 처리**](#13-대용량-데이터-처리)
- [2. 느낀점 및 다음 계획](#2-느낀점-및-다음-계획)

<!-- /TOC -->

이번 과제는 JPA 심화라는 주제로, 동적 쿼리와 성능 최적화 그리고 배포와 대용량 처리까지 접근해볼 수 있는 과제였습니다. 도전 기능 이어서 작성하겠습니다.

# 1. 과제 해결

## 10. **QueryDSL 을 사용하여 검색 기능 만들기**

- 문제
    
    ### **10. QueryDSL 을 사용하여 검색 기능 만들기**
    
    <aside>
    👉 일정을 검색하는 기능을 만들고 싶어요!
    검색 기능의 성능 및 사용성을 높이기 위해 QueryDSL을 활용한 쿼리 최적화를 해보세요.
    ❗Projections를 활용해서 필요한 필드만 반환할 수 있도록 해주세요.❗
    
    </aside>
    
    - 새로운 API로 만들어주세요.
    - 검색 조건은 다음과 같아요.
        - 검색 키워드로 일정의 제목을 검색할 수 있어요.
            - 제목은 부분적으로 일치해도 검색이 가능해요.
        - 일정의 생성일 범위로 검색할 수 있어요.
            - 일정을 생성일 최신순으로 정렬해주세요.
        - 담당자의 닉네임으로도 검색이 가능해요.
            - 닉네임은 부분적으로 일치해도 검색이 가능해요.
    - 다음의 내용을 포함해서 검색 결과를 반환해주세요.
        - 일정에 대한 모든 정보가 아닌, 제목만 넣어주세요.
        - 해당 일정의 담당자 수를 넣어주세요.
        - 해당 일정의 총 댓글 개수를 넣어주세요.
    - 검색 결과는 페이징 처리되어 반환되도록 합니다.
- 해결 과정
    
    ### 1. DTO 추가
    
    먼저 검색 요청과 응답에 사용될 DTO를 만들었습니다.
    
    ```java
    package org.example.expert.domain.todo.dto.request;
    
    import java.time.LocalDate;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.format.annotation.DateTimeFormat.ISO;
    
    public record TodoSearchRequest(
            String title,
            @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
            @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
            String nickname
    ) {
    }
    
    ```
    
    ```java
    package org.example.expert.domain.todo.dto.response;
    
    public record TodoSearchResponse(
            String title,
            int managerCount,
            int commentCount) {
    
        public TodoSearchResponse(String title, int managerCount, int commentCount) {
            this.title = title;
            this.managerCount = managerCount;
            this.commentCount = commentCount;
        }
    }
    
    ```
    
    ### 2. Projection에 사용될 DTO 추가
    
    그리고 문제에서 `Projection`을 활용하라는 조건이 있었습니다. 리포지토리의 메서드가 반환하는 쿼리 결과를 바로 바인딩할 Projection 용도의 DTO를 만들었습니다. 
    
    사실 Prjoection에 사용될 DTO의 값이나, 앞에서 만든 Response 용도의 DTO나 필드의 차이가 없습니다. 그래서 Projection DTO를 Response 용도로도 사용할까 고민했습니다. 그러나, 이후에 DB 쿼리 결과가 변경이 되면 API 응답까지 영향을 미치게 되므로, 미리 분리를 해놓는 것이 좋겠다는 생각을 하여 앞에서 Response용 DTO도 만들었습니다.
    
    그리고 사실, 쿼리 결과가 API의 응답으로만 쓰이는 것은 아니고 배치나 캐시 데이터 생성 혹은 다른 내부 로직에서도 사용될 수 있기 때문에 엔티티를 Response로 그대로 전달하지 않는 것 처럼, Projection 용도의 DTO도 API 응답을 위한 DTO와는 분리하는 것이 맞다는 생각도 들었습니다.
    
    ```java
    package org.example.expert.domain.todo.dto;
    
    import com.querydsl.core.annotations.QueryProjection;
    
    public record TodoSearchProjection(
            String title,
            int managerCount,
            int commentCount) {
    
        @QueryProjection
        public TodoSearchProjection {
        }
    }
    
    ```
    
    그리고 잘 보면 기본 생성자에 `@QueryProjection`이라는 어노테이션이 붙어있는 것을 확인할 수 있습니다. queryDSL 에서는 DTO를 반환하는 여러가지 방법을 지원하는데, 크게 4가지 정도로 정리할 수 있습니다.
    
    | 방식 | 장점 | 단점 | 핵심 |
    | --- | --- | --- | --- |
    | 1. Projections.bean | 사용이 간단함 | DTO에 Setter와 기본 생성자가 반드시 필요 (객체 불변성 깨짐) | 가변(Mutable) DTO 필요 |
    | 2. Projections.fields | Setter가 필요 없어 외부에선 불변처럼 보임 | DTO에 기본 생성자 필요, Reflection으로 필드에 직접 주입 | 불완전한 불변성 |
    | 3. Projections.constructor | 완벽한 불변(Immutable) 객체 생성 가능. DTO가 Querydsl에 의존하지 않음. | 파라미터 순서나 타입이 다르면 런타임 시점에 에러 발생 | 아키텍처 순수성 |
    | 4. @QueryProjection | 완벽한 불변 객체 생성 가능. 파라미터 오류를 컴파일 시점에 잡을 수 있음. | DTO가 Querydsl에 의존하게 됨. Q파일 생성 필요. | 타입 안정성 |
    
    일단, 3번과 4번이 생성자를 이용하는 방식이기 때문에 객체의 불변성을 보장합니다. DTO는 한 번 데이터가 바인딩되면 바뀔 일이 없기 때문에 불변 객체로 만드는 것이 데이터를 다루기에 오히려 안전하다고 할 수 있습니다. 최근에 `record` 타입으로 DTO를 만드는 것도 같은 맥락이라고 생각합니다.
    
    그럼 3번과 4번은 무슨 차이냐면, 4번은 엔티티를 Q-타입으로 생성하여 컴파일 시점에 타입 안정성을 체크했던 것처럼 DTO에도 동일한 효과를 줄 수 있습니다. 즉, `queryDSL`로 DTO 생성자의 파라미터 타입과 순서같은 것을 컴파일 타임에 확인할 수 있습니다. 따라서, 타입안정성을 최우선으로 생각하여 런타임 에러를 컴파일 타임에 처리하고 싶다면 4번을 선택하면 됩니다. 
    
    대신 DTO도 `queryDSL`에 의존하게 됩니다. 사실 테스트 코드만으로도 파라미터 타입이나 순서 오류에 대해 방지할 수 있다면, 굳이 DTO까지 Q-타입을 이용할 필요는 없을 것 같기도 합니다.
    
    ### 3. QueryDSL을 이용한 로직 작성
    
    이전에 8번 과제에서 QueryDSL을 사용하면서 이미 Custom Repository Interface와 구현체를 만들어두었습니다. 
    
    ```java
    package org.example.expert.domain.todo.repository;
    
    import java.time.LocalDate;
    import java.util.Optional;
    import org.example.expert.domain.todo.dto.TodoSearchProjection;
    import org.example.expert.domain.todo.entity.Todo;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    
    public interface TodoRepositoryCustom {
    
        Optional<Todo> findByIdWithUser(Long todoId);
    
        Page<TodoSearchProjection> findAllBySearch(String titleKeyword,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   String nicknameKeyword,
                                                   Pageable pageable);
    }
    
    ```
    
    따라서 이번에는 `Custom Repository`에 정의를 하고, 만들어 놓은 구현체에 검색 기능에 대한 로직을 작성했습니다. 먼저 `findAllBySearch`라는 메서드를 custom interface repository에 정의했습니다. 문제의 요구사항에서 페이징도 요구했기 때문에 `Pageable` 객체도 함께 인자로 주었습니다.
    
    ```java
    package org.example.expert.domain.todo.repository;
    
    ...
    
    @RequiredArgsConstructor
    public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {
    
        private final JPAQueryFactory queryFactory;
    
    		...
    		
        @Override
        public Page<TodoSearchProjection> findAllBySearch(String titleKeyword,
                                                          LocalDate startDate,
                                                          LocalDate endDate,
                                                          String nicknameKeyword,
                                                          Pageable pageable) {
    
            JPAQuery<TodoSearchProjection> query = queryFactory
                    .select(new QTodoSearchProjection(
                            todo.title,
                            todo.managers.size().intValue(),
                            todo.comments.size().intValue()
                    ))
                    .from(todo);
    
            applyJoinsForNickname(query, nicknameKeyword);
    
            List<TodoSearchProjection> content = query
                    .where(
                            titleCIC(titleKeyword),
                            startDateGoe(startDate),
                            endDateLoe(endDate),
                            nicknameCic(nicknameKeyword)
                    )
                    .orderBy(todo.createdAt.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
    
            JPAQuery<Long> countQuery = queryFactory
                    .select(todo.id.countDistinct())
                    .from(todo);
    
            applyJoinsForNickname(countQuery, nicknameKeyword);
    
            countQuery.where(
                    titleCIC(titleKeyword),
                    startDateGoe(startDate),
                    endDateLoe(endDate),
                    nicknameCic(nicknameKeyword));
    
            return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }
    
        private BooleanExpression titleCIC(String title) {
            return hasText(title) ? todo.title.containsIgnoreCase(title) : null;
        }
    
        private BooleanExpression startDateGoe(LocalDate startDate) {
            return startDate != null ? todo.createdAt.goe(startDate.atStartOfDay()) : null;
        }
    
        private BooleanExpression endDateLoe(LocalDate endDate) {
            return endDate != null ? todo.createdAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
        }
    
        private BooleanExpression nicknameCic(String nickname) {
            return hasText(nickname) ? todo.managers.any().user.nickname.containsIgnoreCase(nickname) : null;
        }
    
        private <T> JPAQuery<T> applyJoinsForNickname(JPAQuery<T> query, String nickname) {
            if (hasText(nickname)) {
                query.leftJoin(todo.managers, QManager.manager)
                        .leftJoin(QManager.manager.user, QUser.user);
            }
            return query;
        }
    }
    
    ```
    
    `BooleanExpression` ****을 이용하여 제목과 nickname, 그리고 날짜 범위에 대한 필터링 조건을 작성했습니다. 그리고 `applyJoinsForNickname` 메서드를 통해서, nickname에 해당하는 검색 조건이 있을 때에만 필요한 left join을 추가하도록 일종의 동적 조인이 이뤄지도록 작성했습니다. 이렇게 하면, 본 쿼리와 카운트 쿼리에도 모두 메서드로 쉽게 재사용할 수 있었습니다.
    
    또한 `todo.managers`와 `todo.comments`는 size()로 컬렉션 크기를 간단하게 가져오고 있는데, 이게 결국 변환되는 SQL 쿼리를 확인했을 때, 아래처럼 변환되고 있었습니다.
    
    ```java
    select
         t1_0.title,
         (select
             count(1) 
         from
             managers m1_0 
         where
             t1_0.id=m1_0.todo_id),
         (select
             count(1) 
         from
             comments c1_0 
         where
             t1_0.id=c1_0.todo_id)
    ```
    
    즉, 알아서 메인 쿼리의 각 행에 대해 실행되어 단 하나의 값 (하나의 행, 하나의 열)을 반환하는 스칼라 서브쿼리로 변환되고 있었습니다. 스칼라 서브쿼리는 여러 개의 컬렉션(`managers`, `comments`)을 동시에 카운트할 때 `groupBy`를 사용할 때 발생할 수 있는 **카티전 곱 문제를 원천적으로 방지**할 수 있는데 알아서 잘 변환하고 있으므로, 굳이 `JPAExpressions`을 이용해 별도로 쿼리를 만들어주지 않았습니다.
    
    ### 4. Service 로직과 엔드포인트 구현
    
    ```java
    package org.example.expert.domain.todo.service;
    
    ...
    
    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class TodoService {
    
        private final TodoRepository todoRepository;
        private final WeatherClient weatherClient;
    
    	  ...
    
        public Page<TodoSearchResponse> getTodosBySearch(TodoSearchRequest todoSearchRequest, Pageable pageable) {
    
            return todoRepository.findAllBySearch(
                            todoSearchRequest.title(),
                            todoSearchRequest.startDate(),
                            todoSearchRequest.endDate(),
                            todoSearchRequest.nickname(), pageable)
                    .map(item -> new TodoSearchResponse(
                            item.title(),
                            item.managerCount(),
                            item.commentCount())
                    );
        }
    }
    
    ```
    
    그리고 서비스 레이어에서는 전달받은 Projction DTO 들을 Response DTO로 변환하여 반환했습니다.
    
    ```java
    package org.example.expert.domain.todo.controller;
    
    ...
    
    @RestController
    @RequiredArgsConstructor
    public class TodoController {
    
        private final TodoService todoService;
    
    	  ...
    
        @GetMapping("/todos/search")
        public ResponseEntity<Page<TodoSearchResponse>> getTodo(
                @ModelAttribute TodoSearchRequest todoSearchRequest,
                @PageableDefault(size = 10) Pageable pageable
        ) {
            return ResponseEntity.ok(todoService.getTodosBySearch(todoSearchRequest, pageable));
        }
    }
    
    ```
    
    마지막으로 컨트롤러에 최종적으로 엔드포인트를 구현하여 기능 개발을 완료했습니다.
    

## 11. **Transaction 심화**

- 문제
    
    ### **11. Transaction 심화**
    
    <aside>
    👉 매니저 등록 요청 시 로그를 남기고 싶어요!
    `@Transactional`의 옵션 중 하나를 활용하여 매니저 등록과 로그 기록이 각각 독립적으로 처리될 수 있도록 해봅시다.
    
    </aside>
    
    - 매니저 등록 요청을 기록하는 로그 테이블을 만들어주세요.
        - DB 테이블명: `log`
    - 매니저 등록과는 별개로 로그 테이블에는 항상 요청 로그가 남아야 해요.
        - 매니저 등록은 실패할 수 있지만, 로그는 반드시 저장되어야 합니다.
        - 로그 생성 시간은 반드시 필요합니다.
        - 그 외 로그에 들어가는 내용은 원하는 정보를 자유롭게 넣어주세요.
- 해결 과정
    
    과정을 크게 4단계로 구분했습니다. 일단 AOP에 필요한 의존성을 등록하고, Log 라는 엔티티와 필요한 repository 만듭니다. 그리고 제일 중요한 조건인 “**매니저 등록은 실패할 수 있지만, 로그는 반드시 저장되어야 합니다.”**을 고려하여 서비스 레벨의 로직을 작성합니다. 마지막으로, AOP를 통해 로그를 남깁니다. 
    
    ### 1. Log 엔티티 및 LogRepository 추가
    
    ```java
    package org.example.expert.domain.log.entity;
    
    ...
    
    @Entity
    @Table(name = "logs")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Log extends Timestamped {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 50)
        private OperationType operation;
    
        @Column
        private Long actorUserId;
    
        @Column(length = 500)
        private String requestUri;
    
        @Lob
        private String requestBody;
    
        @Column
        private Long targetTodoId;
    
        @Column
        private Long targetUserId;
    
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private ResultType result;
    
        @Column(length = 200)
        private String failureException;
    
        public static Log create(OperationType operation,
                                 Long actorUserId,
                                 String requestUri,
                                 String requestBody,
                                 Long targetTodoId,
                                 Long targetUserId,
                                 ResultType result,
                                 String failureException) {
            return new Log(null, operation, actorUserId, requestUri, requestBody, targetTodoId, targetUserId, result,
                    failureException);
        }
    }
    ```
    
    보통 애플리케이션은 로거에 출력되는 메시지를 별도의 파일을 통해 기록을 하고, 일정 기간 동안 보관합니다. 이렇게 로그를 남기는데 불구하고 로그를 위한 테이블을 별도로 만드는 것은 재사용하거나 다른 용도로 사용을 하기 위해서라고 생각합니다. 물론 이번에는 과제이기 때문에 다른 의도도 있을 것입니다.
    
    그래도 DB에 기록하는 데이터이니 만큼, 어느 정도 확장을 고려하여 필드를 구성했습니다. 그래서 엔드포인트로 요청된 내용과 그에 따라 서버에서 실행되는 메서드를 기록하는 것이 좋겠다고 생각했습니다. 그리고 메서드가 실패해도 로그가 기록되어야 하기 때문에, 메서드의 exception ``발생 여부와 exception이 발생한 경우 exception도 기록을 하도록 구성했습니다.
    
    ```java
    package org.example.expert.domain.log.repository;
    
    import org.example.expert.domain.log.entity.Log;
    import org.springframework.data.jpa.repository.JpaRepository;
    
    public interface LogRepository extends JpaRepository<Log, Long> {
    }
    ```
    
    Repository는 `JpaRepository`를 `extends`하는 인터페이스 리포지토리를 만들었습니다.
    
    ### 2. Log 로직 구현
    
    ```java
    package org.example.expert.domain.log.service;
    
    ...
    
    @Service
    @RequiredArgsConstructor
    public class LogService {
    
        private final LogRepository logRepository;
    
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void save(OperationType operation,
                         Long actorUserId,
                         String requestUri,
                         String requestBody,
                         Long targetTodoId,
                         Long targetUserId,
                         ResultType result,
                         String failureException) {
            Log log = Log.create(
                    operation,
                    actorUserId,
                    requestUri,
                    requestBody,
                    targetTodoId,
                    targetUserId,
                    result,
                    failureException
            );
            logRepository.save(log);
        }
    }
    ```
    
    로직은 단순 `save`이기 때문에 복잡할 것이 없습니다. 중요한 것은 “**매니저 등록은 실패할 수 있지만, 로그는 반드시 저장되어야 합니다.”**라는 조건입니다. 따라서 이 조건을 만족시키기 위해 Propagation Level을 `REQUIRES_NEW`로 설정해주었습니다. 
    
    트랜잭션에서 Propagation Level은 하나의 트랜잭션이 존재하는 상황에서 또 다른 트랜잭션이 시작되었을 때 이를 어떻게 처리할지를 정의하는 것입니다.
    
    | **종류** | **기존 트랜잭션 X** | **기존 트랜잭션 O** | 적용 사례 |
    | --- | --- | --- | --- |
    | **REQUIRED** | 새 트랜잭션 생성 | 기존 트랜잭션에 참여 → 흡수 | **기본값. 대부분의 비지니스 로직** |
    | **REQUIRES_NEW** | 새 트랜잭션 생성 | 기존 트랜잭션 일시중단, 새로운 트랜잭션 생성 | **독립적인 작업 처리** |
    | **SUPPORTS** | 트랜잭션 없이 진행 | 기존 트랜잭션 참여 | 트랜잭션이 필수가 아닌 작업 |
    | **NOT_SUPPORTED** | 트랜잭션 없이 진행 | 기존 트랜잭션 일시중단, 트랜잭션 없이 진행 | 로그 저장 등 트랜잭션과 독집적인 작업 |
    | **MANDATORY** | IllegalTransactionStateException 발생 | 기존 트랜잭션 참여 | 트랜잭션 내부에서만 호출 가능한 메소드 |
    | **NEVER** | 트랜잭션 없이 진행 | IllegalTransactionStateException 발생 | 외부 시스템 호출시 |
    | **NESTED** | 새 트랜잭션 생성 | 중첩 트랜잭션 생성 | 부분적으로 롤백 가능한 작업 |
    
    로그 기록은 로깅의 대상이 되는 메소드와 독립적으로 수행이 되어야 하므로, `REQUIRES_NEW`를 사용했습니다. 만약 기본값인 `REQUIRED`로 설정을 한다면, 기존 트랜잭션에 포함되기 때문에, 메서드에서 예외가 발생하면 트랜잭션 전체가 롤백되므로, 로그 기록도 취소되게 됩니다.
    
    ## 3. AOP 구현
    
    ```java
    package org.example.expert.aop;
    
    ...
    
    @Slf4j
    @Aspect
    @Component
    @RequiredArgsConstructor
    public class ManagerRegisterLoggingAspect {
    
        private final LogService logService;
        private final ObjectMapper objectMapper;
    
        @Pointcut("execution(* org.example.expert.domain.manager.service.ManagerService.saveManager(..))")
        private void saveManagerPointcut() {
        }
    
        @Around(value = "saveManagerPointcut() && args(authUser, todoId, managerSaveRequest)",
                argNames = "pjp,authUser,todoId,managerSaveRequest")
        public Object logManagerRegister(ProceedingJoinPoint pjp,
                                         AuthUser authUser,
                                         long todoId,
                                         ManagerSaveRequest managerSaveRequest) throws Throwable {
    
            String uri = null;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                if (request != null && request.getRequestURI() != null) {
                    uri = request.getRequestURI();
                }
            }
    
            String bodyJson = saveJson(managerSaveRequest);
            Long actorUserId = authUser != null ? authUser.id() : null;
            Long targetUserId = managerSaveRequest != null ? managerSaveRequest.getManagerUserId() : null;
    
            try {
                Object result = pjp.proceed();
                callSaveLog(OperationType.MANAGER_REGISTER, actorUserId, uri, bodyJson, todoId, targetUserId, null);
                return result;
            } catch (Throwable t) {
                callSaveLog(OperationType.MANAGER_REGISTER, actorUserId, uri, bodyJson, todoId, targetUserId, t);
                throw t;
            }
        }
    
        private String saveJson(Object obj) {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                return String.valueOf(obj);
            }
        }
    
        private void callSaveLog(OperationType operationType, Long actorUserId, String uri, String bodyJson, Long todoId,
                                 Long targetUserId, Throwable t) {
            try {
                log.info(
                        "Admin Access Logging start: operationType={}, actorUserId={}, uri={}, body={}, todoId={}, targetUserId={}, result={}, error={}",
                        operationType,
                        actorUserId,
                        uri,
                        bodyJson,
                        todoId,
                        targetUserId,
                        t == null ? ResultType.SUCCESS : ResultType.FAILURE,
                        t != null ? t.getClass().getSimpleName() : null
                );
                logService.save(
                        operationType,
                        actorUserId,
                        uri,
                        bodyJson,
                        todoId,
                        targetUserId,
                        t == null ? ResultType.SUCCESS : ResultType.FAILURE,
                        t != null ? t.getClass().getSimpleName() : null
                );
            } catch (Exception e) {
                log.warn("Admin Access Logging failed: {}", e.getMessage());
            }
        }
    }
    
    ```
    
    다음으로 AOP를 구현했습니다. 로그 기록을 확장성을 고려하여 범용적인 컬럼들로 구성했습니다. 그래서 AOP도 어노테이션으로 구성할까 고민했는데, 이번에는 일단 과제에서 요구하는 매니저 등록에만 적용되도록 `Pointcut`을 메소드로 지정했습니다.
    
    또한, `request`에 대한 데이터도 필요하며, 메서드의 성공 여부도 확인해야 되기 때문에 `@Around`로 구현했습니다.
    
    내부에서 활용해야 하는 `argument`의 순서나 타입이 바뀔 수도 있으므로, 인덱스를 통해 접근하기 보다는 명시적으로 `args()`와 `argNames`을 사용하여 메서드 파라미터를 직접 바인딩했습니다. 그리고 `HttpServletRequest`를 직접 주입받기 보다는, `RequestContextHolder`를 활용했습니다.
    
    데이터는 `Request Body`도 기록을 하도록 구성했으므로 `saveJson`이라는 유틸성 함수를 만들어서 String으로 변환하는 처리를 했습니다. 
    
    이렇게 하면, `pointcut`으로 지정한 매니저 등록 메서드가 실행될 때마다, 메서드의 실행 결과와는 독립적으로 log 테이블에 로깅 데이터가 저장됩니다.
    

## 12. **AWS 활용**

작성 중…

## 13. **대용량 데이터 처리**

# 2. 느낀점 및 다음 계획