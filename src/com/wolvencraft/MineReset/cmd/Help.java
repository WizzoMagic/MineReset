package com.wolvencraft.MineReset.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.wolvencraft.MineReset.CommandManager;

public class Help
{
	public static void run()
	{
		String title = Util.getConfigString("messages.title");
		Util.sendMessage("                    -=[ " + title + " ]=-");
		
		formatHelp("mine info", " <name>", "Returns the information about a mine", "info");
		
		formatHelp("mine list", "", "Lists all the available mines", "list");
		
		formatHelp("mine reset", " <name>", "Resets the mine automatically", "reset");
		
		formatHelp("mine select", "", "Shows region selection help page", "edit");
		
		formatHelp("mine save", " <name>", "Saves the region for future use", "edit");
		
		formatHelp("mine edit", "", "Shows a help page on how to handle the mine options", "edit");
		
		formatHelp("mine delete", " <name>", "Completely deletes a mine and all information about it", "edit");
		
		formatHelp("mine auto", "", "Shows a help page on how to automate the mine", "auto");
		
		formatHelp("mine protection", "", "Shows how to set up a mine protection", "protection");
		
		return;
	}
	
	public static void getSave()
	{
		formatHelp("mine save", " <name>", "Saves the region for future use", "");
		Util.sendMessage(" If no name is provided, the default name will be used");
		Util.sendMessage(" The default name is defined in the configuration file");
		
		return;
	}
	
	public static void getSelect()
	{
		formatHelp("mine select", " hpos1", "Creates a reference point 1 at the block you are looking at", "");
		formatHelp("mine select", " hpos2", "Creates a reference point 2 at the block you are looking at", "");
		Util.sendMessage(" Your field of view is limited to 100 blocks");
		formatHelp("mine select", " pos1", "Creates a reference point 1 at your immediate location", "");
		formatHelp("mine select", " pos2", "Creates a reference point 2 at your immediate location", "");
		Util.sendMessage(" You can also select a region with your normal World Edit tool");
		
		return;
	}
	
	private static void formatHelp(String command, String arguments, String description, String node)
	{
		CommandSender sender = CommandManager.getSender();
		if(Util.senderHasPermission(node, true))
			sender.sendMessage(ChatColor.GREEN + "/" + command + ChatColor.GRAY + arguments + ChatColor.WHITE + " " + description);
		
		return;
	}
}
