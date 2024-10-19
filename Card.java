

/* Class will represent each individual card in teh game */

import java.io.Serializable;

enum card_type 
{
	S, H, C, D
}

enum card_color
{
	BLACK, RED
}


public class Card implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3721463948752637456L;
	private String name; 
	private int value; 
	private card_type type; 
	private card_color color;
	private boolean faceUp; 
	private boolean held; 
	
	public Card(String name, card_type type, card_color color, int value) 
	{
		this.name = name; 
		this.value = value;
		this.type = type; 
		this.color = color; 
		this.held = false;
		this.faceUp = false; 
	}
	
	
	public void setFace(boolean face) 
	{
		this.faceUp = face; 
	}
	
	public boolean isFacedUp() 
	{
		return this.faceUp; 
	}
	
	
	public card_type  getType() 
	{
		return this.type; 
	}
	
	public card_color getColor() {
		return this.color;
	}
	
	public boolean isHeld()
	{
		return this.held; 
	}
	
	public void setHeld(boolean arg)
	{
		this.held = arg; 
	}
	
	public String getName() 
	{
		return this.name; 
	}
	
	public int getValue() 
	{
		return this.value; 
	}
	
	@Override 
	public String toString()
	{
		String result="";
		
		// Faced up print type of card 
		if(this.isFacedUp()) 
		{
			result = this.name + " "; 
		}
		else 
		{
			result = "***"; 
		}
		
		return result; 
	}
	
}
