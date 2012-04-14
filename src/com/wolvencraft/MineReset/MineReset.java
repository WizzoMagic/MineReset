/**
 * 
 */
package com.wolvencraft.MineReset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.wolvencraft.MineReset.cmd.Util;


/**
 * Mine Reset
 * Copyright (C) 2012 bitWolfy
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

public class MineReset extends JavaPlugin
{
	private FileConfiguration regionData = null;
	private File regionDataFile = null;
	
	public void onEnable()
	{
		
		
	}
	
	public void onDisable()
	{
		
	}
	
	public void reloadRegionData() {
	    if (regionDataFile == null) {
	    regionDataFile = new File(getDataFolder(), "regions.yml");
	    }
	    regionData = YamlConfiguration.loadConfiguration(regionDataFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = getResource("customConfig.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        regionData.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getRegionData() {
	    if (regionData == null) {
	        reloadRegionData();
	    }
	    return regionData;
	}

	public void saveRegionData() {
	    if (regionData == null || regionDataFile == null) {
	    return;
	    }
	    try {
	        regionData.save(regionDataFile);
	    } catch (IOException ex) {
	        Util.logWarning("Could not save config to " + regionDataFile);
	    }
	}
}