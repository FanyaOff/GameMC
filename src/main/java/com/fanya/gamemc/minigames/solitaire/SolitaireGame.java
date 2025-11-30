package com.fanya.gamemc.minigames.solitaire;

import com.fanya.gamemc.minigames.solitaire.subclass.SolitaireCard;
import net.minecraft.util.math.random.Random;

public class SolitaireGame {
    public enum State { RUNNING, VICTORY }
    private SolitaireGame.State state = SolitaireGame.State.RUNNING;
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
    }

    public void tryToMoveInTable(SolitaireCard from, int colon) { // перенос карты на стол
        if(colon > 7 || colon < 0) return;
        SolitaireCard last = colons[colon];
        if(last == null && from.getDenomination().equals(SolitaireCard.Denominations.KING)) {
            moveCard(from);
            colons[colon] = from.setPrevious(null);
        } else if(last != null) {
            while (last.getNext() != null) last = last.getNext();
            if((from.getSuit().ordinal() < 2 && last.getSuit().ordinal() < 2)
                    || (from.getSuit().ordinal() > 1 && last.getSuit().ordinal() > 1)) return;
            if(last.getDenomination().ordinal() - from.getDenomination().ordinal() != 1) return;

            moveCard(from);
            from.setPrevious(last);
            last.setNext(from);
        }
    }

    public boolean tryToMoveInBase(SolitaireCard from) { // перенос карты в базу
        int colon = from.getSuit().ordinal();
        SolitaireCard last = bases[colon];
        if(last == null && from.getDenomination().equals(SolitaireCard.Denominations.ACE)) {
            moveCard(from);
            bases[colon] = from.setPrevious(null);
            return true;
        } else if(last != null) {
            if(from.getDenomination().ordinal() - last.getDenomination().ordinal() != 1) return false;
            moveCard(from);
            from.setPrevious(last);
            last.setNext(from);
            bases[colon] = from;
            return true;
        }
        return false;
    }

    public void tryToMoveInDeck(SolitaireCard from) { // Прокрутка колоды
        if(gameDeck != null) gameDeck = gameDeck.getNext();
    }

    private void moveCard(SolitaireCard from) { // общий перенос карты
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
            from.getPrevious()
                    .setNext(null)
                    .show();
        }
    }

    public void checkWin() { // авто складыватель карт
        if(check()) {
            checkWin();
        }
        for (SolitaireCard card : bases)
            if(card.getDenomination() != SolitaireCard.Denominations.KING)
                return;
        state = State.VICTORY;
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
}
