package com.stein.repairit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection sec = cfg.getConfigurationSection("repair-per-material");
        if (sec == null) sec = cfg.createSection("repair-per-material");

        ensure(sec, "IRON_INGOT", 500);
        ensure(sec, "GOLD_INGOT", 500);
        ensure(sec, "DIAMOND", 500);
        ensure(sec, "NETHERITE_INGOT", 500);
        ConfigurationSection hammerSec = cfg.getConfigurationSection("hammer");
        if (hammerSec == null) hammerSec = cfg.createSection("hammer");

        ensure(hammerSec, "NAME", "Hammer");
        ensure(hammerSec, "ITEM", "MACE");
        plugin.saveConfig();
    }

    private void ensure(ConfigurationSection sec, String key, Object value) {
        if (!sec.contains(key)) {
            sec.set(key, value);
        }
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

    public void reload() { plugin.reloadConfig(); }
}
