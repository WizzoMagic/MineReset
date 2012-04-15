package com.wolvencraft.MineReset.cmd;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.wolvencraft.MineReset.util.Pattern;

public class Reset
{
	public static void run(String[] args)
	{
		if(!Util.senderHasPermission("reset", true))
		{
			Util.sendDenied("reset");
			return;
		}
		String mineName;
		if(args[0].length() > 2)
		{
			Util.sendInvalid(args[0] + " " + args[1] + " " + args[2]);
			return;
		}
		if(args[0].length() == 1)
			mineName = Util.getConfigString("configuration.default-name");
		else mineName = args[1];
		
		if(!Util.mineExists(mineName))
		{
			Util.sendError("Mine '" + mineName + "' does not exist");
			return;
		}
		
		List<String> blockList = Util.getConfigList("mines." + mineName + ".materials.blocks");
		List<String> weightList = Util.getConfigList("mines." + mineName + ".materials.weights");
		Pattern pattern = new Pattern(blockList, weightList);
		boolean blacklist = Util.getRegionBoolean("mines." + mineName + ".blacklist.enabled");
		boolean whitelist = Util.getRegionBoolean("mines." + mineName + ".blacklist.whitelist");
		List<String> blacklistedBlocks = null;
		if(blacklist)
			blacklistedBlocks = Util.getRegionList("mines." + mineName + ".blacklist.blocks");
		String worldName = Util.getRegionString("mines." + mineName + ".coordinates.world");
		World mineWorld = Bukkit.getServer().getWorld(worldName);
		
		boolean broadcastReset = Util.getConfigBoolean("configuration.broadcast-on-reset");
		
		if(Util.debugEnabled()) Util.log("Determining coordinates");
		
		int[] point1 = {
				Util.getRegionInt("mines." + mineName + ".coordinates.pos0.x"),
				Util.getRegionInt("mines." + mineName + ".coordinates.pos0.y"),
				Util.getRegionInt("mines." + mineName + ".coordinates.pos0.z")
		};
		int[] point2 = {
				Util.getRegionInt("mines." + mineName + ".coordinates.pos1.x"),
				Util.getRegionInt("mines." + mineName + ".coordinates.pos1.y"),
				Util.getRegionInt("mines." + mineName + ".coordinates.pos1.z")
		};
		
		int blockID = 0;
		if(Util.debugEnabled()) Util.log("x " + point1[0] + ", " + point2[0]);
		if(Util.debugEnabled()) Util.log("y " + point1[1] + ", " + point2[1]);
		if(Util.debugEnabled()) Util.log("z " + point1[2] + ", " + point2[2]);
		
		// Comment for the future me:
		// It makes more sense to handle all the logic here, since
		// doing the same thing in the middle of resetting a bunch
		// of blocks is clearly not the best idea ever. Best wishes,
		// me from the past.
		
		if(whitelist)
		{
			for(int y = point1[1]; y <= point2[1]; y++)
			{
				for(int x = point1[0]; x <= point2[0]; x++)
				{
					for(int z = point1[2]; z <= point2[2]; z++ )
					{
						Block b = mineWorld.getBlockAt(x, y, z);
						blockID = pattern.next();
						for(int i = 0; i < blacklistedBlocks.size(); i++)
						{
							if(blockID == Integer.parseInt(blacklistedBlocks.get(i)))
								b.setTypeId(blockID);
							else
							{
								if(Util.debugEnabled()) Util.log(blockID + " is not whitelisted and thus not replaced");
							}	
						}
					}
				}
			}
		}
		else if(blacklist)
		{
			for(int y = point1[1]; y <= point2[1]; y++)
			{
				for(int x = point1[0]; x <= point2[0]; x++)
				{
					for(int z = point1[2]; z <= point2[2]; z++ )
					{
						Block b = mineWorld.getBlockAt(x, y, z);
						blockID = pattern.next();
						for(int i = 0; i < blacklistedBlocks.size(); i++)
						{
							if(blockID == Integer.parseInt(blacklistedBlocks.get(i)))
								if(Util.debugEnabled()) Util.log(blockID + " is blacklisted and thus not replaced");
							else
							{
								b.setTypeId(blockID);
							}	
						}
					}
				}
			}
		}
		else
		{
			for(int y = point1[1]; y <= point2[1]; y++)
			{
				for(int x = point1[0]; x <= point2[0]; x++)
				{
					for(int z = point1[2]; z <= point2[2]; z++ )
					{
						Block b = mineWorld.getBlockAt(x, y, z);
						blockID = pattern.next();
						b.setTypeId(blockID);
					}
				}
			}
		}
			
		if(broadcastReset)
		{
			String broadcastMessage = Util.getConfigString("messages.manual-mine-reset-successful");
			Util.broadcastSuccess(broadcastMessage);
		}
	}
}
