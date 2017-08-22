package com.github.kraftykaleb.listeners;

import com.github.kraftykaleb.Main;
import com.github.kraftykaleb.StatusSign;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * Created by Kraft on 4/18/2017.
 */
public class OnInteract implements Listener {

    private Main plugin;

    public OnInteract(Main instance) {
        plugin = instance;
    }
    public ArrayList<String> WaitTimer = new ArrayList<>();

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if (b.getType() != Material.SIGN && b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN)
                return;
            if (WaitTimer.contains(plugin.getName())) {
                WaitTimer.add(e.getPlayer().getName());
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        WaitTimer.remove(e.getPlayer().getName());
                    }
                }, 20L);
            }
            for (StatusSign s : plugin.signs) {
                if (s.getBlock().equals(b)) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    out.writeUTF("ConnectOther");
                    out.writeUTF(e.getPlayer().getName());
                    out.writeUTF("mini1A");

                    e.getPlayer().sendMessage(ChatColor.GREEN + "Sending you to mini1A...");

                    Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                }
            }
        }
    }

    //public void onAnimate(PlayerAnimationEvent e) {
     //   if (e.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) {
     //       Block b = e.getPlayer().getTargetBlock((Set) null, 10);
//
     //       if (b != null) {
     //           if (b.getType() != Material.SIGN && b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN)
      //              return;
      //          for (StatusSign s : plugin.signs) {
     //               if (s.getBlock().equals(b)) {
      //
       //                 out.writeUTF("ConnectOther");
      //                  out.writeUTF(e.getPlayer().getName());
       //                 out.writeUTF("mini1A");
//
        //                e.getPlayer().sendMessage(ChatColor.GREEN + "Sending you to mini1A...");
//
        //                Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        //            } else {
        //                Bukkit.getServer().broadcastMessage("ERROR");
        //           }
        //        }
        //    }
       // }
  //  }
}
