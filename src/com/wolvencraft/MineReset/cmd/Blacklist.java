package com.wolvencraft.MineReset.cmd;

import org.bukkit.command.CommandSender;

import com.wolvencraft.MineReset.CommandManager;
import com.wolvencraft.MineReset.MineReset;

public class Blacklist
{
	public static void run()
	{
		CommandSender sender = CommandManager.getSender();
		MineReset plugin = CommandManager.getPlugin();
		
		// This function should allow the user to enable blacklist / whitelist for each
		// individual mine, as well as add and remove blocks from them. Checks for the
		// correctness of arguments should be included.
		
		// Help.java includes command help. Util.java has a bunch of helpful functions
		// for accessing the configuration and printing out messages to the user.
	}
}
