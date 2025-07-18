---
title: "Java final 키워드와 상수 그리고 불변 객체"
summary: ""
date: "July 18 2025"
draft: false
tags:
- Java
---


오늘은 Java의 `final` 키워드에 대해 학습한 내용을 정리해 보려고 합니다.  `final`이 변수, 메서드, 클래스에 각각 어떻게 적용되는지 또 얻는 효과와 주의점, 그리고 상수와 불변 객체에 대해 알아보겠습니다.

---

# final 키워드의 세 가지 활용법

`final`은 "최종적인", "변경될 수 없는"이라는 의미를 가지며, 어디에 위치하는지에 따라 그 역할이 조금씩 달라지는데요. `final` 키워드는 변수, 메소드, 클래스와 함께 사용할 수 있습니다.

## **1. `final` 변수: 단 한 번의 할당만 허용**

변수 앞에 `final`을 붙이면, 해당 변수는 선언 시 또는 생성자 내에서 **단 한 번만 초기화될 수 있으며, 그 이후에는 값을 변경할 수 없습니다.** 반대로 말하면, **값이 변경되어서는 안 되기 때문에,** `final` 키워드와 함께 인스턴스 변수는 선언과 함께 혹은 생성자 ****그리고 `static`으로 선언된 클래스 변수는  선언과 동시에 혹은 `static`블록에서 값을 초기화해야 합니다. 

```java
// 선언과 동시에 초기화
final int MAX_SIZE = 100;
MAX_SIZE = 200; // 컴파일 에러 발생!

// 선언 후 생성자에서 초기화 (blank final)
class MyClass {
    final String name;

    MyClass(String name) {
        this.name = name; // 생성자에서 단 한 번 초기화 가능
    }

    void setName(String newName) {
        // this.name = newName; // 컴파일 에러 발생!
    }
}
```

`final` 변수는 재할당이 불가능하기 때문에, 예시의 `MAX_SIZE`처럼 값의 일관성이 필요한 경우에는 일종의 상수처럼 사용할 수 있습니다. 

**매개 변수**나 **지역 변수**를 final로 선언하는 경우에는 반드시 선언할 때 초기화할 필요는 없습니다. 애초에 매개변수는 이미 초기화가 되어서 넘어 옵니다. 그리고 지역 변수는 다른 곳에서 사용할 수가 없기 때문에, 선언과 동시에 초기화를 하지 않아도 사용 전까지만 별도로 초기화가 되면 사용에 문제가 없습니다.

| 구분 | 초기화 필수 여부 |
| --- | --- |
| 인스턴스 변수 | 선언 시 또는 생성자 |
| 클래스 변수 (static) | 선언 시 또는 static 블록 |
| 매개변수 | 메소드 호출 시 (자동 초기화) |
| 지역 변수 | 사용 전까지 |

---

## **2. `final` 메서드: 오버라이딩(재정의) 금지**

부모 클래스의 메서드에 `final`을 붙이면, **자식 클래스에서 해당 메서드를 오버라이딩(재정의)할 수 없습니다.** 이는 클래스의 핵심적인 동작이 자식 클래스에 의해 임의로 변경되는 것을 막고 싶을 때 유용합니다.

```java
class Parent {
    final void coreLogic() {
        System.out.println("이 로직은 절대 변경되면 안 됩니다.");
    }
}

class Child extends Parent {
    // @Override
    // void coreLogic() { // 컴파일 에러! final 메서드는 오버라이딩 불가
    //     System.out.println("로직을 변경하고 싶어요.");
    // }
}
```

프레임워크나 라이브러리에서 핵심 동작의 변경을 막기 위해 사용됩니다. 그러나 일반적으로는 거의 사용되는 일이 드뭅니다.

---

## **3. `final` 클래스: 상속 금지**

클래스에 `final`을 붙이면, **어떤 다른 클래스도 이 클래스를 상속(extends)할 수 없습니다.** 더 이상 확장할 필요가 없거나, 보안상의 이유로 클래스의 구현을 최종적으로 완성시키고 싶을 때 사용합니다. 

```java
final class SecureString {
    // ...
}

// class MyString extends SecureString {} // 컴파일 에러! final 클래스는 상속 불가
```

대표적인 예로 Java의 `String`, `Integer`와 같은 래퍼 클래스들이 있습니다. 이들은 불변성을 유지하고 예측 가능하게 동작해야 하므로 `final`로 선언되었습니다. 이런 경우는 **더 이상 확장을 해서는 안되는 클래스**, 상속 받아서 내용을 변경해서는 안 되는 클래스에 `final`로 선언한 것이라고 생각할 수 있습니다.

---

# 상수(Constant)와 불변 객체(Immutable Object)

`final` 키워드는 상수와 불변 객체를 만드는 데 핵심적인 역할을 합니다.

## **1. 상수 (Constant): `static final`**

상수는 프로그램 전체에서 **공유되며, 절대 변하지 않고 항상 일정한 값을 갖는 것**을 의미합니다. Java에서는 `static final` 키워드를 조합하여 상수로 사용하며 보통 **대문자**와 **스네이크 케이스**를 이용하여 표현합니다.

> The `static` modifier, in combination with the `final` modifier, is also used to define constants. 
[**Dev.java - More on Classes: Understanding Class Members**](https://dev.java/learn/classes-objects/more-on-classes/#class-members)
> 
- `final`: 값이 재할당될 수 없도록 합니다.
- `static`: 모든 인스턴스가 값을 공유하도록 하며, 프로그램 시작 시 메모리에 한 번만 할당됩니다.

```java
`public class MathConstants {
    public static final double PI = 3.1415926535; // 상수 선언
    public static final double E = 2.7182818284;
}

// 사용 예시
double circleArea = MathConstants.PI * radius * radius;`
```

---

## **2. 불변 객체 (Immutable Object)**

**불변 객체**는 **생성된 이후 내부 상태(필드 값)가 절대 변하지 않는 객체**입니다. 

- **스레드 안전성 (Thread-Safe)**: 여러 스레드가 동시에 접근해도 상태가 변하지 않으므로 동기화 문제에서 비교적 자유롭습니다.
- **예측 가능성**: 객체의 상태가 항상 동일하므로 코드를 이해하고 디버깅하기 쉽습니다.
- **캐싱**: 값이 변하지 않으므로 캐시하여 재사용하기 용이합니다.

**불변 객체 만드는 방법:**

1. **클래스를 `final`로 선언**하여 상속을 막습니다.
2. **모든 필드를 `private final`로 선언**하여 외부 접근과 재할당을 막습니다.
3. 상태를 변경하는 메서드(**Setter 등)를 제공하지 않습니다.**
4. 생성자를 통해 모든 필드를 초기화합니다.
5. **참조의 변경은 막지만, 내부상태 변경은 막지 않으므로, 생성자와 Getter에서 방어적 복사(Defensive Copy)를 통해 외부에서 내부 상태를 변경할 수 없도록 해야 합니다.**

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ImmutableTeam {
    private final String teamName;
    private final List<String> members; // 변경 가능한 객체를 필드로 가짐

    public ImmutableTeam(String teamName, List<String> members) {
        this.teamName = teamName;
        // 생성자에서 방어적 복사: 외부의 리스트를 그대로 사용하지 않고 복사본을 만듦
        this.members = new ArrayList<>(members);
    }

    public String getTeamName() {
        return teamName;
    }

    // Getter에서 방어적 복사: 내부 리스트의 수정 불가능한 뷰(view)를 반환
    public List<String> getMembers() {
        return Collections.unmodifiableList(this.members);
    }
}
```

**⚠️ `final`의 함정**: 객체 참조 변수에 `final`을 사용해도 객체 자체가 불변이 되는 것은 아닙니다.

```java
// radius가 final이 아닌 경우
class MutableCircle {
    public double radius;
    public MutableCircle(double r) { this.radius = r; }
}

final MutableCircle c1 = new MutableCircle(5.0);
c1 = new MutableCircle(10.0); // 에러! c1은 다른 객체를 참조할 수 없음

c1.radius = 7.0; // 가능! c1이 참조하는 객체의 내부 상태는 변경 가능
```

위 예시처럼 `final`은 참조 변수 `c1`이 다른 객체를 가리키는 것을 막을 뿐, `c1`이 가리키는 객체의 내부 필드(`radius`)를 변경하는 것은 막지 못합니다. 진정한 불변성을 위해서는 객체의 **내부 필드 또한 `final`로 선언**해야 합니다.

정리하면, 객체 참조 변수의 경우에는 두 번 이상 생성할 수 없지만, 그 **객체의 안에 있는 필드 혹은 객체들은 final로 선언된 것이 아니기 때문**에 제약이 없다는 것을 기억하고, 캡슐화를 지키기 위해 setter역할을 메소드 뿐만 아니라, getter역할을 하는 메소드도 주의해야 합니다.

```java
import java.util.ArrayList;
import java.util.List;

// 내부 상태를 그대로 노출
class BadShoppingCart {
    private final List<String> items;

    public BadShoppingCart() {
        this.items = new ArrayList<>();
    }

    // 내부의 List 참조를 그대로 반환하여 캡슐화가 깨짐
    public List<String> getItems() {
        return this.items;
    }
}

// 외부에서 내부 상태를 직접 조작
public class Main {
    public static void main(String[] args) {
        BadShoppingCart cart = new BadShoppingCart();
        cart.getItems().add("사과"); // 외부에서 직접 아이템 추가
        cart.getItems().add("바나나");
        
        // 심지어 외부에서 리스트를 통째로 비워버릴 수도 있음
        cart.getItems().clear(); 
        
        System.out.println("현재 아이템 개수: " + cart.getItems().size()); // 출력: 0
    }
}
```

이를 방지하기 위해 getter가 필요한 경우, getter()를 제공하기 보다는 getter()를 필요로 하는 로직 자체를 클래스에 작성하여 메소드로 제공하는 것이 좋습니다. 아니면 위에서 언급했던, **방어적 복사**의 방법을 선언하여 읽기 전용으로 객체를 반환하는 방법도 있습니다. 방어적 복사의 여러 방법과 차이는 다른 글에서 깊게 다룰 예정입니다.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

// 로직을 메소드로 제공하여 캡슐화 유지
class GoodShoppingCart {
    private final List<String> items;

    public GoodShoppingCart() {
        this.items = new ArrayList<>();
    }

    // '아이템 추가'라는 로직을 메소드로 제공
    public void addItem(String item) {
        // 아이템 유효성 검사 등 방어 로직을 추가할 수 있음
        if (item != null && !item.isEmpty()) {
            this.items.add(item);
        }
    }
    
    // '아이템 개수 조회'라는 로직을 메소드로 제공
    public int getItemCount() {
        return this.items.size();
    }

    // '모든 아이템 출력'이라는 로직을 메소드로 제공
    public void printAllItems() {
        System.out.println("--- 장바구니 목록 ---");
        for (String item : items) {
            System.out.println("- " + item);
        }
        System.out.println("--------------------");
    }
}

// 외부에서는 제공된 메소드만 사용 가능
public class Main {
    public static void main(String[] args) {
        GoodShoppingCart cart = new GoodShoppingCart();
        
        // 제공된 메소드를 통해서만 상호작용
        cart.addItem("사과");
        cart.addItem("바나나");

        // cart.getItems().clear(); // 컴파일 에러! getItems()가 없으므로 직접 조작 불가
        
        System.out.println("현재 아이템 개수: " + cart.getItemCount()); // 출력: 2
        cart.printAllItems();
    }
}
```

이렇게 보면 언뜻 객체의 불변성이 지켜지더라도 내부의 값들은 변경이 가능하니, 무슨 의미일까? 라는 생각이 들수도 있지만 참조의 불변성으로 인해 사용되는 경우도 있습니다. 

Java에서 람다 표현식이나 익명 클래스가 외부의 지역 변수를 사용하려면, 그 변수는 `final`혹은 `effectively final`이어야 합니다. 

> *Variable used in lambda expression should be final or effectively final
[Dev.java - first lambdas: Capturing Local Values](https://dev.java/learn/lambdas/first-lambdas/#local-variables)*
> 
- `effectively final`(사실상 final): Java SE 8에서 도입되었으며, 변수를 명시적으로 `final` 로 선언하지 않아도 컴파일러가 `final`이 있는 것처럼 간주합니다. 주로 **람다**나 **익명 클래스**에서 외부의 지역 변수를 참조할 때 필요하며, 컴파일러는 참조될 변수의 값이 도중에 바뀌지 않을 것이라는 보장을 받기 위해 이 규칙을 적용합니다.
    - **초기화와 동시에 선언**했든 (`int x = 10;`), **선언 후 나중에 한 번만 할당**했든 (`int x; x = 10;`), 그 이후로 **값이 절대 변경되지 않았으면** `effectively final` 입니다.
    - 값이 변경되는 경우란 다음과 같습니다.
        - 대입 연산자(`=`)를 사용해 **새로운 값을 할당**하는 경우 (`x = 20;`)
        - **증감 연산자**(`x++`, `--x`)를 사용하는 경우
    - 만약 어떤 변수가 `effectively final`이라면, 그 변수 선언에 `final` 변경자를 추가해도 컴파일 시점 오류도 발생하지 않습니다. 반대로, 정상적으로 작동하는 프로그램에서 `final`로 선언된 지역 변수나 매개변수는 `final` 변경자를 제거하면 `effectively final`이 됩니다.

람다나 익명 클래스는 **별도의 객체**로 만들어지며, 자신이 참조하는 지역 변수를 **캡처**(capture)하여 복사본처럼 사용합니다. **만약 원본 변수가 계속 바뀔 수 있다면, 캡처된 값과 원본 값 사이의 데이터 불일치로 인해 예측 불가능한 동작이 발생할 수 있습니다.**

```java
public void processItems(List<Item> items) {
    final List<Result> results = new ArrayList<>(); // 이 리스트는 다른 리스트로 교체되지 않음

    items.forEach(item -> {
        // 람다 표현식 안에서 외부 지역 변수인 results를 사용
        // results가 final이 아니라면(또는 사실상 final이 아니라면) 컴파일 에러 발생
        Result r = process(item);
        results.add(r); // 객체 내부의 상태를 바꾸는 것은 OK! (리스트에 요소 추가)
    });
}
```

`final`을 명시적으로 붙여주면 이 규칙을 항상 만족시키므로, 람다나 익명 클래스에서 안전하게 변수를 사용할 수 있습니다.

---

`final` 이라는 키워드는 친숙한 단어인만큼 용법도 간단할 것 같았지만, 생각보다 쓰이는 곳도 많고 특히 불변 객체와 관련해서는 주의해야할 내용들이 있습니다. 따라서, 사용의 목적과 올바른 사용법을 익히고 적재적소에 사용하기 위해 노력해야 합니다.

**참고:**

- [Oracle Dev.java: Classes and Objects](https://dev.java/learn/classes-objects/)
- [Oracle Dev.java: Lambda Expressions](https://dev.java/learn/lambdas/)
- [Oracle Java SE Specs SE 8: 4.12.4 final Variables](https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.12.4)
- [Baeldung: Final vs Effectively Final in Java](https://www.baeldung.com/java-effectively-final)
- [이상민, 『자바의 신 VOL.1』, 로드북, 2023](https://www.yes24.com/product/goods/122886212)
- 내일배움캠프 제공 Java 문법 종합반: final - 변하지 않는 값