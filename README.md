# GameMC
A lightweight mod that adds an arcade game hub to the main menu and pause screen. To open game selection in singleplayer or multiplayer - use keybind (Default: Home)

This mod is in beta, if you find any bugs, open the bug report. 

In the plans:
- add api, that gave ability to modders to make own games and integrate it with GameMC & Minesweeper
- games design rework
- add more games (Flappy Bird, Ping Pong, 2048 WIP)

# Changelogs:

## 1.2.1

**New Features & Updates**

### Snake Game

* **Adaptive Speed:** Snake moves faster on larger grids, slower on smaller ones.
* **Input Queue:** Direction inputs are queued for instant, precise movement, even with rapid key presses.

### Simon Game

* **Sequence Display:** Visual feedback improved, changed note block to copper blob

### 2048: Tower

* **Pause Keybind:** Press `P` to pause/resume the game.
* **UI Updates:** Controls, scores, and panel info now display correctly in both English and Russian.

## 1.2 

**Changes:**

* Added new game - **2048: Tower**

**Read more about the game below**

## 1.1 – Snake Minigame Update

**Changes & Features:**

* **Minecraft verions:** Updates are available only for Minecraft 1.21.6 and above, cuz i'm lazy to recode this for older versions :D
* **Field Size Selection:** Choose Small, Medium, or Large grids at game start.
* **Wrap-Around Walls:** Snake now passes through field edges instead of crashing.
* **Food Weight System:** Different foods have spawn probabilities and point values:

| Food Item    | Weight | Points |
| ------------ | ------ | ------ |
| Apple        | 10     | 20     |
| Golden Apple | 25     | 5      |
| Carrot       | 8      | 15     |
| Potato       | 7      | 10     |
| Beetroot     | 6      | 8      |
| Melon Slice  | 9      | 10     |
| Bread        | 12     | 6      |
| Cookie       | 4      | 12     |
| Pumpkin Pie  | 18     | 4      |
| Cooked Beef  | 20     | 5      |

**Notes:**

* **Weight** affects how likely the food is to spawn (higher → more likely).
* **Points** are added to the player’s score when the food is eaten.
  
# Available games:

## Snake game
Classic snake game

![2025-10-2623-25-19online-video-cutter com-ezgif com-optimize](https://github.com/user-attachments/assets/f95756a5-68cd-4700-a23e-8dea9a209770)


## Simon says
A memory game where your task is to memorize the number of activation sequences of musical blocks, the further it gets, the more difficult it becomes.
<img width="891" height="628" alt="image" src="https://github.com/user-attachments/assets/8395995e-aaa6-4c1e-84f7-e96e11a6319f" />

## 2048: Tower

A block-merging puzzle game inspired by 2048. Drop ore blocks from the top and combine them to reach higher-value ores. The game blends 2048 logic with Tetris-like vertical dropping, creating a new strategic puzzle experience.

<img width="1920" height="983" alt="image" src="https://github.com/user-attachments/assets/ba20082d-5fff-459f-8af9-a99534b1f9df" />
