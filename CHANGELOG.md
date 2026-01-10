# 1.3.2

- Important fix in solitare [(by mr_GrANTt)](https://github.com/FanyaOff/GameMC/pull/13)

# 1.3.1

* **Very important fix:** Game resets after pressing f11

# 1.3

* Added solitare game

## 1.2.2

### **Fixes in snake game:**

* **Score multipiers:** Score multipier now depends on grid size (2x for small, 1.5x for medium and 1x for big)
* **Negative food:** New feature, that allows you to lose score or lose the size of your snake if you eat rotten flesh or poisonous potato
* **More food:** The amount of food can now spawn more than 1 piece per field, and change when you eat 1 of the pieces of food.
* **Food weight system fix:** With negative & more food update, weight table look like this:

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

### Small changes:
* Added update notification below game selection screen, that indicates when new version is out, based on modrinth api

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
