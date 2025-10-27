package com.stein.repairit;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class RepairITMain extends JavaPlugin {

    public static ConfigManager CONFIG;

    public static final class Keys {
        public static final NamespacedKey REPAIRIT = new NamespacedKey("repairit", "repairit");
        public static final NamespacedKey HIT_COUNT = new NamespacedKey("repairit", "hitcount");
        private Keys() {}
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        CONFIG = new ConfigManager(this);
        PluginCommand c = getCommand("repairit");

        getServer().getPluginManager().registerEvents(new RepairITListener(this), this);

        if (c != null) {
            RepairITCommand exec = new RepairITCommand(this);
            c.setExecutor(exec);
            c.setTabCompleter(exec);
        }
        getLogger().info("RepairIT is enabled!");

        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();

        meta.setDisplayName(ChatColor.WHITE + "Hammer");

        AttributeModifier dmgModifier = new AttributeModifier(
                UUID.fromString("8b2e5399-b04e-4fef-802c-f60037a3f36c"),
                "repairit:hammer_damage",
                -1.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, dmgModifier);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(Keys.REPAIRIT, PersistentDataType.INTEGER, 1);

        mace.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "repairit_hammer");
        ShapedRecipe recipe = new ShapedRecipe(key, mace);
        recipe.shape("AAA", "ABA", " B ");
        recipe.setIngredient('A', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.STICK);
        getServer().addRecipe(recipe);
    }

    @Override
    public void onDisable() {
        getLogger().info("RepairIT was disabled!");
    }
}