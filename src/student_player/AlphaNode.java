package student_player;

import boardgame.BoardState;
import boardgame.Move;

public class AlphaNode extends Node {

	BoardState state;
	Move move;
	
	float
		prior, // Psa
		actionvalue; // Qsa
	
	int visitCount; // Nsa
	
	public AlphaNode () {
		super();
	}
	
	public AlphaNode (Node parent, BoardState state, Move move) {
		super(parent);
		this.state = state;
		this.move = move;
	}
}
