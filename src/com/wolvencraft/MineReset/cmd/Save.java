package com.wolvencraft.MineReset.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.wolvencraft.MineReset.CommandManager;

public class Save
{
	public static void run(String[] args)
	{
		if(Util.debugEnabled()) Util.log("Intitiating the creation of a new mine");
		if(args.length == 1)
		{
			Help.getSave();
			return;
		}
		if(args.length > 2)
		{
			Util.sendInvalid(args[0] + " " + args[1] + " " + args[2]);
			return;
		}
		
		if(Util.debugEnabled())Util.log("Argument check passed");
		
		Location[] loc = CommandManager.getLocation();
		if(loc == null)
		{
			Util.sendError("Make a selection first");
			return;
		}
		if(loc[0].getWorld() == loc[1].getWorld())
		{
			Util.sendError("Your selection points are in different worlds");
			return;
		}
		
		if(Util.debugEnabled()) Util.log("Selections checks passed");
		
		String mineName = args[1];
		if(Util.mineExists(mineName))
		{
			Util.sendError("Mine " + mineName + " already exists!");
			return;
		}
		if(Util.debugEnabled()) Util.log("Mine existance check passed");
		
		if(Util.debugEnabled()) Util.log("Reading default values");
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
		
		// - - Materials
		String defaultBlock = Util.getConfigString("defaults.materials.default-block");
		
		// - - Reset
		
		// - - - Auto
		boolean resetAutoEnabled = Util.getConfigBoolean("defaults.reset.auto.reset");
		int resetAutoTime = Util.getConfigInt("defaults.reset.auto.reset-time");
		boolean resetAutoWarnEnabled = Util.getConfigBoolean("defaults.reset.auto.warn");
		int resetAutoWarnTime = Util.getConfigInt("defaults.reset.auto.warn-time");
		
		// - - - Manual
		boolean resetManualCooldownEnabled = Util.getConfigBoolean("defaults.reset.manual.cooldown-enabled");
		int resetManualCooldownTime = Util.getConfigInt("defaults.reset.manual.cooldown-time");
		
		
		
		
		if(Util.debugEnabled()) Util.log("Finished reading defaults");
		
		// = Setting values to the mine
		String baseNode = "mines." + mineName;
		// = = Basic info
		Util.setRegionBoolean(baseNode + ".enabled", enabled);
		
		if(Util.debugEnabled()) Util.log("Writing blacklist data");
		
		// = = Blacklist
		baseNode = "mines." + mineName + ".blacklist";
		Util.setRegionBoolean(baseNode + ".enabled", blacklistEnabled);
		Util.setRegionBoolean(baseNode + ".whitelist", whitelistEnabled);
		Util.setRegionList(baseNode + ".blocks", blacklistedBlocks);
		
		if(Util.debugEnabled()) Util.log("Writing protection data");
		
		// = = Protection
		baseNode = "mines." + mineName + ".protection";
		Util.setRegionInt(baseNode + ".padding", protectionPadding);
		Util.setRegionInt(baseNode + ".padding-top", protectionPaddingTop);
		
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
		
		if(Util.debugEnabled()) Util.log("Writing coordinates");
		
		// = = Coordinates
		baseNode = "mines." + mineName + ".coordinates";
		Util.setRegionString(baseNode + ".world", loc[0].getWorld().getName());
		
		// = = = Position 0
		Util.setRegionInt(baseNode + ".pos0.x", (int)loc[0].getX());
		Util.setRegionInt(baseNode + ".pos0.y", (int)loc[0].getY());
		Util.setRegionInt(baseNode + ".pos0.z", (int)loc[0].getZ());
		
		// = = = Position 1
		Util.setRegionInt(baseNode + ".pos1.x", (int)loc[1].getX());
		Util.setRegionInt(baseNode + ".pos1.y", (int)loc[1].getY());
		Util.setRegionInt(baseNode + ".pos1.z", (int)loc[1].getZ());
		
		if(Util.debugEnabled()) Util.log("Writing reset data");
		
		// = = Materials
		baseNode = "mines." + mineName + ".materials";
		List<String> blockList = new ArrayList<String>();
		blockList.add(defaultBlock);
		List<String> weightList = new ArrayList<String>();
		weightList.add("100");
		// = = = Blocks
		Util.setRegionList(baseNode + ".blocks", blockList);
		
		// = = = Weights
		Util.setRegionList(baseNode + ".weights", blockList);
		
		// = = Reset
		// = = = Automatic
		baseNode = "mines." + mineName + ".reset.auto";
		Util.setRegionBoolean(baseNode + ".reset", resetAutoEnabled);
		Util.setRegionInt(baseNode + ".reset-time", resetAutoTime);
		Util.setRegionBoolean(baseNode + ".warn", resetAutoWarnEnabled);
		Util.setRegionInt(baseNode + ".warn-time", resetAutoWarnTime);
		
		// = = = Manual
		baseNode = "mines." + mineName + ".reset.manual";
		Util.setRegionBoolean(baseNode + ".cooldown-enabled", resetManualCooldownEnabled);
		Util.setRegionInt(baseNode + ".cooldown-time", resetManualCooldownTime);
		
		if(Util.debugEnabled()) Util.log("Mine creation completed");
		
		Util.saveRegionData();
		
		if(Util.debugEnabled()) Util.log("Data saved successfully");
		
	}
}
