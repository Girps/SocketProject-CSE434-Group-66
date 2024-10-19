

/* Each game will have a distinct id and hold number of 
 * players in this game session,m */ 
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
public class Game implements Runnable {
	
	/* This staic int needs to be synchronoized and atomic to avoid other clients form getting a duplicate 
	 * id.  */ 
	volatile int gameId = 1; 
	volatile int holes = 9; 
	volatile int round =0; 
	volatile ArrayList<Player> playersList; // can be accessed from other players outside game must be synchronized 
	Cards stock = new Cards();  // 
	Cards discard = new Cards(); 
	volatile Player dealer; 
	volatile LinkedList<Player> moves = new LinkedList<Player>(); // while store moves the player has made 
	static DatagramSocket sSocket = null; 
	
	Object lock = new Object(); 
	
	
	public Game(int gameId , int holes, Player dealer, ArrayList<Player>playersList , DatagramSocket sSocket) 
	{
		// Increment sync
		this.gameId= gameId; 
		this.holes = holes; 
		this.playersList = playersList;
		this.dealer = dealer;
		discard.getCards().clear();  
		this.sSocket = sSocket; 
	}
	
	
	/* Needs to be synchronized */ 
	public void addMove(Player playerMov) 
	{
		moves.add(playerMov); 
	}
	
	
	public boolean isRoundOver() 
	{
		return checkAllFacedUp(); 
	}
	
	
	// Print players 
	public String printPlayers() 
	{
		String result= ""; 
		synchronized(this.playersList) 
		{ 
			for(int i =0 ; i < this.playersList.size(); ++i) 
			{
				result += this.playersList.get(i).getName() + " "; 
			}
		}
				
		return result; 
	}
	
	/* Method , changes state of game and player depending on the move value */ 
	public int processMove(Player currPlayer, int index) 
	{
		String[] move = currPlayer.getCommand().split("\\|"); 
		Player movPlayer = null; 
		// get player 
		for (int i  =0; i < this.playersList.size(); ++i ) 
		{
			if(currPlayer.getName().equalsIgnoreCase(playersList.get(i).getName())) 
			{
				movPlayer = playersList.get(i); 
				break; 
			}
		}
		
		// Got the player now do turn 
		switch(move[0]) 
		{
			case "FLIP": // end turning move
			{ 
				// Player is fliping 2 cards faced
				int cardI = Integer.valueOf(move[1]); 
				int cardJ = Integer.valueOf(move[3]); 
				movPlayer.getDeck().get(cardI - 1 ).setFace(true);
				movPlayer.getDeck().get(cardJ - 1).setFace(true); 
				index--; 
			}
				break; 
			case "DISCARD": // end turn move 
			{ 
				int cardI = -1 ; 
				// Player is swaping a discard card in this case forced to swap 
				if (move[1].equals("Q")) 
				{
					// this case just do nothing send player a message 
					String msg = "\n"+ currPlayer.getName() + " placed discard card back\n"; 
					sendMessage(currPlayer,"MESSAGE", msg);
					index--; 
				}
				else 
				{
					cardI = Integer.valueOf(move[1]); 
					movPlayer.swap(this.stock.getCards(), this.discard.getCards(), "DISCARD", cardI-1);
				 // print all cards send it back to the player 
					 index--; 
				}
			}
				break; 
			case "STOCK": // end turn move
			{
				// Check if Q was selected 
				if (move[1].equals("Q")) 
				{ 
					// take stock out and put it on the discard pile remaining face up
					Card picked= this.stock.getCards().removeFirst(); 
					picked.setFace(true);
					this.discard.getCards().addFirst(picked); // add it to discard pile faced up 
					String msg = "\n"+ currPlayer.getName() + " placed stock card on discard pile\n"; 
					sendMessage(currPlayer,"MESSAGE", msg);
				} 
				else 
				{
					// player is swaping for stock card
					int cardI = Integer.valueOf(move[1]); 
					movPlayer.swap(this.stock.getCards(), this.discard.getCards(), "STOCK", cardI-1);
					index--;
				}
			}
			break; 
			case "FACEUP": // not an end turning move 
			{
				// face up the stock card for the player 
				Card stCard =this.stock.getCards().get(0);
				stCard.setFace(true);
			}
				break; 
			case "STEAL": // end turn move
			{
				int cardI = Integer.valueOf(move[1]) - 1; 
				int cardJ = Integer.valueOf(move[3]) - 1;
				
				String name = move[2]; 
				for(int i =0; i < this.playersList.size(); ++i ) 
				{
					if ( this.playersList.get(i).getName().equals(name)) 
					{
						Player victim = this.playersList.get(i); 
						// swap card with this player 
						movPlayer.stealSwap(victim, cardI,cardJ);
						// send a message 
						String msg = movPlayer.getName() + " stole from " + victim.getName(); 
						
						for(int j =0;j < this.playersList.size(); ++j ) 
						{
							sendMessage(this.playersList.get(j),"MESSAGE", msg);
						}
						
						break; 
					}
				}
				index--;
			}
			default: 
				System.out.println("INVALID TERM CAUGHT BY GAME SERVER"); 
				break; 
		}
		
		// now print message to all other players 
		String info = "\nStock:" + this.stock.getCards().getFirst() + "\nDiscard:" + this.discard.getCards().getFirst() 
				+"\n\n"+ movPlayer.printCards(playersList); 
		// print this move to all players 
		for (int i = 0; i < this.playersList.size() ; ++i) 
		{
			sendMessage(this.playersList.get(i), "MESSAGE" , info); 
		}
		return index; 
	} 
	
	/*
	 *  Display purposes only not for actions 
	 * 
	 * */ 
	public void sendMessage(Player receiver, String header, String message) 
	{
		int magic_constant = 10; 
		try 
		{
			// iterate create a socket and message other players 
			String result = header + "|" + message; 
			InetAddress ip = InetAddress.getByName(receiver.getRIP()); 
			int port = Integer.valueOf( receiver.getPPort()); 
			receiver.setMessage(result);
			byte[] sendData;
			sendData = Tracker.constructObject(receiver);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip, port+magic_constant);  
			sSocket.send(sendPacket); // send socket  
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}; 
	
	public int getRounds() 
	{
		return this.holes; 
	}
	
	public void setId(int gameId) 
	{
		this.gameId= gameId; 
	}
	
	public int getId() 
	{
		return gameId; 
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
			playersList.get(i).setGameId(this.gameId);
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
		
		/* Game starts here it blocks loop so I must change this into a series of state machines */ 
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
				try 
				{	
					this.playersList.get(index).takeTurn(stock.getCards(),discard.getCards(),this.playersList);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	public String showScore() 
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
		return result; 
	}
	
	
	/* Get winner of the game  */ 
	public String getWinner() 
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
		return result; 
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
	
	/* Corresponding player to the index is notified of their turn sent to display thread and another packet sent to 
	 * action thread which will get player input */ 
	public void makeTurn(int index, boolean turnNotify) 
	{
		// size 
		int size = this.playersList.size(); 	
	
			// ensure only notified once 
			if (!turnNotify) 
			{ 
				// current player takes turn 
				Player currPlayer = this.playersList.get(index); 
				currPlayer.takeTurnNotif();
				// Notify player to do Flip play
				if (currPlayer.getMessage().equals("FLIPTURN")) 
				{
					sendMessage(currPlayer, "MESSAGE", "\n" +currPlayer.getName() + " its your turn!!!\n"); 
					// tell player to do a flip move 
					String cardsStr = currPlayer.printCards(playersList) + "\n";  
					// show all cards 
					for(int j =0; j < this.playersList.size(); ++j ) 
					{
						sendMessage(this.playersList.get(j),"MESSAGE", cardsStr ); 
					}
					// now send a packet to command thread 
					sendAction("FLIPTURN", currPlayer); 
	
				} // Notify player to do a swap play
				else if(playersList.get(index).getMessage().equals("SWAPTURN")) 
				{
					
					sendMessage(currPlayer, "MESSAGE", currPlayer.getName() + " its your turn!!!\n"); 
					// tell player to do SWAP move 
					// show all cards and stock card
					String cardsStr = "Stock: " + this.stock.getCards().getFirst().toString() + 
							"\n" + "Discard: " + this.discard.getCards().getFirst().toString() + "\n" + 
							currPlayer.printCards(playersList) + "\n"; 
					//sendMessage(currPlayer,"MESSAGE" ,"player enter deck to swap with [1] Stock , [2] Discard"); 
					for(int j =0; j < this.playersList.size(); ++j ) 
					{
						sendMessage(this.playersList.get(j),"MESSAGE", cardsStr ); 
					}
					// now send a packet to command thread 
					sendAction("SWAPTURN", currPlayer); 
				}
			}
		
	
	}
	
	
	/* Sends action that the player is supposed to do, when player is recieved it will copy state of this object 
	 * so when object is called it can make correct decisions */ 
	public static void sendAction(String action, Player curr) 
	{
		try 
		{
			curr.setMessage(action);
			byte[] sendData =  Tracker.constructObject(curr);
			InetAddress ip =  InetAddress.getByName( curr.getIP());  
			int port = Integer.valueOf(	curr.getPPort()); 
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
			sSocket.send(sendPacket);  // send packet
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		
	}

	/* Run game loop here */ 
	@Override
	public  void run() {
		boolean gameActive = true; 
		
		// intialize game 
		this.initializeGame();
		
		// game started loop through it 
	
		for (int i =0; i < this.playersList.size(); ++i) 
		{
			// Not a dealer send a message 
			if(!this.dealer.getName().equals(this.playersList.get(i).getName())) 
			{
				sendMessage(this.playersList.get(i),"MESSAGE", "Dealer is " + this.dealer.getName() + " game has begun!\nPress **ENTER** to join session!\n"); 
			}
		}
		
		// Everyone has been notified of the game now check for turns 
		int index = 0; 
		// find dealer 
			for(int i =0; i < this.playersList.size(); ++i) 
			{
				if(this.playersList.get(i) == this.dealer) 
				{
					index = i - 1; 
					break; 
				}
			}
		
			if(index < 0 ) 
			{
				index = this.playersList.size() - 1; 
			}
		boolean notifedTurn = false; 
		do 
		{
			/* Moves list contain players moves in the game in fifo order*/ 
			synchronized (moves) 
			{  
				if (!moves.isEmpty()) 
				{
					Player move = moves.removeFirst(); 
					int prior = index; 
					// Now process move
					index = processMove(move, index); 
	 
					notifedTurn = (index == prior);  
					// change turn 
					if (index < 0) 
					{
						index = this.playersList.size() - 1; 
					}
				}
			} 
			if(index < 0 ) 
			{
				index = this.playersList.size() - 1; 
			}
			
			// Check round is over before making a move
			if (isRoundOver()) 
			{
				round++; // increment 
				
				// Round is over now calculate the score of each player 
				for(int i =0; i < this.playersList.size(); ++i) 
				{
					calculatePlayerScore(this.playersList.get(i)); 
				}
				// compute score and message all other players 
				String roundScore = "\n"+ showScore() + "\n"; 
				// Send everyone the score
				for(int i =0; i < this.playersList.size(); ++i) 
				{
					sendMessage(this.playersList.get(i), "MESSAGE", roundScore) ;
				}
				// set up next round if needed 
				this.setUpNextRound();
				// reinitalize the game 
				this.initializeGame(); 
			}
			else // round not over do a move  
			{
				// Make player turn
				makeTurn(index, notifedTurn); 
				notifedTurn = true;
			}
			
			// Check game is over all holes have been reached 
			if (round == holes) 
			{
				// announce the winner
				String winner = getWinner();
				for(int i =0; i < this.playersList.size(); ++i) 
				{
					sendMessage(this.playersList.get(i),"MESSAGE", winner); 
				}
				
				try 
				{
					Thread.sleep(5000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				// send a message to the dealer to start another game or end it 
				sendMessage(this.dealer,"OVER", "ROUNDOVER");
				
				// send message to other players that game notify them
				for(int i =0; i < this.playersList.size(); ++i ) 
				{
					if(!this.playersList.get(i).getName().equals(this.dealer.getName())) 
					{
						sendMessage(this.playersList.get(i), "MESSAGE" , "Game has ended! Let your dealer decide to continue the game!\n"); 
					}
				}
				
				sendAction("OVER", this.dealer); 
				// some branch if over then break it otherwise continue  
				do 
				{
					synchronized (moves) 
					{  
						if (!moves.isEmpty()) 
						{
							Player move = moves.removeFirst(); 
							gameActive = ( !move.getCommand().split("\\|")[1].equals("Y")); 
							// add code to let players leave?
							
							if(gameActive) 
							{
								for(int i =0;i < this.playersList.size(); ++i)
								{
									sendMessage(this.playersList.get(i), "MESSAGE" , "Dealer Restarted the game!\n"); 
									this.playersList.get(i).setScore(0);
									round =0 ; 
								}
							}
							break; 
						}
					}
				}
				while(true); 
			}
		}
		while(gameActive); 
		
		// send a message telling other players dealers ended the game
		for(int i =0; i < this.playersList.size(); ++i) 
		{
			sendAction("END", this.playersList.get(i)); 
			this.playersList.get(i).setState(gameState.FREE); // Free each statea 
			
		}
	}
	
}
