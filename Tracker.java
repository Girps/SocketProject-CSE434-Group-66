
import java.util.ArrayList;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class Tracker {
	
	/* Tracker class is resposnible in keeping track the numer of players registered in the 
	 *  this server. Server will run to listen for requests from other clients through a specific port 
	 *  */ 
	
	// Concurrent hashmaps used to avoid race conditions between threads 
	/* Map contains registered players contains both free and in_game players */  
	public final static ConcurrentHashMap<String, Player> registeredPlayers = new ConcurrentHashMap<String, Player>(); // this needs to be synchronized  
	/* Map constains currently running games and the game object hold references to in_game players */ 
	public final static ConcurrentHashMap<Integer, Game> games = new ConcurrentHashMap<Integer, Game>();  // this needs to be synchronize  
	
	public final static AtomicInteger globalGameId = new AtomicInteger(); 
	
	public static void main(String[] args) throws ClassNotFoundException {
		
		/* Create cached threads, threads will be create and disposed of on runtime */
		ExecutorService executor = Executors.newCachedThreadPool(); 
		// Tracker server has the same port number and IP address as it needs to be found by the client 
		int portNumber = 5001; 
		ByteArrayOutputStream bStream = new ByteArrayOutputStream(); 
		
		// Exception to catch failure to port 
		try
		{
			DatagramSocket serverSocket = new DatagramSocket(portNumber); 
			System.out.println("Tracker started listening on port: " + portNumber); 
			 
			/* Main thread to listen to events and call a thread for certain operations , create another thread when a game starts  
			 * so that the game can listen to events from clients */ 
			// listen for client messasges 
			while (true) 
			{
				byte[] receiveData = new byte[5000]; 
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket); // so this blocks  
				
				// Get Client IP and port 
				InetAddress ip= receivePacket.getAddress(); 
				int port = receivePacket.getPort(); 
				ObjectInputStream iStream = new ObjectInputStream( new ByteArrayInputStream(receivePacket.getData())); 
				Player receivedPlayer = (Player)iStream.readObject(); 
				iStream.close(); 
				
				// got the player check if the player is in the game 
				if ( receivedPlayer.getState() == gameState.FREE || receivedPlayer.getState() == gameState.NOT_REG ) 
				{   
					String message = new String(receivedPlayer.getCommand());
					String[] command = message.split("\\|");  
					
					switch (command[0]) 
					{
						case "register":
						{
							// create a thread and complete that task 
							executor.execute( () -> playerRegister(serverSocket, receivePacket, command) ); 
							break; 
						} 
						case "query players": 
						{
							// async excute the command 
							System.out.println("Query players command called tracker"); 
							executor.execute( () -> 
							{
								try  
								{ 
									
									// get all player info on the structure
									String responseMessage = getAllPlayers(registeredPlayers); 
									receivedPlayer.setMessage(responseMessage); 
									byte[] sendData = constructObject(receivedPlayer); 
									DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, ip, port);
									serverSocket.send(sendPacket);  
								} 
								catch (IOException e) 
								{
									e.printStackTrace(); 
								}
							}); 
							
						}
						break; 
						case "query games": 
						{
							executor.execute( () ->
							{
								try 
								{
									// get all games in proces
									String responseMessage = getAllGames(command[0], games);  
									receivedPlayer.setMessage(responseMessage); // set response to dummy player 
									byte[] sendData = constructObject(receivedPlayer); 
									DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
									serverSocket.send(sendPacket); // sent packet 
								} 
								catch (IOException e) 
								{
									e.printStackTrace();
								}	
							}); 
							
						}
						break; 
						case "de-register":
						{
							// De-register player from the tracker async
							executor.execute( () -> 
							{
								// make dummy player 
								deRegisterPlayer(command[1], receivePacket, serverSocket , registeredPlayers ); 
							}  ); 
						}
						break ; 
						case "start game": 
						{
							// Attempt to start game by checking if players are available to join
							executor.execute( () -> 
								{
								
								}
							);
						}
							break; 
						default: 
						{
							System.out.println("Received an unknown command"); 
						}
						break; 
					}
					} 
				else 
				{
					// Game command process game turns includes end-game command and other game commands
					String message = new String(receivedPlayer.getCommand());
					String[] command = message.split("\\|");
					switch(command[0]) 
					{
						case "end-game": 
							break; 
						case "": 
							break; 
							default: 
								break; 
					}
					
				}
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			System.out.print("Failure to port " + portNumber);
			e.printStackTrace();
		} 
		

	}
	
	
	
	/* Get player will be the dealer of the game and check if there is enough players to start a game 
	 *  optional holes [1,9] , additional players [1,3] + 1 dealer total of 4 players, make sure players are registered and 
	 *  not in a game  
	 * */ 
	public static void makeGame( String dealerName, String[] command ,DatagramSocket serverSocket, DatagramPacket receivePacket ) 
	{
		
		try 
		{ 
			Player dealer = null; 
			// If player is not in the hash map nor is free return a failure 
			if ( !registeredPlayers.containsKey(dealerName) || registeredPlayers.get(dealerName).getState() != gameState.FREE) 
			{
				// tell client failure 
				Player sendPlayer = new Player(); 
				sendPlayer.setMessage("FAILURE NOT A REGISTERED PLAYER");
				byte[] sendData =  constructObject(sendPlayer);
				InetAddress ip = receivePacket.getAddress(); 
				int port = receivePacket.getPort();  
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
				serverSocket.send(sendPacket); // sent packet 
			} 
			else 
			{
				/* Critical section player elements are not thread safe need to modify to make them thread safe  */ 
				
				dealer = new Player ( registeredPlayers.get(dealerName)); 
				int playerCount = 0; 
				int n = Integer.valueOf(command[3]);  
				boolean enoughPlayers = false; 
				ArrayList<Player> chosenPlayers = new ArrayList<Player>(); 
				// iterate the hashmap 
				for (Map.Entry<String, Player> players : registeredPlayers.entrySet() ) 
				{
					String name = players.getKey(); 
					Player p = players.getValue(); 
					
					// its free now change state and add it to the list 
					if ( p.getState() == gameState.FREE && !p.getName().equals(dealerName) ) 
					{
						chosenPlayers.add(p); 
						playerCount++; 
					}
					 // if equal break 
					if (playerCount == n  ) 
					{
						enoughPlayers = true; 
						break; 
					}
				}
				
				// set as gameState as inGame
				if(enoughPlayers) 
				{
					/* Critical section I am modifying the players data heere and creating a new game */ 
					for (int i =0; i < chosenPlayers.size(); ++i) 
					{
						chosenPlayers.get(i).setState(gameState.IN_GAME);
					}
					
					// Create a game instance and send it
					Game game = new Game(globalGameId.addAndGet(1),n,dealer,chosenPlayers); 
					// Theres enough players create a game and send a packet for success 
					dealer.setCommand("SUCCESS");
					dealer.setGameId(game.getId());
					byte[] sendData =  constructObject(dealer);
					InetAddress ip = receivePacket.getAddress(); 
					int port = receivePacket.getPort();  
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
					serverSocket.send(sendPacket); // sent packet 
				}
				else 
				{
					// not enough players send an error
					Player sendPlayer = new Player(); 
					sendPlayer.setMessage("FAILURE");
					byte[] sendData =  constructObject(sendPlayer);
					InetAddress ip = receivePacket.getAddress(); 
					int port = receivePacket.getPort();  
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
					serverSocket.send(sendPacket); // sent packet 
				}
				
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace(); 
		}
	}
		
	// Write Player data into the byte stream 
	public static byte[] constructObject( Player player) throws IOException 
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); 
		ObjectOutputStream objO = new ObjectOutputStream(byteStream); 
		objO.writeObject(player);
		objO.flush(); 
		objO.close();
		return byteStream.toByteArray(); 
	}	

	
	// Pass the socket 
	public static void playerRegister (DatagramSocket serverSocket, DatagramPacket receivePacket,String[] command)
	{
		InetAddress ip = receivePacket.getAddress(); 
		int port =  receivePacket.getPort(); 
		DatagramPacket sendPacket = null; 
		String responseMessage = ""; 
		byte[] sendData = null; 
		
		String name = command[1]; 
		String IP = command[2]; 
		String tPort= command[3]; 
		String pPort = command[4]; 
		
		// Construct player 
		Player player = new Player(name,IP,tPort, pPort, receivePacket.getAddress().toString(), gameState.FREE); 
		
		// We got player info now create a player object and add it to the structure. 
		try
		{ 
			if (addPlayer(player, registeredPlayers)) 
			{
				// inserted new user now send a response back to the client 
				responseMessage = "SUCCESS"; 
				player.setMessage(responseMessage);
				sendData = constructObject( player);
				sendPacket = new DatagramPacket(sendData,sendData.length, ip, port);
				serverSocket.send(sendPacket); 
			} 
			else 
			{
				// duplicate users 
				responseMessage = "FAILURE";
				player.setMessage(responseMessage);
				sendData = constructObject( player);  
				sendPacket = new DatagramPacket(sendData,sendData.length, ip, port);
				serverSocket.send(sendPacket);
			}	
		} 
		catch(IOException e) 
		{
			e.printStackTrace(); 
		} 
	}
	
	/* Method will iterate game hashamp and collect game info of each, return  a string of amount of 
	 *  games and data of each*/ 
	public static String getAllGames(String player, ConcurrentHashMap<Integer, Game> games) 
	{
		// Games exist iterate each game print its information 
		if (games.size() > 0) 
		{
			String result = "Total Games : "  + games.size(); 
			
			// iterate each game and get information on each 
			for (Map.Entry<Integer, Game> entry : games.entrySet()) 
			{
				result +=  "Game ID: " 
				+ entry.getValue().getId() +
				" Rounds: " + entry.getValue().getRounds() 
				+ "Dealer: " + entry.getValue().getDealer().getName() + "\n"; 
			}
			return result; 
		}
		else 
		{
			return "No games"; 
		}

	}
	
	/* Access the hashmap and delete a player from it assuming they are not IN_GAME state */ 
	public static void deRegisterPlayer(String playerName, DatagramPacket recievedPacket, DatagramSocket sSocket , ConcurrentHashMap<String, Player> map) 
	{
		try 
		{ 
			
			InetAddress ip = recievedPacket.getAddress();  
			int port = recievedPacket.getPort();  
			Thread.currentThread().getName(); 
			Player sendPlayer = new Player(); 
			boolean response; 
			
			if ( !map.containsKey(playerName)) 
			{
				response = false; 
			}
			else if (map.get(playerName).getState() == gameState.IN_GAME) 
			{
				response = false; 
			}
			else 
			{
				map.remove(playerName); 
				response = true; 
			}
			
			// Now respond to the client 
			if(response) 
			{
				// Removed player now send message back 
				String responseMessage = "SUCCESS"; 
				sendPlayer.setMessage(responseMessage);
				byte[] sendData = constructObject( sendPlayer);  
				DatagramPacket sendPacket;
				sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
				sSocket.send(sendPacket);
			}
			else
			{
				String responseMessage = "FAILURE"; 
				sendPlayer.setMessage(responseMessage);
				byte[] sendData = constructObject(sendPlayer); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip, port);
				sSocket.send(sendPacket);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
	}
	
	/* Iterawte hashmap and prints player infromation */ 
	public static String getAllPlayers(ConcurrentHashMap<String, Player> map) 
	{
		String result = "Size: " + map.size(); 
		
		for (Map.Entry<String, Player> entry : map.entrySet()) 
		{
			result += entry.getValue() + "\n"; 
		}
		
		if(map.size() == 0)
		{
			result += " No players "; 
		}
	
		return result; 
	}
	
	
	/* Method either adds a player or checks for existing player, thread safe as hashmap is concurrent*/ 
	public static boolean addPlayer(Player player, ConcurrentHashMap<String, Player> records ) 
	{
		if (records.containsKey(player.getName())) 
		{
			return false; 
		}
		else 
		{
			// Now add player 
			records.put(player.getName(), new Player(player.getName(),player.getIP(),player.getTPort(),player.getPPort(), player.getRIP() ,gameState.FREE)); 
			return true; 
		}
	} 

}

