package com.wolvencraft.MineReset.cmd;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.wolvencraft.MineReset.CommandManager;
import com.wolvencraft.MineReset.MineReset;
import com.wolvencraft.MineReset.Utilities.Pattern;

public class Reset
{
	
	public static void run(String mineName)
	{
		MineReset plugin = CommandManager.getPlugin();
		int blacklist = -1;

		List<String> blacklistBlocks = plugin.getRegions().getStringList(mineName + ".blacklist.blocks");
		if(Util.debugEnabled()) Util.log("Determining world");
		World curWorld = Bukkit.getServer().getWorld(plugin.getRegions().getString(mineName + ".coords.world"));
		
		if(Util.debugEnabled()) Util.log("Checking for blacklist");
		if(plugin.getRegions().getBoolean(mineName + ".blacklist.enabled") && blacklistBlocks.size() != 0)
		{
			blacklist = true;
			if(Util.debugEnabled()) Util.log("Blacklist found");
			if(plugin.getRegions().getBoolean(mineName + ".blacklist.whitelist"))
			{
				whitelist = true;
				if(Util.debugEnabled()) Util.log("Using the blacklist as a whitelist");
			}
		}
		else
			Util.log("The blacklist is empty or disabled");
		
		if(Util.debugEnabled()) Util.log("Initializing the reset");
		
		if(Util.debugEnabled()) Util.log("Creating a random pattern");
		
		List<String> itemList = plugin.getRegions().getStringList(mineName + ".blocks");
		List<String> weightList = plugin.getRegions().getStringList(mineName + ".weights");

		int[] items = new int[itemList.size()];
		double[] weights = new double[weightList.size()];
		for(int i = 0; i < itemList.size(); i++)
		{
			items[i] = Integer.parseInt(itemList.get(i));
			weights[i] = Double.parseDouble(weightList.get(i));
		}
		
		Pattern pattern = new Pattern(items, weights);
		
		if(Util.debugEnabled()) Util.log("Determining coordinates");
		
		int[] point1 = {
				plugin.getRegions().getInt(mineName + ".coords.p1.x"),
				plugin.getRegions().getInt(mineName + ".coords.p1.y"),
				plugin.getRegions().getInt(mineName + ".coords.p1.z")
		};
		int[] point2 = {
				plugin.getRegions().getInt(mineName + ".coords.p2.x"),
				plugin.getRegions().getInt(mineName + ".coords.p2.y"),
				plugin.getRegions().getInt(mineName + ".coords.p2.z")
		};
		int blockID = 0;
		if(Util.debugEnabled()) log.info("x " + point1[0] + ", " + point2[0]);
		if(Util.debugEnabled()) log.info("y " + point1[1] + ", " + point2[1]);
		if(debug) log.info("z " + point1[2] + ", " + point2[2]);
		
		if(blacklist)
		{
			if(whitelist)
			{
				for(int y = point1[1]; y <= point2[1]; y++)
				{
					for(int x = point1[0]; x <= point2[0]; x++)
					{
						for(int z = point1[2]; z <= point2[2]; z++ )
						{
							Block b = curWorld.getBlockAt(x, y, z);
							if(blacklistBlocks.indexOf(b.getTypeId() + "") == -1)
							{
								blockID = pattern.next();
								b.setTypeId(blockID);
							}
						}
					}
					if(debug) log.info("Completed plane" + y);
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
							Block b = curWorld.getBlockAt(x, y, z);
							if(blacklistBlocks.indexOf(b.getTypeId() + "") != -1)
							{
								blockID = pattern.next();
								if(debug) log.info(b.getTypeId() + " -> " + blockID);
								b.setTypeId(blockID);
							}
							else
								if(debug) log.info(b.getTypeId() + " -/> O-");
						}
					}
					if(debug) log.info("Completed plane" + y);
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
						Block b = curWorld.getBlockAt(x, y, z);
						blockID = pattern.next();
						b.setTypeId(blockID);			
					}
				}
				if(debug) log.info("Completed plane" + y);
			}
		}
			
		int min = plugin.getRegions().getInt(mineName + ".auto-reset.time.default-time");
		plugin.getRegions().set(mineName + ".auto-reset.time.cur-min", min);
		plugin.getRegions().set(mineName + ".auto-reset.time.cur-sec", 0);
	    	
	    plugin.saveRegions();
		
		
		if(broadcast) broadcastReset(mineName);
		return true;
	}
}
