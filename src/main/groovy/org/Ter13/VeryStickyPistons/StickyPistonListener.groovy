package org.Ter13.VeryStickyPistons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.material.PistonBaseMaterial;

public class StickyPistonListener implements Listener
{
	public VeryStickyPistons vspp;
	public boolean isEnabled;

	
	public StickyPistonListener(VeryStickyPistons pg)
	{
		vspp = pg;
		isEnabled = false;
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{	
		if(!isEnabled)
			return

		List<Block> mv = event.getBlocks();
	
		def map = calcExtendBlocks(mv)

		def moving        = map["moving"]	
		def pistons       = map["pistons"]

		
		moving = extendWorker(pistons,moving)

		moving.removeAll(mv); //Why add them in the first place? 

		final BlockFace bf = event.getDirection();
		
		extendServerTask(moving, bf)
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		if(!isEnabled)
			return;
		
		Block tb;
		PistonBaseMaterial tp;

		final ArrayList<Block> moving = new ArrayList<Block>();

		ArrayList<Block> stickypistons = new ArrayList<Block>();
		ArrayList<Integer> distances = new ArrayList<Integer>();

		Location loc = event.getRetractLocation();
		tb = loc.getBlock();
		Block pb;

		
		if(isStickyPistonOff(tb) )
		{
			stickypistons.add(tb);
			pb = tb;
			moving.add(pb);
			distances.add(2);
	
	
			Block ta;
			Block tm;
			int d;
				
			while(stickypistons.size()!=0)
			{
				d = distances.get(0);
	
				tb = stickypistons.get(0);
				tp = (PistonBaseMaterial)tb.getState().getData();
				ta = tb.getRelative(tp.getFacing(),1);
				
				if(isRelativeBlockMovable(ta) )
				{
					if(!moving.contains(ta))
					{
						if(((ta.getType()==Material.PISTON_STICKY_BASE||ta.getType()==Material.PISTON_BASE)&&ta.getBlockPower()==0)||ta.getPistonMoveReaction()==PistonMoveReaction.MOVE)
						{
							tm = ta.getRelative(event.getDirection().getOppositeFace(),1);
							if(tm.isEmpty()||moving.contains(tm))
							{
								if(moving.contains(tm))
								{
									moving.add(moving.indexOf(tm)+1,ta);
								}
								else
								{
									moving.add(ta);
								}
							
								if(ta.getType()==Material.PISTON_STICKY_BASE&&d<12)
								{
									stickypistons.add(ta);
									distances.add(d+1);
								}
							}
						}
					}
				}
				stickypistons.remove(0);
				distances.remove(0);
			}
				moving.remove(pb);
				final BlockFace bf = (event.getDirection()).getOppositeFace();
				
				
				retractServerTask(moving,bf)	
		}
	}


	void retractServerTask(List moving,BlockFace facing) {
		def task = {
			moving.each { b ->
				Block nb = b.getRelative(facing,1);
				nb.setType(b.getType());
				nb.setData(b.getData());
				b.setType(Material.AIR);
			}
		}
		
		vspp.getServer().getScheduler().scheduleSyncDelayedTask(
			vspp,
			task,
			0L);
	}

	// Takes a list of Blocks that will be effected by the ExtendEvent
	// Returns three lists
	//   Blocks that will be moving
	//   Stickypistons 
	//   and the distances
	def calcExtendBlocks(List<Block> blocks) {
		def moving        = []
		def pistons	  = []
		blocks.eachWithIndex { b, i ->
			moving += b
			if((b.getType() == Material.PISTON_STICKY_BASE)
				&& (b.getBlockPower()==0) ) 
			{
				pistons += ["piston":b,"dist":i]
			}
		}
		
		return ["moving": moving, "pistons": pistons]
	}

/**
 *
 *
 */
	void extendServerTask(List moving, BlockFace facing){
		def task = {
			Block nb;
			moving.each { block -> 
				nb = block.getRelative(facing,1);
				nb.setType(block.getType());
				nb.setData(block.getData());
				block.setType(Material.AIR);
			}	
		} as Runnable

		vspp.getServer().getScheduler().scheduleSyncDelayedTask(
		vspp, 		
		task,

		0L);
	}


/*
 *
 * Refactored functions from Retract
 *
 */

	boolean isStickyPistonOff(Block block) {
		if ( (block.getType()==Material.PISTON_STICKY_BASE)
		     &&
		     (block.getBlockPower()==0) )
		{
			return true
		}
			return false
	}

/*
 *
 * Refactored functions from Extend
 *
 */
	//
	def extendWorker( def pistons, def moving){
			
		pistons.each { pair -> 
			Block ta = getBlockRelativeToPiston(pair["piston"])
			int d = pair["dist"]
			
			if( (isRelativeBlockMovable(ta)) 
				&& (!moving.contains(ta) )
				&& (isPistonOff(ta) ) )
			{
			
			Block tm = ta.getRelative(event.getDirection(),1);
			//something odd about the logic here
				if(tm.isEmpty()||moving.contains(tm))
				{
					if(moving.contains(tm))
						moving.add(moving.indexOf(tm)+1,ta);
					else
						moving.add(ta);
							
					if(ta.getType()==Material.PISTON_STICKY_BASE&&d<12)
					{
// what this tell me is that this should be a recursive funtion..
//however this seems to work
//XXX				//stickypistons.add(ta);
				//distances.add(d+1);
				pistons << ["piston":ta,"dist":d+1]
				
					}
				}
			}
		}
	return moving
	}


//
	Block getBlockRelativeToPiston(Block block) {
		PistonBaseMaterial mat = (PistonBaseMaterial)block.getState().getData()
		return block.getRelative(mat.getFacing(),1)
	
	}

	// checks if block is a piston and that the power if off
	// or
	// that this block can be moved
	boolean isPistonOff(Block block) {
		def pistonMaterial = [Material.PISTON_STICKY_BASE, 
				      Material.PISTON_BASE ]

		if ( (pistonMaterial.any { m -> block.getType() == m } )
		    &&
		   (block.getBlockPower() == 0 ) ) {
			return true
		}
		
		if (block.getPistonMoveReaction()!=PistonMoveReaction.BLOCK) 
		{
			return true
		}

		return false
	}


/*
 *
 * Refactored functions shared
 *
 */


	// isRelativeBlockMovable
	// takes a block
	boolean isRelativeBlockMovable(Block block) {
		def notMovableMaterial = [Material.CHEST,
			Material.FURNACE, Material.ENCHANTMENT_TABLE,
			Material.BREWING_STAND, Material.SIGN_POST,
			Material.OBSIDIAN, Material.NOTE_BLOCK, 
			Material.BEDROCK, Material.DISPENSER, 
			Material.JUKEBOX ]

		if ((block == null) || block.isEmpty() || block.isLiquid() ) 
			return false

		if (notMovableMaterial.any { block.getType() == it }) 
			return false

		return true
	}
}
