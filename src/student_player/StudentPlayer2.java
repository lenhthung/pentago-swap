package student_player;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;

/** A player file submitted by a student. */
public class StudentPlayer2 extends PentagoPlayer {

	private static String
		studentNumber = "persistent";
	
	private PersistentUCT ai;
	private final int NUM_SIMS = 5000;
	
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer2() {
        super(studentNumber);
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove (PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
 
    	if (ai == null)
    		ai = new PersistentUCT(
    			boardState.getTurnPlayer(), // Colour of player (BLACK or WHITE)
    			NUM_SIMS
    		); 
		
    	Move myMove = ai.chooseMove(boardState);
		
        // Return your move to be processed by the server.
        return myMove;
    }
}
