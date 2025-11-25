package com.fanya.gamemc.minigames.solitaire.subclass;

import com.fanya.gamemc.GameMC;

public class SolitaireCard {
    public enum Denominations {
        ACE("ace"),
        TWO("two"),
        THREE("three"),
        FOUR("four"),
        FIVE("five"),
        SIX("six"),
        SEVEN("seven"),
        EIGHT("eight"),
        NINE("nine"),
        TEN("ten"),
        JACK("jack"),
        QUEEN("queen"),
        KING("king");

        private final String key;
        Denominations(String k) { key = k;}

        public String getKey() { return key; }
    }
    public enum Suits {
        HEARTS("hearts"),
        DIAMONDS("diamond"),
        CLUBS("clubs"),
        SPADES("spades");

        private final String key;
        Suits(String k) { key = k; }

        public String getKey() { return key; }
    }

    private final Denominations denomination;
    private final Suits suit;

    private SolitaireCard next;
    private SolitaireCard previous;
    private boolean shown;

    public SolitaireCard(Denominations d, Suits s, SolitaireCard n, SolitaireCard p, boolean sh) {
        denomination = d;
        suit = s;

        next = n;
        previous = p;
        shown = sh;
    }
    public SolitaireCard(Denominations d, Suits s) { this(d, s, null, null, false); }

    public Denominations getDenomination() {return denomination;}
    public Suits getSuit() {return suit;}
    public SolitaireCard getNext() {return next;}
    public SolitaireCard getPrevious() {return previous;}

    public String getTexturePath() { return GameMC.MOD_ID + ":gui/suits/" + suit.getKey() + "/" + denomination.getKey(); }

    public SolitaireCard setNext(SolitaireCard n) { next = n; return this; }
    public SolitaireCard setPrevious(SolitaireCard p) { previous = p; return this; }

    public boolean isShown() {return shown;}
    public void show() {shown = true;}
}
