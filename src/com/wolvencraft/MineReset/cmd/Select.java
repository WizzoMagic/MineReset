package com.wolvencraft.MineReset.cmd;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.wolvencraft.MineReset.CommandManager;

public class Select
{
	public static Location[] run(String[] args)
	{
		if(!Util.isPlayer() || !Util.hasPermission("edit", false))
		{
			Util.sendDenied(args[0]);
			return null;
		}
		
		if(args.length == 1)
		{
			Help.getSelect();
		}
		if(args.length > 2)
		{
			Util.sendInvalid(args[0] + " " + args[1] + " " + args[2]);
		}
		
		Location[] loc = {null, null};
		Player player = (Player) CommandManager.getSender();
		
		if(args[1].equalsIgnoreCase("hpos1"))
		{
			loc[0] = player.getTargetBlock(null, 100).getLocation();
			Util.sendSuccess ("First point selected");
		}
		else if(args[1].equalsIgnoreCase("pos1"))
		{
			loc[0] = player.getLocation();
			Util.sendSuccess ("First point selected");
		}
		else if(args[1].equalsIgnoreCase("hpos2"))
		{
			loc[1] = player.getTargetBlock(null, 100).getLocation();
			Util.sendSuccess ("Second point selected");
		}
		else if(args[1].equalsIgnoreCase("pos2"))
		{
			loc[1] = player.getLocation();
			Util.sendSuccess ("Second point selected");
		}
		else
		{
			Util.sendInvalid(args[0] + " " + args[1] + " " + args[2]);
			return null;
		}
		return loc;
	}
}
