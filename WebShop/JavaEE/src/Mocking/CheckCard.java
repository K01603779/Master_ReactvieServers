package Mocking;

import java.util.Random;

public class CheckCard {
	static Random random = new Random(34453434);
	public static Boolean isValidCard(String card) {
		return random.nextInt(1000)%10 != 1;
	}

}
