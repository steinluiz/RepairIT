package com.stein.repairit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }


    public int getRepairAmount(Material material) {
        return plugin.getConfig().getInt("repair-per-material." + material.name(), 0);
    }
    public String getHammerName() {
        return plugin.getConfig().getString("hammer.NAME", "Hammer");
    }

    public Material getHammerMaterial() {
        String matName = plugin.getConfig().getString("hammer.ITEM", "MACE");
        Material mat = Material.matchMaterial(matName);
        return mat != null ? mat : Material.MACE;
    }

    public int getHammerCustomModelData() {
        if (plugin.getConfig().contains("hammer.CUSTOM_MODEL_DATA")) {
            return plugin.getConfig().getInt("hammer.CUSTOM_MODEL_DATA");
        }
        return -1;
    }

    public boolean isCraftingEnabled() {
        return plugin.getConfig().getBoolean("hammer.crafting.enabled", true);
    }

    public String[] getCraftingShape() {
        List<String> shapeList = plugin.getConfig().getStringList("hammer.crafting.shape");

        if (shapeList == null || shapeList.isEmpty()) {
            shapeList = Arrays.asList("AAA", "ABA", " B ");
        }

        return shapeList.toArray(new String[0]);
    }

    public Map<Character, Material> getCraftingIngredients() {
        Map<Character, Material> ingredients = new HashMap<>();
        ConfigurationSection ingredientsSec = plugin.getConfig().getConfigurationSection("hammer.crafting.ingredients");

        if (ingredientsSec != null) {
            for (String key : ingredientsSec.getKeys(false)) {
                if (key.length() == 1) {
                    char ingredientChar = key.charAt(0);
                    String matName = ingredientsSec.getString(key);

                    if (matName != null) {
                        Material material = Material.matchMaterial(matName);

                        if (material != null) {
                            ingredients.put(ingredientChar, material);
                        } else {
                            plugin.getLogger().warning("Invalid material'" + matName +  key);
                        }
                    }
                }
            }
        }

        if (ingredients.isEmpty()) {
            plugin.getLogger().info("No custom ingredient found, using default (IRON_INGOT, STICK).");
            ingredients.put('A', Material.IRON_INGOT);
            ingredients.put('B', Material.STICK);
        }

        return ingredients;
    }
    public void reload() { plugin.reloadConfig(); }
}
