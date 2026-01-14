package it.crystalnest.hotbar_refill;

/**
 * Main plugin class.
 * 
 * TODO: Implement your plugin logic here.
 * 
 * @author YourName
 * @version 1.0.0
 */
public class HotbarRefill {

    private static HotbarRefill instance;
    
    /**
     * Constructor - Called when plugin is loaded.
     */
    public HotbarRefill() {
        instance = this;
        System.out.println("[HotbarRefill] Plugin loaded!");
    }
    
    /**
     * Called when plugin is enabled.
     */
    public void onEnable() {
        System.out.println("[HotbarRefill] Plugin enabled!");
        
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
        System.out.println("[HotbarRefill] Plugin disabled!");
        
        // TODO: Cleanup your plugin here
        // - Save data
        // - Stop services
        // - Close connections
    }
    
    /**
     * Get plugin instance.
     */
    public static HotbarRefill getInstance() {
        return instance;
    }
}
