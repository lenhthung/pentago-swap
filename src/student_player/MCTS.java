// File name: AlphaPentago

package student_player;

import boardgame.Move;
import pentago_swap.PentagoBoardState;

/**
 * Monte Carlo Tree Search
 * 
 * @author Le Nhat Hung
 *
 */
public class MCTS {

	private int maxSims;
	
	public MCTS (int maxSims) {
		this.maxSims = maxSims;
	}
	
	public Move chooseMove (PentagoBoardState boardState) {
		
		AlphaNode root = new AlphaNode();
		Tree tree = new Tree(root);
		
		for (int i = 0; i < maxSims; i++) {
			select();
			expand();
			simulate();
			update();
		}
		
		return new Move();
	}
	
	public AlphaNode select() {
		return
	}
	
	public void expand(){}
	public void simulate(){}
	public void update(){}
}
