package com.github.kraftykaleb.listeners;

import com.github.kraftykaleb.Main;
import com.github.kraftykaleb.StatusSign;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created by Kraft on 4/21/2017.
 */
public class onBreak implements Listener {

    /*
    private Main plugin;

    public onBreak(Main instance) {
        plugin = instance;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.WALL_SIGN || e.getBlock().getType() == Material.SIGN_POST || e.getBlock().getType() == Material.SIGN) {
            StatusSign statusSign = new StatusSign(e.getBlock());
            if (plugin.signs.contains(statusSign)) {
                plugin.signs.remove(statusSign);
                for (int i = 1; i <= plugin.getConfig().getKeys(false).size(); i++) {
                    Integer in = i;
                    World w = Bukkit.getServer().getWorld("world");
                    double x = plugin.getConfig().getDouble(in + ".loc.x");
                    double y = plugin.getConfig().getDouble(in + ".loc.y");
                    double z = plugin.getConfig().getDouble(in + ".loc.z");
                    Location loc = new Location(w, x, y, z);
                    Block b = loc.getBlock();
                    //Bukkit.getServer().broadcastMessage("#" + in + " Located a " + b.getType().toString() + " at " + getConfig().getString(in + ".loc.world") + ", " + (getConfig().getDouble(in + ".loc.x")) + ", " + getConfig().getDouble(in + ".loc.y") + ", " + (getConfig().getDouble(in + ".loc.z")));

                    if (!(b.getState() instanceof Sign)) {
                        //Bukkit.getServer().broadcastMessage("NULL");
                        e.getPlayer().sendMessage(ChatColor.GREEN + "The sign you broke was removed!");
                        String str = in.toString();
                        plugin.getConfig().set(str, null);
                        plugin.saveConfig();
                    }
                }
            }
        }
    }
    */
}
