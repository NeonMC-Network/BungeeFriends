package net.simplyrin.bungeefriends.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;
import net.simplyrin.threadpool.ThreadPool;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by SimplyRin on 2018/07/03.
 * <p>
 * Copyright (c) 2018 SimplyRin
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class EventListener implements Listener {

    private Main plugin;

    public EventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (player.getUniqueId().toString().equals("b0bb65a2-832f-4a5d-854e-873b7c4522ed")) {
            ThreadPool.run(() -> {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ignored) {
                }
                this.plugin.info(player, "&aNeonMC is using &lBungeeFriends (" + this.plugin.getDescription().getVersion() + ")&r&a.");
            });
        }

        FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);

        this.plugin.set("Player." + player.getUniqueId().toString() + ".Name", player.getName());

        this.plugin.set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
        this.plugin.set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());

        for (ProxiedPlayer target : this.plugin.getProxy().getPlayers()) {
            if (!player.equals(target)) {
                if (myFriends.getFriends().contains(target.getUniqueId().toString())) {
                    LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);
                    this.plugin.info(target, targetLangUtils.getString("Friends.Join").replace("%%displayName%%", myFriends.getDisplayName()));
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);

        for (ProxiedPlayer target : this.plugin.getProxy().getPlayers()) {
            if (!player.equals(target)) {
                if (myFriends.getFriends().contains(target.getUniqueId().toString())) {
                    LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);
                    this.plugin.info(target, targetLangUtils.getString("Friends.Quit").replace("%%displayName%%", myFriends.getDisplayName()));
                }
            }
        }
    }

    private HashMap<String, String> previousServer = new HashMap<>();

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);

        String server = player.getServer().getInfo().getName();
        String previousServer = this.previousServer.get(player.getName());

        if (previousServer == null) {
            this.previousServer.put(player.getName(), server);
            return;
        }

        if (!previousServer.equals(server)) {
            for (String friends : myFriends.getFriends()) {
                LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(UUID.fromString(friends));

                String message = targetLangUtils.getString("Friends.Server-Move");

                message = message.replace("%%displayName%%", myFriends.getDisplayName());
                message = message.replace("%%server%%", server);

                this.plugin.info(UUID.fromString(friends), message);
            }
        }

        this.previousServer.put(player.getName(), server);
    }

}
