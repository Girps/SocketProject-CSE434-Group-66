import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

enum gameState
{
	FREE,IN_GAME, NOT_REG
}

public class Player implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8126866215239971956L;
	/**
	 * 
	 */
	
	private String name, dIP, rIP, tPort, pPort; 
	private gameState state; 
	private LinkedList<Card> playerDeck = new LinkedList<Card>(); 
	private int score =0 ;
	private boolean initial = true; 
	private int gameId = -1; 
	private String command = null; 
	transient Scanner scan = new Scanner(System.in); 
	private Cards stock = new Cards(); 
	private String message; 
	
	public Player () 
	{
		this.state = gameState.NOT_REG; 
	}
	
	public Player (Player player) 
	{
		this.name = player.name; 
		this.dIP = player.dIP; 
		this.state = player.state; 
		this.tPort = player.tPort; 
		this.pPort = player.pPort; 
		this.rIP =  player.rIP; 
		this.gameId = player.gameId; 
		this.playerDeck = player.playerDeck; 
		this.message = player.message; 
		this.command = player.command; 
		
	}
	
	public Player(String name, String IP, String tport, String pport, String rIP, gameState state)
	{
		this.name = name; 
		this.dIP = IP; 
		this.state = state; 
		this.tPort = tport; 
		this.pPort = pport; 
		this.rIP = rIP; 
	}
	
	public void setName(String name)
	{
		this.name = name; 
	}
	
	
	
	public String getMessage() 
	{
		return this.message; 
	}
	
	public void setMessage(String message) 
	{
		this.message = message; 
	}
	
	public void setIP(String rIP) 
	{
		this.rIP = rIP;
	}
	
	public void setCommand(String command) 
	{
		this.command = command; 
	}
	
	public String getCommand() 
	{
		return this.command; 
	}
	
	public void setGameId(int gameId) 
	{
		this.gameId = gameId; 
	}
	
	public int getGameId() 
	{
		return this.gameId; 
	}
	
	public void setScore(int score)
	{
		this.score = score; 
	}
	
	public int getScore() 
	{
		return this.score; 
	}
	
	
	public LinkedList<Card> getDeck ()
	{
		return this.playerDeck; 
	}
	
	public String getName() 
	{
		return name; 
	}
	
	public String getIP() 
	{
		return dIP; 
	}
	
	public String getTPort() 
	{
		return tPort; 
	}
	

	public String getPPort() 
	{
		return pPort; 
	}
	
	public String getRIP() 
	{
		return dIP; 
	}
	
	public gameState getState() 
	{
		return state; 
	}
	
	public void setState(gameState state) 
	{
		this.state = state; 
	}
	
	@Override
	public String toString() 
	{
		String result  = "( " +"Player: " +this.name + " dest IP: " + this.dIP + "rec IP: " + this.rIP +
				" T-Port: " + this.getTPort() + " P-Port: " + this.getPPort() + 
				" State: " + this.getState().toString() + " )"; 
		return result ; 
	}
	
	public boolean getInital()
	{
		return this.initial; 
	}
	
	public void setInital(boolean initial) 
	{
		this.initial = initial; 
	}
	
	/* 
	 * Player takes a turn, they should be given a menu 
	 * */ 
	public void takeTurn(ArrayList<Card> stock, ArrayList<Card> discard, ArrayList <Player> players) 
	{
		String input = ""; 
		if (this.initial) 
		{
			int n =2; 
			this.initial = false; 
			
			do
			{
				System.out.println( this.name + " flip " + (n) + " cards enter corresponding number\n");
				printCards(players); 
				input = scan.next();
				// Valid number face up corresponding card 
				if (input.toCharArray()[0] < '7' && input.toCharArray()[0] > '0') 
				{ 
					int number =Integer.valueOf(input); 
					this.playerDeck.get(number-1).setFace(true);
					n--; 
				}
				else 
				{
					System.out.println("Invalid selectiont try again\n"); 
				}
				 
				 
			}while(n>0); 
			
		}
		// Now give option to pick stock card and discard card 
		
		String choice = ""; 
		boolean flag = true; 
		/* Choose card to swap from , stock card or discard card Make sure to 
		 * remove card from the corresponding deck */ 
		do 
		{
			System.out.println("[1] to draw from stock cards or [2] discard cards\n"); 
			System.out.println("Stock: " +stock.getFirst()); 
			System.out.println("Discard: "+ discard.getFirst() + "\n");
			printCards(players); 
			input = scan.next();
			switch (input) 
			{
				case "1": 
				{
					flag = false; 
					choice = "stock";
				} 
					break;
				case "2":
				{
					flag = false; 
					choice = "discard"; 
				}
					break; 
				default:
					System.out.println("Invalid input\n"); 
					break; 
			}
		} 
		while(flag);
		flag = true; 
		// Pick card to swap or just discard 
		do 
		{
			System.out.println("Enter input to swap card with\n"); 
			printCards(players); 
			System.out.println("[Q] to discard card\n"); 
			input = scan.next(); 
			switch (input) 
			{
				case "1":
				{
					if (choice.equals("stock"))
					{
						swap(stock, discard, choice,0); 
					}
					else 
					{
						swap(stock, discard, choice,0);
					}
					flag = false; 
				}
					break; 
				case "2": 
				{
					if (choice.equals("stock"))
					{
						swap(stock, discard, choice,1);
					}
					else 
					{
						swap(stock, discard, choice,1);
					}
					flag = false; 
				}
					break;
				case "3": 
				{
					if (choice.equals("stock"))
					{
						swap(stock, discard, choice,2);
					}
					else 
					{
						swap(stock, discard, choice,2);
					}
					flag = false; 
				}
					break; 
				case "4": 
				{
					if (choice.equals("stock"))
					{
						swap(stock, discard, choice,3);
					}
					else 
					{
						swap(stock, discard, choice,3);
					}
					flag = false; 
				}
					break; 
				case "5": 
				{
					if (choice.equals("stock"))
					{
						swap(stock, discard, choice,4);
					}
					else 
					{
						swap(stock, discard, choice,4);
					}
					flag = false; 
				}
					break; 
				case "6": 
				{
					if (choice.equals("stock"))
					{
						swap(stock, discard, choice,5);
					}
					else 
					{
						swap(stock, discard, choice,5);
					}
					flag = false; 
				}
				case "Q":
				{
					// This case remove stock card and insert it onto  discard deck 
					System.out.println("Discarded\n");
					if (choice.equals("stock")) 
					{
						Card picked = stock.removeFirst();
						picked.setFace(true);
						discard.addFirst(picked);
					}
					flag = false; 
				}
					break;
				default: 
					{
						System.out.println("Invalid selection"); 
						continue; 
					}
			}
			
			// Now print the stock/discard and the player deck
			System.out.println(stock.getFirst()); 
			System.out.println(discard.getFirst());
			printCards(players); 
		}
		while(flag); 
		System.out.println(this.name + " turn has ended\n");
		
	}
	
	/* Swap the cards */ 
	private void swap(ArrayList<Card> stock, ArrayList<Card> discard , String choice , int n ) 
	{
		
		if (choice.equalsIgnoreCase("stock")) 
		{
			// Swap with stock card 
			Card A = stock.removeFirst(); 
			Card B = this.playerDeck.get(n);
			A.setFace(true);
			A.setHeld(true);
			B.setFace(true);
			B.setHeld(false);
			this.playerDeck.set(n, A); // overwrite
			discard.addFirst(B); // add it to discard stack
		}
		else 
		{
			// Swap with discarded card and add it
			Card A = playerDeck.get(n); 
			Card B = discard.removeFirst();
			playerDeck.set(n, B);  // swap 
			A.setFace(true);
			A.setHeld(true);
			discard.addFirst(A); // add it ontop of the cards 
			
		}
	}
	
	// Print current player and other players cards 
	public void printCards(ArrayList<Player> players) 
	{
		
		
		// iterate each column for each player 
		String result = this.name + " deck\t\t";
		
		for(int i =0 ; i < players.size(); ++i) 
		{
			if(players.get(i) != this)
			{
				result += players.get(i).name + " deck\t\t"; 
			}
		}
		
		result += "\n"; 
		
		result += "[1]" + this.playerDeck.get(0) + "[2]" +this.playerDeck.get(1) + "[3]" +this.playerDeck.get(2) + "\t"; 
		// Now iterate 1st row 
		for (int j = 0; j < players.size(); j++ ) 
		{
			// Get rows of other players 
			if (this != players.get(j))
			{ 
				// iterate the columns 
				for(int i =0; i < (this.playerDeck.size() / 2 )  ; ++i) 
				{
					result +=  players.get(j).getDeck().get(i) + " ";
				}
			}
		}
		
		result += "\n"; 
		result += "[4]" + this.playerDeck.get(3) + "[5]" +this.playerDeck.get(4) + "[6]" +this.playerDeck.get(5) + "\t"; 
		
		// now iterate second row of each player 
		for(int j = 0; j < players.size(); ++j) 
		{
			// Get rows of other players 
			if (this != players.get(j))
			{ 
				// iterate the columns 
				for(int i = (this.playerDeck.size()/2 ) ; i < (this.playerDeck.size() )  ; ++i) 
				{
					result +=  players.get(j).getDeck().get(i) + " ";
				}
			}
		}
		
		
		System.out.println(result + "\n"); 
	}
	
}
