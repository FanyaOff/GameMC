# GameMC
A lightweight mod that adds an arcade game hub to the main menu and pause screen. To open game selection in singleplayer or multiplayer - use keybind (Default: Home)

This mod is in beta, if you find any bugs, open the bug report. 

**1.21 -> 1.21.5 versions will be updated when the mod comes out of beta**

In the plans:
- add api, that gave ability to modders to make own games and integrate it with GameMC & Minesweeper
- games design rework
- add more games (Flappy Bird, Ping Pong, 2048 WIP)

## For full changelogs navigate to [this page](https://github.com/FanyaOff/GameMC/blob/master/CHANGELOG.md) 

# Available Games

## Snake Game

A modernized take on the classic snake experience, enhanced with multiple progression systems, adaptive mechanics, and a fully dynamic food system.

* **Field Size Selection:** Choose between **Small**, **Medium**, or **Large** grids at the start.
* **Wrap-Around:** The snake passes through the edges of the field instead of crashing into them.
* **Score Multipliers:**

  * Small grid → **2×**
  * Medium grid → **1.5×**
  * Large grid → **1×**
* **Expanded Food System:**

  * Multiple food items can spawn simultaneously.
  * Each item has unique points and weight values that influence spawn chance.
  * Certain foods now have **negative effects**—they reduce score or shorten the snake.
* **Food table:**

| Item             | Points | Weight | Segments Removed |
| ---------------- | ------ | ------ | ---------------- |
| Potato           | 1      | 30     | 0                |
| Cookie           | 2      | 25     | 0                |
| Carrot           | 3      | 25     | 0                |
| Apple            | 4      | 20     | 0                |
| Bread            | 5      | 15     | 0                |
| Honey Bottle     | 6      | 15     | 0                |
| Cooked Beef      | 8      | 10     | 0                |
| Golden Apple     | 20     | 5      | 0                |
| Poisonous Potato | -3     | 25     | 0                |
| Rotten Flesh     | -5     | 10     | 1                |

**Notes:**

* **Weight** affects how likely the food is to spawn (higher → more likely).
* **Points** are added to the player’s score when the food is eaten.

![2025-10-2623-25-19online-video-cutter com-ezgif com-optimize](https://github.com/user-attachments/assets/f95756a5-68cd-4700-a23e-8dea9a209770)

---

## Simon Says

A rhythm-memory challenge where players must recall and repeat sequences of glowing blocks. Each round adds a new element, increasing difficulty gradually.

<img width="1920" height="1009" alt="image" src="https://github.com/user-attachments/assets/fecf3881-07a7-4fb1-bed8-7585566d02d6" />


---

## 2048: Tower

A vertical block-merging puzzle that combines the mechanics of **2048** with the falling-block dynamics of **Tetris**.

* **Ore-Based Merge Progression:** Drop ore blocks from above and fuse identical ones to climb through higher-tier ores.
* **Vertical Strategy:** The tower structure adds depth—positioning and timing matter as much as merging.

<img width="1920" height="983" alt="image" src="https://github.com/user-attachments/assets/ba20082d-5fff-459f-8af9-a99534b1f9df" />

