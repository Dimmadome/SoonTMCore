package com.github.kraftykaleb.listeners;

import com.github.kraftykaleb.Main;
import com.github.kraftykaleb.PlayerList;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mojang.authlib.GameProfile;
import net.hypixel.api.reply.GuildReply;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created by Kraft on 4/21/2017.
 */
public class OnJoin implements Listener {


    public HashMap<String, String> hypixelranks = new HashMap<>();
    public HashMap<String, String> plusColorList = new HashMap<>();
    private Main plugin;

    public OnJoin(Main instance) { plugin = instance; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        findHypixelPlayer(p);



    }

    /*private static PacketPlayOutPlayerInfo setInfo(PacketPlayOutPlayerInfo packet, PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, PacketPlayOutPlayerInfo.PlayerInfoData data) {
        try {
            actionField.set(packet, action);
            dataField.set(packet, Arrays.asList(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packet;
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);

            if (Modifier.isFinal(field.getModifiers())) {
                modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
            }

            return field;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    private void findHypixelPlayer(final Player hypixelPlayer) {
        try {
            if (plugin.playerDataContainsPlayer(hypixelPlayer)) {
                plugin.openConnection();
                PreparedStatement rank = plugin.connection.prepareStatement("SELECT rank FROM `player_data` WHERE player=?;");
                rank.setString(1, hypixelPlayer.getUniqueId().toString());


                ResultSet resultRank = rank.executeQuery();
                resultRank.next();

                hypixelranks.put(hypixelPlayer.getName(), resultRank.getString("rank"));

                PreparedStatement plusColorCheck = plugin.connection.prepareStatement("SELECT plus_color FROM `player_data` WHERE player=?;");
                plusColorCheck.setString(1, hypixelPlayer.getUniqueId().toString());


                ResultSet resultPlusColorCheck = plusColorCheck.executeQuery();
                resultPlusColorCheck.next();

                plusColorList.put(hypixelPlayer.getName(), resultPlusColorCheck.getString("plus_color"));


                plusColorCheck.close();
                resultPlusColorCheck.close();
                rank.close();
                resultRank.close();
            } else {
                //hypixelPlayer.kickPlayer(ChatColor.RED + "An SQL error occured, Please report this to a developer! \nSQLPLAYERNOTFOUND AT: " + Thread.currentThread().getStackTrace()[2].getClassName() + "(" + Thread.currentThread().getStackTrace()[2].getMethodName() + "(" + Thread.currentThread().getStackTrace()[2].getLineNumber() + "))");
            }
        }
        catch (Exception error) {
            error.printStackTrace();
        } finally {
            plugin.closeConnection();
        }




            String prefix = getRankPrefix(hypixelPlayer);
            String prefixColor = getRankColor(hypixelPlayer);
            String rank = hypixelranks.get(hypixelPlayer.getName());


            if (rank.equals("MVP_PLUS") && !plusColorList.get(hypixelPlayer.getName()).equals("NONE")) {
                String mvpPrefix = ChatColor.AQUA + "[MVP" + ChatColor.valueOf(plusColorList.get(hypixelPlayer.getName())) + "+" + ChatColor.AQUA + "] ";
                hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                hypixelPlayer.setCustomName(mvpPrefix + hypixelPlayer.getName());
                hypixelPlayer.setPlayerListName(mvpPrefix + hypixelPlayer.getName());
            } else {
                hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                return;
            }


    }

    /*private void findHypixelPlayer(final Player hypixelPlayer) {



        HttpRequest request = Unirest.get("https://api.hypixel.net/player")
                .queryString("key", "811f839c-b801-48e0-a693-a857e48261a0")
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

                ChatColor plusColor;


                if (apiResponse.has("rank")) {

                    String hypixelRank = apiResponse.getString("rank");//(apiResponse.has("rank") ? apiResponse.getString("rank") : "NONE");
                    hypixelranks.put(hypixelPlayer.getName(), hypixelRank);
                    String prefix = getRankPrefix(hypixelPlayer);
                    String prefixColor = getRankColor(hypixelPlayer);


                    if (apiResponse.getString("rank").equals("MVP_PLUS")) {
                        if (apiResponse.has("rankPlusColor")) {
                            plusColor = ChatColor.valueOf(apiResponse.getString("rankPlusColor"));
                            hypixelPlayer.setCustomName(ChatColor.AQUA + "[MVP" + plusColor + "+" + ChatColor.AQUA + "] " + hypixelPlayer.getName());
                            hypixelPlayer.setDisplayName(ChatColor.AQUA + hypixelPlayer.getName());
                            hypixelPlayer.setPlayerListName(ChatColor.AQUA + "[MVP" + plusColor + "+" + ChatColor.AQUA + "] " + hypixelPlayer.getName());
                            return;
                        } else {
                            hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                            hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                            hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                            return;
                        }
                    } else {
                        hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                        hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                        hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                        return;
                    }
                } else if (apiResponse.has("packageRank")) {
                    String oldPackageRank = apiResponse.getString("packageRank");
                    hypixelranks.put(hypixelPlayer.getName(), oldPackageRank);
                    String prefix = getRankPrefix(hypixelPlayer);
                    String prefixColor = getRankColor(hypixelPlayer);
                    if (apiResponse.getString("packageRank").equals("MVP_PLUS")) {
                        if (apiResponse.has("rankPlusColor")) {
                            plusColor = ChatColor.valueOf(apiResponse.getString("rankPlusColor"));
                            hypixelPlayer.setCustomName(ChatColor.AQUA + "[MVP" + plusColor + "+" + ChatColor.AQUA + "] " + hypixelPlayer.getName());
                            hypixelPlayer.setDisplayName(ChatColor.AQUA + hypixelPlayer.getName());
                            hypixelPlayer.setPlayerListName(ChatColor.AQUA + "[MVP" + plusColor + "+" + ChatColor.AQUA + "] " + hypixelPlayer.getName());
                            return;
                        } else {
                            hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                            hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                            hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                            return;
                        }
                    } else {
                        hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                        hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                        hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                        return;
                    }
                } else if (apiResponse.has("newPackageRank")) {

                    String newPackageRank = apiResponse.getString("newPackageRank");
                    hypixelranks.put(hypixelPlayer.getName(), newPackageRank);
                    String prefix = getRankPrefix(hypixelPlayer);
                    String prefixColor = getRankColor(hypixelPlayer);

                    if (apiResponse.getString("newPackageRank").equals("MVP_PLUS")) {
                        if (apiResponse.has("rankPlusColor")) {
                            plusColor = ChatColor.valueOf(apiResponse.getString("rankPlusColor"));
                            hypixelPlayer.setCustomName(ChatColor.AQUA + "[MVP" + plusColor + "+" + ChatColor.AQUA + "] " + hypixelPlayer.getName());
                            hypixelPlayer.setDisplayName(ChatColor.AQUA + hypixelPlayer.getName());
                            hypixelPlayer.setPlayerListName(ChatColor.AQUA + "[MVP" + plusColor + "+" + ChatColor.AQUA + "] " + hypixelPlayer.getName());
                            return;
                        } else {
                            hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                            hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                            hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                            return;
                        }
                    } else {
                        hypixelPlayer.setDisplayName(prefixColor + hypixelPlayer.getName());
                        hypixelPlayer.setCustomName(prefix + hypixelPlayer.getName());
                        hypixelPlayer.setPlayerListName(prefix + hypixelPlayer.getName());
                        return;
                    }
                } else {
                    hypixelPlayer.setDisplayName(ChatColor.GRAY + hypixelPlayer.getName());
                    hypixelPlayer.setCustomName(ChatColor.GRAY + hypixelPlayer.getName());
                    hypixelPlayer.setPlayerListName(ChatColor.GRAY + hypixelPlayer.getName());
                    hypixelranks.put(hypixelPlayer.getName(), "DEFAULT");
                    return;
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


    }*/



    private String getRankColor(Player p) {
        if (hypixelranks.containsKey(p.getName())) {
            if (hypixelranks.get(p.getName()).equals("ADMIN")) {
                return "§c";
            } else if (hypixelranks.get(p.getName()).equals("MODERATOR")) {
                return "§2";
            } else if (hypixelranks.get(p.getName()).equals("HELPER")) {
                return "§9";
            } else if (hypixelranks.get(p.getName()).equals("MVP_PLUS")) {
                return "§b";
            } else if (hypixelranks.get(p.getName()).equals("MVP")) {
                return "§b";
            } else if (hypixelranks.get(p.getName()).equals("VIP_PLUS")) {
                return "§a";
            } else if (hypixelranks.get(p.getName()).equals("VIP")) {
                return "§a";
            } else if (hypixelranks.get(p.getName()).equals("DEFAULT")) {
                return "§7";

            } else {
                return "§7";
            }
        } else {
            return "§7";
        }
    }

    private String getRankPrefix(Player p) {
        if (hypixelranks.containsKey(p.getName())) {
            if (hypixelranks.get(p.getName()).equals("ADMIN")) {
                return "§c[ADMIN] ";
            } else if (hypixelranks.get(p.getName()).equals("MODERATOR")) {
                return "§2[MOD] ";
            } else if (hypixelranks.get(p.getName()).equals("HELPER")) {
                return "§9[HELPER] ";
            } else if (hypixelranks.get(p.getName()).equals("MVP_PLUS")) {
                return "§b[MVP§c+§b] ";
            } else if (hypixelranks.get(p.getName()).equals("MVP")) {
                return "§b[MVP] ";
            } else if (hypixelranks.get(p.getName()).equals("VIP_PLUS")) {
                return "§a[VIP§6+§a] ";
            } else if (hypixelranks.get(p.getName()).equals("VIP")) {
                return "§a[VIP] ";
            } else if (hypixelranks.get(p.getName()).equals("DEFAULT")) {
                return "§7";

            } else {
                return "§7";
            }
        } else {
            return "§7";
        }
    }

}
