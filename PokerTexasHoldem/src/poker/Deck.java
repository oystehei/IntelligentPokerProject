package poker;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: oye
 * Date: 25.08.11
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class Deck {

    private ArrayList<Card> cards = new ArrayList<Card>();

    public Deck (){
        buildDeck();
    }

    /**
     * Clears the current deck and builds a new deck sorted
     */
    public void buildDeck(){
        cards.clear();
        for(Card.Suit suit : Card.Suit.values()){
            for(Card.Value value : Card.Value.values()){
                cards.add(new Card(value, suit));
            }
        }
    }

    /**
     * Shuffles the deck
     */
    public void shuffleDeck (){
        Card temp;
        int tempIndex;
        for (int i=0; i<cards.size(); i++){
            tempIndex = ((int) (i * Math.random()));
            if (tempIndex != i){
                temp = cards.get(i);
                cards.set(i, cards.get(tempIndex));
                cards.set(tempIndex, temp);
            }
        }
    }

    public Card dealCard (){
        if(!cards.isEmpty())
            return cards.remove(0);
        else
            System.out.println("No more cards in deck!");
        return null;
    }

    public int cardsLeft (){
        return cards.size();
    }

    public ArrayList<Card> getCards(){
        return cards;
    }

    public Card getSpecificCard(Card card){
        for(Card c : cards){
            if(c.getSuit()==card.getSuit() && c.getValue()==card.getValue())
                return cards.remove(cards.indexOf(c));
        }
        return null;
    }
}