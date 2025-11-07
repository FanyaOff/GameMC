package com.fanya.gamemc.minigames.snake;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.function.Consumer;

public class SnakeGame {

    private int gridWidth;
    private int gridHeight;
    private final List<RenderSegment> snake;
    private Direction direction;
    private Position food;
    private Identifier currentFoodTexture;
    private FoodConfig currentFoodConfig;
    private boolean gameOver;
    private boolean gameWon;
    private int score;

    private final Random random;
    private long lastMoveTime;
    private long moveDelay;

    private List<FoodConfig> foodConfigs = new ArrayList<>();
    private Consumer<FoodConfig> onFoodEaten = null;

    private final Queue<Direction> inputQueue = new ArrayDeque<>();

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
        reset();
    }

    private void initDefaultFoodConfigs() {
        List<FoodConfig> list = new ArrayList<>();
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[0], 10, 20));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[1], 25, 5));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[2], 8, 15));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[3], 7, 10));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[4], 6, 8));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[5], 9, 10));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[6], 12, 6));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[7], 4, 12));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[8], 18, 4));
        list.add(new FoodConfig(DEFAULT_FOOD_TEXTURES[9], 20, 5));
        this.foodConfigs = list;
    }

    public void setFoodConfigs(List<FoodConfig> configs) {
        if (configs == null || configs.isEmpty()) return;
        this.foodConfigs = new ArrayList<>(configs);
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
        snake.add(new RenderSegment(startX, startY));
        snake.add(new RenderSegment(startX - 1, startY));
        snake.add(new RenderSegment(startX - 2, startY));

        direction = Direction.EAST;
        inputQueue.clear();
        gameOver = false;
        gameWon = false;
        score = 0;
        lastMoveTime = System.currentTimeMillis();
        calculateMoveDelay();
        spawnFood();
    }

    private void calculateMoveDelay() {
        long baseDelay = 180;
        long minDelay = 50;
        int gridArea = gridWidth * gridHeight;
        int maxArea = 50 * 30;
        int minArea = 10 * 10;

        moveDelay = minDelay + (maxArea - gridArea) * (baseDelay - minDelay) / (maxArea - minArea);
        if (moveDelay < minDelay) moveDelay = minDelay;
        if (moveDelay > baseDelay) moveDelay = baseDelay;
    }

    public void update() {
        if (gameOver) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime < moveDelay) return;
        float t = 1f;
        lastMoveTime = currentTime;

        for (RenderSegment seg : snake) {
            seg.prevGridX = seg.gridX;
            seg.prevGridY = seg.gridY;
        }

        if (!inputQueue.isEmpty()) {
            direction = inputQueue.poll();
        }

        RenderSegment headSeg = snake.get(0);
        Position newHead = new Position(headSeg.gridX, headSeg.gridY);

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

        for (RenderSegment segment : snake) {
            if (segment.gridX == newHead.x && segment.gridY == newHead.y) {
                gameOver = true;
                return;
            }
        }

        if (snake.size() == gridWidth * gridHeight) {
            gameWon = true;
            gameOver = true;
        }

        snake.add(0, new RenderSegment(newHead.x, newHead.y));

        if (newHead.equals(food)) {
            if (currentFoodConfig != null) score += currentFoodConfig.getPoints();
            else score += 10;
            if (onFoodEaten != null) {
                try { onFoodEaten.accept(currentFoodConfig); }
                catch (Exception e) { e.printStackTrace(); }
            }
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    public void setDirection(Direction newDirection) {
        Direction lastDir = inputQueue.peek() != null ? inputQueue.peek() : direction;

        if ((lastDir == Direction.NORTH && newDirection == Direction.SOUTH) ||
                (lastDir == Direction.SOUTH && newDirection == Direction.NORTH) ||
                (lastDir == Direction.WEST && newDirection == Direction.EAST) ||
                (lastDir == Direction.EAST && newDirection == Direction.WEST)) return;

        inputQueue.add(newDirection);
    }

    private void spawnFood() {
        do {
            food = new Position(random.nextInt(gridWidth), random.nextInt(gridHeight));
        } while (snake.stream().anyMatch(seg -> seg.gridX == food.x && seg.gridY == food.y));

        if (foodConfigs == null || foodConfigs.isEmpty()) {
            currentFoodTexture = DEFAULT_FOOD_TEXTURES[random.nextInt(DEFAULT_FOOD_TEXTURES.length)];
            currentFoodConfig = null;
            return;
        }

        int totalWeight = foodConfigs.stream().mapToInt(FoodConfig::getWeight).sum();
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

    public List<RenderSegment> getSnake() { return snake; }
    public Position getFood() { return food; }
    public Identifier getCurrentFoodTexture() { return currentFoodTexture; }
    public boolean isGameOver() { return gameOver; }
    public int getScore() { return score; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public void setGridSize(int w, int h) {
        this.gridWidth = Math.max(5, w);
        this.gridHeight = Math.max(5, h);
        calculateMoveDelay();
    }
    public long getLastMoveTime() { return lastMoveTime; }
    public long getMoveDelay() { return moveDelay; }

    public static class Position {
        public int x, y;
        public Position(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof Position other)) return false;
            return this.x == other.x && this.y == other.y;
        }
        @Override public int hashCode() { return x * 1000 + y; }
    }

    public static class FoodConfig {
        private final Identifier texture;
        private final int points;
        private final int weight;
        public FoodConfig(Identifier texture, int points, int weight) {
            this.texture = texture; this.points = points; this.weight = weight;
        }
        public Identifier getTexture() { return texture; }
        public int getPoints() { return points; }
        public int getWeight() { return weight; }
    }

    public static class RenderSegment {
        public float x, y;
        public int gridX, gridY;
        public int prevGridX, prevGridY;

        public RenderSegment(int gridX, int gridY) {
            this.gridX = gridX;
            this.gridY = gridY;
            this.prevGridX = gridX;
            this.prevGridY = gridY;
            this.x = gridX;
            this.y = gridY;
        }

        public void updatePosition(float t) {
            this.x = prevGridX + t * (gridX - prevGridX);
            this.y = prevGridY + t * (gridY - prevGridY);
        }
    }
}
