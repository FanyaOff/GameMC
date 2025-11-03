package com.fanya.gamemc.minigames._2048;

import com.fanya.gamemc.data.GameRecords;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.*;

public class Game2048 {
    public enum State { RUNNING, GAMEOVER, PAUSED, VICTORY }

    private static final int COLS = 6;
    private static final int ROWS = 7;

    private final int[][] board = new int[ROWS][COLS];

    private int currentLevel;
    private int currentX;
    private int currentY;

    private int score = 0;
    private State state = State.RUNNING;

    private final Random random = new Random();

    private final String recordKey = "2048_blocks";

    private void playSound(SoundEvent sound, float pitch, float volume) {
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(sound, pitch, volume)
        );
    }

    private void playSpawnSound() {
        playSound(SoundEvents.BLOCK_BAMBOO_PLACE, 1.2f, 0.7f);
    }

    private void playLandSound() {
        playSound(SoundEvents.BLOCK_STONE_PLACE, 1.0f, 0.6f);
    }

    private void playMergeSound() {
        playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f + random.nextFloat() * 0.3f, 0.8f);
    }

    private void playComboSound() {
        playSound(SoundEvents.ENTITY_WIND_CHARGE_THROW, 1.0f, 0.9f);
    }

    private void playGameOverSound() {
        playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.8f, 1.0f);
    }

    private void playVictorySound() {
        playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public Game2048() {
        reset();
    }

    public void reset() {
        for (int r = 0; r < ROWS; r++) Arrays.fill(board[r], 0);
        score = 0;
        state = State.RUNNING;
        spawnBlock();
    }



    public int[][] getBoard() { return board; }
    public int getCols() { return COLS; }
    public int getRows() { return ROWS; }
    public int getScore() { return score; }
    public State getState() { return state; }

    public int getCurrentLevel() { return currentLevel; }
    public int getCurrentX() { return currentX; }
    public int getCurrentY() { return currentY; }

    private void spawnBlock() {
        currentLevel = getRandomBlockLevel();

        switch (currentLevel) {
            case 1 -> currentX = 0;
            case 2 -> currentX = 2;
            case 3 -> currentX = 4;
            case 4 -> currentX = 5;
            default -> currentX = COLS / 2;
        }

        currentY = 0;

        if (board[currentY][currentX] != 0) {
            endGame();
            return;
        }

        playSpawnSound();
    }
    private int getRandomBlockLevel() {
        double r = random.nextDouble();
        if (r < 0.6) return 1;
        if (r < 0.85) return 2;
        if (r < 0.95) return 3;
        return 4;
    }

    public void togglePause() {
        if (state == State.RUNNING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.RUNNING;
    }

    private void endGame() {
        state = State.GAMEOVER;
        GameRecords.getInstance().setBestScore(recordKey, score);
        playGameOverSound();
    }

    private void winGame() {
        state = State.VICTORY;
        GameRecords.getInstance().setBestScore(recordKey, score);
        playVictorySound();
    }

    public boolean dropStep() {
        if (state != State.RUNNING) return false;
        int ny = currentY + 1;
        if (ny >= ROWS || board[ny][currentX] != 0) {
            placeBlock();
            return false;
        } else {
            currentY = ny;
            return true;
        }
    }

    public void hardDrop() {
        while (dropStep()) {}
    }

    public void move(int dx) {
        if (state != State.RUNNING) return;
        int nx = currentX + dx;
        if (nx < 0 || nx >= COLS) return;
        if (board[currentY][nx] == 0) currentX = nx;
    }

    private void placeBlock() {
        playLandSound();
        int y = currentY;
        int x = currentX;
        int lvl = currentLevel;

        if (y + 1 < ROWS && board[y + 1][x] == lvl) {
            board[y + 1][x] = Math.min(11, lvl + 1);
            score += (1 << board[y + 1][x]);
            applyGravity();
        } else {
            board[y][x] = lvl;
            applyGravity();
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == 11) {
                    winGame();
                    return;
                }
            }
        }

        checkTopLine();
        if (state == State.RUNNING) spawnBlock();
    }

    private void checkTopLine() {
        for (int c = 0; c < COLS; c++) {
            if (board[0][c] != 0) {
                endGame();
                break;
            }
        }
    }

    private void applyGravity() {
        boolean merged;
        int comboCount = 0;

        do {
            merged = false;
            for (int c = 0; c < COLS; c++) {
                for (int r = ROWS - 2; r >= 0; r--) {
                    if (board[r][c] != 0 && board[r + 1][c] == 0) {
                        board[r + 1][c] = board[r][c];
                        board[r][c] = 0;
                        merged = true;
                    }
                }
            }

            for (int c = 0; c < COLS; c++) {
                for (int r = ROWS - 2; r >= 0; r--) {
                    if (board[r][c] != 0 && board[r][c] == board[r + 1][c]) {
                        board[r + 1][c] = Math.min(11, board[r][c] + 1);
                        board[r][c] = 0;
                        score += (1 << board[r + 1][c]);
                        playMergeSound();
                        merged = true;
                        comboCount++;
                    }
                }
            }

            for (int r = ROWS - 1; r >= 0; r--) {
                for (int c = 0; c < COLS - 1; c++) {
                    if (board[r][c] != 0 && board[r][c] == board[r][c + 1]) {
                        board[r][c] = Math.min(11, board[r][c] + 1);
                        board[r][c + 1] = 0;
                        score += (1 << board[r][c]);
                        playMergeSound();
                        merged = true;
                        comboCount++;
                    }
                }
            }

        } while (merged);

        if (comboCount >= 3) {
            playComboSound();
        }
    }
}
