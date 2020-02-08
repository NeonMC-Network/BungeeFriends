package net.simplyrin.bungeefriends.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.*;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.utils.FriendManager;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public class FriendCommand extends Command {

	private Main plugin;
	private List<String> availableLanguages = new ArrayList<>();

	public FriendCommand(Main plugin, String command) {
		super(command, null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			this.plugin.info(Messages.INGAME_ONLY);
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		if (!player.hasPermission(Permissions.MAIN)) {
			this.plugin.info(player, langUtils.getString(Messages.NO_PERMISSION));
			return;
		}

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("add")) {
				if (args.length > 1) {
					this.add(player, myFriends, langUtils, args[1]);
					return;
				}
				this.plugin.info(player, langUtils.getString("Add.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("remove")) {
				if (args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

					try {
						myFriends.remove(target);
					} catch (NotAddedException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Exceptions.IsntOnYourFriends").replace("%targetDisplayName", targetFriends.getDisplayName()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					} catch (SelfException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Exceptions.CantRemoveYourself"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Remove.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

					this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(target, targetLangUtils.getString("Remove.Target").replace("%displayName", myFriends.getDisplayName()));
					this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
					return;
				}
				this.plugin.info(player, langUtils.getString("Remove.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("accept")) {
				if (args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					if (this.plugin.getProxy().getPlayer(target) != null) {
						FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
						LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

						try {
							targetFriends.removeRequest(player);
						} catch (NotAddedException e) {
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							this.plugin.info(player, langUtils.getString("Exceptions.NoInvited").replace("%name", targetFriends.getName()));
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							return;
						}

						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Accept.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

						this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(target, targetLangUtils.getString("Accept.Target").replace("%displayName", myFriends.getDisplayName()));
						this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));

						try {
							myFriends.add(target);
						} catch (AlreadyAddedException | FailedAddingException e) {
							e.printStackTrace();
						}
						return;
					} else {
						List<String> requests = this.plugin.getStringList("Player." + target.toString() + ".Requests");
						if (!requests.contains(target.toString())) {
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							this.plugin.info(player, langUtils.getString("Exceptions.NoInvited").replace("%name", this.plugin.getString("Player." + target.toString() + ".Name")));
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							return;
						}
						requests.remove(target.toString());
						this.plugin.set("Player." + target.toString() + ".Requests", requests);

						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Accept.YourSelf").replace("%targetDisplayName", this.plugin.getPrefixManager().getDefaultPrefix(target) + this.plugin.getString("Player." + target.toString() + ".Name")));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

						try {
							myFriends.add(target);
						} catch (AlreadyAddedException | FailedAddingException e) {
							e.printStackTrace();
						}
						return;
					}
				}
				this.plugin.info(player, langUtils.getString("Accept.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("deny")) {
				if (args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						targetFriends.removeRequest(player);
					} catch (NotAddedException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Exceptions.HasntFriend").replace("%name", targetFriends.getName()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Deny.Declined").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}
				this.plugin.info(player, langUtils.getString("Deny.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("list")) {
				List<String> list = myFriends.getFriends();

				if (list.size() == 0) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("List.DontHave.One"));
					this.plugin.info(player, langUtils.getString("List.DontHave.Two"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				List<String> online = new ArrayList<>();
				List<String> offline = new ArrayList<>();

				for (String uuid : list) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(UUID.fromString(uuid));
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(UUID.fromString(uuid));

					if (target != null) {
						online.add(langUtils.getString("List.Online").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%server", target.getServer().getInfo().getName()));
					} else {
						offline.add(langUtils.getString("List.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
					}
				}

				List<String> all = new ArrayList<>();
				all.addAll(online);
				all.addAll(offline);

				int page = 0;
				if (args.length > 1) {
					try {
						if (args[1].equals("1")) {
							args[1] = "0";
						}
						page = Integer.valueOf(args[1]).intValue();
						if (page >= 1) {
							page--;
						}
					} catch (Exception e) {
					}
				}

				if (list.size() <= 7) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, "               " + langUtils.getString("List.Page").replace("%%currentPage%%", String.valueOf(page)).replace("%%maxPage%%", "1"));

					for (String message : all) {
						this.plugin.info(player, message);
					}

					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				List<List<String>> divide = this.divide(all, 7);

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, "               " + langUtils.getString("List.Page").replace("%%currentPage%%", String.valueOf(page + 1)).replace("%%maxPage%%", String.valueOf(divide.size())));

				List<String> pages;
				try {
					pages = divide.get(page);
					if (pages != null && pages.size() >= 1) {
						for (String message : pages) {
							this.plugin.info(player, message);
						}
					}
				} catch (Exception e) {
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}


			if (args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language")) {

				this.plugin.info(player, ChatColor.RED + "This command is outdated. Use /language instead. 该指令已过时。请使用 /language。");

				return;

				/*

				File folder = this.plugin.getDataFolder();
				if (!folder.exists()) {
					folder.mkdir();
				}

				File languageFolder = new File(folder, "Language");
				if (!languageFolder.exists()) {
					languageFolder.mkdir();
				}

 // In this version of BungeeFriends, language files are different from the ones in the original version. So let's delete this feature.
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("update")) {
						if (!player.hasPermission(Permissions.ADMIN)) {
							this.plugin.info(player, Messages.NO_PERMISSION);
							return;
						}

						if (args.length > 2) {
							if (this.availableLanguages.size() == 0) {
								this.plugin.info(player, langUtils.getString("Lang.Updater.NeedCheck"));
								return;
							}

							String lang = null;
							for (String available : this.availableLanguages) {
								if (args[2].equalsIgnoreCase(available)) {
									lang = available;
								}
							}

							if (lang == null) {
								this.plugin.info(player, langUtils.getString("Lang.Updater.Unknown"));
								return;
							}

							final String outputLang = lang;
							ThreadPool.run(() -> {
								Configuration config;
								try {
									config = Config.getConfig(new URL("https://api.simplyrin.net/Bungee-Plugins/BungeeFriends/Languages/Files/" + outputLang + ".yml"));
								} catch (Exception e) {
									this.plugin.info(player, langUtils.getString("Lang.Updater.Failed-Connect"));
									return;
								}

								File file = new File(languageFolder, outputLang.toLowerCase() + ".yml");
								Config.saveConfig(config, file);

								this.plugin.info(player, langUtils.getString("Lang.Updater.Updated").replace("%lang", outputLang));
							});
							return;
						}

						ThreadPool.run(() -> {
							this.plugin.info(player, langUtils.getString("Lang.Updater.Checking"));
							Configuration config;
							try {
								config = Config.getConfig(new URL("https://api.simplyrin.net/Bungee-Plugins/BungeeFriends/Languages/available.txt"));
							} catch (Exception e) {
								e.printStackTrace();
								this.plugin.info(player, langUtils.getString("Lang.Updater.Failed-Connect"));
								return;
							}

							this.availableLanguages = new ArrayList<>();

							this.plugin.info(player, langUtils.getString("Lang.Updater.LastUpdate").replace("%data", config.getString("LastUpdate")));
							this.plugin.info(player, langUtils.getString("Lang.Updater.Available"));
							for (String lang : config.getStringList("Langs")) {
								this.plugin.info(player, "&b- " + lang);
								this.availableLanguages.add(lang);
							}
						});

						this.plugin.info(player, langUtils.getString("Lang.Updater.Usage"));
						return;
					}
				}

				List<String> availableList = new ArrayList<>();
				StringBuilder available = new StringBuilder();
				File[] languages = languageFolder.listFiles();
				if (languages != null) {
					for (File languageFile : languages) {
						Configuration langConfig = Config.getConfig(languageFile, Charsets.UTF_8);
						if (langConfig != null && langConfig.getString("Language").length() > 1) {
							availableList.add(languageFile.getName().toLowerCase().replace(".yml", ""));
							available.append(langConfig.getString("Language")).append(", ");
						}
					}
				}

				if (args.length > 1) {
					String lang = args[1];
					if (availableList.contains(lang.toLowerCase())) {
						langUtils.setLanguage(lang.toLowerCase());
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Lang.Update").replace("%lang", langUtils.getLanguage()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Lang.Usage"));
				this.plugin.info(player, langUtils.getString("Lang.Available") + " " + available.substring(0, available.length() - 1));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;

				*/

			}

			if (args[0].equalsIgnoreCase("ignore")) {
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("add")) {
						if (args.length > 2) {
							UUID target = this.plugin.getPlayerUniqueId(args[2]);
							if (target == null) {
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[2]));
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								return;
							}
							FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

							try {
								myFriends.addIgnore(target);
							} catch (AlreadyAddedException e) {
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								this.plugin.info(player, langUtils.getString("Ignore.AlreadyAdded").replace("%targetDisplayName", targetFriends.getDisplayName()));
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								return;
							}

							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							this.plugin.info(player, langUtils.getString("Ignore.Added").replace("%targetDisplayName", targetFriends.getDisplayName()));
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							return;
						}

						this.plugin.info(player, langUtils.getString("Ignore.Usage.Add"));
						return;
					}

					if (args[1].equalsIgnoreCase("remove")) {
						if (args.length > 2) {
							UUID target = this.plugin.getPlayerUniqueId(args[2]);
							if (target == null) {
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[2]));
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								return;
							}
							FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

							try {
								myFriends.removeIgnore(target);
							} catch (NotAddedException e) {
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								this.plugin.info(player, langUtils.getString("Ignore.NotAdded").replace("%targetDisplayName", targetFriends.getDisplayName()));
								this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
								return;
							}

							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							this.plugin.info(player, langUtils.getString("Ignore.Removed").replace("%targetDisplayName", targetFriends.getDisplayName()));
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							return;
						}

						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Ignore.Usage.Remove"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					if (args[1].equalsIgnoreCase("list")) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						List<String> ignoreList = myFriends.getIgnoreList();
						if (ignoreList.size() == 0) {
							this.plugin.info(player, langUtils.getString("Ignore.Havent.One"));
							this.plugin.info(player, langUtils.getString("Ignore.Havent.Two"));
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							return;
						}
						for (String targetUniqueId : myFriends.getIgnoreList()) {
							FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(UUID.fromString(targetUniqueId));
							this.plugin.info(player, "&e- " + targetFriends.getDisplayName());
						}
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
				}
				this.plugin.info(player, langUtils.getString("Ignore.Usage.Main"));
				return;
			}

			if (args[0].equalsIgnoreCase("force-add")) {
				if (!player.hasPermission(Permissions.ADMIN)) {
					this.plugin.info(player, Messages.NO_PERMISSION);
					return;
				}

				if (args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

					try {
						myFriends.add(target);
					} catch (AlreadyAddedException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Exceptions.AlreadyFriend"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					} catch (FailedAddingException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Exceptions.CantAddYourSelf"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Force-Add.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

					this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(target, targetLangUtils.getString("Force-Add.Target").replace("%displayName", myFriends.getDisplayName()));
					this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
					return;
				}

				this.plugin.info(player, langUtils.getString("Force-Add.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("prefix")) {
				if (!player.hasPermission(Permissions.ADMIN)) {
					this.plugin.info(player, Messages.NO_PERMISSION);
					return;
				}

				if (args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					if (args.length > 2) {
						StringBuilder prefix = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							prefix.append(args[i]).append(" ");
						}

						if (!prefix.toString().endsWith(" ")) {
							prefix.append(" ");
						}

						targetFriends.setPrefix(prefix.toString());
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Prefix.To").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%prefix", ChatColor.translateAlternateColorCodes('&', prefix.toString()).substring(0, prefix.length() - 1)));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Prefix.Current").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%prefix", targetFriends.getPrefix().equalsIgnoreCase("&7") ? targetFriends.getPrefix() : targetFriends.getPrefix().substring(0, targetFriends.getPrefix().length() - 1)));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				this.plugin.info(player, langUtils.getString("Prefix.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("reset-prefix") || args[0].equalsIgnoreCase("resetprefix") || args[0].equalsIgnoreCase("resetpref")) {
				if (!player.hasPermission(Permissions.ADMIN)) {
					this.plugin.info(player, langUtils.getString(Messages.NO_PERMISSION));
					return;
				}

				if (args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					targetFriends.setToDefaultPrefix(true);

					this.plugin.info(player, langUtils.getString("ResetPrefix.To").replace("%targetDisplayName", targetFriends.getDisplayName())
							.replace("%prefix", (targetFriends.getDefaultPrefix().equals("&7")
									? ChatColor.translateAlternateColorCodes('&', langUtils.getString("ResetPrefix.Default"))
									: ChatColor.translateAlternateColorCodes('&', targetFriends.getDefaultPrefix()).substring(0, targetFriends.getDefaultPrefix().length() - 1))));
					return;
				}
				this.plugin.info(player, langUtils.getString("ResetPrefix.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("toggle")) {
				boolean bool = this.plugin.getBoolean("Player." + myFriends.getUniqueId().toString() + ".Toggle");
				if (bool) {
					this.plugin.set("Player." + myFriends.getUniqueId().toString() + ".Toggle", false);
					this.plugin.info(player, langUtils.getString("Toggle.Enabled"));
				} else {
					this.plugin.set("Player." + myFriends.getUniqueId().toString() + ".Toggle", true);
					this.plugin.info(player, langUtils.getString("Toggle.Disabled"));
				}
				return;
			}

			if (args[0].equalsIgnoreCase("help")) {
				this.printHelp(player, langUtils);
				return;
			}

			this.add(player, myFriends, langUtils, args[0]);
			return;
		}
		this.printHelp(player, langUtils);
	}

	private void printHelp(ProxiedPlayer player, LanguageUtils langUtils) {
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		this.plugin.info(player, langUtils.getString("Help.Command"));
		this.plugin.info(player, langUtils.getString("Help.Help"));
		// this.plugin.info(player, langUtils.getString("Help.Lang"));
		this.plugin.info(player, langUtils.getString("Help.Add"));
		this.plugin.info(player, langUtils.getString("Help.Remove"));
		this.plugin.info(player, langUtils.getString("Help.Accept"));
		this.plugin.info(player, langUtils.getString("Help.Deny"));
		this.plugin.info(player, langUtils.getString("Help.List"));
		this.plugin.info(player, langUtils.getString("Help.Ignore"));
		if (player.hasPermission(Permissions.ADMIN)) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Help.Force-Add"));
			this.plugin.info(player, langUtils.getString("Help.Prefix"));
			this.plugin.info(player, langUtils.getString("Help.ResetPrefix"));
		}
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
	}

	public void add(ProxiedPlayer player, FriendUtils myFriends, LanguageUtils langUtils, String name) {
		UUID target = this.plugin.getPlayerUniqueId(name);
		if (target == null) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", name));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}
		FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
		LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

		try {
			myFriends.addRequest(target);
		} catch (FailedAddingException e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Exceptions.AlreadySent"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		} catch (AlreadyAddedException e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Exceptions.AlreadyFriend"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		} catch (SelfException e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Exceptions.CantAddYourSelf"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		} catch (IgnoredException e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Exceptions.Ignored"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		} catch (FriendSlotLimitException e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Exceptions.SlotLimitReached"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		} catch (RequestDenyException e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Exceptions.RequestDeny"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}

		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		this.plugin.info(player, langUtils.getString("Add.Sent").replace("%targetDisplayName", targetFriends.getDisplayName()));
		this.plugin.info(player, langUtils.getString("Add.5-Minutes"));
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

		TextComponent prefix = MessageBuilder.get(this.plugin.getPrefix());
		TextComponent grayHyphen = MessageBuilder.get("&r &8- &r", null, ChatColor.DARK_GRAY, null, false);

		TextComponent accept = MessageBuilder.get(targetLangUtils.getString("Add.Accept.Prefix"), "/friend accept " + myFriends.getName(), ChatColor.GREEN, targetLangUtils.getString("Add.Accept.Message"), true);
		TextComponent deny = MessageBuilder.get(targetLangUtils.getString("Add.Deny.Prefix"), "/friend deny " + myFriends.getName(), ChatColor.GREEN, targetLangUtils.getString("Add.Deny.Message"), true);
		TextComponent ignore = MessageBuilder.get(targetLangUtils.getString("Add.Ignore.Prefix"), "/friend ignore add " + myFriends.getName(), ChatColor.GREEN, targetLangUtils.getString("Add.Ignore.Message"), true);

		this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
		this.plugin.info(target, targetLangUtils.getString("Add.Request.Received").replace("%displayName", myFriends.getDisplayName()));
		if (targetFriends.getPlayer() != null) {
			targetFriends.getPlayer().sendMessage(prefix, accept, grayHyphen, deny, grayHyphen, ignore);
		}
		this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));

		ThreadPool.run(() -> {
			try {
				TimeUnit.MINUTES.sleep(5);
			} catch (Exception ignored) {
			}

			try {
				myFriends.removeRequest(target);
			} catch (NotAddedException e) {
				return;
			}

			FriendCommand.this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			FriendCommand.this.plugin.info(player, langUtils.getString("Add.Expired.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
			FriendCommand.this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

			FriendCommand.this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
			FriendCommand.this.plugin.info(target, targetLangUtils.getString("Add.Expired.Target").replace("%displayName", myFriends.getDisplayName()));
			FriendCommand.this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
		});
	}

	/**
	 * @author seijikohara
	 * https://qiita.com/seijikohara/items/ae3c428d7a7f6f013c0a
	 */
	public <T> List<List<T>> divide(List<T> original, int size) {
		if (original == null || original.isEmpty() || size <= 0) {
			return Collections.emptyList();
		}

		try {
			int block = original.size() / size + (original.size() % size > 0 ? 1 : 0);

			return IntStream.range(0, block).boxed().map(i -> {
				int start = i * size;
				int end = Math.min(start + size, original.size());
				return original.subList(start, end);
			}).collect(Collectors.toList());
		} catch (Exception e) {
			return null;
		}
	}

}
