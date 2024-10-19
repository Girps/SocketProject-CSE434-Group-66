 
import java.io.Serializable;
import java.util.ArrayList; 
public class Cards implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5657610491768255429L;
	public  ArrayList<Card> deck = new ArrayList<Card>(); 
	// Construct cards
	public Cards () 
	{
		
		card_type types[] = {card_type.S, card_type.H,card_type.C, card_type.D}; 
		
		// fill numbered cards first 
		int number_cards = 36;
		int n_type = 0; 
		card_color currColor = card_color.BLACK; 
		int x =0;
		int number = 2; 
		for (int i = 0 ;i < number_cards; i++) 
		{
			
			this.deck.add( new Card(String.valueOf(number) + types[n_type].toString(), types[n_type],currColor,number == 2 ? (number*-1) : number ));
			x = i;
			number++; 
			
			if( (i+1) % 9 == 0 ) 
			{		
				n_type += 1; 
				number = 2; // make sure in range [2,9]
				if (currColor == card_color.RED) 
				{
					currColor = card_color.BLACK; 
				}
				else 
				{
					currColor = card_color.RED; 
				}
				//this.deck[i] = new Card(null, types[n_type],currColor,number+1);
			}
		
		}
		
		// ACES
		this.deck.add( new Card("A" + card_type.D.toString(), card_type.D,card_color.RED,1));
		this.deck.add( new Card("A" + card_type.C.toString(), card_type.C,card_color.BLACK,1));
		this.deck.add( new Card("A" + card_type.H.toString(), card_type.H,card_color.RED,1));
		this.deck.add( new Card("A" + card_type.S.toString(), card_type.S,card_color.BLACK,1));
		
		this.deck.add( new Card("J" + card_type.D.toString(), card_type.D,card_color.RED,10));
		this.deck.add( new Card("J" + card_type.C.toString(), card_type.C,card_color.BLACK,10));
		this.deck.add( new Card("J" +  card_type.H.toString(), card_type.H,card_color.RED,10));
		this.deck.add(new Card("J" + card_type.S.toString(), card_type.S,card_color.BLACK,10));
		
		this.deck.add( new Card("Q" + card_type.D.toString(), card_type.D,card_color.RED,10));
		this.deck.add( new Card("Q" + card_type.C.toString(), card_type.C,card_color.BLACK,10));
		this.deck.add( new Card("Q"+ card_type.H.toString(), card_type.H,card_color.RED,10));
		this.deck.add( new Card("Q" + card_type.S.toString(), card_type.S,card_color.BLACK,10));
		
		this.deck.add( new Card("K" + card_type.D.toString(), card_type.D,card_color.RED,0));
		this.deck.add( new Card("K" + card_type.C.toString(), card_type.C,card_color.BLACK,0));
		this.deck.add(new Card("K" + card_type.H.toString(), card_type.H,card_color.RED,0));
		this.deck.add(new Card("K" + card_type.S.toString(), card_type.S,card_color.BLACK,0));
	}
	
	public ArrayList<Card> getCards() 
	{
		return this.deck; 
	}
	
}
