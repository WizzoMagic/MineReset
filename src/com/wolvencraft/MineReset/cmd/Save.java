package com.wolvencraft.MineReset.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.wolvencraft.MineReset.CommandManager;

public class Save
{
	public static void run(String[] args)
	{
		if(Util.debugEnabled()) Util.log("Intitiating the creation of a new mine");
		if(args.length == 1)
			Help.getSave();
		if(args.length > 2)
			Util.sendInvalid(args[0] + " " + args[1] + " " + args[2]);
		
		String mineName = args[1];
		if(CommandManager.mineExists(mineName))
		{
			Util.sendError("Mine " + mineName + " already exists!");
			return;
		}
		
		// - Fetching the default values
		// - - Is the mine enabled by default?
		boolean enabled = Util.getConfigBoolean("defaults.enabled");
		
		// - - Blacklist defaults
		boolean blacklistEnabled = Util.getConfigBoolean("defaults.blacklist.enabled");
		boolean whitelistEnabled = Util.getConfigBoolean("defaults.blacklist.whitelist");
		List<String> blacklistedBlocks = Util.getConfigList("defaults.blacklist.blocks");
		
		// - - Protection defaults
		int protectionPadding = Util.getConfigInt("defaults.protection.padding");
		int protectionPaddingTop = Util.getConfigInt("defaults.protection.padding-top");
		
		// - - - PVP
		boolean protectionPVPEnabled = Util.getConfigBoolean("defaults.protection.pvp");
		
		// - - - Block breaking
		boolean protectionBreakingEnabled = Util.getConfigBoolean("defaults.protection.breaking.enabled");
		boolean protectionBreakingBlacklistEnabled = Util.getConfigBoolean("defaults.protection.breaking.blacklist.enabled");
		boolean protectionBreakingWhitelistEnabled = Util.getConfigBoolean("defaults.protection.breaking.blacklist.whitelist");
		List<String> protectionBreakingBlacklistedBlocks = Util.getConfigList("defaults.protection.breaking.blacklist.blocks");
		
		// - - - Block placement
		boolean protectionPlacingEnabled = Util.getConfigBoolean("defaults.protection.placing.enabled");
		boolean protectionPlacingBlacklistEnabled = Util.getConfigBoolean("defaults.protection.placing.blacklist.enabled");
		boolean protectionPlacingWhitelistEnabled = Util.getConfigBoolean("defaults.protection.placing.blacklist.whitelist");
		List<String> protectionPlacingBlacklistedBlocks = Util.getConfigList("defaults.protection.placing.blacklist.blocks");
				
		
		// = Setting values to the mine
		String baseNode = "mines." + mineName;
		// = = Basic info
		Util.setRegionBoolean(baseNode + ".enabled", enabled);
		
		// = = Blacklist
		baseNode = "mines." + mineName + ".blacklist";
		Util.setRegionBoolean(baseNode + ".enabled", blacklistEnabled);
		Util.setRegionBoolean(baseNode + ".whitelist", whitelistEnabled);
		Util.setRegionList(baseNode + ".blocks", blacklistedBlocks);
		
		// = = Protection
		baseNode = "mines." + mineName + ".protection";
		Util.setRegionInt(baseNode + ".padding", protectionPadding);
		Util.setRegionInt(baseNode + ".protection.padding-top", protectionPaddingTop);
		
		// = = = PVP
		Util.setRegionBoolean(baseNode + ".pvp", protectionPVPEnabled);
		
		// = = = Block breaking
		baseNode = "mines." + mineName + ".protection.breaking";
		Util.setRegionBoolean(baseNode + ".enabled", protectionBreakingEnabled);
		Util.setRegionBoolean(baseNode + ".blacklist.enabled", protectionBreakingBlacklistEnabled);
		Util.setRegionBoolean(baseNode + ".blacklist.whitelist", protectionBreakingWhitelistEnabled);
		Util.setRegionList(baseNode + ".blacklist.blocks", protectionBreakingBlacklistedBlocks);
		
		// = = = Block placement
		baseNode = "mines." + mineName + ".protection.placing";
		Util.setRegionBoolean(baseNode + ".enabled", protectionPlacingEnabled);
		Util.setRegionBoolean(baseNode + ".blacklist.enabled", protectionPlacingBlacklistEnabled);
		Util.setRegionBoolean(baseNode + ".blacklist.whitelist", protectionPlacingWhitelistEnabled);
		Util.setRegionList(baseNode + ".blacklist.blocks", protectionPlacingBlacklistedBlocks);
		
	}
}
