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

		Block tb;
		PistonBaseMaterial tp;

		final ArrayList<Block> moving = new ArrayList<Block>();

		List<Block> mv = event.getBlocks();
		ArrayList<Block> stickypistons = new ArrayList<Block>();
		ArrayList<Integer> distances = new ArrayList<Integer>();

		mv.eachWithIndex { b, i -> 
			moving.add(b)
			if(b.getType() == Material.PISTON_STICKY_BASE)
			{
				if(b.getBlockPower()==0)
				{
					stickypistons.add(b)
					distances.add(i)
				}
			}
		}

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
					if(((ta.getType()==Material.PISTON_STICKY_BASE||ta.getType()==Material.PISTON_BASE)&&ta.getBlockPower()==0)||ta.getPistonMoveReaction()!=PistonMoveReaction.BLOCK)
					{
						tm = ta.getRelative(event.getDirection(),1);
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
		moving.removeAll(mv);

		final BlockFace bf = event.getDirection();
		
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

	def calcExtendBlocks(List<Block> blocks) {
		def moving        = []
		def stickypistons = []
		def distances     = []
		blocks.eachWithIndex { b, i ->
			blocksToMove += b
			if((b.getType() == Material.PISTON_STICKY_BASE)
				&& (b.getBlockPower()==0) ) 
			{
				stickypistons += b
				distances     += i
			}
		}
		assert stickypistons.size() == distances.size()
		
		return ["moving": moving, "stickypistons": stickypistons, "distances": distances]
	}	

}
