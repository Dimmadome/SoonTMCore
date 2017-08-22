package com.github.kraftykaleb.commands;

import com.github.kraftykaleb.Main;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kraft on 4/19/2017.
 */
public class Hub implements CommandExecutor {

    private Main plugin;

    public Hub(Main instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            try {
                out.writeUTF("ConnectOther");
                out.writeUTF(p.getName());
                out.writeUTF("lobby1");

                Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                p.sendMessage(ChatColor.GREEN + "Sending you to lobby1...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return true;
    }
}
