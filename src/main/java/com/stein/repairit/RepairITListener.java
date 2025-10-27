package com.stein.repairit;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RepairITListener implements Listener {

    private final RepairITMain plugin;

    private static final double DISPLAY_RADIUS = 0.30;
    private static final double DAMAGE_RADIUS  = 0.20;

    private static final Quaternionf Q_ID    = new Quaternionf(0f, 0f, 0f, 1f);
    private static final Quaternionf Q_RIGHT = new Quaternionf(0.704f, 0f, 0f, 0.7f);

    public RepairITListener(RepairITMain plugin) {
        this.plugin = plugin;
    }

    private static boolean isAnvil(Material type) {
        return type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL;
    }

    private static boolean isRepairable(ItemStack stack) {
        return Repairables.isRepairable(stack);
    }

    private static Material getRepairIngredient(Material itemType) {
        String name = itemType.name();
        if (name.startsWith("NETHERITE_")) return Material.NETHERITE_INGOT;
        if (name.startsWith("DIAMOND_"))   return Material.DIAMOND;
        if (name.startsWith("GOLDEN_") || name.startsWith("GOLD_")) return Material.GOLD_INGOT;
        if (name.startsWith("IRON_") || name.startsWith("CHAINMAIL_") || name.startsWith("CHAIN_")) return Material.IRON_INGOT;
        if (name.startsWith("STONE_"))     return Material.COBBLESTONE;
        return null;
    }

    private static void dropAndKill(ItemDisplay display, Location dropAt) {
        ItemStack stack = display.getItemStack();
        if (stack != null && !stack.getType().isAir()) {
            Location dropLoc = dropAt.clone().add(0.5, 0.5, 0.5);
            stack.setAmount(1);
            dropLoc.getWorld().dropItem(dropLoc, stack);
        }
        display.remove();
    }

    private static void giveOrDrop(Player player, ItemStack one) {
        var leftovers = player.getInventory().addItem(one);
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(lf -> player.getWorld().dropItem(player.getLocation(), lf));
        }
    }

    private static ItemDisplay spawnDisplay(Location center, ItemStack stack, boolean material, float playerYaw) {
        ItemDisplay d = (ItemDisplay) center.getWorld().spawnEntity(center, EntityType.ITEM_DISPLAY);
        d.setItemStack(stack);

        d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        Vector3f translation = material ? new Vector3f(0f, 0.55f, 0f) : new Vector3f(0f, 0.52f, 0f);
        Vector3f scale       = material ? new Vector3f(0.4f, 0.4f, 0.4f) : new Vector3f(0.7f, 0.7f, 0.7f);
        d.setTransformation(new Transformation(translation, new Quaternionf(Q_ID), scale, new Quaternionf(Q_RIGHT)));

        d.setRotation(playerYaw + 40f, 0f);
        return d;
    }

    private static class Targets {
        final ItemDisplay tool;
        final ItemDisplay material;
        Targets(ItemDisplay tool, ItemDisplay material) { this.tool = tool; this.material = material; }
    }

    private static Targets findTargetsNear(Location center, double radius) {
        double bestTool = radius * radius, bestMat = radius * radius;
        ItemDisplay tool = null, material = null;

        for (Entity ent : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(ent instanceof ItemDisplay d)) continue;
            ItemStack s = d.getItemStack();
            if (s == null || s.getType().isAir()) continue;

            double dsq = d.getLocation().distanceSquared(center);
            if (s.getType().getMaxDurability() > 0) {
                if (dsq <= bestTool) { bestTool = dsq; tool = d; }
            } else {
                if (dsq <= bestMat)  { bestMat  = dsq; material = d; }
            }
        }
        return new Targets(tool, material);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakAnvil(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (!isAnvil(b.getType())) return;

        Location center = b.getLocation().clone().add(0.5, 0.5, 0.5);
        for (Entity ent : b.getWorld().getNearbyEntities(center, DISPLAY_RADIUS, DISPLAY_RADIUS, DISPLAY_RADIUS)) {
            if (ent instanceof ItemDisplay d) dropAndKill(d, b.getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClickAnvil(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || !isAnvil(clicked.getType())) return;

        Player player = event.getPlayer();
        Location center = clicked.getLocation().clone().add(0.5, 0.5, 0.5);

        Targets targets = findTargetsNear(center, DISPLAY_RADIUS);
        ItemStack hand = player.getInventory().getItemInMainHand();
        boolean handEmpty = hand.getType().isAir();

        ItemDisplay materialDisp = targets.material;
        ItemDisplay toolDisp     = targets.tool;

        if (materialDisp != null) {
            event.setCancelled(true);
            ItemStack matStack = materialDisp.getItemStack();
            if (matStack != null && !matStack.getType().isAir()) {
                ItemStack give = matStack.clone(); give.setAmount(1);
                giveOrDrop(player, give);

                int amt = matStack.getAmount();
                if (amt <= 1) materialDisp.remove();
                else { matStack.setAmount(amt - 1); materialDisp.setItemStack(matStack); }

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
            }
            return;
        }

        if (toolDisp != null) {
            ItemStack displayItem = toolDisp.getItemStack();
            if (displayItem == null || displayItem.getType().isAir()) {
                toolDisp.remove();
                return;
            }

            Material req = getRepairIngredient(displayItem.getType());
            boolean holdingMat = !handEmpty && hand.getType() == req && hand.getAmount() > 0;

            if (holdingMat) {
                event.setCancelled(true);
                ItemStack placed = hand.clone(); placed.setAmount(1);
                if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
                else player.getInventory().setItemInMainHand(null);

                spawnDisplay(center, placed, true, player.getLocation().getYaw());
                return;
            }
            if (handEmpty) {
                event.setCancelled(true);
                ItemStack give = displayItem.clone(); give.setAmount(1);
                toolDisp.remove();
                player.getInventory().setItemInMainHand(give);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
                return;
            }
            return;
        }
        if (isRepairable(hand)) {
            event.setCancelled(true);
            ItemStack placed = hand.clone(); placed.setAmount(1);
            if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
            else player.getInventory().setItemInMainHand(null);

            spawnDisplay(center, placed, false, player.getLocation().getYaw());
            return;
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e) {
        Block b = e.getBlock();
        if (b == null || !isAnvil(b.getType())) return;

        Player p = e.getPlayer();
        ItemStack inHand = p.getInventory().getItemInMainHand();

        ItemMeta inHandMeta = inHand.getItemMeta();
        if (inHandMeta == null) return;
        Integer mark = inHandMeta.getPersistentDataContainer().get(RepairITMain.Keys.REPAIRIT, PersistentDataType.INTEGER);
        if (mark == null || mark != 1) return;

        if (p.hasCooldown(inHand.getType())) return;
        p.setCooldown(inHand.getType(), 20);

        Location center = b.getLocation().clone().add(0.5, 0.5, 0.5);
        Targets targets = findTargetsNear(center, DAMAGE_RADIUS);
        ItemDisplay toolDisp     = targets.tool;
        ItemDisplay materialDisp = targets.material;

        if (toolDisp == null || materialDisp == null) return;

        PersistentDataContainer pdc = toolDisp.getPersistentDataContainer();
        Integer count = pdc.get(RepairITMain.Keys.HIT_COUNT, PersistentDataType.INTEGER);
        int next = (count == null ? 0 : count) + 1;
        pdc.set(RepairITMain.Keys.HIT_COUNT, PersistentDataType.INTEGER, next);

        b.getWorld().playSound(center, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

        int max = inHand.getType().getMaxDurability();
        if (max > 0) {
            if (inHandMeta instanceof Damageable dmgMeta) {
                int cur = dmgMeta.getDamage();
                dmgMeta.setDamage(Math.min(max, cur + 1));
                inHand.setItemMeta(inHandMeta);
            }
        }

        if (next >= 5) {
            Material matType = materialDisp.getItemStack().getType();
            int repairBy = RepairITMain.CONFIG.getRepairAmount(matType);

            ItemStack tool = toolDisp.getItemStack();
            ItemMeta meta = tool.getItemMeta();
            if (meta instanceof Damageable dmg) {
                int cur = dmg.getDamage();
                dmg.setDamage(Math.max(0, cur - repairBy));
                tool.setItemMeta(dmg);
                toolDisp.setItemStack(tool);
            }

            if (materialDisp != null && !materialDisp.isDead()) {
                materialDisp.remove();
                Location particleLoc = b.getLocation().add(0.5, 1.05, 0.5);
                b.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 10, 0.1, 0.1, 0.1, 0.0);
            }

            pdc.set(RepairITMain.Keys.HIT_COUNT, PersistentDataType.INTEGER, 0);
        }
    }
}