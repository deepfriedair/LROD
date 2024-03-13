package com.braydenbower;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private HashMap<UUID, Long> cooldown;
    private static int numLightnings;
    private static long cooldownTime;
    private static final int MAX_LIGHTNINGS = 100;

    @Override
    public void onEnable() {
        loadConfigValues();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        cooldown = new HashMap<>();
    }

    private void loadConfigValues() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        FileConfiguration config = getConfig();
        numLightnings = config.getInt("numLightnings", 1);
        cooldownTime = config.getLong("cooldownTime", 5000);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            // Console command logic
            if (args.length == 1) {
                String targetPlayerName = args[0];
                Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

                if (targetPlayer != null) {
                    giveLightningRod(targetPlayer);
                    sender.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.GREEN + "Gave Lightning Rod to " + targetPlayerName + "!");
                } else {
                    sender.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Player " + targetPlayerName + " not found or not online.");
                }
            } else {
                sender.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Usage: /lrod <playerName>");
            }
            return true;
        }

        // Player command logic
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "[LROD] Only players can use this!");
            return false;
        }

        Player p = (Player) sender;

        if (cmd.getLabel().equalsIgnoreCase("lrod")) {
            if (args.length == 0) {
                // /lrod without arguments, give Rod to the command sender
                giveLightningRod(p);
                p.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.GREEN + "You've got a Lightning Rod!");
            } else if (args.length == 1 && !args[0].equalsIgnoreCase("setlightnings")) {
                String targetPlayerName = args[0];
                Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

                if (targetPlayer != null) {
                    sendLightningRod(p, targetPlayer);
                    sender.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.GREEN + "Gave Lightning Rod to " + targetPlayerName + "!");
                } else {
                    sender.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Player " + targetPlayerName + " not found or not online.");
                }

            }else if (args.length == 2 && args[0].equalsIgnoreCase("setlightnings")) {
                if (p.hasPermission("lrod.setlightnings")) {
                    setLightningsCommand(p, args);
                } else {
                    p.sendMessage(ChatColor.RED + "You don't have permission to set the number of lightnings!");
                }
            } else {
                p.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Usage: /lrod OR /lrod <playerName> OR /lrod setlightnings <number>");
            }
            return true;
        }
        return false;
    }

    private void sendLightningRod(Player player1, Player player2) {
        if (player1.hasPermission("lrod.get")) {
            ItemStack lightningRod = createLightningRodItem();
            player2.getInventory().addItem(lightningRod);
        } else {
            player1.sendMessage(ChatColor.RED + "You don't have permission to send a lightning Rod!");
        }
    }

    private void giveLightningRod(Player player) {
        if (player.hasPermission("lrod.get")) {
            ItemStack lightningRod = createLightningRodItem();
            player.getInventory().addItem(lightningRod);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to get a lightning Rod!");
        }
    }

    private ItemStack createLightningRodItem() {
        ItemStack lightningRod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = lightningRod.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Lightning Rod!");
        meta.setLore(ImmutableList.of(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Throw some Lightning!"));
        lightningRod.setItemMeta(meta);
        return lightningRod;
    }

    private void setLightningsCommand(Player player, String[] args) {
        try {
            int newLightningCount = Integer.parseInt(args[1]);
            if (newLightningCount > 0 && newLightningCount <= MAX_LIGHTNINGS) {
                numLightnings = newLightningCount;
                player.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.GREEN + "Set number of lightnings to " + newLightningCount + "!");
            } else {
                player.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Please enter a positive number for lightnings, up to a maximum of " + MAX_LIGHTNINGS + "!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Invalid number format!");
        }
    }

    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getItemInHand();

        if (handItem.getType() == Material.BLAZE_ROD && handItem.hasItemMeta()) {
            ItemMeta itemMeta = handItem.getItemMeta();
            if (itemMeta.getDisplayName().equals("" + ChatColor.GOLD + ChatColor.BOLD + "Lightning Rod!")) {
                handleLightningRodUse(player);
            }
        }
    }

    private void handleLightningRodUse(Player player) {
        UUID playerId = player.getUniqueId();
    
        if (!cooldown.containsKey(playerId) || System.currentTimeMillis() - cooldown.get(playerId) >= cooldownTime) {
            cooldown.put(playerId, System.currentTimeMillis());
    
            for (int i = 0; i < numLightnings; i++) {
                player.getWorld().strikeLightning(player.getTargetBlock((Set)null, 200).getLocation());
            }
        } else {
            long remainingTime = (cooldown.get(playerId) + cooldownTime - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You must wait " + remainingTime + " seconds before smiting again!");
        }
    }    
}
