package exam250711;

import java.util.Scanner;

public class ScannerTest3 {


    public static void main(String[] args) {
        Scanner sc = new Scanner("10 20\nHello\n");

        // 1) nextInt() 연속 호출
        int a = sc.nextInt(); // "10" 읽고 버퍼 위치를 ' ' 직후로 옮김
        int b = sc.nextInt(); // 앞에서 남은 공백/개행 스킵 → "20" 읽음

        // 2) 남은 개행은 남아 있지만, nextInt() 호출 시 스킵됐으므로 버퍼는 "Hello\n" 위치
        // sc.nextLine()을 호출하면 "Hello"를 읽고 개행(\n)까지 소비
        String line = sc.nextLine();

        System.out.printf("a=%d, b=%d, line=\"%s\"\n", a, b, line);
        sc.close();
    }


}
