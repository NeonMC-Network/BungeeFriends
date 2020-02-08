package net.simplyrin.bungeefriends.utils;

import com.google.common.base.Charsets;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
public class PlayerManager implements Listener {

	private Main plugin;
	private Runnable runnable;
	private Configuration config;

	public PlayerManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	@SuppressWarnings("unused")
	public UUID getPlayerUniqueId(String name) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(this.plugin.getString("Name." + name.toLowerCase()));
		} catch (Exception ignored) {
		}
		return uuid;
	}

	@SuppressWarnings("unused")
	public String getPlayerName(UUID uuid) {
		return this.plugin.getString("UUID." + uuid.toString());
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "player.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config, Charsets.UTF_8);
	}

	private void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "player.yml");
		if (!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config, Charsets.UTF_8);
			if (this.config != null) {
				this.config.set("UUID.b0bb65a2-832f-4a5d-854e-873b7c4522ed", "SimplyRin");
				this.config.set("Name.simplyrin", "b0bb65a2-832f-4a5d-854e-873b7c4522ed");

				this.config.set("UUID.64636120-8633-4541-aa5f-412b42ddb04d", "SimplyFoxy");
				this.config.set("Name.simplyfoxy", "64636120-8633-4541-aa5f-412b42ddb04d");

				Config.saveConfig(this.config, config);
			}
		}

		this.config = Config.getConfig(config, Charsets.UTF_8);
		this.saveAndReload();
	}

}
