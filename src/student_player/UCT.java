// File name: UCT

package student_player;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

/**
 * Bootleg AlphaGo Zero (no neural network)
 * for Pentago Swap
 * 
 * @author Le Nhat Hung
 *
 */
public class UCT {
	
	public static final int 
		REWARD = 1,
		PENALTY = -100;
	
	private MCTS mcts;
	private int playerTurn;
	
	public UCT (int playerTurn) { this.playerTurn = playerTurn; }
	
	public PentagoMove chooseMove (PentagoBoardState state, long simTime) {
		
		mcts = new MCTS(new Node(
			state,
			1 - playerTurn // The opponent made move at the root node
		));
		
		return mcts.chooseMove(state, simTime);
	}
	
	/**
	 * Monte Carlo Tree Search
	 * 
	 * @author Le Nhat Hung
	 *
	 */
	private class MCTS {
		
		private Node root;
		
		private MCTS (Node root) { this.root = root; }
		
		private PentagoMove chooseMove (PentagoBoardState state, long simTime) {
			Node node;
			long timeLimit;
			int winner;
			
			timeLimit = System.currentTimeMillis() + simTime;

			int simCount = 0;
			
			while (System.currentTimeMillis() < timeLimit) {
				
				// Expansion
				node = treePolicy();
				
				// Simulation
				winner = node.rollout();
				
				// Backpropagation
				node.backpropagate(winner);
			
				simCount++;
			}
			
			Utils.print("normal sims: " + simCount + "\n");
			
			// Selection
			return root.bestChild(.0).a();
		}
		
		private Node treePolicy () {
			Node curNode = root;
			
			while (! curNode.isTerminalNode()) {
				if (! curNode.isFullyExpanded())
					return curNode.expand();
				
				else
					curNode = curNode.bestChild(1.4);
			}
			return curNode;
		}
	}
	
	/**
	 * Node for MCTS
	 * 
	 * @author Le Nhat Hung
	 *
	 */
	private class Node {

		Node parent;
		List<Node> children;
		
		PentagoBoardState state;
		PentagoMove move;
		
		ArrayList<PentagoMove> untriedMoves;
		
		double moveValue; // Qsa
		
		int visitCount, // Nsa
			turn;
		
		public Node (PentagoBoardState state, int turn) {
			this.state = state;
			this.turn = turn;
			
			this.children = new ArrayList<Node>();
			this.visitCount = 0;
		}

		public Node (Node parent, PentagoBoardState state, PentagoMove move, int turn) {
			this.parent = parent;
			this.state = state;
			this.move = move;
			this.turn = turn;
			
			this.children = new ArrayList<Node>();
			this.visitCount = 0;
		}
		
		private Node expand () {
			PentagoMove move = getUntriedMoves().remove(0);
			PentagoBoardState curStateClone = s();
			Node child;
			
			curStateClone.processMove(move);
			
			child = new Node(
				this, // Parent
				curStateClone, // State
				move, // Action
				1 - turn // Opponent
			);
			addChild(child);
			
			return child;
		}
		
		private int rollout () {
			PentagoBoardState curRolloutState = s();
			PentagoMove move;
			
			while (! curRolloutState.gameOver()) {
				move = rolloutPolicy(curRolloutState);
				curRolloutState.processMove(move);
			}
			
			return curRolloutState.getWinner();
		}
		
		private PentagoMove rolloutPolicy (PentagoBoardState state) {
			return (PentagoMove) state.getRandomMove();
		}
		
		private void backpropagate (int winner) {
			updateUcb(winner);
			
			if ( hasParent() )
				parent.backpropagate(winner);
		}
		
		private Node bestChild(double cParam) {
			double[] ucbs = getUcbs(this, cParam);
			
			return children.get(Utils.argmax(ucbs));
		}
		
		private double[] getUcbs(Node curNode, double cParam) {
			int numChildren = curNode.children.size();
			double[] ucbs = new double[numChildren];
			Node child;
			
			for (int i = 0; i < numChildren; i++) {
				child = curNode.children.get(i);
				
				ucbs[i] = child.qsa()
						+ cParam
						* Math.sqrt( curNode.nsa() / child.nsa() );
			}
			return ucbs;
		}
		
		private boolean isTerminalNode () {
			return state.gameOver();
		}
		
		private boolean isFullyExpanded () {
			return getUntriedMoves().isEmpty();
		}
		
		private ArrayList<PentagoMove> getUntriedMoves () {
			if (untriedMoves == null)
				untriedMoves = state.getAllLegalMoves();
			
			return untriedMoves;
		}
		
		private void updateUcb (int winner) {
			updateNsa();
			updateQsa(winner);
		}
		
		private void updateQsa (int winner) {
			if ( wonSimulation(winner) )
				moveValue += (UCT.REWARD  - qsa()) / nsa();
			
			else
				moveValue += (UCT.PENALTY - qsa()) / nsa();
		}
		
		private boolean wonSimulation (int winner) { return winner == turn; } 
		
		private void updateNsa () { visitCount++; }
		
		private double qsa () { return moveValue; }
		
		private int nsa () { return visitCount; }
		
		private PentagoBoardState s () { return (PentagoBoardState) state.clone(); }
		
		private PentagoMove a () { return move; }
		
		private void addChild (Node child) { children.add(child); }
		
		private boolean hasParent () { return parent != null; }
		
	}
}
