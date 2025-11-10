package com.fanya.gamemc.minigames.snake;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Consumer;

public class SnakeGame {

    private final int gridWidth;
    private final int gridHeight;
    private final List<RenderSegment> snake;
    private Direction direction;

    private final List<Position> foods = new ArrayList<>();
    private final Map<Position, FoodConfig> foodMap = new HashMap<>();

    private boolean gameOver;
    private boolean gameWon;
    private int score;

    private final Random random;
    private long lastMoveTime;
    private long moveDelay;

    private List<FoodConfig> foodConfigs = new ArrayList<>();
    private Consumer<FoodConfig> onFoodEaten = null;

    private final Queue<Direction> inputQueue = new ArrayDeque<>();


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

        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/potato.png"), 1, 30));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/cookie.png"), 2, 25));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/carrot.png"), 3, 25));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/apple.png"), 4, 20));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/bread.png"), 5, 15));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/honey_bottle.png"), 6, 15));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/cooked_beef.png"), 8, 10));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/golden_apple.png"), 20, 5));

        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/poisonous_potato.png"), -3, 25));
        list.add(new FoodConfig(Identifier.ofVanilla("textures/item/rotten_flesh.png"), -5, 10, 1));

        this.foodConfigs = list;
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
        spawnFoods();
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
        lastMoveTime = currentTime;

        for (RenderSegment seg : snake) {
            seg.prevGridX = seg.gridX;
            seg.prevGridY = seg.gridY;
        }

        if (!inputQueue.isEmpty()) {
            direction = inputQueue.poll();
        }

        RenderSegment head = snake.getFirst();
        Position newHead = new Position(head.gridX, head.gridY);

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

        for (RenderSegment seg : snake) {
            if (seg.gridX == newHead.x && seg.gridY == newHead.y) {
                gameOver = true;
                return;
            }
        }

        snake.addFirst(new RenderSegment(newHead.x, newHead.y));

        FoodConfig eaten = foodMap.get(newHead);
        int segmentsToRemove = 1;

        if (eaten != null) {
            double multiplier = getScoreMultiplier();
            score += (int)(eaten.getPoints() * multiplier);

            if (onFoodEaten != null) {
                try { onFoodEaten.accept(eaten); } catch (Exception ignored) {}
            }

            int extraRemove = Math.max(0, eaten.getSegmentsToRemove());
            if (extraRemove > 0) {
                segmentsToRemove += extraRemove;
            } else {
                segmentsToRemove = 0;
            }

            spawnFoods();
        }

        for (int i = 0; i < segmentsToRemove; i++) {
            if (snake.size() > 1) {
                snake.removeLast();
            }
        }


        if (snake.size() == gridWidth * gridHeight) {
            gameWon = true;
            gameOver = true;
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

    private void spawnFoods() {
        clearFoods();

        int gridArea = gridWidth * gridHeight;
        int freeCells = gridArea - snake.size();
        if (freeCells <= 0) return;

        int maxSpawn = Math.min(4, freeCells);
        int count = 1 + random.nextInt(maxSpawn);

        int totalWeight = foodConfigs.stream().mapToInt(FoodConfig::getWeight).sum();

        for (int i = 0; i < count; i++) {
            Position p;
            int attempts = 0;

            while (true) {
                p = new Position(random.nextInt(gridWidth), random.nextInt(gridHeight));
                final Position testPos = p;

                boolean collidesWithSnake = snake.stream()
                        .anyMatch(seg -> seg.gridX == testPos.x && seg.gridY == testPos.y);
                boolean collidesWithFood = foods.contains(testPos);

                if (!collidesWithSnake && !collidesWithFood) break;

                attempts++;
                if (attempts > 1000) return;
            }

            FoodConfig fc;
            if (totalWeight <= 0) {
                fc = foodConfigs.get(random.nextInt(foodConfigs.size()));
            } else {
                int pick = random.nextInt(totalWeight);
                int acc = 0;
                fc = foodConfigs.getFirst();
                for (FoodConfig cand : foodConfigs) {
                    acc += cand.getWeight();
                    if (pick < acc) { fc = cand; break; }
                }
            }

            foods.add(p);
            foodMap.put(p, fc);
        }
    }



    private void clearFoods() {
        foods.clear();
        foodMap.clear();
    }


    public List<RenderSegment> getSnake() { return snake; }

    public List<Position> getFoods() { return Collections.unmodifiableList(foods); }

    public FoodConfig getFoodConfigAt(Position pos) { return foodMap.get(pos); }

    public boolean isGameOver() { return gameOver; }
    public int getScore() { return score; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public long getLastMoveTime() { return lastMoveTime; }
    public long getMoveDelay() { return moveDelay; }


    public double getScoreMultiplier() {
        int area = gridWidth * gridHeight;
        int smallThreshold = 20 * 12;
        int mediumThreshold = 30 * 20;

        if (area <= smallThreshold) return 2.0;
        if (area <= mediumThreshold) return 1.5;
        return 1.0;
    }


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
        private final int segmentsToRemove;

        public FoodConfig(Identifier texture, int points, int weight, int segmentsToRemove) {
            this.texture = texture;
            this.points = points;
            this.weight = weight;
            this.segmentsToRemove = segmentsToRemove;
        }

        public FoodConfig(Identifier texture, int points, int weight) {
            this(texture, points, weight, 0);
        }

        public Identifier getTexture() { return texture; }
        public int getPoints() { return points; }
        public int getWeight() { return weight; }
        public int getSegmentsToRemove() { return segmentsToRemove; }
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
