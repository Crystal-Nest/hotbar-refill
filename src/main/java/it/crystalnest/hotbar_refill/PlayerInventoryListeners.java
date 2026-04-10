package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * ECS event system that handles inventory change events for player entities.
 * <p>
 * Replaces the old global {@code LivingEntityInventoryChangeEvent} listener with the new ECS-based {@link InventoryChangeEvent} + {@link InventoryComponent} approach.
 */
public class PlayerInventoryListeners extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
  /**
   * Reference to the main plugin instance for accessing config and refill logic.
   */
  private final HotbarRefill plugin;

  /**
   * @param plugin main plugin instance.
   */
  public PlayerInventoryListeners(HotbarRefill plugin) {
    super(InventoryChangeEvent.class);
    this.plugin = plugin;
  }

  @Override
  public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InventoryChangeEvent event) {
    if (!(event.getInventory() instanceof InventoryComponent.Hotbar)) {
      return;
    }
    PlayerRef playerRef = archetypeChunk.getComponent(i, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) {
      return;
    }
    if (event.getTransaction().succeeded()) {
      switch (event.getTransaction()) {
        case ItemStackTransaction transaction -> transaction.getSlotTransactions().forEach(t -> plugin.handleTransaction(archetypeChunk, i, event.getInventory().getInventory(), playerRef, t));
        case ItemStackSlotTransaction transaction -> plugin.handleTransaction(archetypeChunk, i, event.getInventory().getInventory(), playerRef, transaction);
        default -> {}
      }
    }
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return Query.any();
  }
}
