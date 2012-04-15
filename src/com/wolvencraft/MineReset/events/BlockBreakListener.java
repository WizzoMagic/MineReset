package com.wolvencraft.MineReset.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.wolvencraft.MineReset.cmd.Util;

public class BlockBreakListener implements Listener
{
	public BlockBreakListener()
	{
		// does nothing
	}
	
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(!Util.getConfigBoolean("use-protection")) return;
		Player player = event.getPlayer();
		if(player.isOp()) return;
		
		boolean usePermissions = Util.getConfigBoolean("configuration.use-permissions");
		int padding;
		int paddingTop;
		
		List<String> regionList = Util.getRegionList("data.list-of-mines");
		
		for(int i = 0; i < regionList.size(); i++)
		{
			if(usePermissions)
			{
				if(!Util.playerHasPermission(player, "break") && !Util.playerHasPermission(player, "break." + regionList.get(i)))
				{
					Location blockLocation = event.getBlock().getLocation();
					padding = Util.getRegionInt("mines." + regionList.get(i) + ".protection.padding");
					paddingTop = Util.getRegionInt("mines." + regionList.get(i) + ".protection.padding-top");
					int[] x = {Util.getConfigInt("regions." + regionList.get(i) + ".coords.p1.x"), Util.getConfigInt("regions." + regionList.get(i) + ".coords.p2.x")};
					int[] y = {Util.getConfigInt("regions." + regionList.get(i) + ".coords.p1.y"), Util.getConfigInt("regions." + regionList.get(i) + ".coords.p2.y")};
					int[] z = {Util.getConfigInt("regions." + regionList.get(i) + ".coords.p1.z"), Util.getConfigInt("regions." + regionList.get(i) + ".coords.p2.z")};
			
					if(blockLocation.getX() < (x[0] - padding) || blockLocation.getX() > (x[1] + padding)) return;
					if(blockLocation.getY() < (y[0] - padding) || blockLocation.getY() > (y[1] + paddingTop)) return;
					if(blockLocation.getZ() < (z[0] - padding) || blockLocation.getZ() > (z[1] + padding)) return;
					
					Util.sendPlayerError(player, "You are not allowed to break blocks in this mine");
					event.setCancelled(true);
				}
			}
		}
		
		return;
	}
}
