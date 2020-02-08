package net.simplyrin.bungeefriends.utils;

import com.google.common.base.Charsets;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SimplyRin on 2018/09/04.
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
public class PrefixManager {

	private Main plugin;
	private Runnable runnable;
	private Configuration config;

	public PrefixManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	private void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "prefix.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config, Charsets.UTF_8);
	}

	private void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File prefix = new File(folder, "prefix.yml");
		if (!prefix.exists()) {
			try {
				prefix.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(prefix, Charsets.UTF_8);

			if (this.config != null) {
				this.config.set("List.VIP.Prefix", "&a[VIP] ");
				this.config.set("List.VIP.Permission", "friends.prefix.vip");

				this.config.set("List.VIP+.Prefix", "&a[VIP&6+&a] ");
				this.config.set("List.VIP+.Permission", "friends.prefix.vip_plus");
				Config.saveConfig(this.config, prefix);
			}
		}

		this.config = Config.getConfig(prefix, Charsets.UTF_8);
		this.saveAndReload();
	}

	public String getDefaultPrefix(UUID uuid) {

		if (this.plugin.isEnabledMySQL) {

			String rank = this.plugin.getMySQLManager().getRankEditor().get(String.valueOf(uuid));

			if (rank == null || rank.equals("") || rank.equals("null") || rank.equalsIgnoreCase("member")) {
				return "&7";
			}
			if (rank.equalsIgnoreCase("hacker")) {
				return "&5[HACKER] ";
			}
			if (rank.equalsIgnoreCase("vip")) {
				return "&a[VIP] ";
			}
			if (rank.equalsIgnoreCase("vip+") || rank.equalsIgnoreCase("vipplus") || rank.equalsIgnoreCase("vip-plus")) {
				return "&a[VIP&6+&a] ";
			}
			if (rank.equalsIgnoreCase("mvp")) {
				return "&b[MVP] ";
			}
			if (rank.equalsIgnoreCase("mvp+") || rank.equalsIgnoreCase("mvpplus") || rank.equalsIgnoreCase("mvp-plus")) {
				return "&b[MVP&c+&b] ";
			}
			if (rank.equalsIgnoreCase("mvp++") || rank.equalsIgnoreCase("mvpplusplus") || rank.equalsIgnoreCase("mvp-plusplus") || rank.equalsIgnoreCase("mvp-plus-plus")) {
				return "&6[MVP&c++&6] ";
			}
			if (rank.equalsIgnoreCase("up") || rank.equalsIgnoreCase("uploader")) {
				return "&c[&fUP&c] ";
			}
			if (rank.equalsIgnoreCase("mod")) {
				return "&2[MOD] ";
			}
			if (rank.equalsIgnoreCase("admin")) {
				return "&c[ADMIN] ";
			}
			if (rank.equalsIgnoreCase("owner")) {
				return "&c[OWNER] ";
			}
			return "&7[" + rank.toUpperCase() + "&7] ";

		} else {
			ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
			if (player == null) {
				return null;
			}
			File playerData = new File(
					Main.playerDataFolder.getAbsolutePath() + File.separator + player.getUniqueId() + ".yml");
			if (!playerData.exists()) {
				try {
					//noinspection ResultOfMethodCallIgnored
					playerData.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return "";
			}
			Configuration configuration = Config.loadConfig(playerData);

			String rank = configuration.getString("rank");
			if (rank.equalsIgnoreCase("owner")) {
				return "&c[OWNER] ";
			} else if (rank.equalsIgnoreCase("admin")) {
				return "&c[ADMIN] ";
			} else if (rank.equalsIgnoreCase("mod")) {
				return "&2[MOD] ";
			} else if (rank.equalsIgnoreCase("up")) {
				return "&c[&fUP&c] ";
			} else if (rank.equalsIgnoreCase("mvp++")) {
				return "&6[MVP&c++&6] ";
			} else if (rank.equalsIgnoreCase("mvp+")) {
				return "&b[MVP&c+&b] ";
			} else if (rank.equalsIgnoreCase("mvp")) {
				return "&b[MVP] ";
			} else if (rank.equalsIgnoreCase("vip+")) {
				return "&a[VIP&6+&a] ";
			} else if (rank.equalsIgnoreCase("vip")) {
				return "&a[VIP] ";
			} else if (rank.equalsIgnoreCase("hacker")) {
				return "&5[HACKER] ";
			} else if (rank.equalsIgnoreCase("member")) {
				return "&7";
			} else {
				return "&7[" + rank.toUpperCase() + "] ";
			}

		}
	}

	public Configuration getConfig() {
		return config;
	}
}
