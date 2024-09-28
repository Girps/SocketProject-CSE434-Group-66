

enum gameState
{
	FREE,IN_GAME
}

public class Player 
{
	private String name, dIP, rIP, tPort, pPort; 
	private gameState state; 
	
	Player(String name, String IP, String tport, String pport, String rIP, gameState state)
	{
		this.name = name; 
		this.dIP = IP; 
		this.state = state; 
		this.tPort = tport; 
		this.pPort = pport; 
		this.rIP = rIP; 
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
	
	@Override
	public String toString() 
	{
		String result  = "( " +"Player: " +this.name + " dest IP: " + this.dIP + "rec IP: " + this.rIP +
				" T-Port: " + this.getTPort() + " P-Port: " + this.getPPort() + 
				" State: " + this.getState().toString() + " )"; 
		return result ; 
	}
	
}
