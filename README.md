![Hotbar Refill banner](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/banner.png "Hotbar Refill banner")

---

## 📝 **Description**

Automatically refill your hotbar when running out of items if a replacement exists in your inventory.  
Works with weapons and tools as well, refilling your slot when they break if a suitable replacement is found in your inventory.

Every feature of this mod is highly configurable individually, allowing you to customize the behavior to your liking!

## ✨ **Features**

Run out of blocks, foods, ammo, or really whatever? Hotbar Refill has got you covered!

Whenever you run out of an item in your hotbar, it will automatically search your inventory and backpack for a matching item and refill the hotbar slot!  
Works with stackable items (e.g., blocks, food, arrows, etc.) as well as weapons and tools, making sure to give you only suitable replacements!

When an item gets replaced, you'll get a sound notification so you know when a refill has occurred.

The configuration is explained in details further below.

Examples of hotbar refills include:

- Running out of block to place:  
  ![Block refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/block-refill.gif "Block refill")
- Running out of food to eat:  
  ![Food refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/food-refill.gif "Food refill")
- Tossing away items:  
  ![Toss refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/toss-refill.gif "Toss refill")
- Breaking a tool:  
  ![Pickaxe refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/pickaxe-refill.gif "Pickaxe refill")  
  ![Hoe refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/hoe-refill.gif "Hoe refill")
- Breaking a weapon while fighting:  
  ![Weapon refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/weapon-refill.gif "Weapon refill")
- Keeping an eye on your ammo:  
  ![Ammo refill](https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/hotbar-refill/bow-refill.gif "Ammo refill")

## ⚙️ **Configuration**

Configuration is located at `mods/CrystalNest_HotbarRefill/config.toml`.

```json
{
  // Sound effect configuration.
  "SoundEffect": {
    // Whether to play the sound on refill.
    "Enable": true,
    // Sound effect ID to play on refill.
    // Check the full list of in-game sounds here: https://hytalemodding.dev/en/docs/server/sounds
    "ID": "SFX_Player_Pickup_Item"
  },
  // Inventory access configuration.
  "InventoryAccess": {
    // Whether to search in the hotbar for refill items.
    "FromHotbar": false,
    // Whether to search in the backpack for refill items.
    "FromBackpack": true,
    // Whether to search in the main storage for refill items.
    "FromStorage": true,
    // Whether to search in the utility slots for refill items.
    "FromUtility": false,
    // Priority order for searching inventory sections.
    // NEVER REMOVE or ADD entries to this list, ONLY MOVE the existing ones around.
    "Priority": [
      "Storage",
      "Backpack",
      "Hotbar",
      "Utility"
    ]
  },
  // Refill behavior configuration.
  "RefillBehavior": {
    // Categories of items to automatically refill when running out.
    "Category": {
      "Tool": true,
      "Weapon": true,
      "Block": true,
      "Food": true,
      "Potion": true,
      "Other": true
    },
    // Matching strategy to use when searching for refill items.
    // If false, only exact matches will be considered.
    // If true, similar items will also be considered (e.g., any type of pickaxe for a broken pickaxe).
    "Similar": {
      "Tool": true,
      "Weapon": true,
      "Block": false,
      "Food": true,
      "Potion": false,
      "Other": true
    }
  }
}
```
The default values are the ones shown above. Feel free to modify them to your liking!

## 🗳️ **Issues and suggestions**

If you encounter any issues or have suggestions for new features, please open an issue on the [GitHub repository](https://github.com/Crystal-Nest/hotbar-refill/issues) or hop into our [Discord server](https://discord.gg/BP6EdBfAmt) and let us know!

## 📜 **License and right of use**

Feel free to use this mod for any modpack or video, just be sure to give credit and possibly link [here](https://github.com/crystal-nest/hotbar-refill#readme).  
This project is published under the [Crystal Nest Community License v1](https://github.com/crystal-nest/hotbar-refill/blob/master/LICENSE).

## ❤️ **Support us**

<a href="https://crystalnest.it"><img alt="Crystal Nest Website" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/crystal-nest/pic512.png" width="14.286%"></a><a href="https://discord.gg/BP6EdBfAmt"><img alt="Discord" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/discord/discord512.png" width="14.286%"></a><a href="https://www.patreon.com/crystalspider"><img alt="Patreon" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/patreon/patreon512.png" width="14.286%"></a><a href="https://ko-fi.com/crystalspider"><img alt="Ko-fi" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/kofi/kofi512.png" width="14.286%"></a><a href="https://github.com/Crystal-Nest"><img alt="Our other projects" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/github/github512.png" width="14.286%"><a href="https://modtale.net/creator/CrystalNest"><img alt="Modtale" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/modtale/modtale512.png" width="14.286%"></a><a href="https://www.curseforge.com/members/crystalspider/projects"><img alt="CurseForge" src="https://raw.githubusercontent.com/crystal-nest/mod-fancy-assets/main/curseforge/curseforge512.png" width="14.286%"></a>

[![Bisect Hosting](https://www.bisecthosting.com/partners/custom-banners/d559b544-474c-4109-b861-1b2e6ca6026a.webp "Bisect Hosting")](https://bisecthosting.com/crystalspider)
