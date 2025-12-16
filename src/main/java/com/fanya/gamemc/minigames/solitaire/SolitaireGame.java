package com.fanya.gamemc.minigames.solitaire;

import com.fanya.gamemc.minigames.solitaire.subclass.SolitaireCard;
import net.minecraft.util.math.random.Random;

public class SolitaireGame {

    public enum State { RUNNING, VICTORY }
    private SolitaireGame.State state;
    public State getState() { return state; }

    private SolitaireCard[] getFullDeck() { // Генератор 52х карт
        SolitaireCard[] deck = new SolitaireCard[52];
        int i = 0;

        for(SolitaireCard.Suits suit : SolitaireCard.Suits.values()) {
            for(SolitaireCard.Denominations denomination : SolitaireCard.Denominations.values()) {
                deck[i++] = new SolitaireCard(denomination, suit);
            }
        }

        return deck;
    }

    private final SolitaireCard[] colons = new SolitaireCard[7];
    private SolitaireCard[] bases;
    private SolitaireCard gameDeck = null; // колоды

    public SolitaireGame() { reset(); }

    public void reset() {
        state = SolitaireGame.State.RUNNING;
        SolitaireCard[] deck = getFullDeck();
        Random rnd = Random.create();
        bases = new SolitaireCard[4];

        // перемешываем
        for (int i = deck.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            SolitaireCard temp = deck[index];
            deck[index] = deck[i];
            deck[i] = temp;
        }

        int count = 0;
        for(int i = 0; i < 7; i++) {
            SolitaireCard node = deck[count++];
            colons[i] = node;
            for(int j = i; j > 0; j--) {
                node.setNext(deck[count++]);
                node.getNext().setPrevious(node);
                node = node.getNext();
            }
            node.show(); // открываем последнюю
        }
        gameDeck = deck[count];
        gameDeck.setPrevious(deck[51]);
        deck[51].setNext(gameDeck);
        for(int i = count; i < 51; i++) {
            deck[i].setNext(deck[i+1]);
            deck[i+1].setPrevious(deck[i]);
        }
    }

    public boolean tryToMoveInTable(SolitaireCard from, int colon) { // перенос карты на стол
        if(colon > 7 || colon < 0) return false;
        SolitaireCard last = colons[colon];
        if(last == null && from.getDenomination().equals(SolitaireCard.Denominations.KING)) {

            if(bases[from.getSuit().ordinal()] == from) bases[from.getSuit().ordinal()] = from.getPrevious();
            if (moveCard(from)) {
                for(int i = 0; i < 7; i++) if(colons[i] == from && i != colon) colons[i] = null;
            }

            colons[colon] = from.setPrevious(null);
            return true;
        } else if(last != null) {
            while (last.getNext() != null) last = last.getNext();
            if((from.getSuit().ordinal() < 2 && last.getSuit().ordinal() < 2)
                    || (from.getSuit().ordinal() > 1 && last.getSuit().ordinal() > 1)) return false;
            if(last.getDenomination().ordinal() - from.getDenomination().ordinal() != 1) return false;

            if(bases[from.getSuit().ordinal()] == from) bases[from.getSuit().ordinal()] = from.getPrevious();
            if (moveCard(from)) {
                for(int i = 0; i < 7; i++) if(colons[i] == from && i != colon) colons[i] = null;
            }

            from.setPrevious(last);
            last.setNext(from);
            return true;
        }
        return false;
    }

    public boolean tryToMoveInBase(SolitaireCard from) { // перенос карты в базу
        int colon = from.getSuit().ordinal();
        SolitaireCard last = bases[colon];
        if(last == null && from.getDenomination().equals(SolitaireCard.Denominations.ACE)) {
            if(moveCard(from)) for(int i = 0; i < 7; i++) if(colons[i] == from) colons[i] = null;
            bases[colon] = from.setPrevious(null);
            if(isWin()) state = State.VICTORY;
            return true;
        } else if(last != null) {
            if(from.getDenomination().ordinal() - last.getDenomination().ordinal() != 1) return false;
            if(moveCard(from)) for(int i = 0; i < 7; i++) if(colons[i] == from) colons[i] = null;
            from.setPrevious(last);
            last.setNext(from);
            bases[colon] = from;
            if(isWin()) state = State.VICTORY;
            return true;
        }
        return false;
    }

    public boolean nextDeckCard() { // Прокрутка колоды
        if(gameDeck != null && gameDeck != gameDeck.getNext()) {
            gameDeck = gameDeck.getNext();
            return true;
        }
        return false;
    }

    private boolean moveCard(SolitaireCard from) { // общий перенос карты
        if (gameDeck != null && gameDeck == from) {
            if (from.getPrevious() == from) {
                gameDeck = null;
            } else {
                gameDeck = from.getPrevious();
                from.getNext().setPrevious(from.getPrevious());
                from.getPrevious().setNext(from.getNext());
            }
            from.setNext(null)
                    .show();
        } else {
            if(from.getPrevious() != null)
                from.getPrevious()
                        .setNext(null)
                        .show();
            else return true;
        }
        return false;
    }

    public int checkWin() { // авто складыватель карт
        if(check()) {
            return 1+checkWin();
        } else {
            if(!isWin()) return 0;
            state = State.VICTORY;
            return 1;
        }
    }

    private boolean isWin() {
        for (SolitaireCard card : bases)
            if(card == null || card.getDenomination() != SolitaireCard.Denominations.KING)
                return false;
        return true;
    }

    private boolean check() {
        for(SolitaireCard card : colons) {
            if(card == null) continue;
            while (card.getNext() != null) card = card.getNext();
            if(tryToMoveInBase(card)) return true;
        }
        return gameDeck != null && tryToMoveInBase(gameDeck);
    }

    public boolean isDeckEmpty() {return gameDeck==null;}
    public SolitaireCard getGameDeck() {return gameDeck;}
    public SolitaireCard getColon(int num) {return (0>num || num > 6) ? null : colons[num];}
    public SolitaireCard getBase(int num) {return (0>num || num > 4) ? null : bases[num];}

    public int getColonCount(int num) {
        SolitaireCard card = getColon(num);
        if(card==null) return 0;
        int count = 1;
        while(card.getNext() != null) {
            count++;
            card = card.getNext();
        }
        return count;
    }

    public SolitaireCard getCardAt(int cartX, int cartY) {// 0 1
        if(cartY == 0) {
            if (cartX == 1) return gameDeck;
            if (cartX > 2) return bases[cartX - 3];
            return null;
        } else {
            SolitaireCard card = colons[cartX];
            for(int i = 0; i < cartY-1; i++) {
                if(card == null) return null;
                card = card.getNext();
            }
            return card;
        }
    }
}
