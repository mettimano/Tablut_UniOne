package it.unibo.ai.didattica.competition.tablut.UniOne;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;


//stars = ("a2", "a3", "a7", "a8", "b1", "b9", "c1", "c9", "g1", "g9", "h1", "h9", "i2", "i3","i7", "i8");

public class Heuristic {
    private GameAshtonTablut game;
    private State state;
    private State.Turn turn;
    private State.Pawn[][] board;
    private Integer kingPosition[];
    private int[][] citadels = {{0, 3}, {0, 4}, {0, 5}, {1, 4}, {3, 0}, {4, 0}, {5, 0}, {4, 1}, {8, 3}, {8, 4},
            {8, 5}, {7, 4}, {3, 8}, {4, 8}, {5, 8}, {4, 7}};
    /*private int[][]stars= {{0, 1}, {0, 2}, {0, 6}, {0, 7}, {1, 0}, {1, 8}, {2, 0}, {2, 8}, {6, 0}, {6, 8}, {7, 0},
    		{7, 8}, {8, 1}, {8, 2}, {8, 6}, {8, 7}};
    */
    private int [][] nearThroneBoxes= {{3, 4}, {5, 4}, {4, 3},{4, 5},{4, 4}};

    private double numWhiteOnBoard = 0; //double to force double division
    private double numBlackOnBoard = 0;

    private final static int NUM_BLACK = 16;
    private final static int NUM_WHITE = 8;

    // White weights
    private final double[] whiteWeights = {5, 4, 50, 30,110};
    private final static int WHITE_REMAINING  = 0;
    private final static int BLACK_EATEN = 1;
    private final static int KING_IS_SAFE= 2;
    private final static int KING_HAS_OPEN_WAYS=3;
    //private final static int KING_ON_STAR=4;

    // Black weights
    private final double[] blackWeights = {2, 5, 2, 20,50,10,110,2};
    private final static int BLACK_REMAINING  = 0;
    private final static int WHITE_EATEN = 1;
    private final static int PROTECT_ESCAPES = 2;
    private final static int KING_HAS_OPEN_WAYS_B=3;
    private final static int KING_IS_NOT_SAFE=4;
    private final static int KING_IS_SAFE_B= 5;
    private final static int BLACKS_NEAR_KING=6;
    //private final static int KING_ON_STAR_B=6;

    public Heuristic(State state, State.Turn turn) {
        this.state = state;
        this.turn = turn;
        this.kingPosition= new Integer[2];
        this.board=state.getBoard();
    }

    public double evaluateState() {
        if(this.turn.equals(State.Turn.WHITE))
            return evaluateStateWhite();
        else
            return evaluateStateBlack();
    }

    private double evaluateStateWhite() {
        double value = 0;
        
        
        getNumPawns();
        
        value += whiteWeights[WHITE_REMAINING] * getWhiteRemaining();
        value += whiteWeights[BLACK_EATEN] * getBlackEaten();
       // if(KingIsOnStar())value += whiteWeights[KING_ON_STAR]*1.0;
        if(KingIsNearThrone()) {
        	if(!KingIsSafeNearThrone()) {
        	value-=whiteWeights[KING_IS_SAFE]*2.0;
        	}
        }
        else {
        	if(KingIsSafe()) {
        		if(KingOpenWays()>1)
        		value+=whiteWeights[KING_HAS_OPEN_WAYS]*5;
        		else value+=whiteWeights[KING_HAS_OPEN_WAYS]*2;
        	}
        	else value-=whiteWeights[KING_IS_SAFE]*2.0;
        	
        }

        return value;
    }

    private double evaluateStateBlack() {
        double value = 0;
        getNumPawns();

        value += blackWeights[BLACK_REMAINING] * getBlackRemaining();
        value += blackWeights[WHITE_EATEN] * getWhiteEaten();
        value += blackWeights[PROTECT_ESCAPES] * getBlackProtectingEscapes();
        value +=blackWeights[BLACKS_NEAR_KING]*getBlacksNearKing();
        //if(KingIsOnStar())value -= blackWeights[KING_ON_STAR_B]*1.0;
        
        if(KingIsNearThrone()) {
        	value-=blackWeights[KING_IS_SAFE_B]*5.0;
        	
        }
        else {
        value-=blackWeights[KING_HAS_OPEN_WAYS_B]*KingOpenWays();
        	/*if(KingIsSafe()) {
        		value-=blackWeights[KING_IS_SAFE_B]*5.0;
        	}
        	else value+=blackWeights[KING_IS_NOT_SAFE]*1.0;
        	*/
        }
        return value;
    }

    // counts the number of white and black pawns and adds the king position;
    public void getNumPawns() {
        for(int i = 0; i < this.state.getBoard().length; i++) {
            for (int j = 0; j < this.state.getBoard().length; j++) {
                if (this.state.getPawn(i, j).equals(State.Pawn.WHITE))
                    this.numWhiteOnBoard++;
                else if (this.state.getPawn(i, j).equals(State.Pawn.BLACK))
                    this.numBlackOnBoard++;
                else if (this.state.getPawn(i, j).equals(State.Pawn.KING)) {
                    this.kingPosition[0]=i;
                    this.kingPosition[1]=j;
                    this.numWhiteOnBoard++;
                }
            }
        }
    }

    public double getWhiteRemaining() {
        return this.numWhiteOnBoard;
    }

    public double getBlackEaten() {
        return (NUM_BLACK - this.numBlackOnBoard);
    }
    
    
    public int[] checkNearBoxes(State.Pawn color){
        int count[]= {-1, -1, -1, -1};
        int x=this.kingPosition[0];
        int y=this.kingPosition[1];
        String sColor=color.toString();
        if(this.board[x-1][y].equalsPawn(sColor) || isCitadel(x-1,y)|| this.board[x-1][y].equalsPawn(State.Pawn.THRONE.toString()))
            count[0]=1;
        else if (this.board[x][y+1].equalsPawn(State.Pawn.EMPTY.toString()))
        	count[0]=0;
        if(this.board[x+1][y].equalsPawn(sColor) || isCitadel(x+1,y)|| this.board[x+1][y].equalsPawn(State.Pawn.THRONE.toString()))
            count[1]=1;
        else if (this.board[x][y+1].equalsPawn(State.Pawn.EMPTY.toString()))
        	count[0]=0;
        if(this.board[x][y-1].equalsPawn(sColor)|| isCitadel(x,y-1)|| this.board[x][y-1].equalsPawn(State.Pawn.THRONE.toString()))
            count[2]=1;
        else if (this.board[x][y+1].equalsPawn(State.Pawn.EMPTY.toString()))
        	count[0]=0;
        if(this.board[x][y+1].equalsPawn(sColor)|| isCitadel(x,y+1)|| this.board[x][y+1].equalsPawn(State.Pawn.THRONE.toString()))
            count[3]=1;
        else if (this.board[x][y+1].equalsPawn(State.Pawn.EMPTY.toString()))
        	count[0]=0;
        return count;
    }


    public double getBlackRemaining() {
        return this.numBlackOnBoard;
    }

    public double getWhiteEaten() {
        return (NUM_WHITE - this.numWhiteOnBoard);
    }

    public int getBlacksNearKing() {
    	int count=0;
        int x=this.kingPosition[0];
        int y=this.kingPosition[1];
        if(this.board[x-1][y].equalsPawn(State.Pawn.BLACK.toString()))
            count++;
        if(this.board[x+1][y].equalsPawn(State.Pawn.BLACK.toString()) )
            count++;
       
        if(this.board[x][y-1].equalsPawn(State.Pawn.BLACK.toString()))
            count++;
        
        if(this.board[x][y+1].equalsPawn(State.Pawn.BLACK.toString()))
            count++;
        return count;
        
    }

    public int getBlackProtectingEscapes() {;
        int[][] protectPositions = {{1, 2}, {2, 1}, {6, 1}, {7, 2}, {1, 6}, {2, 7}, {6, 7}, {7, 6}};
        int num = 0;
        for(int[] position : protectPositions) {
            if(this.state.getPawn(position[0], position[1]).equals(State.Pawn.BLACK))
                num++;
        }
        return num;
    }
    

   /*
    public boolean kingIsReachable(int x,int y) {
    	int xK=kingPosition[0];
    	int yK=kingPosition[1];
    	int i=0;
    	
    
    	if(y==yK+1 && x<xK) {// pawn from left/above
    		for (i=x+1;i<=xK;i++) {
    			if(!state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())) {
    				return false;
    			}
    		}
    		return true;	
    	}
    	if(y==yK+1 && x>xK) {// pawn from right/above
    		for (i=x--;i>=xK;i--) {
    			if(!state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())) {
    				return false;
    			}
    		}
    		return true;	
    	}
    	if(x==xK+1 && y<yK) {// pawn from left/below
    		for (i=y+1;i<=yK;i++) {
    			if(!state.getPawn(x, i).equalsPawn(State.Pawn.EMPTY.toString())) {
    				return false;
    			}
    		}
    		return true;	
    	}
    	if(x==xK+1 && y>yK) {// pawn from right/below
    		for (i=y-1;i>=yK;i--) {
    			if(!state.getPawn(x, i).equalsPawn(State.Pawn.EMPTY.toString())) {
    				return false;
    			}
    		}
    		return true;
    	}
    	
    	
    	return true;	
    }
    */
    
    public int KingOpenWays() {
    	int openWays=4;
    	int x=kingPosition[0];
    	int y=kingPosition[1];
    	int i;
    	
    	for(i=0;i<x;i++) { 
    		if(!state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())
    				||isCitadel(i,y)) {
    			openWays--;
    			break;
    		}
    		
    	}
    	
    	for(i=0;i<y;i++) {
    		if(!state.getPawn(x, i).equalsPawn(State.Pawn.EMPTY.toString())
    				|| isCitadel(x,i)) {
    			openWays--;
    			break;
    		}
    	}
    	for(i=8;i>x;i--) {
    		if(!state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())
    				|| isCitadel(i,y)) {
    			openWays--;
    			break;
    		}
    	}
    	for(i=8;i>y;i--) {
    		if(!state.getPawn(x, i).equalsPawn(State.Pawn.EMPTY.toString())
    				|| isCitadel(x,i)) {
    			openWays--;
    			break;
    		}
    	}
    	
    	return openWays;
    }

    
    
    public boolean KingIsSafe() {

    	int nearThreat[]=checkNearBoxes(State.Pawn.BLACK);
    	int i;
    	int x= kingPosition[0];
    	int y= kingPosition[1];
    	if(nearThreat[0]==1) {// there is a threat from the left
    		for(i=x+2;i<9;i++) {
    			if(state.getPawn(i, y).equalsPawn(State.Pawn.BLACK.toString())) { // king can be captured by a black in his the right
    				return false;
    			}
    			else if(isCitadel(i,y) || !state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    		}
    		for(i=y-1;i>=0;i--) {
    			if(state.getPawn(x+1,i).equalsPawn(State.Pawn.BLACK.toString())) { //x+1 form below
    				return false;
    			}
    			else if(isCitadel(x+1,i) || !state.getPawn(x+1, i).equalsPawn(State.Pawn.EMPTY.toString()) ) {
    				break;
    			}
    			
    		}
    		for(i=y+1;i<9;i++) {
    			if(state.getPawn(x+1,i).equalsPawn(State.Pawn.BLACK.toString())) { //x+1 from above
    				return false;
    			}
    			else if(isCitadel(x+1,i) || !state.getPawn(x+1, i).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    			
    		}
    		
    	}
    	if(nearThreat[1]==1) { // there is a threat form the right
    		for(i=x-2;i>=0;i++) {
    			if(!state.getPawn(i, y).equalsPawn(State.Pawn.BLACK.toString())) { // king can be captured by a black in his left
    				return false;
    			}
    			else if(isCitadel(i,y) ||!state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    		}
    		for(i=y-1;i>=0;i--) {
    			if(state.getPawn(x-1,i).equalsPawn(State.Pawn.BLACK.toString())) { //x-1 form below
    				return false;
    			}
    			else if(isCitadel(x-1,i) || !state.getPawn(x-1, i).equalsPawn(State.Pawn.EMPTY.toString()) ) {
    				break;
    			}
    			
    		}
    		for(i=y+1;i<9;i++) {
    			if(state.getPawn(x-1,i).equalsPawn(State.Pawn.BLACK.toString())) { //x-1 from above
    				return false;
    			}
    			else if(isCitadel(x-1,i) || !state.getPawn(x-1, i).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    			
    		}
    		
    	}
    	if(nearThreat[2]==1) { // there is a threat form below
    		for(i=y+1;i<9;i++) {
    			if(!state.getPawn(x, i).equalsPawn(State.Pawn.BLACK.toString())) {
    				return false;
    			}
    			else if(isCitadel(x,i) || !state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    			
    		}
    		for(i=x-1;i>=0;i--) {
    			if(state.getPawn(i,y+1).equalsPawn(State.Pawn.BLACK.toString())) { //y+1 form below
    				return false;
    			}
    			else if(isCitadel(i,y+1) || !state.getPawn(i, y+1).equalsPawn(State.Pawn.EMPTY.toString()) ) {
    				break;
    			}
    			
    		}
    		for(i=x+1;i<9;i++) {
    			if(state.getPawn(i,y+1).equalsPawn(State.Pawn.BLACK.toString())) { //y+1 from above
    				return false;
    			}
    			else if(isCitadel(i,y+1) || !state.getPawn(i, y+1).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    			
    		}
    		
    	}
    	if(nearThreat[3]==1) { // there is a threat form above
    		for(i=y-2;i>=0;i++) {
    			if(!state.getPawn(x, i).equalsPawn(State.Pawn.BLACK.toString())) {
    				return false;
    			}
    			else if(isCitadel(x,i) ||!state.getPawn(i, y).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    		}
    		for(i=x-1;i>=0;i--) {
    			if(state.getPawn(i,y-1).equalsPawn(State.Pawn.BLACK.toString())) { //y-1 form below
    				return false;
    			}
    			else if(isCitadel(i,y-1) || !state.getPawn(i, y-1).equalsPawn(State.Pawn.EMPTY.toString()) ) {
    				break;
    			}
    			
    		}
    		for(i=x+1;i<9;i++) {
    			if(state.getPawn(i,y-1).equalsPawn(State.Pawn.BLACK.toString())) { //y-1 from above
    				return false;
    			}
    			else if(isCitadel(i,y-1) || !state.getPawn(i, y-1).equalsPawn(State.Pawn.EMPTY.toString())) {
    				break;
    			}
    			
    		}
    	}
    	return true;

    }
    
    public boolean KingIsSafeNearThrone() {
    	int nearThreat[]=checkNearBoxes(State.Pawn.BLACK);
    	int x= kingPosition[0];
    	int y= kingPosition[1];
    	if(x==4 && y==4) { // king on throne
    		if((nearThreat[0]+nearThreat[1]+nearThreat[2]+nearThreat[3])<3) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	if((nearThreat[0]+nearThreat[1]+nearThreat[2]+nearThreat[3]+1)<2) {
			return true;
		}
		else {
			return false;
		}
    	
    	
    }
   /* public boolean KingIsOnStar() {
    	int x=kingPosition[0];
    	int y=kingPosition[1];
    	
    	for (int[] star : stars) {
            if (star[0] == x && star[1] == y)
                return true;
        }
        return false;
    }*/
    
    public boolean KingIsNearThrone(){
    	int x= kingPosition[0];
    	int y= kingPosition[1];
    	for (int[] kingBox : nearThroneBoxes) {
            if (kingBox[0]== x && kingBox[1] == y) {
                return true;
            }
        }
    	return false;
    }

    public boolean isCitadel(int x, int y) {

        for (int[] citadel : citadels) {
            if (citadel[0] == x && citadel[1] == y)
                return true;
        }
        return false;
    }
    
    
}
