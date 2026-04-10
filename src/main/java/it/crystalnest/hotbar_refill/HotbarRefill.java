package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * HotbarRefill plugin main class.
 */
public class HotbarRefill extends JavaPlugin {
  /**
   * Plugin logger.
   */
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  /**
   * Plugin configuration.
   */
  private final Config<HotbarRefillConfig> config = withConfig(HotbarRefillConfig.CODEC);

  /**
   * @param init plugin initialization context.
   */
  public HotbarRefill(@Nonnull JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log(getName() + " Plugin loaded!");
  }

  /**
   * Determines whether the hotbar slot should be refilled based on the item stack before and after the transaction.
   *
   * @param before item stack before the transaction.
   * @param after item stack after the transaction.
   * @return whether the hotbar slot should be refilled.
   */
  private static boolean shouldRefill(@Nonnull ItemStack before, @Nullable ItemStack after) {
    return ItemStack.isEmpty(after) || (!before.isBroken() && after.isBroken());
  }

  /**
   * Ad-hoc check for buckets to prevent deleting empty buckets from the inventory.
   *
   * @param before item stack before the transaction.
   * @param query transaction query.
   * @return whether the bucket check passes.
   */
  private static boolean bucketCheck(ItemStack before, ItemStack query) {
    return !before.getItemId().contains("Bucket") || !before.isEquivalentType(query);
  }

  /**
   * Whether the two items are the same kind of item.
   *
   * @param itemStack1 first item.
   * @param itemStack2 second item.
   * @return whether the two items are the same kind of item.
   */
  private static boolean isSameType(ItemStack itemStack1, ItemStack itemStack2) {
    if (itemStack1.isEquivalentType(itemStack2)) {
      return true;
    }
    Item item1 = itemStack1.getItem();
    Item item2 = itemStack2.getItem();
    if (item1.getUtility().isCompatible() != item2.getUtility().isCompatible()) {
      return false;
    }
    if (item1.getTool() != item2.getTool() && item1.getWeapon() != item2.getWeapon()) {
      return false;
    }
    String[] categories1 = item1.getCategories();
    String[] categories2 = item2.getCategories();
    if (categories1.length > categories2.length) {
      return false;
    }
    for (String category1 : categories1) {
      boolean match = false;
      for (String category2 : categories2) {
        if (category1.equals(category2)) {
          match = true;
          break;
        }
      }
      if (!match) {
        return false;
      }
    }
    Map<InteractionType, String> interactions1 = item1.getInteractions();
    Map<InteractionType, String> interactions2 = item2.getInteractions();
    return (interactions1.isEmpty() && interactions2.isEmpty()) || (isSameInteraction(interactions1, interactions2, InteractionType.Primary) && isSameInteraction(interactions1, interactions2, InteractionType.Secondary));
  }

  /**
   * Checks whether the two items have the same interaction for the given interaction type.
   *
   * @param interactions1 first item interaction map.
   * @param interactions2 second item interaction map.
   * @param interaction interaction type to compare.
   * @return whether the two items have the same interaction.
   */
  private static boolean isSameInteraction(Map<InteractionType, String> interactions1, Map<InteractionType, String> interactions2, InteractionType interaction) {
    return Objects.equals(interactions1.get(interaction), interactions2.get(interaction));
  }

  /**
   * Returns the plugin configuration.
   *
   * @return plugin configuration.
   */
  public HotbarRefillConfig getConfig() {
    return config.get();
  }

  @Override
  protected void setup() {
    LOGGER.atInfo().log("Setting up plugin " + getName());
    getEntityStoreRegistry().registerSystem(new PlayerInventoryListeners(this));
  }

  @Override
  protected void start() {
    config.save();
  }

  /**
   * Handles an item stack slot transaction.
   * <p>
   * Checks if an item has been depleted from the hotbar and tries to refill it from the player's inventory.
   *
   * @param archetypeChunk the archetype chunk for accessing entity components.
   * @param entityIndex the entity index in the archetype chunk.
   * @param hotbar the hotbar item container.
   * @param playerRef the player reference for sound playback.
   * @param transaction item stack slot transaction.
   */
  void handleTransaction(ArchetypeChunk<EntityStore> archetypeChunk, int entityIndex, ItemContainer hotbar, PlayerRef playerRef, ItemStackSlotTransaction transaction) {
    if (transaction.getAction().isRemove() || transaction.getAction().isDestroy()) {
      ItemStack before = transaction.getSlotBefore();
      if (!ItemStack.isEmpty(before)) {
        ItemType type = ItemType.compute(before.getItem());
        if (
          type.qualifies(getConfig().behaviorConfig().category()) &&
          shouldRefill(before, transaction.getSlotAfter()) &&
          bucketCheck(before, transaction.getQuery()) &&
          !refill(archetypeChunk, entityIndex, hotbar, playerRef, transaction, candidate -> candidate.isEquivalentType(before) && !candidate.isBroken()) &&
          type.qualifies(getConfig().behaviorConfig().similar())
        ) {
          refill(archetypeChunk, entityIndex, hotbar, playerRef, transaction, candidate -> isSameType(before, candidate) && !candidate.isBroken());
        }
      }
    }
  }

  /**
   * Refill the hotbar slot if there is a matching item in the player's inventory.
   *
   * @param archetypeChunk the archetype chunk for accessing entity components.
   * @param entityIndex the entity index in the archetype chunk.
   * @param hotbar the hotbar item container.
   * @param playerRef the player reference for sound playback.
   * @param transaction item stack slot transaction.
   * @param matchCondition condition to determine if an item stack matches the depleted one.
   * @return true if the hotbar slot got refilled, false otherwise.
   */
  private boolean refill(ArchetypeChunk<EntityStore> archetypeChunk, int entityIndex, ItemContainer hotbar, PlayerRef playerRef, ItemStackSlotTransaction transaction, Predicate<ItemStack> matchCondition) {
    ItemContainer container = getContainer(archetypeChunk, entityIndex);
    for (short slot = 0; slot < container.getCapacity(); ++slot) {
      ItemStack candidate = container.getItemStack(slot);
      if (!ItemStack.isEmpty(candidate) && matchCondition.test(candidate)) {
        container.moveItemStackFromSlotToSlot(slot, candidate.getQuantity(), hotbar, transaction.getSlot());
        playSound(playerRef);
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the appropriate (combined) item container to search for refill items, based on the plugin configuration.
   * <p>
   * Each inventory section (Hotbar, Storage, Backpack, Utility) is a separate ECS component.
   * This method retrieves each enabled section from the archetype chunk and combines them.
   *
   * @param archetypeChunk the archetype chunk for accessing entity components.
   * @param entityIndex the entity index in the archetype chunk.
   * @return appropriate item container.
   */
  private ItemContainer getContainer(ArchetypeChunk<EntityStore> archetypeChunk, int entityIndex) {
    HotbarRefillConfig.InventoryConfig inventoryConfig = getConfig().inventoryConfig();
    String[] priorities = getConfig().inventoryConfig().priority();
    List<ItemContainer> containers = new ArrayList<>(priorities.length);
    for (String id : priorities) {
      HotbarRefillConfig.RefillSource source = HotbarRefillConfig.RefillSource.fromId(id);
      if (source.enabled(inventoryConfig)) {
        InventoryComponent component = archetypeChunk.getComponent(entityIndex, source.toComponentType());
        if (component != null) {
          containers.add(component.getInventory());
        }
      }
    }
    return new CombinedItemContainer(containers.toArray(ItemContainer[]::new));
  }

  /**
   * Plays a sound effect for the player when a hotbar slot gets refilled.
   *
   * @param playerRef the player reference.
   */
  private void playSound(PlayerRef playerRef) {
    if (getConfig().soundConfig().enable()) {
      SoundUtil.playSoundEvent2dToPlayer(playerRef, SoundEvent.getAssetMap().getIndexOrDefault(getConfig().soundConfig().id(), SoundEvent.getAssetMap().getIndex(HotbarRefillConfig.SoundConfig.DEFAULT_REFILL_SOUND)), SoundCategory.UI);
    }
  }
}
