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
	
	public AlphaPentago (PentagoBoardState state, int playerTurn, int numSims) {
		//mcts = new MCTS(new Node(state));
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
			Node node, bestChild;
			int winner;
			
			for (int i = 0; i < numSims; i++) {
				node = treePolicy();
				winner = node.rollout();
				Utils.print("simulated");
				node.backpropagate(winner);
				Utils.print("root qsa:");
				Utils.print(root.qsa()); // maybe check qsa() in backpropagate() too
				Utils.print(i);
			}
			
			//setRoot(node);
			//return node.a();
			bestChild = root.bestChild(.0);
			//setRoot(bestChild);
			return bestChild.a();
		}
		
		public Node treePolicy () {
			Node curNode = root; // maybe get rid of curNode and use just root
			Node bestChild;
			
			while (! curNode.isTerminalNode()) {
				if (! curNode.isFullyExpanded()) {
					return curNode.expand();
				
				} else {
					Utils.print("node fully expanded, choosing best child");
					bestChild = curNode.bestChild(1.4);
					
					Utils.print(curNode.children.contains(bestChild));
					Utils.print(bestChild.isTerminalNode());
					Utils.print(bestChild.isFullyExpanded());
					
					curNode = bestChild;
				}
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
		
		double
			prior, // Psa
			moveValue; // Qsa
		
		int visitCount; // Nsa
		
		public Node (PentagoBoardState state) {
			this.children = new ArrayList<Node>();
			this.state = state;
			this.visitCount = 0;
		}

		public Node (Node parent, PentagoBoardState state, PentagoMove move) {
			this.parent = parent;
			this.children = new ArrayList<Node>();
			this.state = state;
			this.move = move;
			this.visitCount = 0;
		}
		
		public Node expand () {
			Utils.print("num untried moves");
			Utils.print(untriedMoves().size());
			PentagoMove move = untriedMoves().remove(0);
			PentagoBoardState curStateClone = (PentagoBoardState) state.clone();
			
			curStateClone.processMove(move);
			curStateClone.printBoard();
			
			Node childNode = new Node(
				this, // Parent node
				curStateClone, // State
				move // Move/action
			);
			children.add(childNode);
			
			return childNode;
		}
		
		public int rollout () {
			PentagoBoardState curRolloutState = (PentagoBoardState) state.clone();
			//PentagoBoardState curRolloutState = state;
			//ArrayList<PentagoMove> legalMoves;
			PentagoMove move;
			
			//Utils.print("called rollout");
			
			while (! curRolloutState.gameOver() ) {
				//legalMoves = curRolloutState.getAllLegalMoves();
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
		
		public PentagoMove a () {
			return move;
		}
	}
}
