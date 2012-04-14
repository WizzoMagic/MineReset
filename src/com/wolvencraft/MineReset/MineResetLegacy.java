/*
package com.wolvencraft.MineReset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class MineReset extends JavaPlugin
{
	
	public void onEnable()
	{
		log = this.getLogger();
		getConfig().options().copyDefaults(true);

		this.getConfig();
		this.getRegions();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		mineNum = this.getConfig().getInt("data.regions");
		log.info(mineNum + " mine(s) found");
		
		if(this.getConfig().getBoolean("config.general.debug-mode") == true) debug = true;
		else debug = false;
		
		if(this.getConfig().getBoolean("config.mine-reset.broadcast-on-reset") == true) broadcast = true;
		else broadcast = false;
		
		padding = this.getConfig().getDouble("config.general.protection-padding");
		
		if(debug) log.warning("Running in debug mode");
		
		myExecutor = new MineResetCommandExecutor(this);
		getCommand("mine").setExecutor(myExecutor);
		
		if(getConfig().getBoolean("config.mine-reset.automatic-reset"))
		{
			log.info("Running async task");
			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
			{
				int min;
		        int sec;
		        List<String> mineList;
		    	String curMineName;
		    	String warningMessage;
		    	String countdownMessage;
		        
			    public void run()
			    {
			    	warningMessage = getConfig().getString("config.messages.reset-warning");
			    	countdownMessage = getConfig().getString("config.messages.reset-cooldown");
			    	
			    	mineList = getConfig().getStringList("data.region-list");
			    	for(int i = 0; i < mineNum; i++)
			    	{
			    		curMineName = mineList.get(i);
			    		min = getRegions().getInt(curMineName + ".auto-reset.time.cur-min");
			    		sec = getRegions().getInt(curMineName + ".auto-reset.time.cur-sec");
			    		
				    	sec--;
				    	
				        if(sec <= 0)
				        {
				        	min--;
				        	sec = 60;
				        }
				        
			    		getRegions().set(curMineName + ".auto-reset.time.cur-min", min);
			    		getRegions().set(curMineName + ".auto-reset.time.cur-sec", sec);
				        
				        saveRegions();
				        
				        if(min == getConfig().getInt("config.mine-resetbroadcast-reset-warning-at") && sec == 0)
			    		{
				        	if(debug) log.info("Broadcasting a reset warning");
				        	warningMessage = warningMessage.replaceAll("%MINE%", curMineName);
				        	warningMessage = warningMessage.replaceAll("%TIME%", "" + min);
				        	Bukkit.getServer().broadcastMessage(warningMessage);
			    		}
				        else if(getConfig().getBoolean("broadcast-reset-countdown") && min == 0 && sec <= getConfig().getInt("config.mine-reset.broadcast-reset-countdown-at") && sec > 0)
				        {
				        	if(debug) log.info("Broadcasting a countdown");
				        	countdownMessage = countdownMessage.replaceAll("%MINE%", curMineName);
				        	countdownMessage = countdownMessage.replaceAll("%TIME%", "" + sec);
				        	Bukkit.getServer().broadcastMessage("Mine " + curMineName + " resets in " + sec + " seconds");
				        }
				        else if((min == 0 && sec == 0) || min < 0)
				        {
				        	if(debug) log.info("Resetting a mine!");
				        	min =  getRegions().getInt(curMineName + ".auto-reset.time.default-time");
				    		sec = 0;
					        
				    		if(debug) log.info("Resetting a mine automatically");
					        myExecutor.resetMine(curMineName);
				        }
				        
				        if(sec == 30) log.info(curMineName + " resets in " + min + " minutes");
			    	}
			    }
			}, 20L, 20L);
		}
	}
	
	public void onDisable()
	{
		log.info("MineReset disabled");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(!this.getConfig().getBoolean("use-protection")) return;
		Player player = event.getPlayer();
		if(player.isOp()) return;
		
		@SuppressWarnings("unchecked")
		List<String> regionList = (List<String>) this.getConfig().getList("data.region-list");
		
		for(int i = 0; i < mineNum; i++)
		{
			if(this.getConfig().getBoolean("config.use-permissions"))
			{
				if(!hasPermission(player, "break") && !hasPermission(player, "break." + regionList.get(i)))
				{
					Location blockLocation = event.getBlock().getLocation();
					int[] x = {this.getConfig().getInt("regions." + regionList.get(i) + ".coords.p1.x"), this.getConfig().getInt("regions." + regionList.get(i) + ".coords.p2.x")};
					int[] y = {this.getConfig().getInt("regions." + regionList.get(i) + ".coords.p1.y"), this.getConfig().getInt("regions." + regionList.get(i) + ".coords.p2.y")};
					int[] z = {this.getConfig().getInt("regions." + regionList.get(i) + ".coords.p1.z"), this.getConfig().getInt("regions." + regionList.get(i) + ".coords.p2.z")};
			
					if(blockLocation.getX() < (x[0] - padding) || blockLocation.getX() > (x[1] + padding)) return;
					if(blockLocation.getY() < (y[0] - padding) || blockLocation.getY() > (y[1] + padding)) return;
					if(blockLocation.getZ() < (z[0] - padding) || blockLocation.getZ() > (z[1] + padding)) return;
					
					sendError(player, "You are not allowed to break blocks in this mine");
					event.setCancelled(true);
				}
			}
		}
		
		return;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
    	if(debug) log.info("PlayerInteractEvent passed");
		Block blockClicked = event.getClickedBlock();
		String theAction = event.getAction().toString(); //Get to make sure it was right-clicked
        if(theAction =="RIGHT_CLICK_BLOCK" || (blockClicked.getType() == Material.WALL_SIGN || blockClicked.getType() == Material.SIGN_POST))
        {
        	BlockState state = blockClicked.getState();
        	if(state instanceof Sign)
        	{
        		if(debug) log.info("Block is a sign");
        		Sign sign = (Sign)state;
        		
        		Player player = event.getPlayer();
         		if(this.getConfig().getBoolean("config.general.use-permissions"))
         			if(!hasPermission(player, "sign") && !hasPermission(player, "sign.use")) return;
         		

            	if(debug) log.info("Permissions check passed");
         		String signTitle = getConfig().getString("config.messages.sign-title");
         		if(debug) log.info(signTitle + " =?= " + sign.getLine(0));
         		if(!sign.getLine(0).equalsIgnoreCase(signTitle)) return;
         		
            	if(debug) log.info("The sign is formatted");
            	
            	
         		String mineName = sign.getLine(1);
         		myExecutor.resetMine(mineName);

            	if(debug) log.info("Event finishing");
         		return;
         	}
        }
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
    	if(debug) log.info("BlockPlaceEvent passed");
		String signTitle = getConfig().getString("config.messages.sign-title");
		Block blockPlaced = event.getBlock();
		
		Player player = event.getPlayer();
		if(this.getConfig().getBoolean("config.general.use-permissions"))
			if(!hasPermission(player, "break") && !hasPermission(player, "sign.create")) return;
		
    	if(debug) log.info("Permissions check passed");
		
        if(blockPlaced.getType() == Material.WALL_SIGN || blockPlaced.getType() == Material.SIGN_POST)
        {
        	BlockState state = blockPlaced.getState();
        	
        	if(state instanceof Sign)
        	{
            	if(debug) log.info("Block is a sign passed");
        		Sign sign = (Sign)state;
        		
        		if(debug) log.info("Sign-title: " + signTitle);
        		if(!sign.getLine(0).equalsIgnoreCase(signTitle)) return;
        		
        		String mineName = sign.getLine(1);
        		if(mineName == null || !myExecutor.checkIfMineExists(mineName))
        		{
        			sign.setLine(0, ChatColor.DARK_RED + signTitle);
                	if(debug) log.info("Invalid sign");
        		}
        		else
        		{
        			sign.setLine(0, ChatColor.DARK_BLUE + signTitle);
                	if(debug) log.info("Valid sign");
        		}

            	if(debug) log.info("Event finishing");
            	return;
         	}
        }
	}
	
	public void sendError(CommandSender sender, String message)
	{
		String title = this.getConfig().getString("config.messages.title");
		if(debug) log.warning(message);
		sender.sendMessage(ChatColor.RED + "[" + title + "] " + ChatColor.WHITE + message);
		return;
	}
	
	public void sendSuccess(CommandSender sender, String message)
	{
		String title = this.getConfig().getString("config.messages.title");
		if(debug) log.info(message);
		sender.sendMessage(ChatColor.GREEN + "[" + title + "] " + ChatColor.WHITE + message);
		return;
	}
	
	public boolean hasPermission(Player player, String node)
	{
		if(!this.getConfig().getBoolean("config.general.use-permissions"))
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
	
	public boolean isPlayer(Player player, CommandSender sender)
	{
		if ((sender instanceof Player)) return true;
		return false;
	}
	
	public void reloadRegions() {
	    if (regionsFile == null) {
	    regionsFile = new File(getDataFolder(), "regions.yml");
	    }
	    regions = YamlConfiguration.loadConfiguration(regionsFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = getResource("regions.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        regions.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getRegions() {
	    if (regions == null) {
	        reloadRegions();
	    }
	    return regions;
	}
	
	public void saveRegions() {
	    if (regions == null || regionsFile == null) {
	    return;
	    }
	    try {
	        regions.save(regionsFile);
	    } catch (IOException ex) {
	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + regionsFile, ex);
	    }
	}
}
*/