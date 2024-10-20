
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
	
	public static volatile Player player = new Player(); 
	
	public static DatagramSocket cSocket = null; 
	public static DatagramSocket otherSocket = null; 
	public static void main(String[] args) 
	{
		 int portNumber = 5001; 
		String ipAdd = "127.0.0.1"; 
		DatagramPacket sendPacket; 
		DatagramPacket receivePacket = null; 
		
		byte[] receiveData = new byte[5000];  
		Scanner in = new Scanner(System.in);
		try
		{
			System.out.println("Client server started");
		
			// loop to send messages 
			boolean flag = true; 
			while (flag)
			{
				
				// Client can only use these command when not in game
				if (player.getState() != gameState.IN_GAME) 
				{ 
					// Command input 
					System.out.println("Enter command:\n"); 
					
					String[] command = in.nextLine().split(" "); 
					System.out.println(command[0]); 
					// after entering a command skip and join the game! 
					if (player.getState() == gameState.IN_GAME) 
					{
						continue; 
					}
					
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
								ipAdd = ipStr;
								portNumber = tPort;
								player = new Player(playerStr,  ipStr, tPortStr, pPortStr, "Client_Address", gameState.FREE); 
								cSocket = new DatagramSocket(pPort); // starts here 
								otherSocket = new DatagramSocket(pPort+10);
								String message = registerCommand(command); 
								player.setCommand(message);
								player.getRIP(); 
								
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
								System.out.println(recPlayer.getMessage()); 
								if(recPlayer.getMessage().equals("SUCCESS"))
								{
									// now create another thread dedicated to listening for gameStatef
									Thread thread = new Thread( () -> 
									{ 
										boolean runThread = true; 
										do 
										{ 
											try 
											{
											 
												DatagramPacket rpak = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipStr), tPort); 
												otherSocket.receive(rpak);
												
												ObjectInputStream iS = new ObjectInputStream(new ByteArrayInputStream(rpak.getData())); 
												Player rPlayer = (Player)iS.readObject(); 
												
												if (rPlayer.getState() == gameState.IN_GAME ) 
												{
													player.setState(gameState.IN_GAME);
													String[] info = rPlayer.getMessage().split("\\|");
													switch(info[0]) 
													{
														case "START":
															System.out.println("Game has started press **ENTER** to join the session!"); 
															break; 
														case "MESSAGE":
															// Send message about cards 
															System.out.println(info[1]); 
															break;
														case "OVER":
															// send message about round 
															System.out.println(info[1]); 
															break; 
														case "END":
															// signal end 
															System.out.println(info[1]);
															player.setState(gameState.FREE); 
															break;
														default:
															break;
													}
												}
											} 
											catch (IOException | ClassNotFoundException e)
											{
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											
										
										}
										while(runThread); 
										
									}); 
									thread.start();
								}
							}
							catch (IOException | ClassNotFoundException e) 
							{
								e.printStackTrace(); 
							}
							
						}
						break; 
						case "resume": // command to resume game assuming there is one
						{
							
							String query = "resume"; 
							Player sendPlayer = new Player(player);
							sendPlayer.setCommand(query);
							byte[] sendData = Tracker.constructObject(sendPlayer); 
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
							cSocket.send(sendPacket); // Send packet 

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
								cSocket.send(sendPacket); // Send packet 
								
								// Wait for packet to return 
								receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
								cSocket.receive(receivePacket); // recieve packet

								
								ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));  
								// Receive player DO NOT update our real player 
								Player recPlayer = (Player)iStream.readObject(); 
								iStream.close(); 
							
								System.out.println(recPlayer.getMessage()); 
							}
							else 
							{
								
								String message = "query games"; 
								Player sendPlayer = new Player(player);
								sendPlayer.setCommand(message);
								byte[] sendData = Tracker.constructObject(sendPlayer); 
								sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
								cSocket.send(sendPacket);
								
								//Block wait for response  
								receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
								cSocket.receive(receivePacket); // recieve packet
								ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
								
								// Receive player DO NOT update our real player 
								Player recPlayer = (Player)iStream.readObject(); 
								iStream.close();
								
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
							cSocket.send(sendPacket);
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
							
							// Block and receive
							cSocket.receive(receivePacket);
							ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
							
							Player recPlayer = (Player)iStream.readObject(); 
							String result = new String ( recPlayer.getMessage()); 
							System.out.println(result);
							if (result.equals("SUCCESS")) 
							{ 
								flag = false;
							} 
						}
						break; 
						case "end": // 2 parameters 
						{
							// de register the player from the game 
							String query = "end|" + command[1] + "|" + command[2];
							Player sendPlayer = player; // get current player to get the id 
							sendPlayer.setCommand(query); 
							System.out.println(query); 
							byte[] sendData = Tracker.constructObject(sendPlayer); 
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
							cSocket.send(sendPacket); // send packet
							
							
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipAdd) , portNumber); 
							// Block and receive
							cSocket.receive(receivePacket);
							ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
							Player recPlayer = (Player)iStream.readObject(); 
							iStream.close(); // close
							String result = new String ( recPlayer.getMessage()); 
							System.out.println(result);
							
						} 
						break; 
						case "start":
						{
							if (command.length < 4 || command.length > 5) 
							{
								System.out.println("Invalid parameters for start game"); 
							}
							else 
							{
								// Create a query and send a packet 
								String query = registerCommand(command); 
								Player sendPlayer = new Player(player); 
								sendPlayer.setCommand(query); 
								byte[] sendData = Tracker.constructObject(sendPlayer); 
								sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
								cSocket.send(sendPacket); 
								// Sent packet to tracker 
								
								//recoeve packet to tracker 
								sendData = new byte[5000]; 
								receivePacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
								cSocket.receive(receivePacket);
								ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));

								Player recPlayer = (Player)iStream.readObject(); 
								String[] info = recPlayer.getMessage().split("\\|"); 
								String result = new String ( info[0] ); 
								//iStream.close(); // close
								// Successful update the player to IN_GAME and change commands to player commands 
								if(result.equalsIgnoreCase("SUCCESS")) 
								{
									int id =  recPlayer.getGameId();
									String rounds = info[1]; 
									String players = info[2]; 
									if(info.length == 5) { 
									System.out.println("Game session: " + id + "\ndealer : " + player.getName() + "\nHoles : " + info[3] + "!\n" + info[4]  );
									player = recPlayer; // updated the player  	
									}
									else 
									{
										System.out.println("Game session: " + id + "\ndealer : " + player.getName() + "\nHoles : " + info[3] + "!\n" + info[4]  );
										player = recPlayer;
									}
								}
								else 
								{
									// Fail state 
									System.out.println(info[0]); 
								}
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
				else 
				{
					boolean inGame = true; 
					do 
					{ 
						// game state commands 
						System.out.println("Game command:"); 
						byte[] sendData = new byte[5000]; 
						receivePacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAdd), portNumber); 
						cSocket.receive(receivePacket);// recieve a packet when it is the players turn 
						ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
						Player recPlayer = (Player)iStream.readObject(); 
						//System.out.println(recPlayer.getMessage());  
						// use commands that a player will give and send to the server 
						player = recPlayer; 
						String res = recPlayer.getMessage(); 
						//System.out.println(res); 
						// Only accept packet when it is asking for an action from the server 
						
						if (res.equals("FLIPTURN"))
						{ 
							// player flips
							player.takeAction(res, cSocket);
						} 
						else if(res.equals("SWAPTURN")) 
						{
							player.takeAction(res, cSocket);
						}
						else if(res.equals("OVER")) 
						{
							player.takeAction(res, cSocket);
							player.setState(gameState.IN_LOBBY); // so break it 
							break; 
						}
						else if(res.equals("END")) 
						{
							System.out.println("Dealer ended the game!"); 
							inGame= false;
							// change gamestate of player
							player.setState(gameState.FREE);
							break; 
						}
					}
					while(inGame); 
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
		String message = ""; 
		if ( args.length == 5) {
		message  = args[0] + "|" + args[1] + 
				"|" + args[2] + "|" +args[3] + "|" + args[4];
		}
		else 
		{
			message  = args[0] + "|" + args[1] + 
					"|" + args[2] + "|" +args[3] ; 	
		}
		
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
