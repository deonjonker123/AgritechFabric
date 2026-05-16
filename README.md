
![AT](https://raw.githubusercontent.com/deonjonker123/AgritechTwo/refs/heads/26.1.2/at_ban.png)

## Core Features

### **Automated Planters**

-   **Agritech Planter**: A simple wooden planter that automatically grows crops and saplings. Automatically outputs drops to a container under it. Available in all 11 vanilla wood types.

### **Cloche** (NeoForge only)
A glass bell jar that attaches to any planter, boosting both growth speed and harvest yield. Stacks with fertilizer for maximum efficiency. Configurable speed and yield multipliers.

- Right-click a planter with a cloche to attach it
- Shift-right-click with an empty hand to detach and recover it
- Breaking a cloched planter drops both items separately

### **Fertilizer System**

Fertilizer support with configurable speed and yield multipliers:

-   Vanilla bone meal
-   Mystical Agriculture fertilizers (NeoForge only)
-   Immersive Engineering fertilizers (NeoForge only)
-   Forbidden & Arcanus arcane bone meal (NeoForge only)

Fertilizer can be automated via hopper or pipe into any of the four cardinal sides of the planter.

## Mod Compatibility (NeoForge)

### **Supported Crop Mods**

- Mystical Agriculture & Mystical Agradditions
- Farmer's Delight
- Ars Nouveau
- Silent Gear
- Immersive Engineering
- Occultism
- Cobblemon
- Pam's HarvestCraft 2 - Crops
- Actually Additions
- Croptopia
- The Aether II

### **Supported Tree Mods**

- Ars Nouveau & Ars Elemental
- Forbidden & Arcanus
- Integrated Dynamics
- Silent Gear
- Occultism
- Cobblemon
- Pam's HarvestCraft 2 - Trees
- Croptopia
- EvilCraft
- The Aether II

### **Supported Soil Mods**

-   Mystical Agriculture farmlands
-   Farmer's Delight soils
-   Just Dire Things goosoils
- The Aether II

## Mod Compatibility (Fabric)

### **Supported Crop Mods**
- Farmer's Delight Refabricated
- Cobblemon
- Croptopia

### **Supported Tree Mods**
- Cobblemon
- Croptopia

### **Supported Soil Mods**
-   Farmer's Delight Refabricated soils

## Advanced Configuration System

1.  **Mod Compatibility Toggles**: Enable/disable specific mod integrations per-mod, so you only load what you need.
2.  **Crop/Sapling/Soil Database**: Comprehensive JSON-based system defining what grows on what, with full TOML override support for custom rules.

### **Live Config Reloading**

No server/client restart required. Changes to crops, soils, fertilizers via the overrider toml, or compatibility settings can be applied instantly via in-game commands.

| Command | Effect |
|--|--|
| `/agritechtwo reload` | Reloads all configs |
| `/agritechtwo reload plantables` | Reloads the crop/soil/sapling database only |
| `/agritechtwo reload config` | Reloads the main TOML config only |

Failed reloads report errors directly in chat rather than silently failing.

### **Interactive Placement**

-   Right-click with seeds to insert directly into planters
-   Right-click with saplings for instant placement
-   Right-click with soil blocks for instant placement
-   Right-click with hoes to till compatible blocks
- Right-click with mystical essence to convert vanilla farmland to the mystical farmland

### **Visual Feedback**

-   Real-time rendering of planted crops and soil types
-   Progress bars for all processing operations
-   Audio feedback for successful interactions

## Minecraft 26.1.2 Exclusive (in addition to everything above)

The following features are available exclusively on the 26.1.2 version of AgriTech.

### **Raised Beds**

A manual, early-game alternative to the planter. Available in all 12 vanilla wood types.

- Grows crops and saplings automatically, dropping harvests on the ground rather than storing them
- Receives a configurable speed bonus when placed outdoors with sky access during daytime
- Supports manual fertilizer application via right-clicking with a supported fertilizer item
- No cloche, no fertilizer slot, no pipe automation — designed for hands-on farming
- Full JEI and Jade integration

### **Crates**

An open-top wooden storage block with a 54-slot inventory — double chest capacity in a single block footprint. Available in all 12 vanilla wood types.

- Automatically collects item drops within a 5x5 area
- Collection can be toggled on/off via a button in the GUI
- Full item capability on all sides — pipes can insert and extract freely
- Works as a standalone decorative storage block independent of the farming system
- Configurable collection interval

### **JEI Integration**

- Compatible crop/soil combinations for planters and raised beds, showing drops as text with count ranges and chance percentages

### **Jade Integration**

- Displays current crop or sapling name
- Shows growth stage and progress percentage
- Shows active soil type and its growth modifier
- Shows sunlight boost status when active
- Shows cloche status with speed and yield modifiers (NeoForge only)
