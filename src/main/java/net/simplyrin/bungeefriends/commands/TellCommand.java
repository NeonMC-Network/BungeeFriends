package net.simplyrin.bungeefriends.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

import java.util.UUID;

/**
 * Created by SimplyRin on 2018/09/14.
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
public class TellCommand extends Command {

	private Main plugin;

	public TellCommand(Main plugin) {
		super("tell", null, "msg", "message");
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

		if (args.length > 0) {
			UUID target = this.plugin.getPlayerUniqueId(args[0]);
			if (target == null) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[0]));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
			FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
			LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

			if (!(myFriends.isFriend(targetFriends.getUniqueId()) || player.hasPermission(Permissions.ADMIN))) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Tell-Command.MustBeFriends").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (targetFriends.getPlayer() == null) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Tell-Command.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args.length > 1) {
				StringBuilder message = new StringBuilder();
				for (int i = 1; i < args.length; i++) {
					message.append(args[i]).append(" ");
				}

				this.plugin.info(player, langUtils.getString("Tell-Command.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%message", message.toString()));
				this.plugin.info(targetFriends.getPlayer(), targetLangUtils.getString("Tell-Command.Target").replace("%displayName", myFriends.getDisplayName()).replace("%message", message.toString()));

				this.plugin.getReplyTargetMap().put(targetFriends.getUniqueId(), player.getUniqueId());
				return;
			}
		}

		this.plugin.info(player, langUtils.getString("Tell-Command.Usage"));
	}

}
