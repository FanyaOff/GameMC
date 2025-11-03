package com.fanya.gamemc.minigames.snake;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Логика игры "Змейка".
 * - Поддерживает настраиваемые foodConfigs (шанс/очки/текстура).
 * - При поедании вызывает onFoodEaten (callback) с соответствующим FoodConfig.
 * - Поле "проходимо" (wrap-around).
 */
public class SnakeGame {
    private int gridWidth;
    private int gridHeight;
    private final List<Position> snake;
    private Direction direction;
    private Direction nextDirection;
    private Position food;
    private Identifier currentFoodTexture;
    private FoodConfig currentFoodConfig;
    private boolean gameOver;
    private int score;
    private final Random random;
    private long lastMoveTime;
    private static final long MOVE_DELAY = 120;
    private boolean gameWon = false;

    private List<FoodConfig> foodConfigs = new ArrayList<>();
    private Consumer<FoodConfig> onFoodEaten = null;

    // По умолчанию набор еды (можно менять через setFoodConfigs)
    private static final Identifier[] DEFAULT_FOOD_TEXTURES = {
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
        this.gridWidth = Math.max(5, gridWidth);
        this.gridHeight = Math.max(5, gridHeight);
        this.snake = new ArrayList<>();
        this.random = new Random();
        initDefaultFoodConfigs();
        this.reset();
    }

    private void initDefaultFoodConfigs() {
        List<FoodConfig> list = new ArrayList<>();
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[0], 10, 20)); // apple
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[1], 25, 5));  // golden apple
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[2], 8, 15));  // carrot
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[3], 7, 10));  // potato
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[4], 6, 8));   // beetroot
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[5], 9, 10));  // melon
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[6], 12, 6));  // bread
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[7], 4, 12));  // cookie
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[8], 18, 4));  // pumpkin pie
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[9], 20, 5));  // cooked beef
        this.foodConfigs = list;
    }

    public void setFoodConfigs(List<FoodConfig> configs) {
        if (configs == null || configs.isEmpty()) return;
        this.foodConfigs = new ArrayList<>(configs);
    }

    public List<FoodConfig> getFoodConfigs() {
        return new ArrayList<>(this.foodConfigs);
    }

    public void setOnFoodEaten(Consumer<FoodConfig> callback) {
        this.onFoodEaten = callback;
    }

    public boolean isGameWon() {
        return gameWon;
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

        if (newHead.x < 0) newHead.x = gridWidth - 1;
        if (newHead.x >= gridWidth) newHead.x = 0;
        if (newHead.y < 0) newHead.y = gridHeight - 1;
        if (newHead.y >= gridHeight) newHead.y = 0;

        for (Position segment : snake) {
            if (segment.equals(newHead)) {
                gameOver = true;
                return;
            }
            if (snake.size() == gridWidth * gridHeight) {
                gameWon = true;
                gameOver = true;
            }
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            if (currentFoodConfig != null) {
                score += currentFoodConfig.getPoints();
            } else {
                score += 10;
            }

            if (onFoodEaten != null) {
                try {
                    onFoodEaten.accept(currentFoodConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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

        if (foodConfigs == null || foodConfigs.isEmpty()) {
            currentFoodTexture = DEFAULT_FOOD_TEXTURES[random.nextInt(DEFAULT_FOOD_TEXTURES.length)];
            currentFoodConfig = null;
            return;
        }

        int totalWeight = 0;
        for (FoodConfig fc : foodConfigs) totalWeight += fc.getWeight();
        if (totalWeight <= 0) {
            FoodConfig fc = foodConfigs.get(random.nextInt(foodConfigs.size()));
            currentFoodConfig = fc;
            currentFoodTexture = fc.getTexture();
            return;
        }

        int pick = random.nextInt(totalWeight);
        int acc = 0;
        for (FoodConfig fc : foodConfigs) {
            acc += fc.getWeight();
            if (pick < acc) {
                currentFoodConfig = fc;
                currentFoodTexture = fc.getTexture();
                return;
            }
        }

        FoodConfig fc = foodConfigs.get(random.nextInt(foodConfigs.size()));
        currentFoodConfig = fc;
        currentFoodTexture = fc.getTexture();
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

    public void setGridSize(int w, int h) {
        this.gridWidth = Math.max(5, w);
        this.gridHeight = Math.max(5, h);
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
