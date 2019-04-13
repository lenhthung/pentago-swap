package student_player;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import pentago_swap.PentagoBoardState;

public class Utils {

	public static void print (Object o) {
    	System.out.print(o);
    }
	
	public static int argmax (double[] elems) {
		int bestIdx = -1;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < elems.length; i++) {
			double elem = elems[i];
			if (elem > max) {
				max = elem;
				bestIdx = i;
			}
		}
		return bestIdx;
    }
	
	public static boolean areSameState (PentagoBoardState s1, PentagoBoardState s2) {
		return boardString(s1).equals(boardString(s2));
	}
	
	public static String boardString (PentagoBoardState s) {
		
		// Create a stream to hold the output
		ByteArrayOutputStream boardString = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(boardString);
		
		PrintStream old = System.out;
		// Tell Java to use special stream
		System.setOut(ps);
		
		// Print output: goes to special stream
		s.printBoard();
		
		// Put things back
		System.out.flush();
		System.setOut(old);
		
		return boardString.toString();
	}
}
