package exam250711;

import java.util.Scanner;

public class ScannerTest2 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("첫 번째 숫자를 입력하세요: ");
        int first = sc.nextInt(); // 사용자가 '10' 입력 후 Enter
        // '10'을 읽고, 버퍼에는 '\n'이 남아있음

        System.out.print("두 번째 숫자를 입력하세요: ");
        // 다음 nextInt()는 시작하기 전에 버퍼에 남아있던 '\n'을
        // 구분자로 간주하고 무시(skip)한 뒤,
        // 사용자로부터 새로운 토큰(숫자) 입력을 기다린다.
        int second = sc.nextInt();

        System.out.println("첫 번째 숫자: " + first);
        System.out.println("두 번째 숫자: " + second);

        sc.close();
    }
}
