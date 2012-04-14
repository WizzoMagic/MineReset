package com.wolvencraft.MineReset;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.wolvencraft.MineReset.cmd.*;

import com.wolvencraft.MineReset.cmd.Help;

public class CommandManager implements CommandExecutor
{
	private static CommandSender sender;
	private static MineReset plugin;
	private static Location[] loc = null;
	
	public CommandManager(MineReset plugin)
	{
		CommandManager.plugin = plugin;
		plugin.getLogger();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		CommandManager.sender = sender;
		
		if(!command.getName().equalsIgnoreCase("mine")) return false;
		
		if(args.length == 0) Help.get();
		
		if(args[0].equalsIgnoreCase("info")) {}
		else if(args[0].equalsIgnoreCase("list")) {}
		else if(args[0].equalsIgnoreCase("reset")) {}
		else if(args[0].equalsIgnoreCase("select")) {}
		else if(args[0].equalsIgnoreCase("save")) {}
		else if(args[0].equalsIgnoreCase("edit")) {}
		else if(args[0].equalsIgnoreCase("delete")) {}
		else if(args[0].equalsIgnoreCase("auto")) {}
		else if(args[0].equalsIgnoreCase("protection")) {}
		else Util.sendInvalid(args[0]);
			
		return true;
	}
	
	// TODO: This should be moved to Util
	/**
	 * Checks if the mine exists
	 * @param name Name of the mine being checked
	 * @return True if the mine exists, False if it does not
	 */
	public static boolean mineExists(String name)
	{
		List<String> mineList = plugin.getRegionData().getStringList("data.list-of-mines");
		if(mineList.indexOf(name) == -1) return false;
		else return true;
	}
	
	/**
	 * Returns the command sender
	 * @return CommandSender
	 */
	public static CommandSender getSender()
	{
		return sender;
	}
	
	/**
	 * Returns the plugin
	 * @return MineReset
	 */
	public static MineReset getPlugin()
	{
		return plugin;
	}
	
	/**
	 * Returns the location selected with either a command or a wand
	 * @return Location[] if a selection was made, null if it was not
	 */
	public static Location[] getLocation()
	{
		return loc;
	}
}