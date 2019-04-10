package student_player;

public class Utils {

	public static void print (Object o) {
    	System.out.println(o);
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
}
