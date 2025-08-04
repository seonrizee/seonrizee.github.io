---
title: "일정관리 앱 프로젝트 회고"
summary: ""
date: "August 04 2025"
draft: false
tags:
- 회고
- 프로젝트
---

<!-- TOC -->

- [1. 고민한 내용](#1-고민한-내용)
  - [1.1. @RequestBody와 DTO의 Lombok Annotation](#11-requestbody와-dto의-lombok-annotation)
  - [1.2. Schedule 엔티티 save() 후에 테이블에 저장되면서 MySQL이 자동 생성한 ID 값을 모르는데 저장이 되었는지 어떻게 확인할까?](#12-schedule-엔티티-save-후에-테이블에-저장되면서-mysql이-자동-생성한-id-값을-모르는데-저장이-되었는지-어떻게-확인할까)
  - [1.3. ID를 이용한 findById() 조회 결과가 null일 때 어떻게 처리해야 하는지?](#13-id를-이용한-findbyid-조회-결과가-null일-때-어떻게-처리해야-하는지)
  - [1.4. 필드가 조금 다른 경우 요청에 대하여 Request DTO의 재사용](#14-필드가-조금-다른-경우-요청에-대하여-request-dto의-재사용)
  - [1.5. 데이터베이스에서 수정한 시간을 바로 반영하여 반환 하려면?](#15-데이터베이스에서-수정한-시간을-바로-반영하여-반환-하려면)
  - [1.6. Service에서 delete 후 컨트롤러로 어떻게 성공 여부를 알려줘야할까?](#16-service에서-delete-후-컨트롤러로-어떻게-성공-여부를-알려줘야할까)
  - [1.7. 서비스에서 다른 서비스의 메서드가 필요할 때 서비스에 의존하는 게 나은가? 아니면 리포지토리에 의존하는 게 나은가?](#17-서비스에서-다른-서비스의-메서드가-필요할-때-서비스에-의존하는-게-나은가-아니면-리포지토리에-의존하는-게-나은가)
  - [1.8. 서비스 간 순환 참조 문제](#18-서비스-간-순환-참조-문제)
  - [1.9. 엔티티와 DTO, 어떻게 분리해야 할까? 그리고 변환 로직은 어디에서 관리해야 할까?](#19-엔티티와-dto-어떻게-분리해야-할까-그리고-변환-로직은-어디에서-관리해야-할까)
  - [1.10. 반복되는 API 응답 생성에 공통 DTO와 정적 팩토리 메서드 도입하기](#110-반복되는-api-응답-생성에-공통-dto와-정적-팩토리-메서드-도입하기)
- [2. 느낀점 및 다음 계획](#2-느낀점-및-다음-계획)
  - [2.1. 향후 개선 과제](#21-향후-개선-과제)
  - [2.2. 다짐](#22-다짐)

<!-- /TOC -->

오늘은 일정관리 앱 프로젝트를 진행하며 겪었던 고민과 해결 과정 중 일부를 추려서 공유합니다. 이번 프로젝트부터 Spring 프레임워크를 사용하여 본격적인 API 서버 개발을 시작했습니다. 여기에 Spring Boot와 Spring Data JPA를 사용하여 편리하게 개발할 수 있었습니다. 프레임워크 사용법 뿐만 아니라 API 설계, 3 layerd Architecture 그리고 근간이 되는 객체지향에 대해 고민하면서 개발했습니다.

# 1. 고민한 내용

## 1.1. @RequestBody와 DTO의 Lombok Annotation

- **고민 내용**: `@RequestBody`로 JSON 데이터를 받으려면 DTO에 어떤 Lombok 어노테이션이 필요한지 조금 혼동되었습니다. 왜냐하면, 여러가지 어노테이션을 입력해도 모두 가능했기 때문입니다.
- **해결 방향**: 직접 어노테이션을 하나씩 작성했다가 지워가며 테스트 한 결과는 아래와 같았습니다. 눈에 보이지 않아도 알아서 생성해주는 기본 생성자가 존재함을 고려해야 했습니다.
    - **`@Getter`만 있으면 가능**
        
        → 눈에 보이지 않는 **기본 생성자** + **필드 직접 주입**(Reflection)
        
    - **`@Setter`만 있어도 가능**
        
        → 눈에 보이지 않는 **기본 생성자** + **Setter 메서드** 조합으로 동작합니다. (고전적인 방식)
        
    - **`@AllArgsConstructor`만 있어도 가능**
        
        → Jackson이 **모든 필드를 받는 생성자**를 찾아서 JSON의 필드 이름과 매칭해 생성자 파라미터에 값을 넣어 인스턴스 생성
        
    - **`@NoArgsConstructor`만 있으면 불가능**
        
        → 객체를 생성할 방법(**`@NoArgsConstructor`**)은 있지만, 그 안의 필드에 **값을 채워 넣을 방법**(Setter나 다른 생성자)이 없음. 빈 객체만 만들어지고 필드는 모두 `null`이 된다.
        
    
    결론은 `@Getter`, `@AllArgsConstructor`를 사용하기로 했습니다. 일단 `Setter` 만들지 않아 생성자로만 값을 할당할 수 있습니다. 따라서 캡슐화를 보장하면서 불변 객체를 만들 수 있습니다. 그리고 `Getter`메소드로 값을 읽는 역할을 하면 캡슐화를 최대한 지키면서도 객체를 만들고, 읽는 역할 모두 가능하다고 생각했습니다.
    

## 1.2. Schedule 엔티티 save() 후에 테이블에 저장되면서 MySQL이 자동 생성한 ID 값을 모르는데 저장이 되었는지 어떻게 확인할까?

- **고민 내용**: `save()`를 할 때 사용한 객체는 `id`가 `null`인데, 저장된 후의 ID는 어떻게 알 수 있는지 궁금했습니다.
- **해결 방향**: `@NoRepositoryBeanpublic interface CrudRepository<T, ID> extends Repository<T, ID>`에 정의되어 있는 `save()` 메서드를 확인해보니 JPA에서 제공하는 추상화된 `save()` 메서드는 DB에 데이터를 저장한 후, **DB가 생성한 ID를 포함해서 저장된 내용을 담고 있는 Entity 객체를 반환**합니다.
    
    ```java
    	/**
    	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
    	 * entity instance completely.
    	 *
    	 * @param entity must not be {@literal null}.
    	 * @return the saved entity; will never be {@literal null}.
    	 * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
    	 * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute with
    	 *           a different value from that found in the persistence store. Also thrown if the entity is assumed to be
    	 *           present but does not exist in the database.
    	 */
    	<S extends T> S save(S entity);
    ```
    
    따라서 `save()`가 끝난 후, 반환된 객체의 `getId()`를 호출하면 `Entity`에서 설정한 전략에 따라 자동으로 생성된 **Id값**을 정상적으로 확인할 수 있었습니다. 별도의 과정은 필요가 없었습니다. `save()` **메서드가 영속화된 엔티티를 반환한다는 JPA의 명세에 따라** 코드를 작성하는 올바른 접근법을 알게 되었습니다.
    

## 1.3. ID를 이용한 findById() 조회 결과가 null일 때 어떻게 처리해야 하는지?

- **고민 내용:** `findById`의 결과가 없을 때, 이것을 정상적인 **결과 없음**으로 보고 서비스에서 `null`을 반환해야 할지, 아니면 **'오류**'로 보고 **예외**를 던져야 할지 고민했습니다.
- **해결 방향: 클라이언트가 존재하지 않는 리소스를 요청한 것**은 **클라이언트의 요청 오류**로 보는 것이 RESTful API의 관점이라고 생각했습니다. 그리고 Schedule이 핵심 도메인이므로 존재하지 않는 경우를 커스텀 오류로 정의하면 사용자나 개발자 입장 모두 의미가 있는 예외가 될 것이라고 생각했습니다.
    
    따라서 커스텀 예외를 정의한 후에, `findById()` 메소드를 실행한 서비스 계층에서는 **예외를 발생**시키고, `@RestControllerAdvice`에 최종적인 HTTP 응답 처리를 위임하도록 했습니다. 최종적으로 클라이언트에게 **`404 Not Found`** 상태 코드를 반환하는 것이 옳은 방향이라고 생각했습니다.
    
    ### **1. 커스텀 예외 생성**
    
    ```java
    package io.github.seonrizee.scheduler.exception;
    
    /**
     * 요청한 ID에 해당하는 일정을 찾을 수 없을 때 발생하는 예외.
     */
    public class ScheduleNotFoundException extends RuntimeException {
    
        private static final String MESSAGE = "선택한 일정이 존재하지 않습니다.";
    
        public ScheduleNotFoundException() {
            super(MESSAGE);
        }
    }
    
    ```
    
    ### **2. Service에서** `orElseThrow()` **사용**
    
    ```java
        @Override
        @Transactional(readOnly = true)
        public ScheduleResponseDto findScheduleById(Long id) {
    
            Schedule foundSchedule = findScheduleByIdOrThrow(id);
    
            return scheduleMapper.toResponseDto(foundSchedule, commentService.findCommentsByScheduleId(id));
        }
        
        @Override
        @Transactional(readOnly = true)
        public Schedule findScheduleByIdOrThrow(Long id) {
            return scheduleRepository.findById(id)
                    .orElseThrow(ScheduleNotFoundException::new);
        }
    ```
    
    ### **3.** `@RestControllerAdvice`**에서 예외 처리**
    
    ```java
    @RestControllerAdvice
    public class GlobalExceptionHandler {
    
        /**
         * 요청한 리소스를 찾을 수 없을 때 발생하는 예외를 처리합니다.
         *
         * @param e 발생한 ScheduleNotFoundException
         * @return 404 Not Found 상태 코드와 에러 메시지를 담은 응답
         */
        @ExceptionHandler(ScheduleNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleScheduleNotFoundException(ScheduleNotFoundException e) {
            return ApiResponse.notFound(e.getMessage());
        }
    ```
    

## 1.4. 필드가 조금 다른 경우 요청에 대하여 Request DTO의 재사용

- **고민 내용**: 수정(Update) 시 필요한 DTO가 생성(Create) DTO와 필드 하나만 달라 재사용해도 될지 고민했습니다.
- **해결 방향**: **별도의 DTO를 만드는 것이 나중을 생각**하면 낫겠다고 생각했습니다. Request DTO는 사용자의 요청을 매핑하여 사용하는 객체입니다. 따라서, 각 요청마다 필요한 데이터와 유효성 검증 규칙이 다릅니다. 필드는 하나 밖에 차이가 없을지라도, 애초에 목적이 다른 객체라고 인식을 하는 것이 좋겠다고 생각했습니다.
    
    또 유지보수를 고려해보면, 지금은 조금 밖에 다르지 않더라도 차후에 크게 달라지게 되면 그만큼 어차피 분리를 해야한다고 생각했습니다. 따라서 처음부터 목적에 맞게 `ScheduleCreateRequestDto`, `ScheduleUpdateRequestDto`처럼 명확하게 분리하는 것이 낫겠다는 생각이 들었습니다. 
    

## 1.5. 데이터베이스에서 수정한 시간을 바로 반영하여 반환 하려면?

- **고민 내용**: 요구 사항에서 수정이 일어나면, 수정시간을 바로 반영하여 클라이언트로 반환해야 했습니다. 하지만 반환된 API 응답을 확인해보면, 반환된 DTO의 `updatedAt`이 실제 DB 시간과 다른 문제를 겪었습니다. 방금 수정된 시간이 아니라, 이전에 최종적으로 커밋된 시간이 반환되었습니다.
- **해결 방향**: 
이 문제의 원인은 **JPA의 동작 시점 차이** 때문이었습니다. DTO가 생성된 이후에 DB의 `updatedAt`이 갱신되어 발생했습니다.
    
    **문제 발생 과정**
    
    이 문제를 이해하려면 `@Transactional` 내부에서 일어나는 일의 순서를 알아야 합니다.
    
    1. **트랜잭션 시작 & 데이터 조회**:
        - `updateSchedule` 메서드가 호출되면서 `@Transactional`에 의해 트랜잭션이 시작됩니다.
        - `scheduleRepository.findById(id)`가 실행되어 DB에서 데이터를 가져옵니다.
        - JPA는 가져온 데이터의 복사본(스냅샷)을 만들고, 원본 엔티티 객체를 **영속성 컨텍스트**(Persistence Context)라는 1차 캐시 영역에서 관리하기 시작합니다. 이때 `schedule` 객체의 `updatedAt`은 **DB에 저장된 기존 시간**입니다. (예: `2025-08-03 10:00:00`)
    2. **엔티티 수정 (더티 체킹 감지)**:
        - `foundSchedule.updateById(requestDto)`가 호출됩니다.
        - 메모리 위에 있는 `foundSchedule` 객체의 `title` 같은 필드 값이 변경됩니다.
        - 영속성 컨텍스트는 객체 상태가 달라진 것을 인지합니다. 이 상태를 **'더티(Dirty)' 상태**라고 합니다.
    3. **DTO 생성 (문제 발생 시점)**:
        - `return new ScheduleResponseDto(foundSchedule);` 코드가 실행됩니다.
        - 이 시점에서 DTO는 `foundSchedule` 객체의 필드 값을 복사해서 만들어집니다.
        - **하지만 아직 DB에 `UPDATE` 쿼리가 날아가지 않았고, JPA Auditing도 동작하지 않았습니다.** 따라서 DTO에 복사되는 `updatedAt` 값은 여전히 **1번 단계의 기존 시간**(`2025-08-03 10:00:00`)입니다.
    4. **트랜잭션 커밋 & DB 업데이트**:
        - 서비스 메서드가 **모든 코드를 실행하고 종료(`return`)된 후**, `@Transactional`이 트랜잭션을 커밋하려고 합니다.
        - 커밋 직전에 JPA는 영속성 컨텍스트에 있는 '더티' 상태의 객체들을 **DB와 동기화**(`flush`)합니다.
        - 이때 JPA Auditing(`@LastModifiedDate`)이 동작하여 `foundSchedule` 객체의 `updatedAt` 필드를 **현재 시간**으로 갱신합니다. (예: `2025-08-03 10:05:30`)
        - 최종적으로 `title`과 새로운 `updatedAt`이 포함된 `UPDATE` 쿼리가 DB로 전송됩니다.
    
    결과적으로, **클라이언트는 3번 단계에서 만들어진 옛날 시간**이 담긴 DTO를 받고, **DB에는 4번 단계에서 갱신된 최신 시간**이 저장되어 데이터 불일치가 발생합니다.
    
    따라서, **서비스 메서드가 종료되기 전에 변경 내용을 DB에 강제로 업데이트**해야 했습니다. 기존에 저장할 때 사용했던 `save()`가 아니라, `saveAndFlush()`를 호출하여, Auditing 기능이 먼저 동작하여 엔티티의 `updatedAt` 필드를 갱신하고, 그 최신 정보가 담긴 DTO를 반환하도록 했습니다.
    
    ```java
    @Override
    @Transactional
    public ScheduleResponseDto updateScheduleById(Long id, ScheduleUpdateRequestDto requestDto) {
    
        Schedule foundSchedule = findScheduleByIdOrThrow(id);
    
        if (!foundSchedule.getPassword().equals(requestDto.getPassword())) {
            throw new InvalidPasswordException();
        }
    
        foundSchedule.updateById(requestDto.getTitle(), requestDto.getUsername());
    
        scheduleRepository.saveAndFlush(foundSchedule);
    
        return scheduleMapper.toResponseDto(foundSchedule);
    }
    ```
    
    이렇게 하니, DTO를 생성할 때, DB와 동기화된 최신 `updatedAt`값을 사용하여 정확한 정보를 반환할 수 있었습니다.
    

## 1.6. Service에서 delete 후 컨트롤러로 어떻게 성공 여부를 알려줘야할까?

- **고민 내용**: Controller의 `delete` API가 반환형이 없더라도, Service는 결과를 Controller에 알려줘야 하지 않을까라고 생각했습니다. 그래야 정상적으로 삭제가 일어나지 않은 경우에도 어떤 처리를 할 수 있기 때문입니다.
- **해결 방향**: 2-1에서 고민했던 내용과도 유사합니다. 사용자가 요청한 것을 정상적으로 수행하지 못하면 결국 예외를 일으켜야 합니다. 즉, 서비스는 **성공 시 정상 종료, 실패 시 예외 발생**이라는 방식으로 결과를 알알려야 하는 것입니다.
    
    따라서 컨트롤러는 서비스가 정상 종료되면 성공으로 간주하고 `204` 또는 `200` 응답을 보내고, 서비스에서 예외가 발생하면 에러 응답을 보내도록 했습니다. 
    
    처음에는 HTTP 기술 문서에 나와 있는 정의처럼 **204** 응답을 보내는 것으로 설계했지만, **클라이언트 개발자가 API 응답에 일관적인 처리를 하는 것도 중요**하다고 생각하여 **200**과 `Response Body`를 반환하도록 했습니다.
    

## 1.7. 서비스에서 다른 서비스의 메서드가 필요할 때 서비스에 의존하는 게 나은가? 아니면 리포지토리에 의존하는 게 나은가?

- **고민 내용**: `ScheduleService`에서 댓글 목록을 가져오기 위해 `CommentRepository`를 직접 주입받아 사용하는 것이 올바른지에 대한 고민이 있었습니다.
- **해결 방향**: 각 서비스는 자신의 도메인 책임에만 집중하는 것이 좋다는 결론을 내렸습니다. `ScheduleService`는 `Comment` 도메인의 내부를 알 필요 없이, `CommentService`에게 DTO 형태로 데이터를 요청하고 받아야 한다고 정했습니다. 그러면, 도메인 간 의존성을 줄여 결합도를 낮출 수 있기 때문입니다. 그런데 아래 8번에서 언급하겠지만, 순환 참조 문제를 만나게 되었습니다.
    
    ```java
    // ScheduleServiceImpl.java
    @Service
    @RequiredArgsConstructor
    public class ScheduleServiceImpl implements ScheduleService {
        private final ScheduleRepository scheduleRepository;
        private final CommentService commentService; 
    
        public ScheduleResponseDto findScheduleById(Long id) {
            Schedule schedule = scheduleRepository.findById(id).orElseThrow();
            // 댓글 조회 책임을 CommentService에 위임
            CommentsResponseDto commentsDto = commentService.findCommentsByScheduleId(id);
            return new ScheduleResponseDto(schedule, commentsDto);
        }
    }
    ```
    

## 1.8. 서비스 간 순환 참조 문제

- **고민 내용**: `ScheduleService`는 `CommentService`를, `CommentService`는 `ScheduleService`를 서로 주입받게 되면서 발생하는 **순환 참조(Circular Dependency)** 문제를 발견했습니다. `CommentService`에서 이미 댓글 등록 기능을 개발할 때, 댓글을 등록하기 전에 일정이 존재하는 지 확인하기 위해 `ScheduleService`의 메소드를 사용하고 있었기 때문입니다.
- **해결 방향**: 두 의존성 중 하나를 끊어야 해결이 가능했습니다. 각각 서비스의 의존 이유를 생각해보니, `ScheduleService`는 `CommentService`의 반환 값이 직접적으로 필요하고, `CommentService`는 단순히 `Schedule`이 존재하는지 확인하기 위해 `ScheduleService`를 사용하고 있었습니다. 따라서 **`CommentService`가 `ScheduleService`를 의존하는 고리를 끊는 것**이 그나마 나은 방향이라고 생각했습니다.
    
    ```java
    @Service
    @RequiredArgsConstructor
    public class CommentServiceImpl implements CommentService {
    
        private final CommentRepository commentRepository;
        private final ScheduleRepository scheduleRepository; // ScheduleService 대신 주입
    
        @Override
        @Transactional
        public CommentResponseDto createComment(Long scheduleId, CommentRequestDto requestDto) {
            // ScheduleRepository를 직접 사용하여 일정의 존재 여부를 확인
            scheduleRepository.findById(scheduleId).orElseThrow(() ->
                    new ScheduleNotFoundException("일정이 존재하지 않습니다.")
            );
            
            // ... 이하 댓글 생성 로직 ...
        }
    }
    ```
    
    다만 이렇게 하면, 7번에서 언급한 다른 도메인에 대한 내부를 알게되는 점을 완전히 해결할 수는 없습니다. 그래서 이렇게 여러 곳에서 많이 사용되는 메소드들은 차라리 **유틸리티성의 클래스**를 만들어서 공통으로 관리하고, 여러 서비스에서 호출해서 사용하도록 하는 것이 순환 참조도 해결하고, 의존 관계도 명확하게 할 수 있는 방법이라고 생각했습니다. 다만, 이번 프로젝트에서는 규모가 작은 만큼 적용하지는 않았습니다.
    

## 1.9. 엔티티와 DTO, 어떻게 분리해야 할까? 그리고 변환 로직은 어디에서 관리해야 할까?

- **고민 내용**: 처음 작성한 코드에서는 엔티티가 DTO를 직접 참조(`new Schedule(requestDto)`)하거나, DTO가 엔티티를 직접 참조(`new ScheduleResponseDto(entity)`)하는 구조였습니다. 이로 인해 계층 간의 역할이 모호하고, 특히 **핵심 도메인인 엔티티가 외부 API 스펙인 DTO에 의존하는 문제**가 있었습니다. DTO가 핵심인 엔티티의 변화에 반응하는 것은 수긍이 가지만, 엔티티가 DTO의 변화에 반응해야하는 문제가 있었습니다.
- **해결 방향**: 먼저 엔티티에서 DTO를 직접 참조하는 생성자를 제거하고, 순수한 필드 값만을 받는 생성자나 빌더 패턴을 사용하기로 결정했습니다.
    
    ```java
    // AS-IS (Entity가 DTO에 의존)
    public Schedule(ScheduleCreateRequestDto requestDto) {
        this.title = requestDto.getTitle();
        //...
    }
    
    // TO-BE (Builder 패턴으로 독립성 확보)
    @Builder
    public Schedule(String title, String contents, String username, String password) {
        this.title = title;
        //...
    }
    ```
    
    그리고 엔티티와 DTO 간의 변환 책임을 누가 질 것인지에 대해 일차적으로 생각했습니다.
    
    1. RequestDto → Entity
        - **서비스 계층**에서 `RequestDTO`를 `Entity`로 변환하기
    2. Entity → ResponseDto
        - **DTO**에 **정적 팩토리 메서드**(`from`)를 만들어 `Entity`를 `ResponseDTO`로 변환하기
    
    위의 2가지 방법대로 해도 괜찮다고 생각했습니다. **엔티티가 DTO에 의존하는 문제는 해결**했기 때문입니다. 다만, 변환 로직이 서비스 계층과 DTO 클래스에 분산되어 이전보다 훨씬 복잡하다는 생각이 들었습니다. 특히 생성자만 이용할 때와 다르게 빌더 패턴을 사용하게 되니 더 혼잡하게 느껴졌습니다.
    
    또한 서비스 계층은 비즈니스 로직에 집중해야 하는데, 긴 데이터 생성 코드로 인해 가독성이 떨어질 수 있었습니다. 
    
    **매퍼(Mapper) 클래스 도입**
    
    따라서 아예 DTO와 엔티티의 데이터 변환만을 전담하는 별도의 `Mapper` 클래스를 만들기로 했습니다. 
    
    `RequestDTO` → `Entity` 변환과 `Entity` → `ResponseDTO` 등 변환 로직을 모두 매퍼 클래스로 옮겼습니다.
    
    ```java
    // ScheduleMapper.java
    @Component
    public class ScheduleMapper {
    
        // RequestDTO -> Entity 변환 책임
        public Schedule toEntity(ScheduleCreateRequestDto requestDto) {
            return Schedule.builder()
                    .title(requestDto.getTitle())
                    // ...
                    .build();
        }
    
        // Entity -> ResponseDTO 변환 책임
        public ScheduleResponseDto toResponseDto(Schedule entity) {
            return new ScheduleResponseDto(
                entity.getId(),
                entity.getTitle(),
                // ...
            );
        }
    }
    ```
    
    서비스에서는 이제 매퍼를 주입받아 단순히 호출하기만 하면 되므로, 더욱 비즈니스 로직에만 집중할 수 있게 되었습니다.
    
    ```java
    // ScheduleServiceImpl.java
    @Override
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateRequestDto requestDto) {
    
        Schedule savedSchedule = scheduleRepository.save(scheduleMapper.toEntity(requestDto));
    
        return scheduleMapper.toResponseDto(savedSchedule);
    }
    ```
    
    추가적으로 이러한 Mapper를 편리하게 생성하고 사용할 수 있는 `MapStruct`같은 라이브러리도 존재한다고 하여, 사용을 고려해보려고 합니다.
    

## 1.10. 반복되는 API 응답 생성에 공통 DTO와 정적 팩토리 메서드 도입하기

- 고민 내용: 9번 내용을 고민하면서 **정적 팩토리 메서드**에 대해 학습했습니다. 따라서, 활용할 수 있는 다른 곳을 찾아보았습니다.
    
    마침 컨트롤러와 `Exception`핸들러에서 `new ResponseEntity(...)` 코드가 반복되고 있었습니다. 그리고 일관적인 API 응답을 위해 만들어둔 `CommonDto`라는 클래스가 응답 성공과 예외 모든 경우를 제대로 아우른다고 하기에는 조금 아쉬웠습니다. 성공의 경우에는 message가 “success”로 메소드의 리턴마다 중복으로 하드 코딩 되어 있고, HTTP Status를 2번 입력해야 해서 번거로운 점도 있었기 때문입니다.
    
- 해결 방향: 먼저`CommonDto`를 역할이 더 명확한 `ApiResponse`로 변경했습니다. 이렇게 해서 최종적으로 `Controller`나 `Exception`핸들러에서 일관된 응답을 리턴하기 위해 래퍼로 사용된다는 의미를 더 부여했습니다.
    
    **응답 생성 로직 추상화**
    
    그리고 나서 `ApiResponse` 클래스 내부에 `ResponseEntity`를 직접 생성해서 반환하는 **정적 팩토리 메서드**를 만들었습니다. 이를 통해 성공(`ok`, `created`)과 실패(`badRequest`, `notFound`) 응답을 일관되고 간결하게 생성할 수 있게 되었습니다.
    
    ```java
    // ApiResponse.java
    public class ApiResponse<T> {
    
    		private static final String SUCCESS_MESSAGE = "요청을 성공적으로 처리했습니다.";
        
        // ...
    
        /**
         * 200 OK 응답을 생성합니다. (데이터 포함)
         */
        public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, SUCCESS_MESSAGE, data));
        }
    
        /**
         * 200 OK 응답을 생성합니다. (데이터 없음)
         */
        public static ResponseEntity<ApiResponse<Void>> ok() {
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, SUCCESS_MESSAGE, null));
        }
    
        /**
         * 201 Created 응답을 생성합니다.
         */
        public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(HttpStatus.CREATED, SUCCESS_MESSAGE, data));
        }
    
        /**
         * 400 Bad Request 응답을 생성합니다.
         */
        public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST, message, null));
        }
    
        /**
         * 404 Not Found 응답을 생성합니다.
         */
        public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND, message, null));
        }
    }
    ```
    
    **컨트롤러 및 Exception핸들러** **리팩토링**
    
    이제 컨트롤러와 `GlobalExceptionHandler`에서는 `ApiResponse`의 정적 메서드만 호출하면 되므로, 일관된 API 응답을 유지하면서도 코드에 대한 유지보수도 용이해졌습니다.
    
    ```java
    // CommentContoroller.java
    
    		// ... 중략 ...
        public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
                @PathVariable Long scheduleId,
                @RequestBody CommentCreateRequestDto requestDto) {
    
            commentCreateRequestValidator.validate(requestDto);
    
            CommentResponseDto responseDto = commentService.createComment(scheduleId, requestDto);
    
            return ApiResponse.created(responseDto);
        }
    }
    
    // GlobalExceptionHandler.java
    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleScheduleNotFoundException(ScheduleNotFoundException e) {
        return ApiResponse.notFound(e.getMessage());
    }
    ```
    
    
# 2. 느낀점 및 다음 계획

오랜만에 Spring을 사용해보았고, Spring Data JPA는 처음 사용해보았습니다. Spring Boot와 Spring Data JPA를 사용해보니 정말 개발자의 생산성을 높여주는 여러가지 기능들이 놀라웠습니다. 특히 Spring Data JPA의 쿼리 메소드는 충격을 받았을 정도입니다.

다만 이렇게 프레임워크가 제공하는 기능들은 내부적으로 어떻게 추상화를 하여 만들어졌는지 이해를 하는 것이 중요하겠다는 생각을 했습니다. 올바른 사용법을 익히는 것도 필요하지만, 프레임워크가 만들어진 과정 자체를 이해하면 객체지향에 대한 이해와 함께 올바른 사용법은 알아서 익혀질 것이라고 생각합니다.

또한 공통적인 API 응답을 보내도록 개발하면서 실무적으로 클라이언트 개발자와 협업하는 것에 대해서도 고민해 보았습니다. 결국은 기준이라는 것도, 협력이 잘 되기 위해 만들어진 것인 만큼 서로의 유지보수를 위해 유연성을 발휘하는 것도 필요하다고 생각했습니다.

이번 프로젝트도 향후 개선 과제가 있습니다.

## 2.1. 향후 개선 과제

1. **테스트 코드 작성**
    
    리팩토링을 본격적으로 하기 전에 기능을 하나씩 추가하는 단계에서는 단위 테스트를 나름대로 작성했습니다. 다만 리팩토링 이후에는 테스트 코드를 수정하는 것도 조금 부담으로 다가왔습니다. 따라서, 테스트 코드 작성에 대한 방법론들을 많이 익혀보고, 단위 테스트를 용도에 맞게 작성하면서 API 통합 테스트까지 할 수 있도록 개선해야 할 것입니다.
    
2. **Swagger 도입**
    
    현재는 수동으로 관리하고 있는 API 명세서를 코드를 통해 자동으로 생성하고, 테스트 가능한 UI를 제공하여 API 문서 관리의 효율성을 높이기 위해 도입하려고 합니다.
    
3. **고아 객체 제거**
    
    현재 일정 삭제는 일정만 삭제되고 있으므로, 일정 삭제 시 관련된 댓글까지 삭제되도록 확장 방법에 대해 고민하고 개발해보려고 합니다. 
    
4. **일정 전체 조회 시 댓글 함께 조회**
    
    현재는 일정 전체 조회에서는 댓글을 가져오지 않으므로, 댓글도 함께 가져오도록 기능을 구현하며 유명한 JPA N+1 문제에 대해 고민해보면 좋을 것 같습니다.
    
5. **ErrorCode Enum 확장**
    
    현재는 예외가 많지 않아서 별도로 구현하지는 않았지만, 애플리케이션의 모든 비즈니스 예외 상황을 상태 코드와 메시지와 함께 관리하는 **`ErrorCode`같은 클래스의 Enum**으로 개선하면 좋을 것 이라고 생각합니다. 
    
    사실 이전에 실무에서 이렇게 개발했었는데, 예외 처리를 중앙에서 편리하게 관리할 수 있을뿐만 아니라, 클라이언트에서도 에러에 대해 분기적인 처리가 필요한 경우 쉽게 처리를 할 수 있습니다.
    

## 2.2. 다짐

Spring 프레임워크 전반과 Spring Boot, JPA, Spring Data JPA 등 많은 내용을 익히고, 또 온전히 이해하기 위해 노력하고 올바르게 사용하기 위해 많은 노력을 해야겠습니다. 그리고 그 중심에 있는 OOP와 문법을 이루고 있는 Java에 대해서도 계속해서 노력하려고 합니다.