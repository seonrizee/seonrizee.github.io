---
title: "객체(Object)와 인스턴스(Instance)의 차이 알아보기"
summary: ""
date: "July 08 2025"
draft: false
tags:
- JAVA, OOP
---

최초 작성 날짜: 2025년 7월 8일  

Java를 공부하다 보면 **객체**(**Object**)와 **인스턴스**(**Instance**)라는 용어를 자주 접하게 됩니다. 많은 경우 두 용어가 혼용되어 사용되며 거의 같은 뜻을 지닌 것으로 쓰이곤 합니다. 그래서 이 두 개념의 정확한 의미를 알아보려고 합니다.

### 1. 객체 지향 프로그래밍의 '설계도': 클래스(Class)

객체와 인스턴스를 이해하기 전에 먼저 **클래스**(**Class**)의 역할을 명확히 알아야 합니다. 클래스는 쉽게 말해 우리가 만들고자 하는 **객체의 설계도** 또는 **틀**입니다.

이 설계도 안에는 객체가 가질 **상태**(state)와 상태와 관련된 **행동(behavior)을** 가지고 있습니다.  `Java`에서는 상태를 `Fields`라고 표현하며 흔히 **속성**이라고 합니다. 그리고 행동은 `Method`(메서드)라고 표현합니다. 그리고 이렇게 상태와 메소드를 활용하여 객체 간의 **상호작용**으로 프로그램을 구성하는 것이 **객체 지향 프로그래밍**의 기본 컨셉입니다.

```java
class Car { // 자동차 설계도
    String color; // 색상 (속성)
    String model; // 모델 (속성)
    int speed;    // 속도 (속성)

    void accelerate() { // 가속하는 기능 (메서드)
        speed += 10;
    }

    void stop() { // 정지하는 기능 (메서드)
        speed = 0;
    }
}
```

위 `Car` 클래스는 '자동차'라는 개념의 설계도를 제공합니다. 아직 실제 자동차는 만들어지지 않았습니다.

---

### 2. 설계도에서 만들어진 '실체': 객체(Object)

이제 설계도를 가지고 실제 자동차를 만들어 봅시다. **객체**(**Object**)는 이 **클래스**라는 설계도를 기반으로 **메모리에 실제로 만들어진 실체**입니다.

우리가 현실에서 '자동차'라고 부르는 구체적인 대상(예: 흰색 소나타, 검은색 벤츠)에 해당합니다. 각 객체는 클래스에서 정의한 속성에 대해서 자신만의 고유한 값을 가질 수 있습니다.

```java
public class Main {
    public static void main(String[] args) {
        Car myCar = new Car(); // 'Car' 설계도로 'myCar' 객체를 만듭니다.
        myCar.color = "White";
        myCar.model = "Sonata";
        myCar.speed = 0;

        Car yourCar = new Car(); // 'Car' 설계도로 'yourCar' 객체를 만듭니다.
        yourCar.color = "Black";
        yourCar.model = "Benz";
        yourCar.speed = 0;
    }
}
```

위 코드에서 `myCar`와 `yourCar`는 각각 독립적인 **객체**입니다. 서로 다른 색상과 모델을 가질 수 있습니다.

---

### 3. 클래스와의 '관계'를 강조하는 표현: 인스턴스(Instance)

여기서 인스턴스가 등장합니다. **인스턴스**(**Instance**)는 기본적으로 **객체와 같은 의미**로 사용됩니다. 하지만 인스턴스라는 용어는 **객체가 특정 클래스로부터 생성되었다는, 즉 클래스와의 관계를 강조할 때** 주로 사용됩니다.

`myCar` 객체는 `Car` 클래스의 **인스턴스**입니다. 이는 `myCar`가 `Car`라는 설계도에 따라 만들어진 구체적인 자동차임을 명확히 보여줍니다.

| 구분 | 의미 | 강조하는 점 | 예시 |
| --- | --- | --- | --- |
| **클래스** | 객체를 만들기 위한 **설계도** | 객체의 공통적인 구조와 행동 정의 | `Car` 클래스 (자동차 설계도) |
| **객체** | 클래스를 기반으로 **메모리에 생성된 실체** | 속성과 기능을 가진 '어떤 대상' 그 자체 | `myCar`, `yourCar` (실제로 존재하는 자동차들) |
| **인스턴스** | **특정 클래스로부터 생성된 구체적인 객체를 지칭** | 객체와 클래스 간의 '생성 관계' | `myCar`는 `Car` 클래스의 **인스턴스**이다. |

**즉, 모든 인스턴스는 객체이지만, 모든 객체를 인스턴스라고 부를 때는 '어떤 클래스의 인스턴스'인지 특정하는 의미가 내포됩니다.** Oracle에서 제공하는 자바 공식 가이드 dev.java에서도 아래와 같이 표현하고 있습니다.

> *Note: The phrase "instantiating a class" means the same thing as "creating an object." When you create an object, you are creating an "instance" of a class, therefore "instantiating" a class.
"클래스 인스턴스화"라는 표현은 "객체 생성"과 같은 의미입니다. 객체를 생성하면 클래스의 "인스턴스"가 생성되므로 클래스를 "인스턴스화"하는 것입니다.  
-**Java 공식 튜토리얼 ([dev.java](http://dev.java))**
> 

그러니까, 사실 `car` 클래스로부터 인스턴스화한 `myCar` 는 객체인 것도 맞고, 인스턴스인 것도 맞습니다. 하지만 myCar를 어떤 **목적과 맥락**으로 부르는지에 따라서 객체 혹은 인스턴스 2가지 중 하나라고 일컫는다고 생각하면 됩니다.

---

다시 정리하자면, **객체**(**Object**)는 클래스를 통해 만들어진 '**실체**' 그 자체를 의미하는 **넓은 의미의 표현**입니다. 반면 **인스턴스**(**Instance**)는 이 '실체'가 **어떤 특정 클래스로부터 생성된 것임을 강조할 때** 사용하는 표현입니다.

일상적인 대화나 코드에서는 두 용어가 혼용되는 경우가 많습니다. 하지만, 기술에서 사용되는 단어들에 대해 정확한 **용어**(Term)를 알고 사용하는 것이 의사소통을 더 효율적으로 이뤄지게 하는 기본이라고 생각합니다. 앞으로도 이런 케이스가 있다면 또 글을 작성해보겠습니다.

**참고**

- [Dev.java - Learn Java: Creating and Using Objects](https://dev.java/learn/classes-objects/creating-objects/)
- [Dev.java - Learn Java: Objects, Classes, Interfaces, Packages, and Inheritance](https://dev.java/learn/oop/)
- [김종민, 『스프링 입문을 위한 자바 객체 지향의 원리와 이해』, 위키북스, 2015](https://www.yes24.com/product/goods/17350624)
- Google Gemini