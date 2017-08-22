package com.github.kraftykaleb.listeners;

import com.github.kraftykaleb.Main;
import com.github.kraftykaleb.Util;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.request.Request;
import net.hypixel.api.request.RequestBuilder;
import net.hypixel.api.request.RequestParam;
import net.hypixel.api.request.RequestType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Kraft on 4/21/2017.
 */
public class OnJoin implements Listener {

    public HashMap<String, GuildReply.Guild.Member> guilds = new HashMap<>();
    public HashMap<String, String> hypixelranks = new HashMap<>();
    private Main plugin;
    public OnJoin(Main instance) { plugin = instance; }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        findHypixelPlayer(e.getPlayer());

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                plugin.openConnection();
                try {
                    if (plugin.playerDataContainsPlayer(e.getPlayer())) {
                        PreparedStatement skyflagwins = plugin.connection.prepareStatement("SELECT skyflag_wins FROM `player_data` WHERE player=?;");
                        skyflagwins.setString(1, e.getPlayer().getUniqueId().toString());

                        ResultSet resultWinsSet = skyflagwins.executeQuery();
                        resultWinsSet.next();

                        plugin.skyflagwins.put(p.getName(), resultWinsSet.getInt("skyflag_wins"));

                        PreparedStatement skyflagkills = plugin.connection.prepareStatement("SELECT skyflag_kills FROM `player_data` WHERE player=?;");
                        skyflagkills.setString(1, e.getPlayer().getUniqueId().toString());

                        ResultSet resultKillsSet = skyflagkills.executeQuery();
                        resultKillsSet.next();

                        plugin.skyflagkills.put(p.getName(), resultKillsSet.getInt("skyflag_kills"));

                        plugin.loadSkyflagCoins(p.getName());

                        skyflagkills.close();
                        skyflagwins.close();
                        resultKillsSet.close();
                        resultWinsSet.close();
                        Bukkit.getServer().getLogger().log(Level.INFO, p.getName() + " was found and now has " + plugin.skyflagwins.get(p.getName())+ " wins!");
                    } else {

                        p.kickPlayer(ChatColor.RED + "an SQL error occured, Please report this to a developer! \nSQLPLAYERNOTFOUND AT: " + Thread.currentThread().getStackTrace()[2].getClassName() + "(" + Thread.currentThread().getStackTrace()[2].getMethodName() + "(" +Thread.currentThread().getStackTrace()[2].getLineNumber()+"))");
                        //Bukkit.getServer().broadcast(ChatColor.R + "[BOT] SoonTM: " + net.md_5.bungee.api.ChatColor.YELLOW + "Please welcome " + p.getDisplayName() + net.md_5.bungee.api.ChatColor.YELLOW + " to the server!"));
                        //ProxyServer.getInstance().getLogger().log(Level.INFO, "Created a new player on the database!");
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                } finally {
                    plugin.closeConnection();
                }
            }
        }, 20);

    }
    public void findHypixelGuild(Player p, String id) {
            HypixelAPI.getInstance().setApiKey(UUID.fromString("94512d8c-d83c-46b4-a789-a11347fff344"));

            Request request = RequestBuilder.newBuilder(RequestType.GUILD)
                    .addParam(RequestParam.GUILD_BY_ID, id)
                    .createRequest();
            HypixelAPI.getInstance().getAsync(request, (net.hypixel.api.util.Callback<GuildReply>) (failCause, result) -> {
                if (failCause != null) {
                    failCause.printStackTrace();
                } else {
                    if (result.getGuild().getName().toUpperCase().equals("SOONTM")) {
                        for (GuildReply.Guild.Member member : result.getGuild().getMembers()) {
                            if (member.getUuid().equals(p.getUniqueId())) {
                                //If they are a member of SoonTM, run this.
                            }
                        }
                    }
                }
                HypixelAPI.getInstance().finish();
            });

    }

    public void findHypixelPlayer(final Player hypixelPlayer) {
        HttpRequest request = Unirest.get("https://api.hypixel.net/player")
                .queryString("key", "94512d8c-d83c-46b4-a789-a11347fff344")
                .queryString("uuid", hypixelPlayer.getUniqueId().toString());

        request.asJsonAsync(new Callback<JsonNode>() {
            @Override
            public void completed(HttpResponse<JsonNode> httpResponse) {
                JSONObject apiResponse = httpResponse.getBody().getObject();
                if (apiResponse.isNull("player")) {
                    // Invalid Hypixel player. Handle how you choose to.
                    return;
                }

                apiResponse = apiResponse.getJSONObject("player");

                Bukkit.getLogger().log(Level.INFO, "Found player " + hypixelPlayer.getName());

                net.md_5.bungee.api.ChatColor plusColor = null;
                String prefix = null;


                if (apiResponse.has("rank")) {

                    String hypixelRank = (apiResponse.has("rank") ? apiResponse.getString("rank") : "NONE");
                    hypixelranks.put(hypixelPlayer.getName(), hypixelRank);
                    if (hypixelRank.equals("ADMIN")) {
                        hypixelPlayer.setDisplayName((net.md_5.bungee.api.ChatColor.RED + "[ADMIN] " + hypixelPlayer.getName()));
                    }
                    if (hypixelRank.equals("MODERATOR")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.DARK_GREEN + "[MOD] " + hypixelPlayer.getName());
                    }
                    if (hypixelRank.equals("HELPER")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.BLUE + "[HELPER] " + hypixelPlayer.getName());
                    }
                    if (hypixelRank.equals("MVP")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP] " + hypixelPlayer.getName());
                    }
                    if (hypixelRank.equals("VIP")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GREEN + "[VIP] " + hypixelPlayer.getName());
                    }
                    if (hypixelRank.equals("VIP_PLUS")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GREEN + "[VIP" + net.md_5.bungee.api.ChatColor.GOLD + "+" + net.md_5.bungee.api.ChatColor.GREEN + "] " + hypixelPlayer.getName());
                    }
                    if (apiResponse.getString("rank").equals("MVP_PLUS")) {
                        if (apiResponse.has("rankPlusColor")) {
                            plusColor = net.md_5.bungee.api.ChatColor.valueOf(apiResponse.getString("rankPlusColor"));
                            hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP" + plusColor + "+" + net.md_5.bungee.api.ChatColor.AQUA + "] " + hypixelPlayer.getName());
                        } else {
                            hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP" + net.md_5.bungee.api.ChatColor.RED + "+" + net.md_5.bungee.api.ChatColor.AQUA + "] " + hypixelPlayer.getName());
                        }
                    }
                } else if (apiResponse.has("packageRank")) {
                    String oldPackageRank = (apiResponse.has("packageRank") ? apiResponse.getString("packageRank") : "DEFAULT");
                    hypixelranks.put(hypixelPlayer.getName(), oldPackageRank);
                    if (oldPackageRank.equals("ADMIN")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.RED + "[ADMIN] " + hypixelPlayer.getName());
                    }
                    if (oldPackageRank.equals("MODERATOR")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.DARK_GREEN + "[MOD] " + hypixelPlayer.getName());
                    }
                    if (oldPackageRank.equals("HELPER")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.BLUE + "[HELPER] " + hypixelPlayer.getName());
                    }
                    if (oldPackageRank.equals("MVP")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP] " + hypixelPlayer.getName());
                    }
                    if (oldPackageRank.equals("VIP")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GREEN + "[VIP] " + hypixelPlayer.getName());
                    }
                    if (oldPackageRank.equals("VIP_PLUS")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GREEN + "[VIP" + net.md_5.bungee.api.ChatColor.GOLD + "+" + net.md_5.bungee.api.ChatColor.GREEN + "] " + hypixelPlayer.getName());
                    }
                    if (apiResponse.getString("packageRank").equals("MVP_PLUS")) {
                        if (apiResponse.has("rankPlusColor")) {
                            plusColor = net.md_5.bungee.api.ChatColor.valueOf(apiResponse.getString("rankPlusColor"));
                            hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP" + plusColor + "+" + net.md_5.bungee.api.ChatColor.AQUA + "] " + hypixelPlayer.getName());
                        } else {
                            hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP" + net.md_5.bungee.api.ChatColor.RED + "+" + net.md_5.bungee.api.ChatColor.AQUA + "] " + hypixelPlayer.getName());
                        }
                    }
                } else if (apiResponse.has("newPackageRank")) {

                    String newPackageRank = (apiResponse.has("newPackageRank") ? apiResponse.getString("newPackageRank") : "DEFAULT");
                    hypixelranks.put(hypixelPlayer.getName(), newPackageRank);
                    if (newPackageRank.equals("ADMIN")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.RED + "[ADMIN] " + hypixelPlayer.getName());
                    }
                    if (newPackageRank.equals("MODERATOR")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.DARK_GREEN + "[MOD] " + hypixelPlayer.getName());
                    }
                    if (newPackageRank.equals("HELPER")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.BLUE + "[HELPER] " + hypixelPlayer.getName());
                    }
                    if (newPackageRank.equals("MVP")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP] " + hypixelPlayer.getName());
                    }
                    if (newPackageRank.equals("VIP")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GREEN + "[VIP] " + hypixelPlayer.getName());
                    }
                    if (newPackageRank.equals("VIP_PLUS")) {
                        hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GREEN + "[VIP" + net.md_5.bungee.api.ChatColor.GOLD + "+" + ChatColor.GREEN + "] " + hypixelPlayer.getName());
                    }
                    if (apiResponse.getString("newPackageRank").equals("MVP_PLUS")) {
                        if (apiResponse.has("rankPlusColor")) {
                            plusColor = net.md_5.bungee.api.ChatColor.valueOf(apiResponse.getString("rankPlusColor"));
                            hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP" + plusColor + "+" + net.md_5.bungee.api.ChatColor.AQUA + "] " + hypixelPlayer.getName());

                        } else {
                            hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.AQUA + "[MVP" + net.md_5.bungee.api.ChatColor.RED + "+" + net.md_5.bungee.api.ChatColor.AQUA + "] " + hypixelPlayer.getName());
                        }
                    }
                } else {
                    hypixelPlayer.setDisplayName(net.md_5.bungee.api.ChatColor.GRAY + hypixelPlayer.getName());
                    hypixelranks.put(hypixelPlayer.getName(), "DEFAULT");
                }
                // Handle response some how
            }

            @Override
            public void failed(UnirestException e) {
                // Handle request error some how
            }

            @Override
            public void cancelled() {

            }
        });


    }
}
