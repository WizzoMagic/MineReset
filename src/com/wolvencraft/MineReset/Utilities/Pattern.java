package com.wolvencraft.MineReset.Utilities;

import java.util.Random;

public class Pattern {
    double []Totals;
 
    int[] items;
    double[] weights;
    int[] bin;
    
    public Pattern (int[] items, double[] weights)
    {
    	this.items = items;
    	this.weights = weights;
    	bin = new int[100000];
    }
    
    public int next()
    {
Random seed = new Random();
    	
    	int counter = 0;
    	for(int i = 0; i < items.length; i++)
    	{
    		for(int j = 0; j < (weights[i] * 1000); j++)
    		{
    			bin[counter] = items[i];
    			counter++;
    		}
    	}
    	
    	int rand = bin[seed.nextInt(bin.length)];
    	return rand;
    }
}