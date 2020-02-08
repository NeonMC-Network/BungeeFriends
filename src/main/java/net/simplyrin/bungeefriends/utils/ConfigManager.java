package net.simplyrin.bungeefriends.utils;

import com.google.common.base.Charsets;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
public class ConfigManager {

	private Main plugin;
	private Runnable runnable;
	private Configuration config;

	public ConfigManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "config.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config, Charsets.UTF_8);
	}

	private void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "config.yml");
		if (!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config, Charsets.UTF_8);

			assert this.config != null;
			this.config.set("Plugin.Prefix", "&7[&cFriends&7] &r");
			this.config.set("Plugin.Disable-Prefix", true);

			this.config.set("Plugin.Disable-Aliases./msg", false);
			this.config.set("Plugin.Disable-Aliases./f", false);
			this.config.set("Plugin.Disable-Aliases./r", false);
			this.config.set("Plugin.Disable-Aliases./reply", false);
			this.config.set("Plugin.Disable-Aliases./fl", false);

			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Name", "SimplyRin");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Language", "english");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Prefix", "&c[BFRIENDS] ");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Toggle", true);
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Friends", Collections.singletonList("64636120-8633-4541-aa5f-412b42ddb04d"));

			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Name", "SimplyFoxy");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Language", "english");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Prefix", "&c[BFRIENDS] ");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Toggle", true);
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Friends", Collections.singletonList("b0bb65a2-832f-4a5d-854e-873b7c4522ed"));

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config, Charsets.UTF_8);

		assert this.config != null;
		this.config.set("Plugin.Disable-Alias", null);

		this.resetValue("Plugin.Disable-Aliases./msg");
		this.resetValue("Plugin.Disable-Aliases./f");
		this.resetValue("Plugin.Disable-Aliases./r");
		this.resetValue("Plugin.Disable-Aliases./reply");
		this.resetValue("Plugin.Disable-Aliases./fl");

		if (this.config.getString("Plugin.Default-Language").equals("")) {
			this.config.set("Plugin.Default-Language", "english");
		}

		this.saveAndReload();
	}

	private void resetValue(String key) {
		if (!this.config.getBoolean(key)) {
			this.config.set(key, false);
		}
	}

	public Configuration getConfig() {
		return config;
	}
}
