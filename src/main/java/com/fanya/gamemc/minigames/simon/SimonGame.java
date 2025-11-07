package com.fanya.gamemc.minigames.simon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Логика мини-игры "Саймон".
 * Работает с 4 лампами, поддерживает задержку между раундами.
 */
public class SimonGame {
    public enum State {
        SHOWING, INPUT, LOST, WAITING_NEXT_ROUND, IDLE
    }

    private final Random random = new Random();
    private final List<Integer> sequence = new ArrayList<>();

    private int showingIndex = -1;
    private int playerInputIndex = 0;

    private boolean currentlyLit = false;
    private long lastChangeTime = 0;

    private final long showDuration = 500;
    private final long betweenDelay = 200;
    private final long betweenRoundsDelay = 800;

    private State state = State.IDLE;
    private long waitStartTime = 0;

    private final int initialLength;
    private static final int LAMP_COUNT = 4;

    public SimonGame(int initialLength) {
        this.initialLength = Math.max(1, initialLength);
        reset();
    }

    public void reset() {
        sequence.clear();
        for (int i = 0; i < initialLength; i++) {
            sequence.add(random.nextInt(LAMP_COUNT));
        }
        startShowing();
    }

    private void startShowing() {
        state = State.SHOWING;
        showingIndex = 0;
        playerInputIndex = 0;
        currentlyLit = false;
        lastChangeTime = System.currentTimeMillis();
    }

    private void startNextRoundDelay() {
        state = State.WAITING_NEXT_ROUND;
        waitStartTime = System.currentTimeMillis();
    }

    public void startInputPhase() {
        state = State.INPUT;
        currentlyLit = false;
        showingIndex = -1;
        playerInputIndex = 0;
    }

    public void update() {
        long now = System.currentTimeMillis();

        switch (state) {
            case SHOWING -> updateShowing(now);
            case WAITING_NEXT_ROUND -> updateWaiting(now);
        }
    }

    private void updateWaiting(long now) {
        if (now - waitStartTime >= betweenRoundsDelay) {
            startShowing();
        }
    }

    private void updateShowing(long now) {
        if (!currentlyLit) {
            // пауза между лампами
            if (now - lastChangeTime >= betweenDelay) {
                currentlyLit = true;
                lastChangeTime = now;
                playLampSound(sequence.get(showingIndex));
            }
        } else {
            // лампа горит
            if (now - lastChangeTime >= showDuration) {
                currentlyLit = false;
                lastChangeTime = now;
                showingIndex++;

                if (showingIndex >= sequence.size()) {
                    startInputPhase();
                }
            }
        }
    }

    public boolean clickButton(int index) {
        if (state != State.INPUT) return false;

        playLampSound(index);
        int expected = sequence.get(playerInputIndex);

        if (expected != index) {
            state = State.LOST;
            return true;
        }

        playerInputIndex++;

        if (playerInputIndex >= sequence.size()) {
            sequence.add(random.nextInt(LAMP_COUNT));
            startNextRoundDelay();
        }

        return true;
    }

    public int getCurrentlyShowingIndex() {
        if (state != State.SHOWING || !currentlyLit || showingIndex < 0 || showingIndex >= sequence.size())
            return -1;
        return sequence.get(showingIndex);
    }

    private void playLampSound(int index) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        float pitch = switch (index) {
            case 0 -> 0.7f;
            case 1 -> 0.9f;
            case 2 -> 1.1f;
            case 3 -> 1.3f;
            default -> 1.0f;
        };
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_HARP, pitch));
    }

    public long getShowDuration() { return showDuration; }
    public long getBetweenDelay() { return betweenDelay; }
    public long getBetweenRoundsDelay() { return betweenRoundsDelay; }

    public State getState() { return state; }
    public List<Integer> getSequence() { return sequence; }
    public int getSequenceLength() { return sequence.size(); }
}
