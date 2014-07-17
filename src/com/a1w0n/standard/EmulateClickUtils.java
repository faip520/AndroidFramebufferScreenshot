package com.a1w0n.standard;

public class EmulateClickUtils {
	
	private static final String leftButtonClick = "sendevent /dev/input/event0 4 4 36865\n"
			+ "sendevent /dev/input/event0 1 272 1\n"
			+ "sendevent /dev/input/event0 0 0 0\n"
			+ "sendevent /dev/input/event0 4 4 36865\n"
			+ "sendevent /dev/input/event0 1 272 0\n"
			+ "sendevent /dev/input/event0 0 0 0\n";
	
	
	private static final String moveToLeftEdge = "sendevent /dev/input/event0 2 0 4294963200\n"
			+ "sendevent /dev/input/event0 0 0 0\n";
	
	private static final String moveToTopEdge = "sendevent /dev/input/event0 2 1 4294963200\n"
			+ "sendevent /dev/input/event0 0 0 0\n";
	
	private EmulateClickUtils() {
	}
	
	private static String getMoveToTargetCommand(int x, int y) {
		return "sendevent /dev/input/event0 2 0 " + x + "\n"
				+ "sendevent /dev/input/event0 0 0 0\n"
				+ "sendevent /dev/input/event0 2 1 " + y + "\n"
				+ "sendevent /dev/input/event0 0 0 0\n";
	}
	
	public static void emulateClick(int x, int y) {
		CMDUtils.runWithRoot(moveToLeftEdge + moveToTopEdge + getMoveToTargetCommand(x, y)
				+ leftButtonClick);
	}
	
}
