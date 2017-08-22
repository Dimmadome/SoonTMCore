package com.github.kraftykaleb;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Kraft on 4/18/2017.
 */
public class StatusSign {

    private Block block;
    private Sign sign;

    public StatusSign(Block block) {
        this.block = block;

        if (block.getState() instanceof Sign) {
            this.sign = (Sign) block.getState();
        }
    }

    public Block getBlock() {
        return block;
    }


    public void updateSkyFlag() {
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

            sign.setLine(0, "§a[JOIN]");
            sign.setLine(1, "§0§lSKYFLAG");
            sign.setLine(2, onlinePlayers + " Playing");
            sign.setLine(3, "MODE: §aONLINE");
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();

            sign.setLine(0, "§a[JOIN]");
            sign.setLine(1, "§0§lSKYFLAG");
            sign.setLine(2, "0 Playing");
            sign.setLine(3, "MODE:§c OFFLINE");
        }

        sign.update();
    }
}
