package com.wolvencraft.MineReset.cmd;

import java.util.List;

import com.wolvencraft.MineReset.CommandManager;

public class Edit
{
	private static String curMine;
	
	public static void run(String[] args)
	{
		if(!Util.senderHasPermission("edit", true))
		{
			Util.sendDenied(args[0] + " " + args[1]);
			return;
		}
			
		// 3 mine edit default
		// 3 mine cooldown toggle
		// 3 mine cooldown <time>
		// 4 mine add <name> <percentage>
		// 4 mine remove <name> <percentage>
		// 4 mine delete <name>
		
		if(args.length == 1)
		{
			Help.getEdit();
			return;
		}
		if(args.length > 3)
		{
			Util.sendInvalid(args[0] + " " + args[1] + " " + args[2] + " " + args[3]);
		}
		
		curMine = CommandManager.getMine();
		
		if(args[0].equalsIgnoreCase("edit"))
		{
			String mineName = args[1];
			if(!Util.mineExists(mineName))
			{
				Util.sendError("Mine '" + mineName + "' does not exist");
				return;
			}
			CommandManager.setMine(mineName);
			Util.sendSuccess("Mine '" + mineName + "' has been selected");
		}
		else if(args[0].equalsIgnoreCase("cooldown"))
		{
			if(curMine == null)
			{
				Util.sendError("Select a mine first with /mine edit <name>");
				return;
			}
			
			if(args.length != 3)
			{
				Util.sendInvalid(args[0] + " " + args[1]);
				return;
			}
			
			if(curMine == null)
			{
				Util.sendError("Select a mine first with /mine edit <name>");
				return;
			}
			
			if(Util.isNumeric(args[2]))
			{
				Util.setRegionInt("mines." + curMine + ".reset.manual", Integer.parseInt(args[2]));
				Util.sendSuccess("The cooldown of mine '" + curMine + "' has been set to " + args[2]);
				return;
			}
			else if(args[2].equalsIgnoreCase("toggle"))
			{
				return;
			}
			else
			{
				Util.sendInvalid(args[0] + " " + args[1] + " " + args[2]);
				return;
			}
		}
		else if(args[0].equalsIgnoreCase("add"))
		{
			if(curMine == null)
			{
				Util.sendError("Select a mine first with /mine edit <name>");
				return;
			}
			
			String blockName = args[2];
			int blockID = Util.getBlockId(args[2]);
			
			if(blockID == -1)
			{
				Util.sendError("Block '"+ args[2] + "' does not exist");
				return;
			}
			
			double percent;
			if(Util.isNumeric(args[3])) percent = Double.parseDouble(args[3]);
			else {
				if(Util.debugEnabled()) Util.log("Argument not numeric, attempting to parse");
				String awkwardValue = args[3];
				String[] awkArray = awkwardValue.split("%");
				percent = Double.parseDouble(awkArray[0]);
			}
			if(Util.debugEnabled()) Util.log("Percent value is " + percent);
			
			List<String> itemList = Util.getRegionList("mines." + curMine + ".materials.blocks");
			List<String> weightList = Util.getRegionList("mines." + curMine + ".materials.weights");
			
			double percentAvailable = Double.parseDouble(weightList.get(0));
			double newStonePercent;
			if((percentAvailable - percent) < 0)
			{
				Util.sendError("Invalid percentage. Use /mine info " + curMine + " to review the percentages");
				return;
			}
			else newStonePercent = percentAvailable - percent;
			
			// Writing everything down
			itemList.add(""+blockID);
			weightList.add(""+percent);
			weightList.set(0, ""+newStonePercent);
			Util.setRegionList(curMine + ".blocks", itemList);
			Util.setRegionList(curMine + ".weights", weightList);
			
			Util.saveRegionData();
			
			Util.sendSuccess(percent + "% of " + blockName + " added to " + curMine);
			Util.sendSuccess("Reset the mine for the changes to take effect");
			return;
		}
		else if(args[0].equalsIgnoreCase("remove"))
		{
			if(curMine == null)
			{
				Util.sendError("Select a mine first with /mine edit <name>");
				return;
			}
			
			int blockID = Util.getBlockId(args[2]);
			
			if(blockID == -1)
			{
				Util.sendError("Block '"+ args[2] + "' does not exist");
				return;
			}
			
			if(blockID == Util.getConfigInt("defaults.materials.default-block"))
			{
				Util.sendError("You cannot remove the default block from the mine");
				return;
			}
			
			List<String> itemList = Util.getRegionList("mines." + curMine + ".materials.blocks");
			List<String> weightList = Util.getRegionList("mines." + curMine + ".materials.weights");
			
			
			int index = itemList.indexOf("" + blockID);
			if(Util.debugEnabled()) Util.log(blockID + " ? " + index);
			if(index == -1)
			{
				Util.sendError("There is no '" + args[2] + "' in mine '" + curMine + "'");
				return;
			}
			double oldStoneWeight = Double.parseDouble(weightList.get(0));
			double newStoneWeight = oldStoneWeight + Double.parseDouble("" + weightList.get(index));
			weightList.set(0, "" + newStoneWeight);
			itemList.remove(index);
			weightList.remove(index);
			

			Util.setRegionList("mines." + curMine + ".materials.blocks", itemList);
			Util.setRegionList("mines." + curMine + ".materials.weights", weightList);
			
			Util.saveRegionData();
			Util.sendSuccess(args[2] + " was successfully removed from mine '" + args[1] + "'");
			return;
		}
		else if(args[0].equalsIgnoreCase("delete"))
		{
			CommandManager.getPlugin().getRegionData().set(args[1], null);
			Util.saveRegionData();
			List<String> regionList = Util.getRegionList("data.list-of-mines");
			regionList.remove(regionList.indexOf(args[1]));
			Util.setRegionList("data.list-of-mines", regionList);
			Util.saveRegionData();
			Util.sendSuccess("Mine '" + args[1] + "' removed successfully");
			return;
		}
		else
		{
			Util.sendInvalid("args[0]");
			return;
		}
	}
}
