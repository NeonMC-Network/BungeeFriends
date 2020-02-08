package net.simplyrin.bungeefriends.utils;

import com.google.common.base.Charsets;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.MySQL;
import net.simplyrin.bungeefriends.tools.Config;
import net.simplyrin.threadpool.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class MySQLManager {

	private Main plugin;
	private Runnable runnable;
	private Configuration config;
	private MySQL.Editor editor;
	private MySQL.Editor rank;

	private boolean debugMode;

	public MySQLManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		if (this.config.getBoolean("Enable")) {

			this.loginToMySQL();
			this.loginToRankTable();
			this.migrate();
		}
	}

	private void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File file = new File(folder, "mysql.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(file, Charsets.UTF_8);
			if (this.config != null) {
				this.config.set("Enable", false);
				this.config.set("DebugMode", false);
				this.config.set("Username", "root");
				this.config.set("Password", "password");
				this.config.set("Address", "localhost:3306");
				this.config.set("Database", "neonmc");
				this.config.set("Timezone", "CST");
				this.config.set("UseSSL", false);
				Config.saveConfig(this.config, file);
			}
		}

		this.config = Config.getConfig(file, Charsets.UTF_8);

		if (this.config != null) {
			this.debugMode = this.config.getBoolean("DebugMode");
		} else {
			this.debugMode = false;
		}
	}

	private void loginToMySQL() {
		MySQL mySQL = new MySQL(this.config.getString("Username"), this.config.getString("Password"));
		mySQL.setAddress(this.config.getString("Address"));
		mySQL.setDatabase(this.config.getString("Database"));
		mySQL.setTable("bungeefriends");
		mySQL.setTimezone(this.config.getString("Timezone"));
		mySQL.setUseSSL(this.config.getBoolean("UseSSL"));

		try {
			this.editor = mySQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ThreadPool.run(this::autoReconnect);
	}

	private void loginToRankTable() {
		MySQL mySQL = new MySQL(this.config.getString("Username"),
				this.config.getString("Password"),
				this.config.getString("Address"),
				"neonmc",
				"rank",
				this.config.getString("Timezone"),
				this.config.getBoolean("UseSSL"));
		try {
			this.rank = mySQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ThreadPool.run(this::autoReconnect);
	}

	@SuppressWarnings("InfiniteRecursion")
	private void autoReconnect() {
		if (this.debugMode) {
			this.plugin.info("Reconnecting in 30 minutes...");
		}

		try {
			TimeUnit.MINUTES.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (this.debugMode) {
			this.plugin.info("Reconnecting...");
		}
		try {
			this.editor = this.editor.getMySQL().reconnect();
			this.rank = this.rank.getMySQL().reconnect();
		} catch (SQLException e) {
			if (this.debugMode) {
				this.plugin.info("Reconnection failed!");
			}
			this.autoReconnect();
			return;
		}
		if (this.debugMode) {
			this.plugin.info("Reconnection succeeded!");
		}
		this.autoReconnect();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void migrate() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File file = new File(folder, "config.yml");
		if (file.exists()) {
			Configuration config = Config.getConfig(file, Charsets.UTF_8);
			assert config != null;
			if (config.getBoolean("Plugin.AlreadyMigrated")) {
				return;
			}

			if (this.debugMode) {
				this.plugin.info("Migration from config.yml to MySQL is starting...");
			}

			this.editor.set("Plugin.Prefix", config.getString("Plugin.Prefix"));

			Collection<String> player = config.getSection("Player").getKeys();
			for (String value : player) {
				if (this.debugMode) {
					this.plugin.info("Migrating: " + value);
				}
				this.editor.set("Player." + value + ".Name", config.getString("Player." + value + ".Name"));
				this.editor.set("Player." + value + ".Language", config.getString("Player." + value + ".Language"));
				this.editor.set("Player." + value + ".Prefix", config.getString("Player." + value + ".Prefix"));
				this.editor.set("Player." + value + ".Friends", config.getStringList("Player." + value + ".Friends"));
				List<String> list = config.getStringList("Player." + value + ".Requests");
				if (list.size() == 0) {
					this.editor.set("Player." + value + ".Requests", "[]");
				} else {
					this.editor.set("Player." + value + ".Requests", config.getStringList("Player." + value + ".Requests"));
				}
			}

			config.set("Plugin.AlreadyMigrated", true);
			Config.saveConfig(config, file);

			if (this.debugMode) {
				this.plugin.info("Migration from config.yml succeeded!");
			}
		}

		file = new File(folder, "player.yml");
		if (file.exists()) {
			Configuration config = Config.getConfig(file, Charsets.UTF_8);
			assert config != null;
			if (config.getBoolean("Plugin.AlreadyMigrated")) {
				return;
			}

			if (this.debugMode) {
				this.plugin.info("Migration from player.yml to MySQL is starting...");
			}

			Collection<String> collection = config.getSection("UUID").getKeys();
			for (String value : collection) {
				if (this.debugMode) {
					this.plugin.info("Migrating: " + value);
				}
				this.editor.set("UUID." + value, config.getString("UUID." + value));
			}
			collection = config.getSection("Name").getKeys();
			for (String value : collection) {
				if (this.debugMode) {
					this.plugin.info("Migrating: " + value);
				}
				this.editor.set("Name." + value, config.getString("Name." + value));
			}

			config.set("Plugin.AlreadyMigrated", true);
			Config.saveConfig(config, file);

			if (this.debugMode) {
				this.plugin.info("Migration from player.yml succeeded!");
			}
		}
	}

	public void save() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		{
			File file = new File(folder, "config.yml");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Configuration config = Config.getConfig(file, Charsets.UTF_8);

			if (this.debugMode) {
				this.plugin.info("Migration from MySQL to config.yml is starting...");
			}
			for (String key : this.editor.getAllKeys()) {
				if (key.contains("Plugin.Prefix") || key.contains(".Name") || key.contains(".Language") || key.contains(".Prefix")) {
					assert config != null;
					config.set(key, this.editor.get(key));
				}
				if (key.contains(".Friends") || key.contains(".Requests")) {
					assert config != null;
					config.set(key, this.editor.getList(key));
				}
			}
			Config.saveConfig(config, file);

			if (this.debugMode) {
				this.plugin.info("Migration to config.yml succeeded!");
			}
		}

		{
			File file = new File(folder, "player.yml");
			if (file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Configuration config = Config.getConfig(file, Charsets.UTF_8);

			if (this.debugMode) {
				this.plugin.info("Migration from MySQL to player.yml is starting...");
			}
			for (String key : this.editor.getAllKeys()) {
				if (key.contains("UUID.") || key.contains("Name.")) {
					assert config != null;
					config.set(key, this.editor.get(key));
				}
			}
			Config.saveConfig(config, file);

			if (this.debugMode) {
				this.plugin.info("Migration to player.yml succeeded!");
			}
		}
	}

	public Configuration getConfig() {
		return config;
	}

	public MySQL.Editor getEditor() {
		return editor;
	}

	MySQL.Editor getRankEditor() {
		return rank;
	}
}
