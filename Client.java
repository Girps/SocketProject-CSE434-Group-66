
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner; 
public class Client 
{

	
	
	// Each client gets one player 
	
	public static Player player = new Player(); 
	
	
	public static void main(String[] args) 
	{
		final int portNumber = 5001; 
		final String ipAdd = "127.0.0.1"; 
		DatagramPacket sendPacket; 
		DatagramPacket receivePacket = null;
		byte[] receiveData = new byte[5000];  
		
		try
		{
			System.out.println("Client server started");
		
			// loop to send messages 
			boolean flag = true; 
			while (flag)
			{
				
				System.out.println("Enter command:\n"); 
				Scanner in = new Scanner(System.in);
				String[] command = in.nextLine().split(" "); 
				
				switch (command[0])  
				{
					case "register":
					{
						try
						{ 
							String playerStr = command[1]; 
							String ipStr = command[2]; 
							String tPortStr = command[3]; 
							String pPortStr = command[4]; 
							int pPort = Integer.parseInt(pPortStr); 
							int tPort = Integer.parseInt(tPortStr); 
							player = new Player(playerStr,  ipStr, tPortStr, pPortStr, "Client_Address", gameState.FREE); 
							DatagramSocket cSocket = new DatagramSocket(pPort);
							String message = registerCommand(command); 
							player.setCommand(message);
							
							// Serialize player object 
							 
							byte[] sendData = Tracker.constructObject(player); 
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipStr), tPort); 
							cSocket.send(sendPacket);
							
							/* Now wait and check if recieved the a response should receive a size 5000 byte array at least*/ 
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipStr), tPort); 
							cSocket.receive(receivePacket);
							ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData())); 
							Player recPlayer = (Player)iStream.readObject(); 
							player = recPlayer; 
							iStream.close(); 
							cSocket.close(); 
							System.out.println(recPlayer.getMessage()); 
						}
						catch (IOException | ClassNotFoundException e) 
						{
							e.printStackTrace(); 
						}
						
					}
					break; 
					case "query":
					{	
					// Command will not update player 
						if (command[1].equals("players") ) 
						{
							String query = "query players"; 
							Player sendPlayer = new Player(player);
							sendPlayer.setCommand(query);
							byte[] sendData = Tracker.constructObject(sendPlayer); 
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
							DatagramSocket cSocket = new DatagramSocket();
							cSocket.send(sendPacket); // Send packet 
							
							// Wait for packet to return 
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
							cSocket.receive(receivePacket); // recieve packet 
							ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));  
							// Receive player DO NOT update our real player 
							Player recPlayer = (Player)iStream.readObject(); 
							iStream.close(); 
							cSocket.close();
							System.out.println(recPlayer.getMessage()); 
						}
						else 
						{
							
							String message = "query games"; 
							Player sendPlayer = new Player(player);
							sendPlayer.setCommand(message);
							byte[] sendData = Tracker.constructObject(sendPlayer); 
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
							DatagramSocket cSocket = new DatagramSocket();
							cSocket.send(sendPacket);
							
							//Block wait for response  
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
							cSocket.receive(receivePacket); // recieve packet
							ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
							
							// Receive player DO NOT update our real player 
							Player recPlayer = (Player)iStream.readObject(); 
							iStream.close();
							cSocket.close();
							System.out.println(recPlayer.getMessage());
						}		
					}
					break; 
					case "de-register": 
					{
						// de register the player from the game 
						String query = "de-register|" + command[1];
						Player sendPlayer = new Player(player); 
						sendPlayer.setCommand(query); 
						byte[] sendData = Tracker.constructObject(sendPlayer); 
						sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
						DatagramSocket cSocket = new DatagramSocket();
						cSocket.send(sendPacket);
						receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
						
						// Block and receive
						cSocket.receive(receivePacket);
						ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
						cSocket.close();
						Player recPlayer = (Player)iStream.readObject(); 
						String result = new String ( recPlayer.getMessage()); 
						System.out.println(result);
						if (result.equals("SUCCESS")) 
						{ 
							flag = false;
						} 
					}
					break; 
					case "QUIT": 
					{
						flag = false; 
					}
					break; 
					default: 
					{
						System.out.println("Invalid command"); 
					}
				}
			}
		}
		catch (  IOException | ClassNotFoundException e ) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String registerCommand(String args[]) 
	{
		String message  = args[0] + "|" + args[1] + 
				"|" + args[2] + "|" +args[3] + "|" + args[4];    
		
		return message;
	}
	
	public static void errorChecking(String[] args) throws Exception 
	{
		if (args.length < 4) 
		{
			throw new Exception("Missing arguments"); 
		}
		else if(args[0].length() > 15) 
		{
			throw new Exception("Player name too large"); 
		}

			
	}
}
