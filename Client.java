
import java.io.IOException;
import java.net.*;
import java.util.Scanner; 
public class Client {
	
	public static void main(String[] args) 
	{
		final int portNumber = 5001; 
		// TODO Auto-generated method stub
		DatagramPacket sendPacket; 
		DatagramPacket receivePacket = null; 
		byte[] sendData = null; 
		byte[] receiveData = new byte[1024];  
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
						String playerStr = command[1]; 
						String ipStr = command[2]; 
						String tPortStr = command[3]; 
						String pPortStr = command[4]; 
						int pPort = Integer.parseInt(pPortStr); 
						int tPort = Integer.parseInt(tPortStr); 
						DatagramSocket cSocket = new DatagramSocket(pPort);
						String message = registerCommand(command); 
						sendData = message.getBytes(); 
						sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipStr), tPort); 
						cSocket.send(sendPacket);
						/* Now wait and check if recieved the a response */ 
						receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName(ipStr), tPort); 
						cSocket.receive(receivePacket);
						cSocket.close(); 
						System.out.println(new String ( receivePacket.getData(), 0 , receivePacket.getLength())); 
					}
					break; 
					case "query":
					{	
						if (command[1].equals("players") ) 
						{
							String message = "query players"; 
							sendData= message.getBytes(); 
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("127.0.0.1"), 5001); 
							DatagramSocket cSocket = new DatagramSocket();
							cSocket.send(sendPacket);
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName("127.0.0.1") , 5001); 
							cSocket.receive(receivePacket);
							cSocket.close();
							System.out.println(new String ( receivePacket.getData(), 0 , receivePacket.getLength())); 
						}
						else 
						{
							String message = "query games"; 
							sendData= message.getBytes();
							sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("127.0.0.1"), 5001); 
							DatagramSocket cSocket = new DatagramSocket();
							cSocket.send(sendPacket);
							receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName("127.0.0.1") , 5001); 
							cSocket.receive(receivePacket);
							cSocket.close();
							System.out.println(new String ( receivePacket.getData(), 0 , receivePacket.getLength()));
						}		
					}
					break; 
					case "de-register": 
					{
						// de register the player from the game 
						String message = "de-register|" + command[1]; 
						sendData= message.getBytes();
						sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("127.0.0.1"), 5001); 
						DatagramSocket cSocket = new DatagramSocket();
						cSocket.send(sendPacket);
						receivePacket = new DatagramPacket(receiveData,receiveData.length, InetAddress.getByName("127.0.0.1") , 5001); 
						cSocket.receive(receivePacket);
						cSocket.close();
						String result = new String ( receivePacket.getData(), 0 , receivePacket.getLength()); 
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
		catch (SocketException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
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
