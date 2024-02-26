package com.braydenbower;

import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

    if (sender instanceof ConsoleCommandSender) {
        if (args.length == 1) {
            String targetPlayerName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

            if(targetPlayer != null) {
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
  
      Player p = (Player) sender;
  
      if (cmd.getName().equalsIgnoreCase("lrod")) {
          if (args.length == 1) {
              // /lrod <playerName>
              String targetPlayerName = args[0];
              Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
  
              if (targetPlayer != null) {
                  giveLightningRod(targetPlayer);
                  p.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.GREEN + "Gave Lightning Rod to " + targetPlayerName + "!");
              } else {
                  p.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.RED + "Player " + targetPlayerName + " not found or not online.");
              }
          } else {
              // /lrod without arguments, give Rod to the command sender
              giveLightningRod(p);
          }
          return true;
      }
      return false;
   }
  

   private void giveLightningRod(Player player) {
      if (player.hasPermission("lrod.get")) {
         PlayerInventory inventory = player.getInventory();

         List<String> lores = new ArrayList<>();
         lores.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Throw some Lightning!");
         String myDisplayName = ChatColor.GOLD + ChatColor.BOLD.toString() + "Lightning Rod!";
         ItemStack myItem = new ItemStack(Material.BLAZE_ROD);
         ItemMeta im = myItem.getItemMeta();
         im.setDisplayName(myDisplayName);
         im.setLore(lores);
         myItem.setItemMeta(im);
   
         inventory.addItem(myItem);
         player.sendMessage(ChatColor.AQUA + "[LROD] " + ChatColor.GREEN + "You've got a Lightning Rod!");
      } else {
         player.sendMessage(ChatColor.RED + "You don't have permission to get a lightning Rod!");
      }
      
   }
 
    public void onEnable() {
       Bukkit.getServer().getPluginManager().registerEvents(this, this);
       Roduse();
    }

    private HashMap<UUID, Long> cooldown;

    public void Roduse(){
        this.cooldown = new HashMap<>();
    }
 
    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getType() == Material.BLAZE_ROD) {
            if(player.getInventory().getItemInHand().getItemMeta().getDisplayName().equals("" + ChatColor.GOLD + ChatColor.BOLD + "Lightning Rod!")){
               if (!this.cooldown.containsKey(player.getUniqueId()) || System.currentTimeMillis() - cooldown.get(player.getUniqueId()) >= 5000){

                  this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());
                  player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "You have used the power of Zeus!");
                  player.getWorld().strikeLightning(player.getTargetBlock((Set)null, 200).getLocation());

               } else {

                  long remainingTime = (cooldown.get(player.getUniqueId()) + 5000 - System.currentTimeMillis()) / 1000;

                  player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You must wait " + remainingTime + " seconds before smiting again!"); 
                
               }
            }
        }
 
    }
 }
