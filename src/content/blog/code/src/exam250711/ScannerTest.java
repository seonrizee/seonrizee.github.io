package exam250711;

import java.util.Scanner;

public class ScannerTest {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("숫자를 입력하세요: ");
        int num = sc.nextInt();
        System.out.println("num = " + num);

        // 남은 줄바꿈을 구분자로 사용한 뒤 빈 문자열을 리턴하게 된다.
        sc.nextLine();

        System.out.print("문자열을 입력하세요: ");
        String line = sc.nextLine();
        System.out.println("line = " + line);

        sc.close();
    }
}
