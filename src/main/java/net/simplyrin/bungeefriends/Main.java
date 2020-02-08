package net.simplyrin.bungeefriends;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.bungeecord.MetricsLite;
import net.simplyrin.bungeefriends.commands.FriendCommand;
import net.simplyrin.bungeefriends.commands.ReplyCommand;
import net.simplyrin.bungeefriends.commands.TellCommand;
import net.simplyrin.bungeefriends.commands.alias.FLCommand;
import net.simplyrin.bungeefriends.listeners.EventListener;
import net.simplyrin.bungeefriends.utils.*;
import net.simplyrin.bungeeparties.commands.PartyCommand;
import net.simplyrin.bungeeparties.utils.PartyManager;

import java.io.File;
import java.util.*;

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
public class Main extends Plugin {

    public static File playerDataFolder;
    private static Main instance;
    public boolean isEnabledMySQL;
    private ConfigManager configManager;
    private PrefixManager prefixManager;
    private PlayerManager playerManager;
    private FriendManager friendManager;
    private LanguageManager languageManager;
    private MySQLManager mySQLManager;
    private Map<UUID, UUID> replyTargetMap;

    private FriendCommand friendCommand;
    private TellCommand tellCommand;
    private ReplyCommand replyCommand;
    private FLCommand flCommand;

    private EventListener eventListener;

    @SuppressWarnings("unused")
    public static Main getInstance() {
        return instance;
    }


    @Override
    public void onDisable() {
        this.configManager.saveAndReload();
        this.playerManager.saveAndReload();

        if (this.isEnabledMySQL) {
            this.mySQLManager.save();
            this.mySQLManager.getEditor().getMySQL().disconnect();
        }
        instance = null;
    }

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.prefixManager = new PrefixManager(this);
        this.playerManager = new PlayerManager(this);
        this.friendManager = new FriendManager(this);

        this.mySQLManager = new MySQLManager(this);

        this.languageManager = new LanguageManager(this);

        instance = this;

        this.friendCommand = new FriendCommand(this, "friend");
        this.getProxy().getPluginManager().registerCommand(this, this.friendCommand);

        if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./msg")) {
            this.tellCommand = new TellCommand(this);
            this.getProxy().getPluginManager().registerCommand(this, this.tellCommand);
        }
        if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./f")) {
            this.friendCommand = new FriendCommand(this, "f");
            this.getProxy().getPluginManager().registerCommand(this, this.friendCommand);
        }
        if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./r")) {
            this.replyCommand = new ReplyCommand(this, "r");
            this.getProxy().getPluginManager().registerCommand(this, this.replyCommand);
        }
        if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./reply")) {
            this.getProxy().getPluginManager().registerCommand(this, this.replyCommand);
        }
        if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./fl")) {
            this.flCommand = new FLCommand(this);
            this.getProxy().getPluginManager().registerCommand(this, this.flCommand);
        }

        this.eventListener = new EventListener(this);
        this.getProxy().getPluginManager().registerListener(this, this.eventListener);

        this.replyTargetMap = new HashMap<>();
        this.isEnabledMySQL = this.mySQLManager.getConfig().getBoolean("Enable");

        if (this.getProxy().getPluginManager().getPlugin("BungeeParties") != null) {
            net.simplyrin.bungeeparties.Main.setBungeeFriendsInstance(this);
        }

        playerDataFolder = new File(getDataFolder().getAbsolutePath().split("NeonMC" + (System.getProperty("os.name").startsWith("Windows") ? "\\\\" : File.separator))[0] + "NeonMC" + File.separator + "PlayerData" + File.separator);
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdir();
        }

        new MetricsLite(this);
        getLogger().info("Loaded bStats.");
    }

    public String getString(String key) {
        if (this.isEnabledMySQL) {
            return this.getMySQLManager().getEditor().get(key);
        }
        return this.getConfigManager().getConfig().getString(key);
    }

    public List<String> getAllKeys() {
    	if (this.isEnabledMySQL) {
		    return this.getMySQLManager().getEditor().getAllKeys();
	    }
    	List<String> keys = new ArrayList<>();
        Configuration config = this.getConfigManager().getConfig();
    	for (String key : config.getKeys()) {
    	    if (config.getSection(key) != null) {
    	        for (String key1 : config.getSection(key).getKeys()) {
    	            if (config.getSection(key1) != null) {
    	                for (String key2 : config.getSection(key1).getKeys()) {
    	                    keys.add(key + "." + key1 + "." + key2);
                        }
                    }
                }
            }
        }
    	return keys;
    }

    public List<String> getStringList(String key) {
        if (this.isEnabledMySQL) {
            return this.getMySQLManager().getEditor().getList(key);
        }
        return this.getConfigManager().getConfig().getStringList(key);
    }

    public boolean getBoolean(String key) {
        if (this.isEnabledMySQL) {
            return Boolean.parseBoolean(this.getMySQLManager().getEditor().get(key));
        }
        return this.getConfigManager().getConfig().getBoolean(key);
    }

    public void set(String key, List<String> list) {
        if (this.isEnabledMySQL) {
            this.getMySQLManager().getEditor().set(key, list);
        } else {
            this.getConfigManager().getConfig().set(key, list);
        }
    }

    public void set(String key, String value) {
        if (this.isEnabledMySQL) {
            this.getMySQLManager().getEditor().set(key, String.valueOf(value));
        } else {
            this.getConfigManager().getConfig().set(key, value);
        }
    }

    public void set(String key, boolean value) {
        if (this.isEnabledMySQL) {
            this.getMySQLManager().getEditor().set(key, String.valueOf(value));
        } else {
            this.getConfigManager().getConfig().set(key, value);
        }
    }

    public String getPrefix() {
        if (this.configManager.getConfig().getBoolean("Plugin.Disable-Prefix")) {
            return "";
        } else {
            return this.configManager.getConfig().getString("Plugin.Prefix");
        }
    }

    public UUID getPlayerUniqueId(String name) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(this.getString("Name." + name.toLowerCase()));
        } catch (Exception ignored) {
        }
        return uuid;
    }

    @SuppressWarnings("deprecation")
    public void info(String args) {
        this.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
    }

    @SuppressWarnings("deprecation")
    public void info(ProxiedPlayer player, String args) {
        if (args.equals("")) {
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
    }

    @SuppressWarnings("deprecation")
    public void info(UUID uuid, String args) {
        if (args.equals("")) {
            return;
        }
        ProxiedPlayer player = this.getProxy().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
        }
    }

    @SuppressWarnings("unused")
    public String getPlayerName(UUID uuid) {
        return this.getString("UUID." + uuid.toString());
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }

    public Map<UUID, UUID> getReplyTargetMap() {
        return replyTargetMap;
    }

    @SuppressWarnings("unused")
    public void info(ProxiedPlayer player, TextComponent args) {
        if (args.getText().equals("")) {
            return;
        }
        player.sendMessage(MessageBuilder.get(this.getPrefix()), args);
    }

    public FriendCommand getFriendCommand() {
        return this.friendCommand;
    }

    public TellCommand getTellCommand() {
        return tellCommand;
    }

    public ReplyCommand getReplyCommand() {
        return replyCommand;
    }

    public FLCommand getFlCommand() {
        return flCommand;
    }

    public EventListener getEventListener() {
        return eventListener;
    }
}
