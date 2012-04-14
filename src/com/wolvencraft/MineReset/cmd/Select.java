package com.wolvencraft.MineReset.cmd;

import org.bukkit.Location;

public class Select
{
	public static Location[] run(String[] args)
	{
		if(args.length == 1)
		{
			Help.getSelect();
		
		if(!Util.isPlayer() || !Util.hasPermission("edit", false))
		{
			Util.sendDenied(args[0])
			return null;
		}
			
		
		if(args.length != 2)
		{
			sendError(sender, "Invalid parameters");
			return false;
		}
		
		if(args[1].equalsIgnoreCase("hpos1"))
		{
			p1 = player.getTargetBlock(null, 100).getLocation();
			sendSuccess (sender, "First point selected");
		}
		else if(args[1].equalsIgnoreCase("pos1"))
		{
			p1 = player.getLocation();
			sendSuccess (sender, "First point selected");
		}
		else if(args[1].equalsIgnoreCase("hpos2"))
		{
			p2 = player.getTargetBlock(null, 100).getLocation();
			sendSuccess (sender, "Second point selected");
		}
		else if(args[1].equalsIgnoreCase("pos2"))
		{
			p2 = player.getLocation();
			sendSuccess (sender, "Second point selected");
		}
		else
		{
			sendError(sender, "Invalid subcommand. Use /mine help for help");
			return false;
		}
		return true;
	}
	}
}
