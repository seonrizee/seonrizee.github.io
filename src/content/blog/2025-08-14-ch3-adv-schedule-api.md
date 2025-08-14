---
title: "일정관리 앱 Develop 프로젝트 회고"
summary: ""
date: "August 14 2025"
draft: false
tags:
- 회고
- 프로젝트
---

# 1. 고민한 내용

### **여러 곳에서 의존하는 `findByIdOrElseThrow()` 의 위치**

여러 도메인의 `Service`에서 사용되는 특정 도메인의 기본 조회 및 `Null`에 대한 `exception`을 합친 메서드인 `findByIdOrElseThrow`함수의 위치에 관한 문제에 대해 답을 명확히 내리지 못했었습니다.

같은 클래스내에서 반복 사용이 일어난다면 Service에서 private 메소드로 작성하면 되겠지만, 여러 도메인에서 필요한 상황에서는 적용할 수 없습니다. 주말 내내 고민했지만, 어떤 방식이 확실히 맞는 방향인지 답을 내리지 못하고 일단 `default`메서드를 적용하기로 했습니다. 

1. **해당 `Service`에 `public`으로 정의하고 다른 Service에서 메소드를 참조**
    
    순환 참조가 나타날 수 있습니다.
    
2. **`repository`를 의존하여 `findById()`를 가져온 뒤, `OrElse`를 통한 `throw`하는 부분을 반복 사용**
    
    필요한 곳마다 반복해서 `findById()`부터 `orElse`를 통한 `throw`까지 반복하여 작성하는 방법입니다. 제일 편리하지만 중복된 코드가 여러 곳에 나타날 수 있으며, `Exception`을 `throw`하는 부분의 변화가 일어난다면 유지보수가 번거로울 수 있습니다.
    
3. **Respository Interface에 default 메서드로 작성**
    
    원래 `default`메서드의 도입 취지를 고려하면, 옳은 방법은 아니라고 생각합니다. `default`메서드는 하위 호환을 위해 제공된 기능이며, 비즈니스 로직을 구현체가 아닌 인터페이스에 담기 위한 목적으로 생긴 것이 아니기 때문입니다.
    
    그리고 비즈니스 로직에 의해 커스텀으로 정의한 `Exception`을 `Repository`에서 `throw`하는 게 맞는지도 애매합니다. 그러나, 별도의 패키지나 클래스 생성 없이 다른 `Service`들에서 쉽게 의존할 수 있기 때문에 구현상으로는 제일 간편합니다.
    
4. **별도의 조회 전용 QueryService 클래스를 만들어 작성**
    
    서비스를 분리하여 Query 역할을 하는 클래스와 Command 역할을 하는 클래스로 나누는 방법입니다. 역할이 명확해지며, `@Transactional`도 `Class` 단위로 도입가능하기 편리합니다. 다만, 현재는 프로젝트의 규모가 작은 만큼 하나의 메소드를 위해 클래스를 만드는 경우도 생길 수 있습니다. 
    
    이런 역할을 하는 Class들을 묶으면 **Facade** 패턴과 유사해지는 느낌도 있습니다. 물론 도입 목적은 다릅니다.
    

```java
public interface UserRepository extends JpaRepository<User, Long> {

  default User findByIdOrThrow(Long userId) {
      return findById(userId)
              .orElseThrow(() -> new CustomBusinessException(ErrorCode.USER_NOT_FOUND));
  }
  
  ...
```

3번을 적용했다가, 프로젝트 마지막에 4번을 적용했습니다. 단순 조회를 넘어 복잡한 쿼리나 통계까지 책임질 수 있도록, CQRS 패턴의 의도를 담은 `~QueryService`로 클래스들을 리팩토링했습니다. 기능과 그에 따른 `Service` 그리고 `Respository`가 많아질수록 순환 참조를 방지하는 것도 중요하다고 생각했습니다. 따라서, 메소드들을 명확하게 역할이 분리된 서비스로 구성하는 것이 중요한데 `Query`전용으로 분리하는 것이 이것의 출발이라고 생각하여 최종적으로 4번을 도입했습니다.

### 댓글 관련 기능의 API를 RESTful하게 설계하기

**고민 내용**: 댓글의 단건 조회/수정/삭제 API URL을 `/schedules/{scheduleId}/comments/{commentId}`로 해야 할지, `/comments/{commentId}`로 해야 할지 고민했습니다.

**해결 방향**: 댓글은 그 자체로 고유 ID를 가진 독립적인 리소스라는 점에 집중했습니다. **Comment Entity와 comment 데이터베이스 테이블에서 표현하는 댓글의 id는 일정과는 관련없는 고유 ID**를 나타내기 때문입니다.

따라서 특정 댓글 하나를 다루는 행위는 `/comments/{commentId}`로 설계하는 것이 더 RESTful하다고 결론 내렸습니다. 반면, 댓글 생성이나 목록 조회는 특정 일정에 종속되므로 `/schedules/{scheduleId}/comments`를 사용했습니다.

이렇게 하면 해당 일정이나, 다른 일정에서 댓글 내용을 언급한다든지, 비즈니스적으로도 확장하기에 편리할 것 이라고 생각했습니다. 

대신, 댓글 수정, 삭제에 대한 검증은 서비스 계층에서 사용자가 작성한 댓글인지 검증하는 로직을 추가했습니다.

- **댓글 생성:** `POST /schedules/{scheduleId}/comments`
- **특정 일정의 댓글 목록 조회**: `GET /schedules/{scheduleId}/comments`
- **댓글 단건 조회: `GET** /comments/{commentId}`
- **댓글 수정:** `PATCH /comments/{commentId}`,
- **댓글 삭제**: `DELETE /comments/{commentId}`

### 반복되는 권한 검사를 없애보기

**고민 내용**: “내가 쓴 일정과 댓글만 수정/삭제할 수 있다"는 권한 검사 로직이 여러 서비스의 여러 메서드에 반복적으로 필요했습니다. 따라서, 이 작업을 어떻게 추상화하면 좋을지 고민했습니다.

**해결 방향**: 엔티티마다 validateOwner라는 메서드에 로직을 작성하는 방법을 처음 생각했습니다. 그런데 검증하는 로직은 모든 엔티티에 반복적으로 작성이 되어야 했습니다. 그래서 **검증하는 로직 자체를 추상화할 수 있는 다른 방법을 생각**해보았습니다. 

별도의 유틸 클래스로 만들어 static 메서드로 사용하는 방법도 있었지만, DI를 사용하지 않아 테스트 하기에 애매했습니다. 추상 클래스는 다른 클래스를 상속 받아야 하는 경우가 생기면 사용할 수가 없었습니다. 

그러다가 인터페이스를 사용한 방법을 떠올렸습니다. **`AuthorizationService`** 라는 권한과 관련된 클래스를 만들어 권한 검사 책임을 위임했습니다. 이 과정에서 `User` 를 반환하는 `getter` 메소드를 가진 `Ownable` 인터페이스를 만들었습니다. 어떤 엔티티든 소유자를 확인해야 하면 `Ownable`인터페이스를 구현하도록 해서, 명확하게 작성자에 대한 권한 검사를 강제하도록 규약의 느낌으로 사용했습니다. 

```java
// Ownable.java
package io.github.seonrizee.scheduler.global.entity;

import io.github.seonrizee.scheduler.domain.user.entity.User;

public interface Ownable {
    User getUser();
}

// AuthorizationService.java
package io.github.seonrizee.scheduler.global.security.service;

@Service
public class AuthorizationService {

    public void validateOwnership(Ownable entity, User loginUser) {
        if (!entity.getUser().getId().equals(loginUser.getId())) {
            throw new CustomBusinessException(ErrorCode.NO_PERMISSION); // 권한 없음 예외
        }
    }
}
```

이렇게 해서 필요한 곳에서는 `AuthorizationService`를 주입받아서 `validateOwnership()`를 실행하는 방식으로 구현했습니다.

그러다가, 프로젝트 막바지에 유저를 삭제할 때도 본인 확인을 해야겠다는 생각으로, 리팩터링을 하는 중에 `User` 클래스의 객체를 검증해야 하는 로직이 필요했습니다. 이 때, `Ownable`의 목적이 엔티티가 `User`라는 객체를 가지고 있어서 이 권한을 사용할 수 있다는 증표같은 것이었는데, `User`엔티티로 생성된 `User`가 다시 `ownable`를 구현해야 한다는 게 논리적으로 맞지 않다고 생각했습니다.

따라서, `Ownable` 인터페이스를 삭제하고, `User`의 고유식별자를 가져올 수 있는 객체면 모두 사용할 수 있도록 제네릭과 함수형 인터페이스를 사용하여 메소드를 개선했습니다.

```java
// AuthService.java
public <T> void validateOwnership(T entity, User loginUser, ToLongFunction<T> ownerIdExtractor) {
    Long ownerId = ownerIdExtractor.applyAsLong(entity);
    if (!ownerId.equals(loginUser.getId())) {
        throw new CustomBusinessException(ErrorCode.NO_PERMISSION); // 권한 없음 예외
    }
}

```

이렇게 해서 반복되는 권한 로직 검사의 긴 추상화 과정을 마칠 수 있었습니다.

### 현재 사용자 정보를 얻기 위해 필요한 과정을 추상화해보기

**고민 내용**: 현재 로그인하고 있는 사용자 정보가 필요한 모든 컨트롤러 메서드에서 `HttpSession`을 통해 `userId`를 가져오고, 이 `userId`를  `userFinder.findUserOrThrow(userId)`를 호출하는 코드가 반복되었습니다. 그래서 이 과정도 공통되는 코드를 줄이기 위해 추상화하는 것이 좋다고 생각했습니다.

**해결 방향**: 어노테이션을 만들고, 어노테이션을 선언한 파라미터를 통해서 필요한 유저 정보를 받아오도록 하면, `Controller`의 메소드에서 `httpSession`을 통해 `userId`를 받아오는 것보다 편리할 것이라고 ㅅ생각했습니다. 그래서 `@LoginUser`라는 커스텀 어노테이션을 만들었습니다. 

```java
// LoginUser.java

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
```

그 다음은  선언한 어노테이션에 현재 로그인된 유저의 정보를 매핑하는 과정이 필요했는데 이 과정은 `HandlerMethodArgumentResolver`를 구현해야 했습니다.

`HandlerMethodArgumentResolver`는 컨트롤러 메서드에서 특정 조건에 맞는 파라미터가 있을 때 원하는 값을 바인딩해주는 인터페이스로 딱 원하는 역할을 하는 인터페이스였습니다. 그리고 인터페이스이므로 구현해야 하는 메소드가 있습니다.

- `supportsParameter`
    - 사용하고자 하는 어노테이션과 파라미터를 통해 얻고자 하는 타입을 정의하는 역할입니다. 여기서 정의한 어노테이션을 선언하고 해당 타입의 파라미터를 resolver가 지원합니다.
- `resolveArgument`
    - 실제로 바인딩을 할 객체를 반환하는 역할입니다. 따라서 어떤 객체를 반환할지에 대한 로직을 작성해야 합니다.

```java
// LoginUserIdArgumentResolver.java

@Component
@RequiredArgsConstructor
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final HttpSession httpSession;
    private final UserQueryService userQueryService;

    /**
     * 이 Resolver가 지원하는 파라미터 타입을 결정합니다.
     * @param parameter 검사할 메소드 파라미터
     * @return {@link LoginUser} 어노테이션이 있고, 타입이 {@link User}인 경우 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean hasType = User.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && hasType;
    }

    /**
     * 실제 파라미터를 해석하여 반환합니다.
     * @return 세션에서 조회한 사용자 ID를 기반으로 DB에서 찾은 {@link User} 객체
     * @throws CustomBusinessException 세션에 사용자 ID가 없는 경우 (상태 불일치)
     */
    @Override
    public User resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Object userId = httpSession.getAttribute("userId");
        if (Optional.ofNullable(userId).isEmpty()) {
            throw new CustomBusinessException(ErrorCode.INCONSISTENT_SESSION_STATE);
        }

        return userQueryService.findByIdOrThrow((Long) userId);
    }
}

```

여기서 유저에 대한 정보를 가지고 있는 `Session`에서 `userId`를 가져왔고, 이를 관련 조회 서비스의 메소드를 사용하여 `User`객체를 가져왔습니다. 그리고 이를 반환하도록 하여 `Resolver`를 완성했습니다.

정리하면 `Resolver`는 세션에서 `userId`를 꺼내 DB에서 최신 `User` 엔티티를 조회한 뒤, 컨트롤러 파라미터에 바로 주입해 줍니다. 이를 통해, 로그인한 유저 정보가 필요한 곳에는 반복적인 코드 없이 편리하게 유저 정보를 가져올 수 있게 되었고, 서비스에는 순수히 구현하고자 하는 비즈니스 로직 관련 코드에 더욱 집중할 수 있게 되었습니다.

### **JPA 연관 관계: `@ManyToOne`과 `@OneToMany`의 선택**

**고민 내용**: Spring Data JPA를 사용할 때 `@ManyToOne`만으로도 충분한 경우가 많다고 들었습니다. 하지만 `@OneToMany`나 양방향 관계를 반드시 사용해야 하는 경우는 언제인지 궁금했습니다. 특히, 일정에 달린 **댓글** 목록을 조회하는 경우 어떤 관계가 더 적합할지 고민되었습니다.

**해결 방향**:
단방향 `@ManyToOne`은 대부분의 경우 충분하지만, **부모 엔티티에서 자식 엔티티의 컬렉션을 직접 관리하거나 조회해야 할 때** `@OneToMany`를 사용한 양방향 매핑이 큰 도움이 되는 것을 알게 되었습니다.

- **단방향 `@ManyToOne`**: `Comment`가 `Schedule`을 참조하는 것만으로 충분할 때 사용합니다. 예를 들어, `Comment`를 저장하거나 조회할 때만 `Schedule` 정보가 필요할 경우입니다.
- **양방향 `@OneToMany`**: `Schedule` 엔티티에서 해당 스케줄에 달린 모든 `Comment` 목록을 직접 조회해야 할 때 사용할 수 있습니다.
    
    ```java
    // 양방향 관계로 설정된 Schedule 엔티티
    @Entity
    public class Schedule {
        // ...
        @OneToMany(mappedBy = "schedule", cascade = CascadeType.REMOVE, orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();
    
        // 연관관계 편의 메서드
        public void addComment(Comment comment) {
            comments.add(comment);
            comment.updateSchedule(this);
        }
    }
    ```
    
    이전에는 `scheduleId`를 통해 별도의 댓글을 가져오는 쿼리가 필요했고 `DTO`에서 두 데이터를 조합했는데, 양방향 연관관계를 이용하면 한 번의 쿼리로 댓글 모두를 가져오는 등 성능상 이점과 더불어 도메인 모델의 응집성을 높일 수 있습니다. 그리고 `cascade`, `orphanRemoval` 등의 옵션을 통해서 고아 객체 관리 등도 할 수 있어서 편리합니다.
    
    대신, 반드시 양방 관계의 일관성을 유지하기 위해 연관관계 편의 메서드를 통해 양쪽의 객체를 모두 업데이트하는 과정이 필요합니다.
    
    ```java
    // AS-IS
    Schedule schedule = findScheduleByIdOrThrow(scheduleId);
    CommentListResponse comments = commentFinder.getCommentsWithSchedule(scheduleId);
    return scheduleMapper.toDto(schedule, comments);
    
    // TO-BE
    Schedule schedule = scheduleRepository.findWithCommentsById(scheduleId)
    		        .orElseThrow(() -> new CustomBusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    return scheduleMapper.toDto(schedule);
    ```
    

### 드디어 만난 N+1 문제

**고민 내용**: 일정 목록 조회 기능에 페이징을 도입하면서 각 일정의 댓글 개수도 나타내야 했습니다. 이때 각 일정의 댓글 개수를 `schedule.getComments().size()`로 가져오니까, 각 일정 마다 댓글 목록을 가져오기 위해서 일정(N)만큼 추가 댓글에 대한 쿼리가 발생하는 `N+1` 문제가 발생했습니다.

**해결 방향**: 이 때 어차피 해결을 JPQL로 해야 한다면, **JPQL에서 DTO로 직접 조회**하는 방식에 도전해보기로 했습니다. `JOIN`과 `COUNT`를 사용하여 단 한 번의 쿼리로 일정 정보와 댓글 개수를 모두 가져오도록 쿼리를 최적화했습니다. 이 과정을 통해 쿼리 횟수를 `N+1`에서 단 `2`회(데이터 쿼리, 카운트 쿼리)로 줄일 수 있었습니다.

```java
// ScheduleRepository.java
@Query("""
	          SELECT new io.github.seonrizee.scheduler.dto.response.SchedulePageResponse(s.id,
                    s.summary,
                    s.description,
                    COUNT(c),
                    u.id,
                    u.username,
                    s.createdAt,
                    s.updatedAt
            )
            FROM Schedule s
            JOIN s.user u
            LEFT JOIN s.comments c
            GROUP BY s.id, u.id
            """)
    Page<SchedulePageResponse> findWithCommentCount(Pageable pageable);
```

### 자동 생성되는 카운트 쿼리 최적화

- **고민 내용**: 그런데 카운트 쿼리에 대해서 알게 되니 불안한 마음이 생겼습니다. 지금은 데이터가 적지만, 데이터가 많아질수록 페이징을 위해 전체 데이터 개수를 세는 `count` 쿼리 자체가 문제를 일으킬 수도 있다는 생각이 들었습니다.
    
    ```sql
    # 자동 생성된 카운트 쿼리
    Hibernate: 
        /* SELECT
            count(s) 
        FROM
            Schedule s 
        JOIN
            s.user u 
        LEFT JOIN
            s.comments c 
        GROUP BY
            s.id,
            u.id */ select
                count(s1_0.schedule_id) 
            from
                schedule s1_0 
            join
                user u1_0 
                    on u1_0.user_id=s1_0.user_id 
            left join
                comment c1_0 
                    on s1_0.schedule_id=c1_0.schedule_id 
            group by
                s1_0.schedule_id,
                u1_0.user_id
    ```
    
    왜냐하면, Spring Data JPA가 자동으로 생성하는 카운트 쿼리를 확인해보니 **데이터를 가져오는 원래 쿼리의 `JOIN`과 `WHERE` 절을 그대로 사용**하고 있었습니다. 단순히 행의 개수만 세면 되는데, 불필요하게 복잡한 `JOIN` 연산을 수행하므로 데이터가 많아질수록 더욱 느려질 것이 당연히 예상되었습니다.
    
- **해결 방향**: 제가 이런 걱정 할 것을 알기라도 하는 듯이, Spring Data JPA는 자동으로 생성되는 복잡한 카운트 쿼리 대신 임의로 카운트 쿼리를 작성할 수 있도록 도구를 제공하고 있었습니다.
    
    Spring Data JPA의 `@Query` 어노테이션에 **`countQuery` 속성**을 제공했습니다. 따라서 저는, `count`만을 위한 최적화된 쿼리를 별도로 작성했습니다. 이를 통해 불필요한 `JOIN`을 제거했습니다. 더 나아가, 무한 스크롤 UI를 위해 `Page` 대신 `Slice`를 사용하여 카운트 쿼리 자체를 생략하는 방법도 생각해 볼 수 있었습니다. 
    
    ```sql
    @Query(value = """
        SELECT s.id AS id,
                s.summary AS summary,
                s.description AS description,
                COUNT(c) AS commentCount,
                u.id AS userId,
                u.username AS username,
                s.createdAt AS createdAt,
                s.updatedAt AS updatedAt
        FROM Schedule s
        JOIN s.user u
        LEFT JOIN s.comments c
        GROUP BY s.id, u.id
        """,
        countQuery = "SELECT count(s) FROM Schedule s") <- 최적화한 카운트 쿼리
    Page<SchedulePageResponse> findWithCommentCount(Pageable pageable);
    ```
    

# 2. 느낀점 및 다음 계획

## 2.1 향후 개선 과제

## 2.2 다짐