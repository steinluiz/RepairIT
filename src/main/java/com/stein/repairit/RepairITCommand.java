package com.stein.repairit;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import java.util.List;

public class RepairITCommand implements CommandExecutor, TabCompleter {

    private final RepairITMain plugin;

    private static final String CHAT_PREFIX = ChatColor.GREEN + "" + ChatColor.BOLD + "[" +
            ChatColor.WHITE + ChatColor.BOLD + "REPAIRIT" +
            ChatColor.GREEN + ChatColor.BOLD + "] " + ChatColor.RESET;

    public RepairITCommand(RepairITMain plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("repairit.admin")) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "No permission");
                return true;
            }
            plugin.reloadConfig();
            RepairITMain.CONFIG = new ConfigManager(plugin);
            sender.sendMessage(CHAT_PREFIX + ChatColor.GREEN + "Reload complete.");
            return true;
        }
        sender.sendMessage(CHAT_PREFIX + ChatColor.YELLOW + "Use: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String alias, String[] args) {
        if (args.length == 1) return List.of("reload");
        return List.of();
    }
}