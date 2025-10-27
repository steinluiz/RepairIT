package com.stein.repairit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

    private static final String CHAT_PREFIX = ChatColor.GREEN + "" + ChatColor.BOLD + "[" +
            ChatColor.WHITE + ChatColor.BOLD + "REPAIRIT" +
            ChatColor.GREEN + ChatColor.BOLD + "] " + ChatColor.RESET;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("repairit.admin") && RepairITMain.newVersion != null) {

            RepairITMain plugin = RepairITMain.getPlugin(RepairITMain.class);

            player.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage(CHAT_PREFIX + ChatColor.AQUA + "A new version is available: " + ChatColor.GREEN + RepairITMain.newVersion);
                player.sendMessage(CHAT_PREFIX + ChatColor.GRAY + "Download at: " + ChatColor.WHITE + "https://www.spigotmc.org/resources/" + plugin.RESOURCE_ID);
            }, 60L);
        }
    }
}