package day5;

public class MethodTest3 {
	public static void main(String[] args) {
		System.out.println("main() 수행시작");
		printMyName(1, '*'); // 아규먼트 필수
		printMyName(2, '@'); // 아규먼트 필수
		printMyName(3, '#'); // 아규먼트 필수
		System.out.println("main() 수행종료");
	}
	static void printMyName(int num, char deco) { // 매개변수가 존재함
		for(int i=1; i<=num; i++) {
			System.out.println(deco + "Repeat" + deco);
		}
	}
}
