package com.wolvencraft.MineReset.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pattern {
    double []Totals;
 
    List<String> blocks;
    List<String> weights;
    List<String> bin;
    
    public Pattern (List<String> blockList, List<String> weightList)
    {
    	blocks = blockList;
    	weights = weightList;
    	bin = new ArrayList<String>();
    }
    
    public int next()
    {
    	Random seed = new Random();
    	
    	for(int i = 0; i < blocks.size(); i++)
    	{
    		for(int j = 0; j < (Integer.parseInt(weights.get(i)) * 1000); j++)
    		{
    			bin.add(blocks.get(i));
    		}
    	}
    	
    	int rand = Integer.parseInt(bin.get(seed.nextInt(bin.size())));
    	return rand;
    }
}