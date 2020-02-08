package net.simplyrin.bungeefriends.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.*;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.tools.Config;
import net.simplyrin.threadpool.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
public class FriendManager {

	private Main plugin;

	public FriendManager(Main plugin) {
		this.plugin = plugin;
		ThreadPool.run(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			checkFriends();
		});
	}

	private void checkFriends() {
		for (String key : this.plugin.getAllKeys()) {
			if (key.contains(".Friends")) {
				UUID player = UUID.fromString(key.replace("Player.", "").replace(".Friends", ""));
				List<UUID> playerFriends = new ArrayList<>();
				for (String uuid : this.plugin.getStringList(key)) {
					playerFriends.add(UUID.fromString(uuid));
				}
				for (UUID target : playerFriends) {
					List<String> targetFriends = FriendManager.this.plugin.getStringList("Player." + target.toString() + ".Friends");
					if (playerFriends.contains(target) && !targetFriends.contains(player.toString())) {
						targetFriends.add(player.toString());
						FriendManager.this.plugin.set("Player." + target.toString() + ".Friends", targetFriends);
						this.plugin.info("Repaired the friendship of " + this.plugin.getString("Player." + player.toString() + ".Name") + " and " + this.plugin.getString("Player." + target.toString() + ".Name") + "!");
					}
				}
			}
		}
	}

	private void checkFriends(UUID player) {
		String key = "Player." + player.toString() + ".Friends";
		List<UUID> playerFriends = new ArrayList<>();
		for (String uuid : this.plugin.getStringList(key)) {
			playerFriends.add(UUID.fromString(uuid));
		}
		for (UUID target : playerFriends) {
			List<String> targetFriends = FriendManager.this.plugin.getStringList("Player." + target.toString() + ".Friends");
			if (playerFriends.contains(target) && !targetFriends.contains(player.toString())) {
				targetFriends.add(player.toString());
				FriendManager.this.plugin.set("Player." + target.toString() + ".Friends", targetFriends);
				this.plugin.info("Repaired the friendship of " + this.plugin.getString("Player." + player.toString() + ".Name") + " and " + this.plugin.getString("Player." + target.toString() + ".Name") + "!");
			}
		}
	}

	public FriendUtils getPlayer(ProxiedPlayer player) {
		return new FriendUtils(player.getUniqueId());
	}

	public FriendUtils getPlayer(UUID uniqueId) {
		return new FriendUtils(uniqueId);
	}

	public class FriendUtils {

		private UUID uuid;

		FriendUtils(UUID uuid) {
			this.uuid = uuid;

			ProxiedPlayer player = FriendManager.this.plugin.getProxy().getPlayer(this.uuid);
			FriendManager.this.plugin.set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
			FriendManager.this.plugin.set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());

			Object object = FriendManager.this.plugin.getString("Player." + this.uuid.toString() + ".Name");
			if (object == null || object.equals("")) {

				FriendManager.this.plugin.info("Creating data for player " + player.getName() + "...");

				FriendManager.this.plugin.set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
				FriendManager.this.plugin.set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());

				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Name", player.getName());
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Language", FriendManager.this.plugin.getConfigManager().getConfig().getString("Plugin.Default-Language"));
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Prefix", "&7");
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Toggle", true);
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Friends", "[]");
			} else {
				setToDefaultPrefix(false);
			}
		}

		public ProxiedPlayer getPlayer() {
			return FriendManager.this.plugin.getProxy().getPlayer(this.uuid);
		}

		public String getDisplayName() {
			return this.getPrefix() + this.getName();
		}

		public String getName() {
			return FriendManager.this.plugin.getString("Player." + this.uuid.toString() + ".Name");
		}

		boolean isEnabledReceiveRequest() {
			return FriendManager.this.plugin.getBoolean("Player." + this.uuid.toString() + ".Toggle");
		}

		public String getPrefix() {
			ProxiedPlayer player = this.getPlayer();
			if (player != null) {
				Collection<String> collection = FriendManager.this.plugin.getPrefixManager().getConfig().getSection("List").getKeys();
				for (String list : collection) {
					String prefix = FriendManager.this.plugin.getPrefixManager().getConfig().getString("List." + list + ".Prefix");
					String permission = FriendManager.this.plugin.getPrefixManager().getConfig().getString("List." + list + ".Permission");

					if (player.hasPermission(permission)) {
						FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Prefix", prefix);
						return prefix;
					}
				}
			}

			return FriendManager.this.plugin.getString("Player." + this.uuid.toString() + ".Prefix");
		}

		public FriendUtils setPrefix(String prefix) {
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Prefix", prefix);
			return this;
		}

		public UUID getUniqueId() {
			return this.uuid;
		}

		@SuppressWarnings("unused")
		public List<String> getRequests() {
			return FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Requests");
		}

		public boolean isFriend(UUID targetUniqueId) {
			List<String> list = this.getFriends();
			return list.contains(targetUniqueId.toString());
		}

		@SuppressWarnings("unused")
		public FriendUtils addRequest(ProxiedPlayer player) throws AlreadyAddedException, FailedAddingException, SelfException, IgnoredException, FriendSlotLimitException, RequestDenyException {
			return this.addRequest(player.getUniqueId());
		}

		public FriendUtils addRequest(UUID uuid) throws AlreadyAddedException, FailedAddingException, SelfException, IgnoredException, FriendSlotLimitException, RequestDenyException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new SelfException();
			}

			List<String> list = this.getFriends();
			if (list.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}

			ProxiedPlayer player = this.getPlayer();
			if (player != null) {
				if (this.getPlayer().hasPermission("friends.limit." + this.getFriends().size()) && !this.getPlayer().hasPermission(Permissions.ADMIN)) {
					throw new FriendSlotLimitException();
				}
			}

			FriendUtils targetFriendUtils = FriendManager.this.getPlayer(uuid);
			List<String> ignoreList = targetFriendUtils.getIgnoreList();
			if (ignoreList.contains(this.uuid.toString())) {
				throw new IgnoredException();
			}

			if (!targetFriendUtils.isEnabledReceiveRequest()) {
				throw new RequestDenyException();
			}

			List<String> requests = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Requests");
			if (requests.contains(uuid.toString())) {
				throw new FailedAddingException();
			}
			requests.add(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public void removeRequest(ProxiedPlayer player) throws NotAddedException {
			this.removeRequest(player.getUniqueId());
		}

		public void removeRequest(UUID uuid) throws NotAddedException {
			List<String> requests = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Requests");
			if (!requests.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			requests.remove(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Requests", requests);
		}

		public List<String> getFriends() {
			checkFriends(this.uuid);
			return FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Friends");
		}

		public FriendUtils add(ProxiedPlayer player) throws AlreadyAddedException, FailedAddingException {
			return this.add(player.getUniqueId());
		}

		public FriendUtils add(UUID uuid) throws AlreadyAddedException, FailedAddingException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new FailedAddingException();
			}

			List<String> list = this.getFriends();
			if (list.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}
			list.add(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Friends", list);

			FriendUtils targetFriends = FriendManager.this.plugin.getFriendManager().getPlayer(uuid);
			List<String> targetList = targetFriends.getFriends();
			if (targetList.contains(this.uuid.toString())) {
				throw new AlreadyAddedException();
			}
			targetList.add(this.uuid.toString());
			FriendManager.this.plugin.set("Player." + uuid.toString() + ".Friends", targetList);
			return this;
		}

		@SuppressWarnings("unused")
		public FriendUtils remove(ProxiedPlayer player) throws NotAddedException, SelfException {
			return this.remove(player.getUniqueId());
		}

		public FriendUtils remove(UUID uuid) throws NotAddedException, SelfException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new SelfException();
			}

			List<String> list = this.getFriends();
			if (!list.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			list.remove(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Friends", list);


			FriendUtils targetFriends = FriendManager.this.plugin.getFriendManager().getPlayer(uuid);
			List<String> targetList = targetFriends.getFriends();
			if (!targetList.contains(this.uuid.toString())) {
				throw new NotAddedException();
			}
			targetList.remove(this.uuid.toString());
			FriendManager.this.plugin.set("Player." + uuid.toString() + ".Friends", targetList);
			return this;
		}

		public List<String> getIgnoreList() {
			return FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".IgnoreList");
		}

		@SuppressWarnings("unused")
		public FriendUtils addIgnore(ProxiedPlayer player) throws AlreadyAddedException {
			return this.addIgnore(player.getUniqueId());
		}

		public FriendUtils addIgnore(UUID uuid) throws AlreadyAddedException {
			List<String> ignoreList = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".IgnoreList");
			if (ignoreList.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}
			ignoreList.add(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".IgnoreList", ignoreList);
			return this;
		}

		@SuppressWarnings("unused")
		public FriendUtils removeIgnore(ProxiedPlayer player) throws NotAddedException {
			return this.removeIgnore(player.getUniqueId());
		}

		public FriendUtils removeIgnore(UUID uuid) throws NotAddedException {
			List<String> ignoreList = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".IgnoreList");
			if (!ignoreList.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			ignoreList.remove(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".IgnoreList", ignoreList);
			return this;
		}

		public String getDefaultPrefix() {

			if (FriendManager.this.plugin.isEnabledMySQL) {

				//if (FriendManager.this.plugin.getMySQLManager().getRankEditor().contains(String.valueOf(getUniqueId()))) {
				String rank = FriendManager.this.plugin.getMySQLManager().getRankEditor().get(String.valueOf(getUniqueId()));

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
				/*} else {
					String result = "&f";
					try {
						FriendManager.this.plugin.getMySQLManager().getRankEditor().getMySQL().reconnect();
						result = getDefaultPrefix();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return result;
				}*/

			} else {
				ProxiedPlayer player = getPlayer();
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

		public void setToDefaultPrefix(boolean forceSet) {
			String pf = getPrefix();

			if (forceSet || pf.contains("&7") || pf.contains("HACKER") || pf.contains("VIP") || pf.contains("MVP") || pf.contains("UP")
					|| pf.contains("MOD") || pf.contains("ADMIN") || pf.contains("OWNER")) {
				setPrefix(getDefaultPrefix());
			}

		}

	}

}
