package com.wtx.potlotto.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.wtx.potlotto.PotLotto;

import net.md_5.bungee.api.ChatColor;

public class LotteryCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] args) {

		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (command.getName().equalsIgnoreCase("pot") && args.length == 0) {
			
			player.sendMessage(PotLotto.prefix + ChatColor.DARK_AQUA + "+-- The current pot contains " + ChatColor.RED + "$" + PotLotto.getInstance().getPotWorth() + ChatColor.DARK_AQUA + " worth of items --+");
			
			for (String syntax : PotLotto.Syntax) {
				player.sendMessage(PotLotto.prefix + syntax);
			}
		} else if (args.length == 1) {
			String param = args[0];

			switch (param) {
			case "pot":
				PotLotto.getInstance().showPot(player);
				break;

			case "buyin":

				if ((player.getItemInHand() == null) || player.getItemInHand().getItemMeta() == null) {
					player.sendMessage(PotLotto.prefix + "You don't have an item in your hand, get one!");
					return false;
				}

				if (player.getInventory().getItemInHand() != null) {

					if (!(Bukkit.getScheduler().isCurrentlyRunning(PotLotto.i))) {
						player.sendMessage(PotLotto.prefix + "A pot is not currently running.");
						return true;
					} else {
						ItemStack item = player.getInventory().getItemInHand();
						ItemMeta im = item.getItemMeta();
						im.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "Pot Item",
								ChatColor.RED + "Supplied by " + ChatColor.BOLD + player.getDisplayName()));
						item.setItemMeta(im);

						PotLotto.getInstance().addItemsToPot(item, player);

						break;
					}
				}

			case "time":
				player.sendMessage(PotLotto.prefix + "The next pot payout is: " + ChatColor.RED
						+ PotLotto.getInstance().untilNextPot());
				break;
			}
		}

		return false;
	}

}
