package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;

/**
 * Represents the type of item.
 *
 * @param isTool
 * @param isWeapon
 * @param isBlock
 * @param isFood
 * @param isPotion
 */
public record ItemType(boolean isTool, boolean isWeapon, boolean isBlock, boolean isFood, boolean isPotion) {
  /**
   * Determines the item type based on its categories.
   *
   * @param item the item to evaluate.
   * @return the computed item type.
   */
  public static ItemType compute(Item item) {
    boolean isTool = false;
    boolean isWeapon = false;
    boolean isBlock = false;
    boolean isFood = false;
    boolean isPotion = false;
    for (String category : item.getCategories()) {
      if (category.equals("Items.Foods")) {
        isFood = true;
      } else if (category.equals("Items.Tools")) {
        isTool = true;
      } else if (category.equals("Items.Weapons")) {
        isWeapon = true;
      } else if (category.equals("Items.Potions")) {
        isPotion = true;
      } else if (category.startsWith("Blocks")) {
        isBlock = true;
      }
    }
    return new ItemType(isTool, isWeapon, isBlock, isFood, isPotion);
  }

  /**
   * Checks whether the item is allowed by the given behavior-type configuration.
   *
   * @param config behavior-type configuration.
   * @return whether the item is allowed.
   */
  public boolean qualifies(HotbarRefillConfig.BehaviorConfig.Type config) {
    return isTool && config.tool() || isWeapon && config.weapon() || isBlock && config.block() || isFood && config.food() || isPotion && config.potion() || (!isTool && !isWeapon && !isBlock && !isFood && !isPotion && config.other());
  }
}
