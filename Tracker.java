import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
public class Tracker {
	
	/* Tracker class is resposnible in keeping track the numer of players registered in the 
	 *  this server. Server will run to listen for requests from other clients through a specific port 
	 *  */ 
	
	public static void main(String[] args) {
		
		
		/*
		 * 	HashMap will get store a record of registed clients 
		 * */ 
		HashMap<String, Player> registeredPlayers = new HashMap<String, Player>(); 
		HashMap<String, Player> games = new HashMap<String, Player>(); 
		
		int portNumber = 5001; 
		// Exception to catch failure to port 
		try
		{
			DatagramSocket sSocket = new DatagramSocket(portNumber); 
			System.out.println("Tracker started listening on port: " + portNumber); 
			byte[] receiveData = new byte[1024]; 
			byte[] sendData = new byte[1024]; 
			DatagramPacket receivePacket; 
			// listen for client messasges 
			while (true) 
			{
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				sSocket.receive(receivePacket);
				// Get Client IP and port 
				InetAddress ip= receivePacket.getAddress(); 
				int port = receivePacket.getPort(); 
				String message = new String(receivePacket.getData(), 0 , receivePacket.getLength());
				String[] command = message.split("\\|"); 
				System.out.println(command[0]); 
				switch (command[0]) 
				{
					case "register":
					{
						DatagramPacket sendPacket = null; 
						String responseMessage = ""; 
						// We got player info now create a player object and add it to the structure. 
						if (addPlayer(command, registeredPlayers)) 
						{
							// inserted new user now send a response back to the client 
							responseMessage = "SUCCESS"; 
							System.out.println(responseMessage); 
							sendData = responseMessage.getBytes(); 
							sendPacket = new DatagramPacket(sendData,sendData.length, ip, port);
							sSocket.send(sendPacket);
						} 
						else 
						{
							// duplicate users 
							responseMessage = "FALSE";
							System.out.println(responseMessage);
							sendData = responseMessage.getBytes(); 
							sendPacket = new DatagramPacket(sendData,sendData.length, ip, port);
							sSocket.send(sendPacket); 
						}
						break; 
					} 
					case "query players": 
					{
						// get all player info on the structure
						String responseMessage = getAllPlayers(registeredPlayers); 
						sendData = responseMessage.getBytes(); 
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
						sSocket.send(sendPacket);
					}
					break; 
					case "query games": 
					{
						// get all games in proces
						String responseMessage = getAllGames(command[0], games);
						sendData = responseMessage.getBytes(); 
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port); 
						sSocket.send(sendPacket); 
					}
					break; 
					case "de-register":
					{
						// deregister player from the map 
						boolean response = deRegisterPlayer(command[1], registeredPlayers ); 
						if(response) 
						{
							// Removed player now send message back 
							String responseMessage = "SUCCESS"; 
							sendData = responseMessage.getBytes(); 
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip, port); 
							sSocket.send(sendPacket);
						}
						else
						{
							String responseMessage = "FAILURE"; 
							sendData = responseMessage.getBytes(); 
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip, port);
							sSocket.send(sendPacket);
						}
					}
					break ; 
					default: 
					{
						System.out.println("Received an unknown command"); 
					}
					break; 
				}
				
				// convert byte into String 
			
				// Print message 
				System.out.println("Client IP: " + ip + " , Client port: " + port + ", Message: " + message); 
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			System.out.print("Failure to port " + portNumber);
			e.printStackTrace();
		} 
		

	}
	
	public static String getAllGames(String player, HashMap<String, Player> games) 
	{
		if (games.size() > 0) 
		{
			
			return ""; 
		}
		else 
		{
			return "No games"; 
		}

	}
	
	public static boolean deRegisterPlayer(String player, HashMap<String, Player> map) 
	{
		if ( !map.containsKey(player)) 
		{
			return false; 
		}
		else if (map.get(player).getState() == gameState.IN_GAME) 
		{
			return false; 
		}
		else 
		{
			map.remove(player); 
			return true; 
		}
	}
	
	public static String getAllPlayers(HashMap<String, Player> map) 
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
	
	public static boolean addPlayer(String[] args, HashMap<String, Player> records) 
	{
		String name, ip, tport, pport;  
		System.out.println(args[0]);
		System.out.println(args[1]);
		System.out.println(args[2]);
		System.out.println(args[3]);
		System.out.println(args[4]);
		name = args[1]; 
		ip = args[2]; 
		tport = args[3]; 
		pport = args[4]; 
		
		if (records.containsKey(name)) 
		{
			return false; 
		}
		else 
		{
			// Now add player 
			records.put(name, new Player(name,ip,tport,pport,gameState.FREE)); 
			return true; 
		}
	} 

}
