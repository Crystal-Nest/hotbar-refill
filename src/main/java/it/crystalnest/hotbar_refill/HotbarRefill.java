package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.IBaseEvent;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.command.commands.player.inventory.GiveCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.HotbarManager;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.WorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

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

  private static HotbarRefill instance;

  /**
   * Constructor - Called when plugin is loaded.
   */
  public HotbarRefill(@Nonnull JavaPluginInit init) {
    super(init);
    instance = this;
    LOGGER.atInfo().log(getName() + " Plugin loaded!");
  }

  @Override
  protected void setup() {
    LOGGER.atInfo().log("Setting up plugin " + getName());
    getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, e -> {
      if (e.getEntity() instanceof Player player && e.getTransaction() instanceof ItemStackSlotTransaction transaction) {
        LOGGER.atSevere().log("Transaction type: ItemStackSlotTransaction");
        LOGGER.atSevere().log("Transation Query: " + transaction.getQuery());
        LOGGER.atSevere().log("Transation Remainder: " + transaction.getRemainder());
        LOGGER.atSevere().log("Transaction slotAfter: " + transaction.getSlotAfter());
        if (transaction.getSlotAfter() != null) {
          LOGGER.atSevere().log("Transaction slotAfterEmpty: " + transaction.getSlotAfter().isEmpty());
        }
        LOGGER.atSevere().log("Transaction slotBefore: " + transaction.getSlotAfter());
        // TODO:
        //  Check action when dropping.
        //  Check hoes.
        //  Check spellbooks.
        //  Check arrows (when they run out in the hotbar due to being shot) (as well as guns and blowguns with their respective ammos).
        //  Check bombs.
        //  Play sound.
        //  Remove logs.
        //  Check related ECS instead of event.



        if (transaction.getQuery() != null && transaction.getQuery().getQuantity() == 1) {
          ItemStack query = transaction.getQuery();
          Item item = query.getItem();
          // Check if the item is a tool.
          if (item.getTool() != null) {
            // If it has been broken with this interaction (it was NOT already broken) and there is no other tool of the same type in the inventory (first refill failed), try to refill with a better tool.
            if (
              transaction.getSlotAfter() != null &&
              transaction.getSlotBefore() != null &&
              !transaction.getSlotBefore().isBroken() &&
              transaction.getSlotAfter().isBroken() &&
              !refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.isEquivalentType(query) && !itemStack.isBroken(), player)
            ) {
              refill(
                player.getInventory().getStorage(),
                player.getInventory().getHotbar(),
                transaction,
                itemStack -> itemStack.getItem().getTool() != null && Stream.of(itemStack.getItem().getTool().getSpecs()).allMatch(spec -> Arrays.stream(item.getTool().getSpecs()).anyMatch(s -> s.getGatherType().equals(spec.getGatherType()) && spec.getPower() >= s.getPower() && spec.getQuality() >= s.getQuality())),
                player
              );
            }
            // Check if the item is a weapon.
          } else if (item.getWeapon() != null) {
            // If it has been broken with this interaction (it was NOT already broken) and there is no other weapon of the same type in the inventory (first refill failed), try to refill with another weapon.
            if (
              transaction.getSlotAfter() != null &&
              transaction.getSlotBefore() != null &&
              !transaction.getSlotBefore().isBroken() &&
              transaction.getSlotAfter().isBroken() &&
              !refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.isEquivalentType(query) && !itemStack.isBroken(), player)
            ) {
              refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.getItem().getWeapon() != null,player);
            }
          // Otherwise, just try to refill with the same item type.
          } else {
            refill(player.getInventory().getStorage(), player.getInventory().getHotbar(), transaction, itemStack -> itemStack.isEquivalentType(query),player);
          }
        }
        LOGGER.atSevere().log("----------------------------------------------------");
      }
    });
  }


  private boolean refill(ItemContainer container, ItemContainer hotbar, ItemStackSlotTransaction transaction, Predicate<ItemStack> matchCondition, Player p) {
    for(short slot = 0; slot < container.getCapacity(); ++slot) {
      ItemStack itemStack = container.getItemStack(slot);

      if (!ItemStack.isEmpty(itemStack) && matchCondition.test(itemStack)) {
        container.moveItemStackFromSlotToSlot(slot, itemStack.getQuantity(), hotbar, transaction.getSlot());
        playSound(p);
        return true;
      }
    }
    return false;
  }

  /**
   * Play Sound on player location.
   */
  public void playSound(Player player){
    LOGGER.atSevere().log("Playing Sound");
    //SFX_Player_Pickup_Item
    int index = SoundEvent.getAssetMap().getIndex("SFX_Player_Grab_Item");
    World world = player.getWorld();
    assert world != null;
    EntityStore store = world.getEntityStore();
    Ref<EntityStore> playerRef = player.getReference();
    world.execute(() -> {
      assert playerRef != null;
      TransformComponent transform = store.getStore().getComponent(playerRef, EntityModule.get().getTransformComponentType());
      assert transform != null;
      SoundUtil.playSoundEvent3dToPlayer(playerRef, index, SoundCategory.UI, transform.getPosition(), store.getStore());
    });
  }
  
  /**
   * Get plugin instance.
   */
  public static HotbarRefill getInstance() {
    return instance;
  }

  /**
   * Called when plugin is enabled.
   */
  public void onEnable() {
    LOGGER.atSevere().log("[HotbarRefill] Plugin enabled!");
//    LivingEntityInventoryChangeEvent;
//    ItemContainer.ItemContainerChangeEvent;
//    PlayerEvent;
//    PlayerMouseButtonEvent;
//    DropItemEvent;
//    PlaceBlockEvent;


    // TODO: Initialize your plugin here
    // - Load configuration
    // - Register event listeners
    // - Register commands
    // - Start services
  }

  /**
   * Called when plugin is disabled.
   */
  public void onDisable() {
    LOGGER.atSevere().log("[HotbarRefill] Plugin disabled!");

    // TODO: Cleanup your plugin here
    // - Save data
    // - Stop services
    // - Close connections
  }
}
