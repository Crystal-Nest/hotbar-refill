package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
   * @param init plugin initialization context.
   */
  public HotbarRefill(@Nonnull JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log(getName() + " Plugin loaded!");
  }

  @Override
  protected void setup() {
    LOGGER.atInfo().log("Setting up plugin " + getName());
    getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, event -> {
      // TODO: maybe there is a more robust way to check which container it is.
      if (event.getEntity() instanceof Player player && event.getItemContainer().getCapacity() == player.getInventory().getHotbar().getCapacity()) {
        switch (event.getTransaction()) {
          case ItemStackTransaction transaction -> transaction.getSlotTransactions().forEach(t -> handleTransaction(player, t));
          case ItemStackSlotTransaction transaction -> handleTransaction(player, transaction);
          default -> {}
        }
      }
    });
  }

  /**
   * Handles an item stack slot transaction.
   * <p>
   * Checks if an item has been depleted from the hotbar and tries to refill it from the player's inventory.
   *
   * @param player player.
   * @param transaction item stack slot transaction.
   */
  private void handleTransaction(Player player, ItemStackSlotTransaction transaction) {
    LOGGER.atSevere().log("Transaction: " + transaction);
    LOGGER.atSevere().log("Transaction Action: " + transaction.getAction());
    LOGGER.atSevere().log("Transaction Query: " + transaction.getQuery());
    LOGGER.atSevere().log("Transaction Remainder: " + transaction.getRemainder());
    LOGGER.atSevere().log("Transaction slotBefore: " + transaction.getSlotBefore());
    LOGGER.atSevere().log("Transaction slotAfter: " + transaction.getSlotAfter());
    LOGGER.atSevere().log("Transaction output: " + transaction.getOutput());
    // TODO:
    //  When picking up water with an empty bucket while having another empty bucket in the inventory, the bucket in the inventory disappears: first the empty bucket in the hotbar gets removed, then this plugin refills the hotbar using the empty bucket in the inventory, then the game sets the bucket in the hotbar filled with water.
    //  Remove logs.
    if (transaction.getAction().isRemove() || transaction.getAction().isDestroy()) {
      ItemStack before = transaction.getSlotBefore();
      if (!ItemStack.isEmpty(before) && shouldRefill(before, transaction.getSlotAfter())) {
        for (Map.Entry<InteractionType, String> entry : before.getItem().getInteractions().entrySet()) {
          LOGGER.atSevere().log("Interaction Type: " + entry.getKey() + " -> " + entry.getValue());
        }
        if (!refill(player, transaction, candidate -> candidate.isEquivalentType(before) && !candidate.isBroken())) {
          refill(player, transaction, candidate -> (candidate.isEquivalentType(before) || (before.getMaxDurability() > 0 && isSameItemType(before, candidate))) && !candidate.isBroken());
        }
      }
      LOGGER.atSevere().log("----------------------------------------------------");
    }
  }

  /**
   * Determines whether the hotbar slot should be refilled based on the item stack before and after the transaction.
   *
   * @param before item stack before the transaction.
   * @param after item stack after the transaction.
   * @return whether the hotbar slot should be refilled.
   */
  private boolean shouldRefill(@Nonnull ItemStack before, @Nullable ItemStack after) {
    return ItemStack.isEmpty(after) || (!before.isBroken() && after.isBroken());
  }

  /**
   * Whether the two items are the same kind of item.
   *
   * @param itemStack1 first item.
   * @param itemStack2 second item.
   * @return whether the two items are the same kind of item.
   */
  private boolean isSameItemType(ItemStack itemStack1, ItemStack itemStack2) {
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
  private boolean isSameInteraction(Map<InteractionType, String> interactions1, Map<InteractionType, String> interactions2, InteractionType interaction) {
    return Objects.equals(interactions1.get(interaction), interactions2.get(interaction));
  }

  /**
   * Refill the hotbar slot if there is a matching item in the player's inventory.
   *
   * @param player player.
   * @param transaction item stack slot transaction.
   * @param matchCondition condition to determine if an item stack matches the depleted one.
   * @return true if the hotbar slot got refilled, false otherwise.
   */
  private boolean refill(Player player, ItemStackSlotTransaction transaction, Predicate<ItemStack> matchCondition) {
    ItemContainer container = new CombinedItemContainer(player.getInventory().getStorage(), player.getInventory().getBackpack());
    for (short slot = 0; slot < container.getCapacity(); ++slot) {
      ItemStack candidate = container.getItemStack(slot);
      if (!ItemStack.isEmpty(candidate) && matchCondition.test(candidate)) {
        container.moveItemStackFromSlotToSlot(slot, candidate.getQuantity(), player.getInventory().getHotbar(), transaction.getSlot());
        playSound(player);
        return true;
      }
    }
    return false;
  }

  /**
   * Plays a sound effect for the player when a hotbar slot gets refilled.
   *
   * @param player player.
   */
  public void playSound(Player player) {
    World world = player.getWorld();
    if (world != null) {
      int index = SoundEvent.getAssetMap().getIndex("SFX_Player_Pickup_Item");
      EntityStore store = world.getEntityStore();
      Ref<EntityStore> playerRef = player.getReference();
      world.execute(() -> {
        if (playerRef != null) {
          TransformComponent transform = store.getStore().getComponent(playerRef, EntityModule.get().getTransformComponentType());
          if (transform != null) {
            SoundUtil.playSoundEvent3dToPlayer(playerRef, index, SoundCategory.UI, transform.getPosition(), store.getStore());
          } else {
            LOGGER.atWarning().log("Could not play sound because the TransformComponent is null!");
          }
        } else {
          LOGGER.atWarning().log("Could not play sound because the Ref<EntityStore> for the player is null!");
        }
      });
    } else {
      LOGGER.atWarning().log("Could not play sound because the World is null!");
    }
  }
}
