package student_player;

import java.util.List;
import java.util.ArrayList;

public class Node {
	
	Node parent;
	List<Node> children;
	
	public Node () {
		children = new ArrayList<Node>();
	}
	
	public Node (Node parent) {
		super();
		this.parent = parent;
	}
	
	public Node (Node parent, List<Node> children) {
		super();
		this.parent = parent;
		this.children = children;
	}
	
	public List<Node> getChildren () {
		return children;
	}
	
	public Node getParent () {
		return this.parent;
	}
}
