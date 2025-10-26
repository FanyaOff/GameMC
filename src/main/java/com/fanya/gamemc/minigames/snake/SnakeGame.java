package com.fanya.gamemc.minigames.snake;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGame {
    private final int gridWidth;
    private final int gridHeight;
    private final List<Position> snake;
    private Direction direction;
    private Direction nextDirection;
    private Position food;
    private Identifier currentFoodTexture;
    private boolean gameOver;
    private int score;
    private final Random random;
    private long lastMoveTime;
    private static final long MOVE_DELAY = 120;

    // Список текстур еды (рандомные предметы)
    private static final Identifier[] FOOD_TEXTURES = {
            Identifier.ofVanilla("textures/item/apple.png"),
            Identifier.ofVanilla("textures/item/golden_apple.png"),
            Identifier.ofVanilla("textures/item/carrot.png"),
            Identifier.ofVanilla("textures/item/potato.png"),
            Identifier.ofVanilla("textures/item/beetroot.png"),
            Identifier.ofVanilla("textures/item/melon_slice.png"),
            Identifier.ofVanilla("textures/item/bread.png"),
            Identifier.ofVanilla("textures/item/cookie.png"),
            Identifier.ofVanilla("textures/item/pumpkin_pie.png"),
            Identifier.ofVanilla("textures/item/cooked_beef.png")
    };

    public SnakeGame(int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.snake = new ArrayList<>();
        this.random = new Random();
        this.reset();
    }

    public void reset() {
        snake.clear();

        int startX = gridWidth / 2;
        int startY = gridHeight / 2;
        snake.add(new Position(startX, startY));
        snake.add(new Position(startX - 1, startY));
        snake.add(new Position(startX - 2, startY));

        direction = Direction.EAST;
        nextDirection = Direction.EAST;
        gameOver = false;
        score = 0;
        lastMoveTime = System.currentTimeMillis();
        spawnFood();
    }

    public void update() {
        if (gameOver) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime < MOVE_DELAY) {
            return;
        }
        lastMoveTime = currentTime;

        direction = nextDirection;

        Position head = snake.get(0);
        Position newHead = new Position(head.x, head.y);

        switch (direction) {
            case NORTH -> newHead.y--;
            case SOUTH -> newHead.y++;
            case WEST -> newHead.x--;
            case EAST -> newHead.x++;
        }

        if (newHead.x < 0 || newHead.x >= gridWidth ||
                newHead.y < 0 || newHead.y >= gridHeight) {
            gameOver = true;
            return;
        }

        for (Position segment : snake) {
            if (segment.equals(newHead)) {
                gameOver = true;
                return;
            }
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            score += 10;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    public void setDirection(Direction newDirection) {
        if (newDirection == Direction.NORTH && direction != Direction.SOUTH) {
            nextDirection = newDirection;
        } else if (newDirection == Direction.SOUTH && direction != Direction.NORTH) {
            nextDirection = newDirection;
        } else if (newDirection == Direction.WEST && direction != Direction.EAST) {
            nextDirection = newDirection;
        } else if (newDirection == Direction.EAST && direction != Direction.WEST) {
            nextDirection = newDirection;
        }
    }

    private void spawnFood() {
        do {
            food = new Position(random.nextInt(gridWidth), random.nextInt(gridHeight));
        } while (snake.contains(food));

        currentFoodTexture = FOOD_TEXTURES[random.nextInt(FOOD_TEXTURES.length)];
    }

    public List<Position> getSnake() {
        return snake;
    }

    public Position getFood() {
        return food;
    }

    public Identifier getCurrentFoodTexture() {
        return currentFoodTexture;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getScore() {
        return score;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public static class Position {
        public int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Position other)) return false;
            return this.x == other.x && this.y == other.y;
        }

        @Override
        public int hashCode() {
            return x * 1000 + y;
        }
    }
}
