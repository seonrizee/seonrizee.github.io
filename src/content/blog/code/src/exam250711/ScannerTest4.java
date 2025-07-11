package exam250711;

import java.util.Scanner;

public class ScannerTest4 {

    public static void main(String[] args) {
        // 스캐너에 "10\n20\nHello\n" 데이터가 들어있다고 가정
        Scanner sc = new Scanner("10\n20\nHello\n");

        // 1) nextInt() 호출
        // "10"을 읽고, 커서는 "10" 바로 뒤('\n' 앞)에 위치함.
        // 버퍼 상태: "\n20\nHello\n"
        int a = sc.nextInt();

        // 2) 두 번째 nextInt() 호출
        // 시작하기 전, 앞에 있는 구분자('\n')를 스킵함.
        // 그 다음 "20"을 읽고, 커서는 "20" 바로 뒤('\n' 앞)에 위치함.
        // 버퍼 상태: "\nHello\n"
        int b = sc.nextInt();

        // 3) nextLine() 호출
        // 현재 커서 위치부터 다음 줄바꿈 문자('\n')까지 읽음.
        // 내용은 없으므로 빈 문자열("")을 반환하고, 줄바꿈 문자('\n')를 소비(제거)함.
        // 버퍼 상태: "Hello\n"
        String line = sc.nextLine();

        System.out.printf("a=%d, b=%d, line=\"%s\"\n", a, b, line); // line은 "" (빈 문자열)
        sc.close();
    }

}
