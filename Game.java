

/* Each game will have a distinct id and hold number of 
 * players in this game session,m */ 
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
public class Game {
	
	/* This staic int needs to be synchronoized and atomic to avoid other clients form getting a duplicate 
	 * id.  */ 
	static int gameId = 1; 
	int holes = 9; 
	int round =0; 
	ArrayList<Player> playersList; // can be accessed from other players outside game must be synchronized 
	Cards stock = new Cards();  // 
	Cards discard = new Cards(); 
	Player dealer; 
	
	
	
	
	public Game(int holes, Player dealer, ArrayList<Player>playersList) 
	{
		// Increment sync
		gameId++;
		this.holes = holes; 
		this.playersList = playersList;
		this.dealer = dealer;
		discard.getCards().clear();  
		
	}
	
	
	public ArrayList<Player> getPlayers() 
	{
		return this.playersList; 
	}
	
	/* Method starts the game and distrubtes 6 cards for each player 
	 * Remove cards from stock and set 1 card on discard pile 
	 * */ 
	public void initializeGame() 
	{
		// Shuffle the cards 
		ArrayList<Card> deckRef = stock.getCards(); 
		Collections.shuffle(deckRef);
		
		// now give each player 6 cards 
		for (int i = 0 ; i < playersList.size(); ++i) 
		{
			for(int j =0; j < 6 ; ++j) 
			{
				Card currCard = deckRef.removeFirst();
				currCard.setHeld(true);
				playersList.get(i).getDeck().add(currCard);
			}
			playersList.get(i).setState(gameState.IN_GAME);
			playersList.get(i).setInital(true);
		}	
		
		// Cards shuffeled and give now set up the discarded and stock deck 
		Card currCard = deckRef.removeFirst(); 
		currCard.setFace(true);
		this.discard.getCards().addFirst(currCard);
		
		
	}
	
	
	/* If all cards are faced up end the round */ 
	private boolean checkAllFacedUp() 
	{
		for(int i = 0; i < this.playersList.size(); ++i) 
		{
			if ( ! checkPlayerFacedUp(this.playersList.get(i))) 
			{
				return false; 
			} 
		}
		return true; 
	}
	
	/*  If all deck faced up return true */ 
	private boolean checkPlayerFacedUp(Player player) 
	{
		LinkedList<Card> playerDeck = player.getDeck(); 
		
		int  size = player.getDeck().size(); 
		for(int i =0; i < 6; ++i) 
		{
			if (!playerDeck.get(i).isFacedUp()) 
			{
				return false; 
			}
		}
		
		return true; 
	}
	
	public Player getDealer() 
	{
		return this.dealer; 
	}
	
	/* Start current round of the game , make sure to let the left player get their turn 
	 * End the round when all cards are faced up and calculate the score */ 
	public void startRound() 
	{
		
		int index=0; 
		// find dealer index 
		for (int i = 0 ; i < this.playersList.size(); ++i) 
		{
			if (this.playersList.get(i) == this.dealer) 
			{
				index = i - 1;
				break; 
			}
		}
		
		
		int size = this.playersList.size(); 
		do
		{
			int i =0; 
			do 
			{
				if(index < 0) 
				{
					index = size - 1; 
				}
				/* Current player takes this turn */ 
				this.playersList.get(index).takeTurn(stock.getCards(),discard.getCards(),this.playersList);
				index--;
				i++; 
			}
			while (i < size); 
			
		} // Check if everyones cards are faced up 
		while(!this.checkAllFacedUp()); 
		
		// Round is over now calculate the score of each player 
		for(int i =0; i < size; ++i) 
		{
			calculatePlayerScore(this.playersList.get(i)); 
		}
	}
	
	/* Reference player modify score for the round , make sure to increment prior round score */  
	private void calculatePlayerScore(Player player) 
	{
		LinkedList<Card> deck = player.getDeck(); 
		// get player deck check if corresponding columns have same value increment with 0 
		// otherwise their value. 
	
		// column 1 
		if(deck.get(0).getValue() == deck.get(3).getValue()) 
		{
			player.setScore( player.getScore() + 0); 
		}
		else 
		{
			player.setScore( player.getScore() + deck.get(0).getValue() + deck.get(3).getValue());
		}
		// column 2
		if (deck.get(1).getValue() == deck.get(4).getValue()) 
		{
			player.setScore( player.getScore() + 0);
		}
		else 
		{
			player.setScore( player.getScore() + deck.get(1).getValue() + deck.get(4).getValue());
		}
		// column 3
		if(deck.get(2).getValue() == deck.get(5).getValue())
		{
			player.setScore( player.getScore() + 0);
		}else 
		{
			player.setScore( player.getScore() + deck.get(2).getValue() + deck.get(5).getValue());
		}
	}
	
	
	/* Show current round score */ 
	public void showScore() 
	{
		int size = this.playersList.size(); 
		String name= ""; 
		String score = ""; 
		String round = "Round: " + this.round + "/" +this.holes ;
		String result = "| " + round + "|"; 
		Player currPlayer = null;
		// Iterate and show everyones score 
		for(int i =0 ; i < size; ++i ) 
		{
			currPlayer = this.playersList.get(i); 
			name = currPlayer.getName(); 
			score = String.valueOf( this.playersList.get(i).getScore()); 
			result += "\n" + name + " : " + "score : " + score; 
		}
		System.out.println(result); 
	}
	
	
	/* Get winner of the game  */ 
	public void getWinner() 
	{
		int size= this.playersList.size(); 
		int min = this.playersList.get(0).getScore(); 
		Player winner = this.playersList.get(0); 
		for(int i = 1; i < size; i++) 
		{
			if (winner.getScore() > this.playersList.get(i).getScore() ) 
			{
				winner = this.playersList.get(i); 
			}
		}
		
		String result = "Game Winner : " + winner.getName() + " With score of " + winner.getScore();
		System.out.println(result); 
	}
	
	/* Delete all players cards and create new deck of cards */  
	private void setUpNextRound() 
	{
		this.stock = new Cards(); 
		this.discard.getCards().clear();
		for (int i = 0; i < this.playersList.size(); ++i) 
		{
			this.playersList.get(i).getDeck().clear(); 
		}
	}
	
	private void startGame() 
	{
		
		// Game is turn based dealer go first they are always first in the list 
		for (int i = 0; i < this.holes ; ++i) 
		{
			// intialize the game 
			initializeGame();
			round++; 
			// start round 
			startRound(); 
			// show score
			showScore();
			// set up next round 
			setUpNextRound(); 
		}
		
		// show total score and winner of the game 
		getWinner(); 
	}
	
	/*Test the game make sure the turn based and game logic is working correctly */ 
	public static void main(String args[] ) 
	{
		ArrayList<Player> playersList = new ArrayList<Player>();
		playersList.add(new Player("Player1", "127.0.0.1", "5555", "4343", "dfdf", gameState.FREE));
		playersList.add(new Player("Player2", "127.0.0.1", "5555", "4343", "dfdf", gameState.FREE));
		
		// Initalize game 
		Game game = new Game(2,playersList.get(0),playersList); 
		// Now start the game 
		game.startGame();
		
	}
	
}
