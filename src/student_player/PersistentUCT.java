// File name: PersistentUCT

package student_player;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

/**
 * Persistent UCT for Pentago Swap
 * 
 * @author Le Nhat Hung
 *
 */
public class PersistentUCT {
	
	public static final int
		REWARD = 100,
		PENALTY = -100;
	
	private static int playerTurn;
	
	private MCTS mcts;
	private int numSims;
	
	public PersistentUCT (PentagoBoardState state, int playerTurn, int numSims) {
		mcts = new MCTS(new Node(state));
		this.playerTurn = playerTurn;
		this.numSims = numSims;
	}
	
	public PentagoMove chooseMove (PentagoBoardState state, long simTime) {
		return mcts.chooseMove(state, numSims, simTime);
	}
	
	/**
	 * Monte Carlo Tree Search
	 * 
	 * @author Le Nhat Hung
	 *
	 */
	public class MCTS {
		
		private Node root;
		
		public MCTS (Node root) { this.root = root; }
		
		public PentagoMove chooseMove (PentagoBoardState state, int numSims, long simTime) {
			Node node, bestChild;
			int winner;
			long startTime = System.nanoTime();
			long timeLimit = System.currentTimeMillis() + simTime;
			
			if ( root.hasChildren() ) {
				boolean stateFound = false;
				
				Utils.print("root children not empty");
				for (Node c : root.children) {
					if (Utils.areSameState(c.state, state)) {
						setRoot(c);
						stateFound = true;
						Utils.print("Same state found!");
						break;
					}
				}
				
				if (! stateFound) {
					root = new Node(state);
				}
			}
			
			//for (int i = 0; i < numSims; i++) {
			while (System.currentTimeMillis() < timeLimit) {
				node = treePolicy();
				winner = node.rollout();
				node.backpropagate(winner);
			}
			
			bestChild = root.bestChild(.0);
			setRoot(bestChild);
			
			long endTime = System.nanoTime();
			long timeElapsed = endTime - startTime;
			
			Utils.print("Elapsed time (ms):");
			Utils.print(timeElapsed / 1000000);
			
			return bestChild.a();
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
		
		public void setRoot (Node node) { root = node; }
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

		public Node (PentagoBoardState state, PentagoMove move) {
			this.children = new ArrayList<Node>();
			this.state = state;
			this.move = move;
			this.visitCount = 0;
		}
		
		public Node expand () {
			PentagoMove move = untriedMoves().remove(0);
			PentagoBoardState curStateClone = s();
			Node child;
			
			curStateClone.processMove(move);
			
			child = new Node(
				curStateClone, // State
				move // Move/action
			);
			child.setParent(this);
			addChild(child);
			
			return child;
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
			updateQsa(winner);
			
			if ( hasParent() )
				parent.backpropagate(winner);
		}
		
		public Node bestChild(double cParam) {
			double[] ucts = getUcts(this, cParam);
			
			return children.get(Utils.argmax(ucts));
		}
		
		public double[] getUcts(Node curNode, double cParam) {
			int numChildren = curNode.children.size();
			double[] ucts = new double[numChildren];
			Node child;
			
			for (int i = 0; i < numChildren; i++) {
				child = curNode.children.get(i);
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
		
		public void updateQsa (int winner) {
			if (winner == PersistentUCT.playerTurn)
				moveValue += (PersistentUCT.REWARD  - qsa()) / nsa();
			else
				moveValue += (PersistentUCT.PENALTY - qsa()) / nsa();
		}
		
		public double qsa () { return moveValue; }
		
		public void updateNsa () { visitCount++; }
		
		public int nsa () { return visitCount; }
		
		public PentagoBoardState s () { return (PentagoBoardState) state.clone(); }
		
		public PentagoMove a () { return move; }
		
		public void setParent (Node parent) { this.parent = parent; }
		
		public boolean hasParent () { return parent != null; }
		
		public void addChild (Node child) { children.add(child); }
		
		public boolean hasChildren () {
			return !( children.isEmpty() || children == null );
		}
	}
}
