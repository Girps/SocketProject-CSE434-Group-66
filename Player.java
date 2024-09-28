

enum gameState
{
	FREE,IN_GAME
}

public class Player 
{
	private String name, IP, tPort, pPort; 
	private gameState state; 
	
	Player(String name, String IP, String tport, String pport, gameState state)
	{
		this.name = name; 
		this.IP = IP; 
		this.state = state; 
		this.tPort = tport; 
		this.pPort = pport; 
	}
	
	public String getName() 
	{
		return name; 
	}
	
	public String getIP() 
	{
		return IP; 
	}
	
	public String getTPort() 
	{
		return tPort; 
	}
	

	public String getPPort() 
	{
		return pPort; 
	}
	
	public gameState getState() 
	{
		return state; 
	}
	
	@Override
	public String toString() 
	{
		String result  = "( " +"Player: " +this.name + " IP: "+ this.IP +
				" T-Port: " + this.getTPort() + " P-Port: " + this.getPPort() + 
				" State: " + this.getState().toString() + " )"; 
		return result ; 
	}
	
}
