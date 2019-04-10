package student_player;

public class Test {
	
	public static void main (String[] args) {
		Node parent = new Node();
		Node child = new Node(parent);
		print(parent);
		print(child.getParent());
		print((float)1/2);
	}

	public static void print (Object o) {
    	System.out.println(o);
    }
}
