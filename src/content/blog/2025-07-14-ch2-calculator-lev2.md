---
title: "계산기 프로젝트 Level 2 회고"
summary: ""
date: "July 14 2025"
draft: false
tags:
- 회고
- 프로젝트
---

저번에 이어서 이번에는 계산기를 만드는 과제의 Level 2 를 구현하면서 고민했던 부분을 알아보겠습니다. 이번에는 상대적으로 고민했던 부분이 많은 만큼 조금 간단히 작성해보겠습니다.

---

# 과제 내용

## 학습목표

**<계산기 2단계까지>**

- [ ]  메서드를 정의하고 호출할 수 있는가?
- [ ]  배열을 선언하고 활용해 여러 개의 값을 저장하고 조작할 수 있는가?
- [ ]  클래스를 작성하고 객체를 생성하여 활용할 수 있는가?

## 필수 기능 가이드

<aside>
💡

**Lv 2.**
계산된 결과 값들을 기록하는 컬렉션을 만든다.
컬렉션의 가장 먼저 저장된 데이터를 삭제하는 기능을 만든다.

</aside>

- [ ]  **사칙연산을 수행 후, 결과값 반환 메서드 구현 & 연산 결과를 저장하는 컬렉션 타입 필드를 가진 Calculator 클래스를 생성**
    - [ ]  사칙연산을 수행한 후, 결과값을 반환하는 메서드 구현
    - [ ]  연산 결과를 저장하는 컬렉션 타입 필드를 가진 Calculator 클래스를 생성
    - [ ]  1) 양의 정수 2개(0 포함)와 연산 기호를 매개변수로 받아 사칙연산(➕,➖,✖️,➗) 기능을 수행한 후 2) 결과 값을 반환하는 메서드와 연산 결과를 저장하는 컬렉션 타입 필드를 가진 Calculator 클래스를 생성합니다.
- [ ]  **Lv 1에서 구현한 App 클래스의 main 메서드에 Calculator 클래스가 활용될 수 있도록 수정**
    - [ ]  연산 수행 역할은 Calculator 클래스가 담당
        - [ ]  연산 결과는 Calculator 클래스의 연산 결과를 저장하는 필드에 저장
    - [ ]  소스 코드 수정 후에도 수정 전의 기능들이 반드시 똑같이 동작해야합니다.
- [ ]  **App 클래스의 main 메서드에서 Calculator 클래스의 연산 결과를 저장하고 있는 컬렉션 필드에 직접 접근하지 못하도록 수정 (캡슐화)**
    - [ ]  간접 접근을 통해 필드에 접근하여 **가져올** 수 있도록 구현합니다. (Getter 메서드)
    - [ ]  간접 접근을 통해 필드에 접근하여 **수정할** 수 있도록 구현합니다. (Setter 메서드)
    - [ ]  위 요구사항을 모두 구현 했다면 App 클래스의 main 메서드에서 위에서 구현한 메서드를 활용 해봅니다.
- [ ]  **Calculator 클래스에 저장된 연산 결과들 중 가장 먼저 저장된 데이터를 삭제하는 기능을 가진 메서드를 구현한 후 App 클래스의 main 메서드에 삭제 메서드가 활용될 수 있도록 수정**
    - [ ]  키워드 : `컬렉션`
        - [ ]  컬렉션에서 ‘값을 넣고 제거하는 방법을 이해한다.’가 중요합니다!

---

아래에는 필수 기능 가이드에서 제시한 4개의 큰 기능들을 구현하면서 고민했던 내용들에 대해 간단히 기록해보았습니다.

# 고민한 내용

### **1.** 사칙연산을 수행 후, 결과값 반환 메서드 구현 & 연산 결과를 저장하는 컬렉션 타입 필드를 가진 Calculator 클래스를 생성

**Q.** 연산 결과를 저장할 `Collections` 으로 어떤 것이 적절한가?

- **문제 원인**: '저장 순서대로 기록하고, 가장 먼저 저장된 것을 삭제'하는 요구사항이 존재했기 때문에 어떤 자료구조가 가장 적합할지 고민했습니다.
- **해결 방향**: FIFO(First-In, First-Out) 특성을 가진 `Queue` **인터페이스**가 요구사항에 부합한다고 판단했습니다. 구현체로는 일반적인 `ArrayDeque`를 선택했습니다.
    
    그 이유는 가장 먼저 저장된 데이터를 삭제할 때 `ArrayList` 의 `remove` 메소드의 경우 O(N)의 시간복잡도를 가집니다. 따라서 `Queue` 인터페이스를 구현하고 있는 `ArrayDeque` 과 `LinkedList`를 후보로 남겼습니다. 
    
    여기서 `LinkedList`의 경우 순회할 때 시간복잡도는 **O(N)**으로 같을 수 있으나 앞, 뒤 노드의 정보를 필요로 하기 때문에, `ArrayDeque`보다 메모리를 더 사용하고 성능도 떨어진다고 생각했습니다. 그래서 `ArrayDeque`을 사용했습니다.
    
    | 기능 | ArrayDeque | ArrayList | LinkedList |
    | --- | --- | --- | --- |
    | 값 추가 (맨 뒤) | O(1) | O(1) | O(1) |
    | 첫 값 삭제 | O(1)  | O(n) | O(1) |
    | 전체 조회/필터링 | O(n) (빠름) | O(n) (빠름) | O(n) (상대적으로 느림) |
    | 메모리 효율 | 좋음 | 좋음 | △ 보통 (노드 오버헤드) |

**Q.** `results` 컬렉션 필드에 `final`을 붙여야 하지 않나?

- **문제 원인**: `Calculator` 객체가 살아있는 동안 `results` 큐가 다른 큐 객체로 교체될 가능성을 막아 안정성을 높이고 싶었습니다.
- **해결 방향**: 처음에는 필드를 `final`**로 선언**하여, 참조 변수가 다른 객체를 가리키도록 재할당하는 것을 컴파일 시점에 막았습니다. 다만 과제에서, 캡슐화에 대한 개념을 익히기 위해 setter메소드를 생성해야 했기에, 개념만 익히고 final을 붙이지 않았습니다.

**Q.** `results` 컬렉션 필드를 `getter()`를 통해 접근하면 `add()` 로 조작이 가능한데 불가능하게 하려면?

- **문제 원인:** `pirvate` 으로 선언하더라도, `App` 클래스에서 `calculator.getResults().add(100);`와 같이 큐의 내용을 마음대로 조작할 수 있었습니다. 캡슐화의 의미를 지키려면 조작은 불가능하되, 과제에서 **Getter 메소드**를 구현하라고 했기 때문에, 동시에 만족하는 방법을 찾아야 했습니다.
- **해결 방향:** Collections에서 제공하는 **수정 불가능한 래퍼 클래스**나 `Atomic`클래스 등 여러 방법이 있었습니다. 이번엔 자바에서 제공하는 클래스를 이용하는 방법말고, 코드 상으로 방어할 수 있는 방법도 익혀보기 위해서 모든 데이터를 포함하지만, 새로운 객체를 생성해서 반환하는 방법을 선택했습니다.
    
    간편하게 구현할 수 있지만, 조회할 때마다 매번 데이터가 전체 복사되기 때문에 메모리 상으로 비효율적일 수 있습니다. 대신, 래퍼 클래스와 다르게 객체를 조회한 곳에서 어떤 가공을 해야하는 경우는 장점이 될 수도 있습니다.
    
    ```java
    public Queue<Integer> getResults() {
        return new ArrayDeque<>(results);
    }
    ```
    

---

### **2. Lv 1에서 구현한 App 클래스의 main 메서드에 Calculator 클래스가 활용될 수 있도록 수정**

**Q.** 입출력 관련 기능(메서드)은 어느 클래스에 위치해야 하는가?

- **문제 원인**: 메뉴 출력, 숫자 입력 안내 등 사용자 인터페이스(UI) 관련 코드를 계산 로직이 있는 `Calculator`에 두어야 할지, 실행을 담당하는 `App`에 두어야 할지 역할 분담이 모호했습니다. 그래서 `Calculator`와 `App`의 역할부터 명확히 정의해야겠다고 생각했습니다.
- **해결 방향**: 먼저 각 클래스의 역할을 정의했습니다.
    - **`App`**: 사용자 입출력, 메뉴 선택, 프로그램 흐름 제어 등 **UI와 상호작용**하는 책임을 가지도록 정의했습니다. 이렇게 하면, 과제에서 구현한 콘솔이 아니라 다른 UI 형태로 구성한다고 해도 `Calculator`같은 로직이 담긴 클래스가 영향을 받지 않는 장점이 있습니다.
        
        대신 현재는 어떤 상태를 기록할 필드가 필요하지 않고, 기능이 복잡하지는 않기 때문에 객체를 생성하기 위한 클래스로 정의하지는 않고, `static`을 이용하여 클래스에서 static 메소드로 App의 기능들을 호출하여 사용하기로 했습니다. 그리고 Level 3 에서 테스트 코드 작성이나 상태가 필요한 경우가 생길 수 있으니 분리하기로 계획했습니다.
        
    - `Calculator`: 실제 **계산 로직**만 담당하며, Menu와 같은 Enum 클래스와 호출은 하더라도 `System.out`이나 `Scanner` 등 UI 구현에 필요한 기능은 포함하지 않도록 구분했습니다.

---

### **3. App 클래스의 main 메서드에서 Calculator 클래스의 연산 결과를 저장하고 있는 컬렉션 필드에 직접 접근하지 못하도록 수정 (캡슐화)**

**Q.** Getter/Setter가 캡슐화에 미치는 영향

- **문제 원인**: 요구사항에 Getter/Setter 구현이 있었지만, 특히 `setResults(Queue q)`처럼 내부 상태를 통째로 교체하는 Setter는 캡슐화의 의미를 퇴색시킨다고 판단했습니다.
- **해결 방향**: Getter는 앞에서 언급한 것과 같이 데이터를 복사한 객체로 제공하고, Setter는 **요구사항의 의도를 재해석**하여 데이터의 조작이 필요한 경우 `removeFirstResult()`처럼 아예 필요한 기능으로 개발하여 `Calculator`클래스에서 메소드로 명확하게 제공하는 것이 맞다고 생각했습니다. 마침 튜터님께서도 강의해서 그 방향을 언급해주셨습니다. `setResults()`는 과제를 위해 놔두었지만, 사용하지는 않았습니다.

**Q.** 메뉴 관리를 위해 어떤 방법을 사용하는 것이 적절한가?

- **문제 원인**: 메뉴 목록처럼 정해진 값의 집합을 관리할 때, `static final` 을 이용한 방식보다 더 나은 방법이 있는지 고민했습니다.
- **해결 방향**: **타입 안전성**(정해진 메뉴 외의 값은 받을 수 없음), **가독성**(`Menu.CALCULATE`), **확장성** 면에서 **`enum`을 사용하는 것이** 좋다고 판단했습니다. Level 3 요구 사항에도 있는 만큼 미리 사용해보는 것도 좋다고 생각했습니다. 그리고 `enum`은 `ordinal` 을 이용하기 보다는 사용자가 입력하는 순서의 번호와 그에 대한 설명을 가지도록 객체를 구성했습니다.
    
    ```java
    public enum Menu {
    
        CALCULATE(1, "계산하기"),
        VIEW(2, "이전 연산 결과 조회하기"),
        REMOVE_FIRST(3, "가장 먼저 저장된 연산 결과 삭제하기"),
        EXIT(4, "종료하기"),
        ;
    ```
    

---

### **4. Calculator 클래스에 저장된 연산 결과들 중 가장 먼저 저장된 데이터를 삭제하는 기능을 가진 메서드를 구현한 후 App 클래스의 main 메서드에 삭제 메서드가 활용될 수 있도록 수정**

**Q.** `Queue`의 `poll()`과 `remove()` 중 무엇을 사용해야 하는가?

- **문제 원인**: 큐의 첫 요소를 삭제하기 위해 여러 메서드 중에 어떤 메소드를 사용하는 것이 좋을지 고민했습니다.
- **해결 방향**: 큐가 비어있을 때 `remove()`는 **예외(Exception)를 발생**시키고, `poll()`은 **`null`을 반환**하는 차이를 확인했습니다. 예외 처리 없이 더 안전하게 로직을 구성할 수 있는 **`poll()`을 사용**하기로 결정했습니다. 그리고 `Optional`을 사용하여 `null` 값 유무에 따라 분기를 하도록 로직을 구현했습니다.
    
    ```java
    // Calculator.class
    public Optional<Integer> removeFirstResult() {
        return Optional.ofNullable(results.poll());
    }
    
    // App.class
    private static void handleRemove(Calculator calculator) {
        System.out.println("가장 먼저 저장된 연산 결과 삭제가 시작됩니다.");
    
        Optional<Integer> removedValue = calculator.removeFirstResult();
        if (removedValue.isEmpty()) {
            System.out.println("저장된 연산 결과가 없습니다.");
            return;
        }
    
        System.out.println("가장 먼저 저장된 연산 결과인 " + removedValue.get() + "이(가) 삭제되었습니다.");
        showPrevResults(calculator.getResults());
    }
    ```
    

---

# 느낀점 및 다음 계획

개발을 할 때는 계속 고민을 하고 자료를 찾아서 어떤 방법이 더 근거가 타당한지 계속 찾다 보니 시간을 많이 소모했습니다. 그런데 이렇게 막상 정리를 해보면, 또 고민한 내용이 별로 안되는 것 같기도 합니다. 그리고 고민거리가 생길 때마다 간단히 기록하고 넘어가기도 했던 부분들이, 나중에는 잘 생각이 안 나는 경우도 있기 때문에 항상 **그때 그때 마다 고민거리와 해결 방법까지 잘 기록하는 것이 중요하다고 느꼈습니다.**

다음에는 마지막 단계인 **Level 3**에 대해 작성하려고 합니다. Level 3에는 이른바 모던 자바의 문법 요소들을 사용하는 요구 사항이 많습니다. 그래서 개인적으로도 학습할 거리도 많고, 기대도 됩니다. 특히 es6+ 이상에서 제공하는 문법을 활용하여 `Javascript`로 개발을 할 때처럼, **람다, 스트림같은 함수형 요소들을 이용**하여 개발하는 것에 대해 특히 개인적으로 흥미가 갑니다.

그러면 다음 Level 3 회고로 돌아오겠습니다.

**참고**

- [Dev.java - Learn Java: Enums](https://dev.java/learn/classes-objects/enums/)
- [Dev.java - Storing Elements in Stacks and Queues](https://dev.java/learn/api/collections-framework/stacks-queues/)
- [Dev.java - Iterating over the Elements of a Collection](https://dev.java/learn/api/collections-framework/iterating/)
- [자바 튜토리얼 (JDK 8) - 열거형](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html)
- [Oracle Java SE 17 & JDK 17 - Class ArrayDeque](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayDeque.html)
- [Oracle java SE 17 & JDK 17 - Class LinkedList](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedList.html)
- [이상민, 『자바의 신 VOL.1』, 로드북, 2023](https://www.yes24.com/product/goods/122886212)
- [이상민, 『자바의 신 VOL.2』, 로드북, 2023](https://www.yes24.com/product/goods/122886692)
- Google Gemini