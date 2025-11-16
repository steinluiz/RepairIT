package com.stein.repairit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

final class Repairables {

    private static final Set<String> STONE = Set.of("STONE");
    private static final Set<String> IRON = Set.of("IRON");
    private static final Set<String> GOLD = Set.of("GOLDEN", "GOLD");
    private static final Set<String> DIAMOND = Set.of("DIAMOND");
    private static final Set<String> NETHERITE = Set.of("NETHERITE");
    private static final Set<String> CHAIN = Set.of("CHAINMAIL", "CHAIN");

    private static final EnumSet<Material> CACHE = build();

    private Repairables() {}

    static boolean isRepairable(ItemStack stack) {
        if (stack == null) return false;
        return CACHE.contains(stack.getType());
    }

    private static EnumSet<Material> build() {
        EnumSet<Material> set = EnumSet.noneOf(Material.class);
        for (Material m : Material.values()) {
            if (!m.isItem()) continue;
            String name = m.name().toUpperCase(Locale.ROOT);

            boolean isTool = name.endsWith("_SWORD") ||
                    name.endsWith("_PICKAXE") ||
                    name.endsWith("_AXE") ||
                    name.endsWith("_SHOVEL") ||
                    name.endsWith("_HOE");

            boolean isArmor = name.endsWith("_HELMET") ||
                    name.endsWith("_CHESTPLATE") ||
                    name.endsWith("_LEGGINGS") ||
                    name.endsWith("_BOOTS");

            if (!(isTool || isArmor)) continue;

            boolean matchStone = hasPrefix(name, STONE);
            boolean matchIron = hasPrefix(name, IRON);
            boolean matchGolden = hasPrefix(name, GOLD);
            boolean matchDiamond = hasPrefix(name, DIAMOND);
            boolean matchNetherite = hasPrefix(name, NETHERITE);
            boolean matchChain = hasPrefix(name, CHAIN);

            if (isTool && (matchStone || matchIron || matchGolden || matchDiamond || matchNetherite)) {
                set.add(m);
            } else if (isArmor && (matchGolden || matchDiamond || matchIron || matchChain || matchNetherite)) {
                set.add(m);
            }

        }
        return set;
    }

    private static boolean hasPrefix(String name, Set<String> prefixes) {
        for (String p : prefixes) {
            if (name.startsWith(p + "_")) return true;
        }
        return false;
    }
}