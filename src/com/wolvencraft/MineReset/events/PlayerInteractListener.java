package com.wolvencraft.MineReset.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.wolvencraft.MineReset.cmd.Reset;
import com.wolvencraft.MineReset.cmd.Util;

public class PlayerInteractListener implements Listener
{
	public PlayerInteractListener()
	{
		// does nothing
	}
	
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(Util.debugEnabled()) Util.log("PlayerInteractEvent passed");
		Block blockClicked = event.getClickedBlock();
		String theAction = event.getAction().toString(); //Get to make sure it was right-clicked
	    if(theAction =="RIGHT_CLICK_BLOCK" || (blockClicked.getType() == Material.WALL_SIGN || blockClicked.getType() == Material.SIGN_POST))
	    {
	    	BlockState state = blockClicked.getState();
	    	if(state instanceof Sign)
	    	{
	    		if(Util.debugEnabled()) Util.log("Block is a sign");
	    		Sign sign = (Sign)state;
	    		
	    		Player player = event.getPlayer();
	     		if(!Util.playerHasPermission(player, "sign") && !Util.playerHasPermission(player, "sign.use")) return;
	     		
	
	        	if(Util.debugEnabled()) Util.log("Permissions check passed");
	     		String signTitle = Util.getConfigString("config.messages.sign-title");
	     		if(Util.debugEnabled()) Util.log(signTitle + " =?= " + sign.getLine(0));
	     		if(!sign.getLine(0).equalsIgnoreCase(signTitle)) return;
	     		
	        	if(Util.debugEnabled()) Util.log("The sign is formatted");
	        	
	        	
	     		String mineName[] = {sign.getLine(1)};
	     		Reset.run(mineName);
	
	        	if(Util.debugEnabled()) Util.log("Event finishing");
	     		return;
	     	}
	    }
	}
}
