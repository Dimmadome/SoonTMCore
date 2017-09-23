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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
        //ByteArrayDataOutput out = ByteStreams.newDataOutput();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);

                try {
                    out.writeUTF("Connect");
                    out.writeUTF("lobby1");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //out.writeUTF(e.getPlayer().getName());


                p.sendPluginMessage(Main.get(), "BungeeCord", b.toByteArray());
                p.sendMessage(ChatColor.GREEN + "Sending you to lobby1...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return true;
    }
}
