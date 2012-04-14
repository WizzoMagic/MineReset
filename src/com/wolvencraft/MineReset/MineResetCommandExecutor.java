package com.wolvencraft.MineReset;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wolvencraft.MineReset.Utilities.Pattern;

public class MineResetCommandExecutor implements CommandExecutor
{
	private MineReset plugin;
	World curWorld;
	Logger log;
	Location p1, p2;

	private boolean debug;
	private boolean broadcast;
	
	String defaultBlock;
	 
	public MineResetCommandExecutor(MineReset plugin)
	{
		this.plugin = plugin;
		curWorld = plugin.curWorld;
		log = plugin.log;
		
		debug = plugin.debug;
		broadcast = plugin.broadcast;
	}
 
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!cmd.getName().equalsIgnoreCase("mine")) return false;
		
		Player player = null;
		if(sender instanceof Player) player = (Player) sender;

		if(args.length < 1)
		{
			sendHelp(sender, player);
			return false;
		}
		
		/**
		 * Reset the mine safely
		 * @alias reset
		 * @alias r
		 */
		if(args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("r"))
		{
			String mineName = "";
			
			if(args.length == 2)
			{
				mineName = args[1];
				if(!checkIfMineExists(mineName))
				{
					sendError(sender, "Mine '" + mineName + "' does not exist");
					return false;
				}
			}
			else if(args.length == 1)
			{
				if(debug) log.warning("Invalid mine name; attempting to use 'default'");
				if(!checkIfMineExists("default"))
				{
					sendError(sender, "Mine 'default' does not exist");
					return false;
				}
				if(debug) log.info("Using mine 'default' in place of a missing argument");
				mineName = "default";
			}
			else
			{
				sendError(sender, "Invalid parameters");
			}

			if(isPlayer(player, sender))
				if(!hasPermission(player, "reset") && !hasPermission(player, "reset." + mineName)) return false;
			
			return resetMine(mineName);
		}
		

		/**
		 * Select the region with WorldEdit-like commands
		 * @alias select
		 * @alias sel
		 * @alias set
		 */
		else if(args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("sel") || args[0].equalsIgnoreCase("set"))
		{
			if(isPlayer(player, sender))
			{
				if(!hasPermission(player,"edit")) return false;
			}
			else
			{
				log.warning("This command can only be run by a player");
				return false;
			}
			
			if(args.length != 2)
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
			
			if(args[1].equalsIgnoreCase("hpos1"))
			{
				p1 = player.getTargetBlock(null, 25).getLocation();
				sendSuccess (sender, "First point selected");
			}
			else if(args[1].equalsIgnoreCase("pos1"))
			{
				p1 = player.getLocation();
				sendSuccess (sender, "First point selected");
			}
			else if(args[1].equalsIgnoreCase("hpos2"))
			{
				p2 = player.getTargetBlock(null, 25).getLocation();
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
		

		/**
		 * Save the data into a config file
		 * @alias save
		 * @alias create
		 */
		else if (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("create"))
		{
			if(isPlayer(player, sender))
			{
				if(!hasPermission(player,"edit")) return false;
			}
			else
			{
				log.warning("This command can only be run by a player");
				return false;
			}
			
			if(p1 == null || p2 == null)
			{
				sendError(sender, "Make a selection first");
				return false;
			}
			if(p1.getWorld() != p2.getWorld())
			{
				sendError(sender, "The selection points are in different worlds.");
				return false;
			}
			
			String mineName = "";
			if(args.length < 2)
			{
				if(debug) sendError(sender, "Missing a mine name; Attempting to use 'default'");
				if(checkIfMineExists("default"))
				{
					sendError(sender, "Mine 'default' already exists. Please, pick another name.");
					return false;
				}
				mineName = "default";
			}
			else if(plugin.getRegions().getString(args[1]) != null)
			{
				sendError(sender, "Mine '" + args[1] + "' already exists");
				return false;
			}
			else mineName = args[1];
			
			List<String> bannedNames = plugin.getConfig().getStringList("config.banned-mine-names");
			if(bannedNames.indexOf(mineName) != -1)
			{
				sendError(sender, "You cannot create a mine with this name");
				return false;
			}
			
			return createMine(sender, mineName);
		}
		// Adds a new percentage parameter to the mine config
		// Alias: add
		else if(args[0].equalsIgnoreCase("add"))
		{
			if(isPlayer(player, sender))
				if(!hasPermission(player,"edit")) return false;
			
			if(args.length != 4)
			{
				sendError(sender, "Invalid parameters");
				return false;
			}

			String mineName = args[1];
			if(!checkIfMineExists(mineName))
			{
				sendError(sender, "Mine '" + mineName + "' does not exist");
				return false;
			}
			
			String blockName = args[2];
			int blockID = getBlockID(args[2]);
			
			if(blockID == -1)
			{
				sendError(sender, "Block '"+ args[2] + "' does not exist");
				return false;
			}
			
			double percent;
			if(isNumeric(args[3])) percent = Double.parseDouble(args[3]);
			else {
				if(debug) log.warning("Argument not numeric, attempting to parse");
				String awkwardValue = args[3];
				String[] awkArray = awkwardValue.split("%");
				percent = Double.parseDouble(awkArray[0]);
			}
			if(debug) log.info("Percent value is " + percent);

			List<String> itemList = plugin.getRegions().getStringList(mineName + ".blocks");
			List<String> weightList = plugin.getRegions().getStringList(mineName + ".weights");
			
			double percentAvailable = Double.parseDouble(weightList.get(0));
			double newStonePercent;
			if((percentAvailable - percent) < 0)
			{
				sendError(sender, "Invalid percentage. Use /mine " + mineName + " to review the percentages");
				return false;
			}
			else newStonePercent = percentAvailable - percent;
			
			// Writing everything down
			itemList.add(""+blockID);
			weightList.add(""+percent);
			weightList.set(0, ""+newStonePercent);
			plugin.getRegions().set(mineName + ".blocks", itemList);
			plugin.getRegions().set(mineName + ".weights", weightList);
			plugin.saveRegions();
			sendSuccess(sender, percent + "% of " + blockName + " added to " + mineName);
			sendSuccess(sender, "Reset the mine for the changes to take effect");
			return true;
		}
		// Removes either the mine itself or a percentage parameter of it
		// Alias: remove, delete, del
		else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del"))
		{
			if(isPlayer(player, sender))
				if(!hasPermission(player, "edit")) return false;
			
			if(args.length == 2)
			{
				if(!checkIfMineExists(args[1]))
				{
					sendError(sender, "Mine '" + args[1] + "' does not exist");
					return false;
				}
				plugin.getRegions().set(args[1], null);
				plugin.saveRegions();
				plugin.mineNum--;
				List<String> regionList = plugin.getConfig().getStringList("data.region-list");
				regionList.remove(regionList.indexOf(args[1]));
				plugin.getConfig().set("data.region-list", regionList);
				plugin.getConfig().set("data.regions", plugin.mineNum);
				plugin.saveConfig();
				sendSuccess(sender, "Mine '" + args[1] + "' removed successfully");
				return true;
			}
			else if(args.length == 3)
			{
				String mineName = "";
				if(!checkIfMineExists(args[1]))
				{
					sendError(sender, "Mine " + args[1] + " does not exist");
					return false;
				}
				else mineName = args[1];
				
				int blockID = getBlockID(args[2]);
				
				if(blockID == -1)
				{
					sendError(sender, "Block '"+ args[2] + "' does not exist");
					return false;
				}
				
				if(blockID == plugin.getConfig().getInt("config.general.default-block-id"))
				{
					sendError(sender, "You cannot remove the default block from the mine");
					return false;
				}
				
				List<String> itemList = plugin.getRegions().getStringList(mineName + ".blocks");
				List<String> weightList = plugin.getRegions().getStringList(mineName + ".weights");
				
				
				int index = itemList.indexOf("" + blockID);
				if(debug) log.info(blockID + " ? " + index);
				if(index == -1)
				{
					sendError(sender, "There is no '" + args[2] + "' in mine '" + mineName + "'");
					return false;
				}
				double oldStoneWeight = Double.parseDouble(weightList.get(0));
				double newStoneWeight = oldStoneWeight + Double.parseDouble("" + weightList.get(index));
				weightList.set(0, "" + newStoneWeight);
				itemList.remove(index);
				weightList.remove(index);
				

				plugin.getRegions().set(mineName + ".blocks", itemList);
				plugin.getRegions().set(mineName + ".weights", weightList);
				
				plugin.saveRegions();
				sendSuccess(sender, args[2] + " was successfully removed from mine '" + args[1] + "'");
				return true;
			}
			else
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
		}
		else if(args[0].equalsIgnoreCase("blacklist"))
		{
			if(args.length == 1)
			{
				sender.sendMessage(ChatColor.RED + "          -=[ Blacklist Help ]=-");
				sender.sendMessage(ChatColor.GREEN + "/mine blacklist <name> toggle " + ChatColor.WHITE + "Turn the blacklist ON or OFF");
				sender.sendMessage(ChatColor.GREEN + "/mine blacklist <name> whitelist " + ChatColor.WHITE + "Treat the blacklist as a whitelist");
				sender.sendMessage(ChatColor.GREEN + "/mine blacklist <name> add <block>" + ChatColor.WHITE + "Add a block to the whitelist");
				sender.sendMessage(ChatColor.GREEN + "/mine blacklist <name> remove <block>" + ChatColor.WHITE + "Remove a block from the whitelist");
				return true;
			}
			else if(args.length > 4 || args.length == 2)
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
			
			String mineName = "";
			if(!checkIfMineExists(args[1]))
			{
				sendError(sender, "Mine " + args[1] + " does not exist");
				return false;
			}
			else mineName = args[1];
			
			if(args[2].equalsIgnoreCase("toggle"))
			{
				if(plugin.getRegions().getBoolean(mineName + ".blacklist.enabled"))
				{
					plugin.getRegions().set(mineName + ".blacklist.enabled", false);
					sendSuccess(sender, "Blacklist turned OFF for mine " + mineName);
				}
				else
				{
					plugin.getRegions().set(mineName + ".blacklist.enabled", true);
					sendSuccess(sender, "Blacklist turned ON for mine " + mineName);
				}
				return true;
			}
			else if(args[2].equalsIgnoreCase("whitelist"))
			{
				if(plugin.getRegions().getBoolean(mineName + ".blacklist.whitelist"))
				{
					plugin.getRegions().set(mineName + ".blacklist.whitelist", false);
					sendSuccess(sender, "Blacklist is no longer treated as a whitelist for mine " + mineName);
				}
				else
				{
					plugin.getRegions().set(mineName + ".blacklist.whitelist", true);
					sendSuccess(sender, "Blacklist is now treated as a whitelist for mine " + mineName);
				}
				return true;
			}
			else if(args[2].equalsIgnoreCase("add"))
			{
				int blockID = getBlockID(args[3]);
				if(blockID == -1)
				{
					sendError(sender, "Block '"+ args[3] + "' does not exist");
					return false;
				}
				
				List<String> blacklist = plugin.getRegions().getStringList(mineName + ".blacklist.blocks");
				blacklist.add("" + blockID);
				plugin.getRegions().set(mineName + ".blacklist.blocks", blacklist);

				sendSuccess(sender, "Block " + args[2] + " is now in the blacklist");
				return true;
			}
			else if(args[2].equalsIgnoreCase("remove"))
			{
				int blockID = getBlockID(args[3]);
				if(blockID == -1)
				{
					sendError(sender, "Block '"+ args[3] + "' does not exist");
					return false;
				}
				
				List<String> blacklist = plugin.getRegions().getStringList(mineName + ".blacklist.blocks");
				
				int index = blacklist.indexOf(blockID);
				if(index == -1)
				{
					sendError(sender, "Block " + args[2] + " is not in the blacklist");
					return false;
				}
				blacklist.remove(index);
				plugin.getRegions().set(mineName + ".blacklist.blocks", blacklist);
				sendSuccess(sender, "Block " + args[2] + " has been removed from the blacklist");
				return true;
			}
			else
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
			
			
		}
		// Reloads the configuration
		// Alias: config
		else if(args[0].equalsIgnoreCase("config"))
		{
			if(isPlayer(player, sender))
				if(!hasPermission(player, "config")) return false;
			if(args.length < 2)
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
			if(args[1].equalsIgnoreCase("load"))
			{
				plugin.reloadConfig();
				plugin.reloadRegions();
				sendSuccess(sender, "Configuration reloaded from file");
			}
			else if(args[1].equalsIgnoreCase("save"))
			{
				plugin.saveConfig();
				plugin.saveRegions();
				sendSuccess(sender, "Configuration saved to file");
			}
			else
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
			return true;
		}
		// Timer controls
		// Alias: timer
		else if(args[0].equalsIgnoreCase("timer"))
		{
			if(isPlayer(player, sender))
				if(!hasPermission(player, "timer")) return false;
			
			// mine timer set <mineName> <amount>
			// mine timer toggle <mineName> enabled
			// mine timer toggle <mineName> warning
			
			if(args.length < 3 || args.length > 4)
			{
				sendError(sender, "Invalid parameters");
				return false;
			}	
			
			if(!checkIfMineExists(args[2]))
			{
				sendError(sender, "Mine " + args[2] + " does not exist.");
				return false;
			}
			String mineName = args[2];
			
			if(args[1].equalsIgnoreCase("set"))
			{
				int time;
				try
				{
					time = Integer.parseInt(args[3]);
					if(time < 10)
					{
						sendError(sender, "Invalid time; time cannot be set to less then 10 minutes");
						return false;
					}	
				}
				catch (NumberFormatException nfe)
				{
					sendError(sender, "Invalid parameters; time provided is not numeric");
					return false;
				}
				
				plugin.getRegions().set(mineName + ".auto-reset.time.default-time", time);
				sendSuccess(sender, "Reset time changed to " + time + " for mine '" + mineName + "'");
				return true;
			}
			else if(args[1].equalsIgnoreCase("toggle"))
			{
				if(plugin.getRegions().getBoolean(mineName + ".auto-reset.enabled"))
				{
					plugin.getRegions().set(mineName + ".auto-reset.enabled", false);
					sendSuccess(sender, "Automatic mine resetting turned OFF for mine " + mineName);
				}
				else
				{
					plugin.getRegions().set(mineName + ".auto-reset.enabled", true);
					sendSuccess(sender, "Automatic mine resetting turned ON for mine " + mineName);
				}
				return true;
			}
			else
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
		}
		// Returns the help message
		// Alias: help, ?
		else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
		{
			if(isPlayer(player, sender))
				if(!hasPermission(player, "help")) return false;
			sendHelp(sender, player);
			return true;
		}
		// Returns the information about a mine
		// Alias: none
		else
		{
			if(args.length != 1)
			{
				sendError(sender, "Invalid parameters");
				return false;
			}
			if(isPlayer(player, sender))
				if(!hasPermission(player, "info")) return false;
			
			if(!checkIfMineExists(args[0]))
			{
				sendError(sender, "Mine " + args[0] + " does not exist. Use " + ChatColor.GREEN + "/mine help" + ChatColor.WHITE + " for help");
				return false;
			}
			String mineName = args[0];
			
			List<String> itemList = plugin.getRegions().getStringList(mineName + ".blocks");
			List<String> weightList = plugin.getRegions().getStringList(mineName + ".weights");
			
			sender.sendMessage(ChatColor.DARK_RED + "                    -=[ " + mineName + " ]=-");
			
			boolean autoReset = plugin.getRegions().getBoolean(mineName + ".auto-reset.enabled") && plugin.getConfig().getBoolean("config.mine-reset.automatic-reset");
    		int nextResetMin = plugin.getRegions().getInt(mineName + ".auto-reset.time.cur-min");
    		int nextResetSec = plugin.getRegions().getInt(mineName + ".auto-reset.time.cur-sec");
			String blockName;
			String blockWeight;
			int blockID;
			if(autoReset)
			{
				sender.sendMessage(" Resets every " + ChatColor.GOLD + plugin.getRegions().getInt(mineName + ".auto-reset.time.default-time") + ChatColor.WHITE + " minutes. Next reset in " + ChatColor.GOLD + nextResetMin + ChatColor.WHITE + " minutes " + ChatColor.GOLD + nextResetSec + ChatColor.WHITE + " seconds");
			}
			else
			{
				sender.sendMessage("The mine has to be reset manually");
			}
			sender.sendMessage(ChatColor.BLUE + " Composition:");
			for(int i = 0; i < itemList.size(); i ++)
			{
				blockID = Integer.parseInt(itemList.get(i));
				blockName = Material.getMaterial(blockID).toString();
				blockWeight = weightList.get(i);
				sender.sendMessage(" - " + blockWeight + "% of " + ChatColor.GREEN + blockName);
			}
			List<String> blacklistBlocks = plugin.getRegions().getStringList(mineName + ".blacklist.blocks");
			if(plugin.getRegions().getBoolean(mineName + ".blacklist.enabled") && blacklistBlocks.size() != 0)
			{
				if(plugin.getRegions().getBoolean(mineName + ".blacklist.whitelist"))
				{
					sender.sendMessage(ChatColor.BLUE + " Blacklist:");
				}
				else sender.sendMessage(ChatColor.BLUE + " Whitelist:");
				
				for(int i = 0; i < blacklistBlocks.size(); i++)
				{
					if(debug) log.info("Blacklist: " + blacklistBlocks.get(i));
					sender.sendMessage(" - " + Material.getMaterial(blacklistBlocks.get(i)).toString());
				}
			}
			return true;
		}
	}
	
	/**
	 * Resets the mine safely
	 * @param sender CommandSender Player who sent the command
	 * @param mineName String Name of the mine being reset
	 * @return boolean Returns true if the reset is successful
	 */
	boolean resetMine(String mineName)
	{	
		boolean blacklist = false;
		boolean whitelist = false;

		List<String> blacklistBlocks = plugin.getRegions().getStringList(mineName + ".blacklist.blocks");
		if(debug) log.info("Determining world");
		curWorld = Bukkit.getServer().getWorld(plugin.getRegions().getString(mineName + ".coords.world"));
		
		if(debug) log.info("Checking for blacklist");
		if(plugin.getRegions().getBoolean(mineName + ".blacklist.enabled") && blacklistBlocks.size() != 0)
		{
			blacklist = true;
			if(debug) log.info("Blacklist found");
			if(plugin.getRegions().getBoolean(mineName + ".blacklist.whitelist"))
			{
				whitelist = true;
				if(debug) log.info("Using the blacklist as a whitelist");
			}
		}
		else
			log.info("The blacklist is empty or disabled");
		
		if(debug) log.info("Initializing the reset");
		
		if(debug) log.info("Creating a random pattern");
		
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
		
		if(debug) log.info("Determining coordinates");
		
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
		if(debug) log.info("x " + point1[0] + ", " + point2[0]);
		if(debug) log.info("y " + point1[1] + ", " + point2[1]);
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
	
	/**
	 * Saves the mine data into a config file 
	 * @param sender CommandSender Player who sent the command
	 * @param mineName String the name of a mine
	 * @return boolean True if save is successful
	 */
	public boolean createMine(CommandSender sender, String mineName)
	{
		if(debug) log.info("Mine Creation: Creating a new mine region \"" + mineName + "\"");
		plugin.getRegions().set(mineName, "[]");
		
		@SuppressWarnings("unchecked")
		List<String> regionList = (List<String>) plugin.getConfig().getList("data.region-list");
		regionList.add(mineName);
		plugin.getConfig().set("data.region-list", regionList);
		if(debug) log.info("Mine Creation: Giving a mine a unique ID & marking it as enabled");
		plugin.getRegions().set(mineName + ".ID", plugin.mineNum);
		plugin.mineNum++;
		plugin.getConfig().set("data.regions", plugin.mineNum);
		plugin.getRegions().set(mineName + ".enabled", true);
		if(debug) log.info("Mine Creation: Handling automatic resets");
		plugin.getRegions().set(mineName + ".auto-reset", "[]");
		plugin.getRegions().set(mineName + ".auto-reset.enabled", plugin.getConfig().getBoolean("config.mine-reset.automatic-reset"));
		plugin.getRegions().set(mineName + ".auto-reset.time", "[]");
		plugin.getRegions().set(mineName + ".auto-reset.time.default-time", plugin.getConfig().getInt("config.mine-reset.default-reset-time"));
		plugin.getRegions().set(mineName + ".auto-reset.time.cur-min", plugin.getConfig().getInt("config.mine-reset.default-reset-time"));
		plugin.getRegions().set(mineName + ".auto-reset.time.cur-sec", 0);
		if(debug) log.info("Mine Creating: Handling the blacklist");
		plugin.getRegions().set(mineName + ".blacklist", "[]");
		plugin.getRegions().set(mineName + ".blacklist.enabled", false);
		plugin.getRegions().set(mineName + ".blacklist.blocks", "[]");
		plugin.getRegions().set(mineName + ".blacklist.whitelist", false);
		if(debug) log.info("Mine Creation: Handling coordinates");
		plugin.getRegions().set(mineName + ".coords.world", p1.getWorld().getName());
		
		if(p1.getX() < p2.getX())
		{
			plugin.getRegions().set(mineName + ".coords.p1.x", p1.getX());
			plugin.getRegions().set(mineName + ".coords.p2.x", p2.getX());
		}
		else
		{
			plugin.getRegions().set(mineName + ".coords.p1.x", p2.getX());
			plugin.getRegions().set(mineName + ".coords.p2.x", p1.getX());
		}
		
		if(p1.getY() < p2.getY())
		{
			plugin.getRegions().set(mineName + ".coords.p1.y", p1.getY());
			plugin.getRegions().set(mineName + ".coords.p2.y", p2.getY());
		}
		else
		{
			plugin.getRegions().set(mineName + ".coords.p1.y", p2.getY());
			plugin.getRegions().set(mineName + ".coords.p2.y", p1.getY());
		}
			
		if(p1.getZ() < p2.getZ())
		{
			plugin.getRegions().set(mineName + ".coords.p1.z", p1.getZ());
			plugin.getRegions().set(mineName + ".coords.p2.z", p2.getZ());
		}
		else
		{
			plugin.getRegions().set(mineName + ".coords.p1.z", p2.getZ());
			plugin.getRegions().set(mineName + ".coords.p2.z", p1.getZ());
		}
		
		if(debug) log.info("Mine Creation: Adding the default percentage config");
		defaultBlock = plugin.getConfig().getString("config.mine-reset.default-block-id");
		List<String> itemList = new ArrayList<String>(10);
		List<String> weightList = new ArrayList<String>(10);
		itemList.add(defaultBlock);
		weightList.add(""+100);
		
		plugin.getRegions().set(mineName + ".blocks", itemList);
		plugin.getRegions().set(mineName + ".weights", weightList);
		if(debug) log.info("Mine Creation: Saving into the config and cleaning up");
		plugin.saveRegions();
		plugin.saveConfig();
		p1 = p2 = null;
		itemList = null;
		sendSuccess(sender, "Mine is successfully saved as '" + mineName + "'");
		return true;
	}

	/**
	 * Sends an error message to the player
	 * @param sender CommandSender Player who sent the command
	 * @param message String Message to send to the player
	 */
	public void sendError(CommandSender sender, String message)
	{
		String title = plugin.getConfig().getString("config.messages.title");
		if(debug) log.warning(message);
		sender.sendMessage(ChatColor.RED + "[" + title + "] " + ChatColor.WHITE + message);
		return;
	}
	
	/**
	 * Sends an command success message to the player
	 * @param sender CommandSender Player who sent the command
	 * @param message String Message to send to the player
	 */
	public void sendSuccess(CommandSender sender, String message)
	{
		String title = plugin.getConfig().getString("config.messages.title");
		if(debug) log.info(message);
		sender.sendMessage(ChatColor.GREEN + "[" + title + "] " + ChatColor.WHITE + message);
		return;
	}
	
	/**
	 * Checks if there is data on a specific mine in the config
	 * @param mineName String Name of the mine
	 * @return boolean True if a mine exists
	 */
	public boolean checkIfMineExists(String mineName)
	{
		if(plugin.getRegions().getString(mineName) == null) return false;
		return true;
	}
	
	/**
	 * Checks if the player has a certain permission.
	 * If use-permissions is turned off, the player being operator is checked
	 * @param player Player Player, whose permissions are being checked
	 * @param node String Node to check
	 * @return boolean True if a player has a permission
	 */
	public boolean hasPermission(Player player, String node)
	{
		if(!plugin.getConfig().getBoolean("config.general.use-permissions"))
		{
			if(player.isOp()) return true;
			sendError(player, "Insufficient permissions");
			return false;
		}
		else
		{
			if(player.hasPermission("minereset." + node)) return true;
			sendError(player, "Insufficient permissions");
			return false;
		}
	}
	
	/**
	 * Checks if the command sender is a player or a console
	 * @param player Player
	 * @param sender CommandSender
	 * @return boolean True if sender is a player
	 */
	public boolean isPlayer(Player player, CommandSender sender)
	{
		if ((sender instanceof Player)) return true;
		return false;
	}
	
	/**
	 * Returns blockID if provided a name
	 * @param blockName String Block to be identified
	 * @return int ID if a block exists, -1 otherwise
	 */
	public int getBlockID(String blockName)
	{
		try
		{
			if(isNumeric(blockName)) return Integer.parseInt(blockName);
			else
			{
				Material material = Material.matchMaterial(blockName);
				if(material == null) return -1;
				return material.getId();
			}
		}
		catch(NumberFormatException nfe) { return -1; }
	}
	
	/**
	 * Checks if a string is numeric
	 * @param str String String to be checked
	 * @return boolean True if a string is numeric
	 */
	@SuppressWarnings("unused")
	public static boolean isNumeric(String str)  
	{  
	  try
	  { double d = Double.parseDouble(str); }
	  catch(NumberFormatException nfe)  
	  { return false; }  
	  return true;  
	}
	
	/**
	 * Broadcasts the reset message to all the players on the server
	 * @param mineName String Mine being reset
	 */
	//TODO: Change this to send custom broadcasts
	public void broadcastReset(String mineName)
	{
		String title = plugin.getConfig().getString("config.messages.title");
		String rawMessage = plugin.getConfig().getString("config.messages.on-reset");
		String message = ChatColor.GREEN + "[" + title + "] " + rawMessage.replaceAll("%MINE%", mineName);
		Bukkit.getServer().broadcastMessage(message);
		return;
	}
	
	/**
	 * Displayer a help screen
	 * @param sender CommandSender
	 * @param player Player
	 */
	public void sendHelp(CommandSender sender, Player player)
	{
		if(debug) log.info("Displaying a help page");
		String title = plugin.getConfig().getString("config.messages.title");
		sender.sendMessage(ChatColor.RED + "          -=[ " + title + " ]=-");
		if(plugin.getConfig().getBoolean("config.general.show-all-help") || player.hasPermission("info"))
			sender.sendMessage(ChatColor.GREEN + "/mine [name]" + ChatColor.WHITE + " Shows information about mine [name]");
		if(plugin.getConfig().getBoolean("config.general.show-all-help") || player.hasPermission("reset"))
		{
			sender.sendMessage(ChatColor.GREEN + "/mine reset [name]" + ChatColor.WHITE + " Reset the mine [name]");
			sender.sendMessage("If a name is not provided, resets the default mine");
		}
		if(plugin.getConfig().getBoolean("config.general.show-all-help") || player.hasPermission("edit"))
		{
			sender.sendMessage(ChatColor.GREEN + "/mine select <pos1/pos2>" + ChatColor.WHITE + " Select a mine region at your location");
			sender.sendMessage(ChatColor.GREEN + "/mine select <hpos1/hpos2>" + ChatColor.WHITE + " Select a mine region at the block you are looking at");
			sender.sendMessage(ChatColor.GREEN + "/mine save <name>" + ChatColor.WHITE + " Save a selection as <name>");
			sender.sendMessage("If a name is not provided, saves it as default");
			sender.sendMessage(ChatColor.GREEN + "/mine add <name> <material> <percent>" + ChatColor.WHITE + " Adds <percentage> of <material> to the mine");
			sender.sendMessage(ChatColor.GREEN + "/mine remove <name> <material>" + ChatColor.WHITE + " Removes <material> in out of the mine");
			sender.sendMessage(ChatColor.GREEN + "/mine remove <name>" + ChatColor.WHITE + " Remove the <name> mine");
		}
		if(plugin.getConfig().getBoolean("config.general.show-all-help") || player.hasPermission("timer"))
		{
			sender.sendMessage(ChatColor.GREEN + "/mine timer set <name> <time>" + ChatColor.WHITE + " Sets the automatic reset time to a value specified");
			sender.sendMessage(ChatColor.GREEN + "/mine timer toggle <name>" + ChatColor.WHITE + " Toggles the automatic resets ON or OFF");
			sender.sendMessage(ChatColor.GREEN + "/mine timer toggle <name> warning" + ChatColor.WHITE + " Toggles the automatic reset warnings ON or OFF");
		}
		return;
	}
}