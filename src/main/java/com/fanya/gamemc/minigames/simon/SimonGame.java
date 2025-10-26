package com.fanya.gamemc.minigames.simon;

import net.minecraft.block.NoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimonGame {
    public enum State {
        SHOWING, INPUT, LOST, WON, IDLE
    }

    private final Random random = new Random();
    private final List<Integer> sequence = new ArrayList<>();
    private int nextToShowIndex = 0;
    private int playerInputIndex = 0;

    // Timers (millis)
    private long lastTickTime = 0;
    private long elementShowDuration = 600;
    private long interElementDelay = 250;
    private long lastChangeTime = 0;
    private boolean currentlyLit = false;

    private State state = State.IDLE;

    private final int initialLength;

    public SimonGame(int initialLength) {
        this.initialLength = Math.max(1, initialLength);
        reset();
    }

    public void reset() {
        sequence.clear();
        state = State.IDLE;
        playerInputIndex = 0;
        nextToShowIndex = 0;
        currentlyLit = false;

        for (int i = 0; i < initialLength; i++) {
            sequence.add(random.nextInt(5));
        }
        prepareShowSequence();
    }

    private void prepareShowSequence() {
        state = State.SHOWING;
        nextToShowIndex = 0;
        playerInputIndex = 0;
        currentlyLit = false;
        lastChangeTime = System.currentTimeMillis();
    }

    public State getState() {
        return state;
    }

    public List<Integer> getSequence() {
        return sequence;
    }

    public int getCurrentlyShowingIndex() {
        if (state != State.SHOWING) return -1;
        return nextToShowIndex;
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (lastTickTime == 0) lastTickTime = now;
        long dt = now - lastTickTime;
        lastTickTime = now;

        if (state == State.SHOWING) {
            if (!currentlyLit) {
                if (now - lastChangeTime >= interElementDelay) {
                    currentlyLit = true;
                    lastChangeTime = now;
                    playNoteSound(sequence.get(nextToShowIndex));
                }
            } else {
                if (now - lastChangeTime >= elementShowDuration) {
                    currentlyLit = false;
                    lastChangeTime = now;
                    nextToShowIndex++;
                    // Если показали все элементы — переходим в INPUT
                    if (nextToShowIndex >= sequence.size()) {
                        state = State.INPUT;
                        playerInputIndex = 0;
                    }
                }
            }
        }
    }

    public boolean clickButton(int buttonIndex) {
        if (state != State.INPUT) return false;

        playNoteSound(buttonIndex);

        int expected = sequence.get(playerInputIndex);
        if (expected != buttonIndex) {
            state = State.LOST;
            return true;
        }

        playerInputIndex++;
        if (playerInputIndex >= sequence.size()) {
            sequence.add(random.nextInt(5));
            prepareShowSequence();
        }
        return true;
    }

    private void playNoteSound(int index) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        float pitch = 0.5f + index * 0.15f;
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_HARP, pitch));
    }

    public int getSequenceLength() {
        return sequence.size();
    }
}
