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
		REWARD = 1,
		PENALTY = -100;
	
	private MCTS mcts;
	private int playerTurn;
	
	public PersistentUCT (PentagoBoardState state, int playerTurn) {
		mcts = new MCTS(new Node(
			state,
			1 - playerTurn
		));
		this.playerTurn = playerTurn;
	}
	
	public PentagoMove chooseMove (PentagoBoardState state, long simTime) {
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
		
		public PentagoMove chooseMove (PentagoBoardState state, long simTime) {
			Node node, bestChild;
			int winner;
			long startTime = System.nanoTime();
			long timeLimit = System.currentTimeMillis() + simTime;
			
			if ( root.hasChildren() ) {
				boolean stateFound = false;
				
				Utils.print("root children not empty\n");
				for (Node c : root.children) {
					if (Utils.areSameState(c.state, state)) {
						setRoot(c);
						stateFound = true;
						Utils.print("Same state found!\n");
						break;
					}
				}
				
				if (! stateFound) {
					root = new Node(state, 1 - playerTurn);
				}
			}
			
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
			// Selection
			bestChild = root.bestChild(.0);
			setRoot(bestChild);
			
			long endTime = System.nanoTime();
			long timeElapsed = endTime - startTime;
			
			Utils.print("persistent sims: " + simCount + "\n");
			Utils.print("Elapsed time (ms):\n");
			Utils.print(timeElapsed / 1000000 + "\n");
			
			return bestChild.a();
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
		
		private void setRoot (Node node) { root = node; }
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
			updateUcb(winner);
			
			if ( hasParent() )
				parent.backpropagate(winner);
		}
		
		public Node bestChild(double cParam) {
			double[] ucbs = getUcbs(this, cParam);
			
			return children.get(Utils.argmax(ucbs));
		}
		
		public double[] getUcbs(Node curNode, double cParam) {
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
		
		public boolean isTerminalNode () {
			return state.gameOver();
		}
		
		public boolean isFullyExpanded () {
			return getUntriedMoves().isEmpty();
		}
		
		public ArrayList<PentagoMove> getUntriedMoves () {
			if (untriedMoves == null)
				untriedMoves = state.getAllLegalMoves();
			
			return untriedMoves;
		}
		
		private void updateUcb (int winner) {
			updateNsa();
			updateQsa(winner);
		}
		
		public void updateQsa (int winner) {
			if ( wonSimulation(winner) )
				moveValue += (PersistentUCT.REWARD  - qsa()) / nsa();
			else
				moveValue += (PersistentUCT.PENALTY - qsa()) / nsa();
		}
		
		private boolean wonSimulation (int winner) { return winner == turn; }
		
		public void updateNsa () { visitCount++; }
		
		public double qsa () { return moveValue; }
		
		public int nsa () { return visitCount; }
		
		public PentagoBoardState s () { return (PentagoBoardState) state.clone(); }
		
		public PentagoMove a () { return move; }
		
		public boolean hasParent () { return parent != null; }
		
		public void addChild (Node child) { children.add(child); }
		
		public boolean hasChildren () {
			return !( children.isEmpty() || children == null );
		}
	}
}
