package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Main plugin class.
 * <p>
 * TODO: Implement your plugin logic here.
 *
 * @author YourName
 * @version 1.0.0
 */
public class HotbarRefill extends JavaPlugin {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  /**
   * Constructor - Called when plugin is loaded.
   */
  public HotbarRefill(@Nonnull JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log(getName() + " Plugin loaded!");
  }

  @Override
  protected void setup() {
    LOGGER.atInfo().log("Setting up plugin " + getName());
    getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, event -> {
      // Check if the entity is a player and if the changed inventory is the hotbar.
      // TODO: maybe there is a more robust way to check which container it is.
      if (event.getEntity() instanceof Player player && event.getItemContainer().getCapacity() == player.getInventory().getHotbar().getCapacity()) {
        switch (event.getTransaction()) {
          case ItemStackTransaction transaction -> transaction.getSlotTransactions().forEach(t -> handleTransaction(player, t));
          case ItemStackSlotTransaction transaction -> handleTransaction(player, transaction);
          default -> LOGGER.atSevere().log("Transaction detected: " + event.getTransaction());
        }
      }
    });
  }

  private void handleTransaction(Player player, ItemStackSlotTransaction transaction) {
    LOGGER.atSevere().log("Transaction: " + transaction);
    LOGGER.atSevere().log("Transation Action: " + transaction.getAction());
    LOGGER.atSevere().log("Transation Query: " + transaction.getQuery());
    LOGGER.atSevere().log("Transation Remainder: " + transaction.getRemainder());
    LOGGER.atSevere().log("Transaction slotAfter: " + transaction.getSlotAfter());
    LOGGER.atSevere().log("Transaction slotBefore: " + transaction.getSlotBefore());
    LOGGER.atSevere().log("Transaction output: " + transaction.getOutput());
    if (transaction.getAction().isRemove() && !transaction.getAction().isDestroy()) {
      // TODO:
      //  Check hoes (they are tools, but for some reason don't have the getTool() value).
      //  Check spellbooks (need mana to do so).
      //  Check other inventories (e.g., backpack and tools and quiver) (can't figure out a way to test it in-game) (might be useful to use methods like getCombinedStorageFirst instead of just getStorage to check refills, giving priority to the inventory; we probably also want to exclude the armor slots and maybe the tools, depending on what that container is).
      //  Distinguish between melee weapons and ranged weapons.
      //  Play sound.
      //  Remove logs.

      if (!ItemStack.isEmpty(transaction.getSlotBefore()) && transaction.getSlotBefore().getQuantity() == 1) {
        ItemStack before = transaction.getSlotBefore();
        Item item = before.getItem();
        for (String cat : item.getCategories()) {
          LOGGER.atSevere().log("Item category: " + cat);
        }
        // Check if the item is a tool.
        if (item.getTool() != null) {
          // If it has been broken with this interaction (it was NOT already broken) and there is no other tool of the same type in the inventory (first refill failed), try to refill with a better tool.
          if (
            (ItemStack.isEmpty(transaction.getSlotAfter()) || (!transaction.getSlotBefore().isBroken() && transaction.getSlotAfter().isBroken())) &&
            !refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.isEquivalentType(before) && !itemStack.isBroken())
          ) {
            refill(
              player.getInventory().getStorage(),
              player.getInventory().getHotbar(),
              transaction,
              itemStack -> itemStack.getItem().getTool() != null && Stream.of(itemStack.getItem().getTool().getSpecs()).allMatch(spec -> Arrays.stream(item.getTool().getSpecs()).anyMatch(s -> s.getGatherType().equals(spec.getGatherType()) && spec.getPower() >= s.getPower() && spec.getQuality() >= s.getQuality()))
            );
          }
          // Check if the item is a weapon.
        } else if (item.getWeapon() != null) {
          // If it has been broken with this interaction (it was NOT already broken) and there is no other weapon of the same type in the inventory (first refill failed), try to refill with another weapon.
          if (
            (ItemStack.isEmpty(transaction.getSlotAfter()) || (!transaction.getSlotBefore().isBroken() && transaction.getSlotAfter().isBroken())) &&
            !refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.isEquivalentType(before) && !itemStack.isBroken())
          ) {
            refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.getItem().getWeapon() != null);
          }
          // Otherwise, just try to refill with the same item type.
        } else if (ItemStack.isEmpty(transaction.getSlotAfter()) || transaction.getSlotAfter().isBroken()) {
          refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.isEquivalentType(before));
        }
      }
      LOGGER.atSevere().log("----------------------------------------------------");
    }
  }

  private boolean refill(ItemContainer container, ItemContainer hotbar, ItemStackSlotTransaction transaction, Predicate<ItemStack> matchCondition) {
    for(short slot = 0; slot < container.getCapacity(); ++slot) {
      ItemStack itemStack = container.getItemStack(slot);
      if (!ItemStack.isEmpty(itemStack) && matchCondition.test(itemStack)) {
        container.moveItemStackFromSlotToSlot(slot, itemStack.getQuantity(), hotbar, transaction.getSlot());
        return true;
      }
    }
    return false;
  }
}
