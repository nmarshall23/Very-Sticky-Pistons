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

		vspp.log.debug("[VSP] Moving? " + moving.size() )	
		vspp.log.debug("[VSP] Pistons? " + pistons.size() )
		
		moving = extendWorker(pistons,moving)

		moving.removeAll(mv); //Why add them in the first place? 

		final BlockFace bf = event.getDirection();
		
		extendServerTask(moving, bf)
		
		
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		if(!isEnabled)
		{
			return;
		}
		Block tb;
		PistonBaseMaterial tp;

		final ArrayList<Block> moving = new ArrayList<Block>();

		ArrayList<Block> stickypistons = new ArrayList<Block>();
		ArrayList<Integer> distances = new ArrayList<Integer>();

		Location loc = event.getRetractLocation();
		tb = loc.getBlock();
		Block pb;
		if(tb.getType()==Material.PISTON_STICKY_BASE)
		{
			if(tb.getBlockPower()==0)
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
					
					if(ta!=null&&!ta.isEmpty()&&!ta.isLiquid()&&ta.getType()!=Material.CHEST&&ta.getType()!=Material.FURNACE&&ta.getType()!=Material.ENCHANTMENT_TABLE&&ta.getType()!=Material.BREWING_STAND&&ta.getType()!=Material.SIGN_POST&&ta.getType()!=Material.OBSIDIAN&&ta.getType()!=Material.NOTE_BLOCK&&ta.getType()!=Material.BEDROCK&&ta.getType()!=Material.DISPENSER&&ta.getType()!=Material.JUKEBOX)
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
				
				vspp.getServer().getScheduler().scheduleSyncDelayedTask(vspp, new Runnable()
				{

   					public void run()
					{
						Block b;
						Block nb;
						
						for(int count=0;count<moving.size();count++)
						{
							b = moving.get(count);
							nb = b.getRelative(bf,1);
							nb.setType(b.getType());
							nb.setData(b.getData());
							b.setType(Material.AIR);
						}
					 }
				}, 0L);
			}
		}
	}

	// Takes a list of Blocks that will be effected by the ExtendEvent
	// Returns three lists
	//   Blocks that will be moving
	//   Stickypistons 
	//   and the distances
	def calcExtendBlocks(List<Block> blocks) {
		def moving        = []
		def stickypistons = []
		def distances     = []
		def pistons	  = []
		blocks.eachWithIndex { b, i ->
			blocksToMove += b
			if((b.getType() == Material.PISTON_STICKY_BASE)
				&& (b.getBlockPower()==0) ) 
			{
				stickypistons += b
				distances     += i
				pistons += ["piston":b,"dist":i]
			}
		}
		assert stickypistons.size() == distances.size()
		
		return ["moving": moving, "stickypistons": stickypistons, "distances": distances, "pistons": pistons]
	}

	void extendServerTask(List moving, BlockFace facing){
		vspp.getServer().getScheduler().scheduleSyncDelayedTask(vspp, new Runnable()
		{
		
   		public void run()
		{
			Block nb;

			moving.each { block -> 
				nb = block.getRelative(facing,1);
				nb.setType(block.getType());
				nb.setData(block.getData());
				block.setType(Material.AIR);
			}
		 }
		}, 0L);
	}

	//
	def extendWorker( def pistons){
			
		pistons.each { pair -> 
			Block ta = getBlockRelativeToPiston(pair["piston"])
			
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
//XXX				//stickypistons.add(ta);
				//distances.add(d+1);
					}
				}
			}
		}
	return moving
	}

	Block getBlockRelativeToPiston(Block block) {
		PistonBaseMaterial mat = (PistonBaseMaterial)blcok.getState().getData()
		return block.getRelative(mat.getFacing(),1)
	
	}

	// checks if block is a piston and that the power if off
	// or
	// that this block can be moved
	boolean isPistonOff(Block block) {
		def pistonMaterial = [Material.PISTON_STICKY_BASE, Material.PISTON_BASE]

		if ( (pistonMaterial.any { m -> block.getType() == m } ) && (block.getBlockPower() == 0 ) ) {
			return true
		}
		
		if (block.getPistonMoveReaction()!=PistonMoveReaction.BLOCK) 
		{
			return true
		}

		return false
	}


	// isRelativeBlockMovable
	// takes a block
	boolean isRelativeBlockMovable(Block block) {
		def notMovableMaterial = [Material.CHEST,Material.FURNACE,Material.ENCHANTMENT_TABLE,Material.BREWING_STAND,Material.SIGN_POST,Material.OBSIDIAN, Material.NOTE_BLOCK, Material.BEDROCK, Material.DISPENSER, Material.JUKEBOX]
		if ((block == null) || block.isEmpty() || block.isLiquid() ) 
			return false

		if (notMovableMaterial.any { ta.getType() }) 
			return false

		return true
	}
}
