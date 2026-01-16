package it.crystalnest.hotbar_refill;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Plugin configuration.
 */
public class HotbarRefillConfig {
  /**
   * Codec.
   */
  public static final BuilderCodec<HotbarRefillConfig> CODEC = BuilderCodec.builder(HotbarRefillConfig.class, HotbarRefillConfig::new)
    .append(new KeyedCodec<>("RefillSound", Codec.STRING), (config, value) -> config.refillSound = value, config -> config.refillSound).add()
    .build();

  /**
   * Default refill sound.
   */
  public static final String DEFAULT_REFILL_SOUND = "SFX_Player_Pickup_Item";

  /**
   * Refill sound.
   */
  private String refillSound = DEFAULT_REFILL_SOUND;

  /**
   * Returns the refill sound.
   *
   * @return refill sound.
   */
  public String getRefillSound() {
    return refillSound;
  }
}
