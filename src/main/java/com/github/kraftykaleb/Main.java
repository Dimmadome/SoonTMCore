package com.github.kraftykaleb;

import com.github.kraftykaleb.commands.Hub;
import com.github.kraftykaleb.commands.Warp;
import com.github.kraftykaleb.listeners.OnInteract;
import com.github.kraftykaleb.listeners.OnJoin;
import com.github.kraftykaleb.listeners.onBreak;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Kraft on 4/18/2017.
 */
public class Main extends JavaPlugin implements PluginMessageListener, Listener {

    public String[] serverList = null;
    public Set<StatusSign> signs;
    public Connection connection;

    public HashMap<String, Integer> skyflagwins = new HashMap<>();
    public HashMap<String, Integer> skyflagkills = new HashMap<>();
    public HashMap<String, Integer> skyflagcoins = new HashMap<>();
    public HashMap<String, String> assignedguilds = new HashMap<>();
    public HashMap<String, String> guildranks = new HashMap<>();
    public HashMap<String, String> hypixelranks = new HashMap<>();
    public HashMap<String, String> donationrank = new HashMap<>();

    public void onEnable() {
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.signs = new HashSet<>();

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvents(new OnInteract(this), this);
        pm.registerEvents(this, this);
        pm.registerEvents(new onBreak(this), this);
        pm.registerEvents(new OnJoin(this), this);

        getCommand("warp").setExecutor(new Warp(this));
        getCommand("hub").setExecutor(new Hub(this));


        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        try {
            out.writeUTF("GetServers");
            Bukkit.getServer().sendPluginMessage(this, "BungeeCord", out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }


        //Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
        //@Override
        // public void run() {
        for (int i = 1; i <= getConfig().getKeys(false).size(); i++) {
            Integer in = i;
            World w = Bukkit.getServer().getWorld("world");
            double x = getConfig().getDouble(in + ".loc.x");
            double y = getConfig().getDouble(in + ".loc.y");
            double z = getConfig().getDouble(in + ".loc.z");
            Location loc = new Location(w, x, y, z);
            Block b = loc.getBlock();
            Bukkit.getServer().getLogger().log(Level.INFO,"#" + in + " Located a " + b.getType().toString() + " at " + getConfig().getString(in + ".loc.world") + ", " + (getConfig().getDouble(in + ".loc.x")) + ", " + getConfig().getDouble(in + ".loc.y") + ", " + (getConfig().getDouble(in + ".loc.z")));


            if (!(b.getState() instanceof Sign)) {
                String str = in.toString();
                getConfig().set(str, null);
                saveConfig();
            } else {
                Bukkit.getServer().getLogger().log(Level.INFO, "SIGN CREATED");
                StatusSign sign = new StatusSign(b);
                signs.add(sign);
                sign.updateSkyFlag();
            }
        }
        //}
        //}, 3*20);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {
                for (StatusSign s : signs) {
                    s.updateSkyFlag();
                }
            }
        }, 0, 20);

        try {
            File f = new File(this.getDataFolder() + File.separator + "config.yml");
            if (f.exists()) {
                return;
            } else {
                FileConfiguration config = YamlConfiguration.loadConfiguration(f);
                InputStream defConfigStream = this.getResource("config.yml");
                @SuppressWarnings("deprecation") FileConfiguration defconf = YamlConfiguration.loadConfiguration(defConfigStream);
                config.addDefaults(defconf);
                config.setDefaults(defconf);
                this.saveDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        try {
            if (connection != null || connection.isClosed()) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        saveDefaultConfig();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        Bukkit.getServer().broadcastMessage("Got some data");
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServers")) {
            serverList = in.readUTF().split(", ");
            Bukkit.getServer().broadcastMessage("FOUND SERVERS!");
        }

    }

    public void refreshSign() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("167.114.216.188", 25591), 1 * 1000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.write(0xFE);

            int b;
            StringBuilder str = new StringBuilder();

            while ((b = in.read()) != -1) {
                if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                    str.append((char) b);
                }
            }

            String[] data = str.toString().split("§");
            String motd = data[0];
            int onlinePlayers = Integer.valueOf(data[1]);
            int maxPlayers = Integer.valueOf(data[2]);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        String line1 = e.getLine(0);
        if (line1.equals("[skyflagjoin]")) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "Skyflag sign created!");
            Sign sign = (Sign) e.getBlock().getState();
            StatusSign statusSign = new StatusSign(e.getBlock());

            signs.add(statusSign);
            save(statusSign);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can create status signs.");
            return true;
        }

        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("statussigns")) {
            Block block = p.getTargetBlock((Set) null, 10);
            if (block == null) {
                p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
                return true;
            }

            if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
                p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
                return true;
            }
            p.sendMessage(ChatColor.GREEN + "Skyflag sign created!");
            StatusSign statusSign = new StatusSign(block);
            signs.add(statusSign);
            save(statusSign);
        }

        return true;
    }

    public void save(StatusSign sign) {
        sign.updateSkyFlag();
        Integer size = getConfig().getKeys(false).size() + 1;

        String world = size + ".loc.world", x = size + ".loc.x", y = size + ".loc.y", z = size + ".loc.z";
        //String size1 = size.toString();
        getConfig().set(world, sign.getBlock().getWorld().getName());
        getConfig().set(x, ((double) sign.getBlock().getX()));
        getConfig().set(y, ((double) sign.getBlock().getY()));
        getConfig().set(z, ((double) sign.getBlock().getZ()));
        saveConfig();
    }

    public synchronized void openConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://172.106.202.99:3306/Kraft_SoonTMDatabase", "Kraft", "KraftLegos11");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void closeConnection () {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean playerDataContainsPlayer(Player player) {
        try {
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `player_data` WHERE player=?;");
            sql.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = sql.executeQuery();

            boolean containsPlayer = resultSet.next();

            sql.close();
            resultSet.close();

            return containsPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadSkyflagCoins (String name) {
        UUID playerUUID = Bukkit.getServer().getPlayer(name).getUniqueId();

        try {
            PreparedStatement skyflagCoinsStatement = connection.prepareStatement("SELECT skyflag_coins FROM `player_data` WHERE player=?;");
            skyflagCoinsStatement.setString(1, playerUUID.toString());

            ResultSet resultCoinsSet = skyflagCoinsStatement.executeQuery();
            resultCoinsSet.next();

            skyflagcoins.put(name, resultCoinsSet.getInt("skyflag_coins"));

            skyflagCoinsStatement.close();
            resultCoinsSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
