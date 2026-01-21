package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

/**
 * Plugin configuration.
 */
public class HotbarRefillConfig {
  /**
   * Codec.
   */
  public static final BuilderCodec<HotbarRefillConfig> CODEC = BuilderCodec.builder(HotbarRefillConfig.class, HotbarRefillConfig::new)
    .append(new KeyedCodec<>("SoundEffect", SoundConfig.CODEC), (config, value) -> config.soundConfig = value, config -> config.soundConfig).add()
    .append(new KeyedCodec<>("InventoryAccess", InventoryConfig.CODEC), (config, value) -> config.inventoryConfig = value, config -> config.inventoryConfig).add()
    .append(new KeyedCodec<>("RefillBehavior", BehaviorConfig.CODEC), (config, value) -> config.behaviorConfig = value, config -> config.behaviorConfig).add()
    .build();

  /**
   * Refill sound configuration.
   */
  private SoundConfig soundConfig = new SoundConfig();

  /**
   * Inventory access configuration.
   */
  private InventoryConfig inventoryConfig = new InventoryConfig();

  /**
   * Refill behavior configuration.
   */
  private BehaviorConfig behaviorConfig = new BehaviorConfig();

  /**
   * Returns the refill sound configuration.
   *
   * @return refill sound configuration.
   */
  public SoundConfig soundConfig() {
    return soundConfig;
  }

  /**
   * Returns the inventory access configuration.
   *
   * @return inventory access configuration.
   */
  public InventoryConfig inventoryConfig() {
    return inventoryConfig;
  }

  /**
   * Returns the refill behavior configuration.
   *
   * @return refill behavior configuration.
   */
  public BehaviorConfig behaviorConfig() {
    return behaviorConfig;
  }

  /**
   * Refill inventory source.
   */
  public enum RefillSource {
    /**
     * Hotbar inventory.
     */
    HOTBAR("Hotbar"),
    /**
     * Storage inventory.
     */
    STORAGE("Storage"),
    /**
     * Backpack inventory.
     */
    BACKPACK("Backpack"),
    /**
     * Utility inventory.
     */
    UTILITY("Utility");

    /**
     * Returns the RefillSource from its ID.
     *
     * @param id ID.
     * @return RefillSource.
     */
    public static RefillSource fromId(String id) {
      return switch (id) {
        case "Hotbar" -> HOTBAR;
        case "Storage" -> STORAGE;
        case "Backpack" -> BACKPACK;
        case "Utility" -> UTILITY;
        default -> throw new IllegalArgumentException("Unknown RefillSource id: " + id);
      };
    }

    /**
     * Returns the corresponding ItemContainer for the given player.
     *
     * @param player player.
     * @return corresponding ItemContainer.
     */
    public ItemContainer toContainer(Player player) {
      return switch (id) {
        case "Hotbar" -> player.getInventory().getHotbar();
        case "Storage" -> player.getInventory().getStorage();
        case "Backpack" -> player.getInventory().getBackpack();
        case "Utility" -> player.getInventory().getUtility();
        default -> throw new IllegalArgumentException("Unknown RefillSource id: " + id);
      };
    }

    /**
     * ID.
     */
    private final String id;

    /**
     * @param id {@link #id}.
     */
    RefillSource(String id) {
      this.id = id;
    }

    /**
     * Returns the ID.
     *
     * @return ID.
     */
    public String id() {
      return id;
    }

    /**
     * Returns whether this source is enabled in the given inventory configuration.
     *
     * @param config inventory configuration.
     * @return whether this source is enabled.
     */
    public boolean enabled(InventoryConfig config) {
      return switch (id) {
        case "Hotbar" -> config.fromHotbar();
        case "Storage" -> config.fromStorage();
        case "Backpack" -> config.fromBackpack();
        case "Utility" -> config.fromUtility();
        default -> throw new IllegalArgumentException("Unknown RefillSource id: " + id);
      };
    }
  }

  /**
   * Refill sound configuration.
   */
  public static class SoundConfig {
    /**
     * Codec.
     */
    public static final BuilderCodec<SoundConfig> CODEC = BuilderCodec.builder(SoundConfig.class, SoundConfig::new)
      .append(new KeyedCodec<>("Enable", Codec.BOOLEAN), (config, value) -> config.enable = value, config -> config.enable).add()
      .append(new KeyedCodec<>("ID", Codec.STRING), (config, value) -> config.id = value, config -> config.id).add()
      .build();

    /**
     * Default refill sound.
     */
    public static final String DEFAULT_REFILL_SOUND = "SFX_Player_Pickup_Item";

    /**
     * Whether the sound effect is enabled.
     */
    private boolean enable = true;

    /**
     * Sound effect to play on refill.
     */
    private String id = DEFAULT_REFILL_SOUND;

    /**
     * Returns whether the sound effect is enabled.
     *
     * @return whether the sound effect is enabled.
     */
    public boolean enable() {
      return enable;
    }

    /**
     * Returns the sound effect ID to play on refill.
     *
     * @return sound effect ID.
     */
    public String id() {
      return id;
    }
  }

  /**
   * Inventory access configuration.
   */
  public static class InventoryConfig {
    /**
     * Codec.
     */
    public static final BuilderCodec<InventoryConfig> CODEC = BuilderCodec.builder(InventoryConfig.class, InventoryConfig::new)
      .append(new KeyedCodec<>("FromHotbar", Codec.BOOLEAN), (config, value) -> config.fromHotbar = value, config -> config.fromHotbar).add()
      .append(new KeyedCodec<>("FromBackpack", Codec.BOOLEAN), (config, value) -> config.fromBackpack = value, config -> config.fromBackpack).add()
      .append(new KeyedCodec<>("FromStorage", Codec.BOOLEAN), (config, value) -> config.fromStorage = value, config -> config.fromStorage).add()
      .append(new KeyedCodec<>("FromUtility", Codec.BOOLEAN), (config, value) -> config.fromUtility = value, config -> config.fromUtility).add()
      .append(new KeyedCodec<>("Priority", Codec.STRING_ARRAY), (config, value) -> config.priority = value, config -> config.priority).add()
      .build();

    /**
     * Whether the hotbar should be searched for suitable refill items.
     */
    private boolean fromHotbar = false;

    /**
     * Whether the backpack should be searched for suitable refill items.
     */
    private boolean fromBackpack = true;

    /**
     * Whether the storage should be searched for suitable refill items.
     */
    private boolean fromStorage = true;

    /**
     * Whether the utility should be searched for suitable refill items.
     */
    private boolean fromUtility = false;

    /**
     * Priority order for searching refill sources.
     */
    private String[] priority = new String[]{RefillSource.STORAGE.id(), RefillSource.BACKPACK.id(), RefillSource.HOTBAR.id(), RefillSource.UTILITY.id()};

    /**
     * Returns whether to search the hotbar for suitable refill items.
     *
     * @return whether to search the hotbar.
     */
    public boolean fromHotbar() {
      return fromHotbar;
    }

    /**
     * Returns whether to search the backpack for suitable refill items.
     *
     * @return whether to search the backpack.
     */
    public boolean fromBackpack() {
      return fromBackpack;
    }

    /**
     * Returns whether to search the storage for suitable refill items.
     *
     * @return whether to search the storage.
     */
    public boolean fromStorage() {
      return fromStorage;
    }

    /**
     * Returns whether to search the utility for suitable refill items.
     *
     * @return whether to search the utility.
     */
    public boolean fromUtility() {
      return fromUtility;
    }

    /**
     * Returns the priority order for searching refill sources.
     *
     * @return priority order.
     */
    public String[] priority() {
      return priority;
    }
  }

  /**
   * Refill behavior configuration.
   */
  public static class BehaviorConfig {
    /**
     * Codec.
     */
    public static final BuilderCodec<BehaviorConfig> CODEC = BuilderCodec.builder(BehaviorConfig.class, BehaviorConfig::new)
      .append(new KeyedCodec<>("Category", Type.CODEC), (config, value) -> config.category = value, config -> config.category).add()
      .append(new KeyedCodec<>("Similar", Type.CODEC), (config, value) -> config.similar = value, config -> config.similar).add()
      .build();

    /**
     * Category type configuration.
     */
    private Type category = new Type();

    /**
     * Similar type configuration.
     */
    private Type similar = new Type(true, true, false, true, false, true);

    /**
     * Returns the category type configuration.
     *
     * @return category type configuration.
     */
    public Type category() {
      return category;
    }

    /**
     * Returns the similar type configuration.
     *
     * @return similar type configuration.
     */
    public Type similar() {
      return similar;
    }

    /**
     * Type configuration.
     */
    public static class Type {
      /**
       * Codec.
       */
      public static final BuilderCodec<Type> CODEC = BuilderCodec.builder(Type.class, Type::new)
        .append(new KeyedCodec<>("Tool", Codec.BOOLEAN), (config, value) -> config.tool = value, config -> config.tool).add()
        .append(new KeyedCodec<>("Weapon", Codec.BOOLEAN), (config, value) -> config.weapon = value, config -> config.weapon).add()
        .append(new KeyedCodec<>("Block", Codec.BOOLEAN), (config, value) -> config.block = value, config -> config.block).add()
        .append(new KeyedCodec<>("Food", Codec.BOOLEAN), (config, value) -> config.food = value, config -> config.food).add()
        .append(new KeyedCodec<>("Potion", Codec.BOOLEAN), (config, value) -> config.potion = value, config -> config.potion).add()
        .append(new KeyedCodec<>("Other", Codec.BOOLEAN), (config, value) -> config.other = value, config -> config.other).add()
        .build();

      /**
       * Tool type.
       */
      private boolean tool;

      /**
       * Weapon type.
       */
      private boolean weapon;

      /**
       * Block type.
       */
      private boolean block;

      /**
       * Food type.
       */
      private boolean food;

      /**
       * Potion type.
       */
      private boolean potion;

      /**
       * Other type.
       */
      private boolean other;

      public Type() {
        this(true, true, true, true, true, true);
      }

      /**
       * @param tool {@link #tool}.
       * @param weapon {@link #weapon}.
       * @param block {@link #block}.
       * @param food {@link #food}.
       * @param potion {@link #potion}.
       * @param other {@link #other}.
       */
      public Type(boolean tool, boolean weapon, boolean block, boolean food, boolean potion, boolean other) {
        this.tool = tool;
        this.weapon = weapon;
        this.block = block;
        this.food = food;
        this.potion = potion;
        this.other = other;
      }

      /**
       * Returns whether tool type is enabled.
       *
       * @return whether tool type is enabled.
       */
      public boolean tool() {
        return tool;
      }

      /**
       * Returns whether weapon type is enabled.
       *
       * @return whether weapon type is enabled.
       */
      public boolean weapon() {
        return weapon;
      }

      /**
       * Returns whether block type is enabled.
       *
       * @return whether block type is enabled.
       */
      public boolean block() {
        return block;
      }

      /**
       * Returns whether food type is enabled.
       *
       * @return whether food type is enabled.
       */
      public boolean food() {
        return food;
      }

      /**
       * Returns whether potion type is enabled.
       *
       * @return whether potion type is enabled.
       */
      public boolean potion() {
        return potion;
      }

      /**
       * Returns whether other type is enabled.
       *
       * @return whether other type is enabled.
       */
      public boolean other() {
        return other;
      }
    }
  }
}
