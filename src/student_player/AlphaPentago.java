// File name: AlphaPentago

package student_player;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

/**
 * AlphaGo Zero for Pentago Swap
 * 
 * @author Le Nhat Hung
 *
 */
public class AlphaPentago {
	
	public static final int WIN_REWARD = 100;
	private static int playerTurn;
	
	private MCTS mcts;
	private int numSims;
	
	public AlphaPentago (int playerTurn, int numSims) {
		this.playerTurn = playerTurn;
		this.numSims = numSims;
	}
	
	public PentagoMove chooseMove (PentagoBoardState state) {
		mcts = new MCTS(new Node(state));
		
		return mcts.chooseMove(state, numSims);
	}
	
	/**
	 * Monte Carlo Tree Search
	 * 
	 * @author Le Nhat Hung
	 *
	 */
	public class MCTS {
		
		private Node root;
		
		public MCTS (Node root) {
			this.root = root;
		}
		
		public PentagoMove chooseMove (PentagoBoardState state, int numSims) {
			Node node;
			int winner;
			
			for (int i = 0; i < numSims; i++) {
				node = treePolicy();
				winner = node.rollout();
				node.backpropagate(winner);
			}
			
			return root.bestChild(.0).a();
		}
		
		public Node treePolicy () {
			Node curNode = root;
			
			while (! curNode.isTerminalNode()) {
				if (! curNode.isFullyExpanded())
					return curNode.expand();
				
				else
					curNode = curNode.bestChild(1.4);
			}
			return curNode;
		}
		
		public void setRoot (Node node) {
			root = node;
		}
	}
	
	/**
	 * Node for MCTS
	 * 
	 * @author Le Nhat Hung
	 *
	 */
	public class Node {

		Node parent;
		List<Node> children;
		
		PentagoBoardState state;
		PentagoMove move;
		
		ArrayList<PentagoMove> untriedMoves;
		
		double moveValue; // Qsa
		
		int visitCount; // Nsa
		
		public Node (PentagoBoardState state) {
			this.children = new ArrayList<Node>();
			this.state = state;
			this.visitCount = 0;
		}

		public Node (Node parent, PentagoBoardState state, PentagoMove move) {
			this.children = new ArrayList<Node>();
			this.parent = parent;
			
			this.state = state;
			this.move = move;
			this.visitCount = 0;
		}
		
		public Node expand () {
			PentagoMove move = untriedMoves().remove(0);
			PentagoBoardState curStateClone = s();
			
			curStateClone.processMove(move);
			
			Node childNode = new Node(
				this, // Parent node
				curStateClone, // State
				move // Move/action
			);
			
			children.add(childNode);
			return childNode;
		}
		
		public int rollout () {
			PentagoBoardState curRolloutState = s();
			PentagoMove move;
			
			while (! curRolloutState.gameOver()) {
				move = rolloutPolicy(curRolloutState);
				curRolloutState.processMove(move);
			}
			
			return curRolloutState.getWinner();
		}
		
		public PentagoMove rolloutPolicy (PentagoBoardState state) {
			return (PentagoMove) state.getRandomMove();
		}
		
		public void backpropagate (int winner) {
			updateNsa();
			updateQsa(winner, AlphaPentago.WIN_REWARD);
			
			if (parent != null)
				((Node) parent).backpropagate(winner);
		}
		
		public Node bestChild(double cParam) {
			double[] ucts = getUcts(this, cParam);
			
			return (Node) children.get(Utils.argmax(ucts));
		}
		
		public double[] getUcts(Node curNode, double cParam) {
			int numChildren = curNode.children.size();
			double[] ucts = new double[numChildren];
			Node child;
			
			for (int i = 0; i < numChildren; i++) {
				child = (Node) curNode.children.get(i);
				ucts[i] = child.qsa() / child.nsa()
						+ cParam * Math.sqrt( curNode.nsa() / (child.nsa() + 1) );
			}
			return ucts;
		}
		
		public boolean isTerminalNode () {
			return state.gameOver();
		}
		
		public boolean isFullyExpanded () {
			return untriedMoves().isEmpty();
		}
		
		public ArrayList<PentagoMove> untriedMoves () {
			if (untriedMoves == null)
				untriedMoves = state.getAllLegalMoves();
			
			return untriedMoves;
		}
		
		public void updateQsa (int winner, int reward) {
			if (winner == AlphaPentago.playerTurn)
				moveValue += ( reward - qsa()) / nsa();
			else
				moveValue += (-reward - qsa()) / nsa();
		}
		
		public void updateNsa () {
			visitCount++;
		}
		
		public int nsa () {
			return visitCount;
		}
		
		public double qsa () {
			return moveValue;
		}
		
		public PentagoBoardState s () {
			return (PentagoBoardState) state.clone();
		}
		
		public PentagoMove a () {
			return move;
		}
	}
}
