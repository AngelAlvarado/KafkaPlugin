package us.kafkacraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MapMaker extends BukkitRunnable {
	// TBD - set from ascii art
	int width,
		height,
		level = 0,
		startX,
		startZ,
		locX,
		locZ,
		z;
	
    private final World world;
    private final KafkaCraft plugin;
    
    public MapMaker(KafkaCraft plugin, Player player) {
    	this.plugin = plugin;
    	
    	width = plugin.worldMap.get(0).length();
    	height = plugin.worldMap.size();
    			
    	Location loc = player.getLocation();
    	world = loc.getWorld();
    	locX = loc.getBlockX();
    	locZ = loc.getBlockZ();
    	for (int x = locX - (width/2); x < locX + (width/2); x++) {
        	for (int z = locZ - (height/2); z < locZ + (height/2); z++) {
        		Block b = world.getHighestBlockAt(x, z);
        		level = Math.max(level, b.getY());
        	}
    	}
    	
    	plugin.getLogger().info("Highest block is at y = "+level);

    	level += 5;
    	
    	startX = locX - (width/2);
    	startZ = locZ - (height/2);
    	z = startZ;
    	
    	player.setFlying(true);
    	loc.setZ(locZ + (height/2));
    	loc.setY(level + 50);
    	loc.setPitch(55);
    	loc.setYaw(180);
    	player.teleport(loc);
    }

    @Override
    public void run() {
    	
    	if (z < locZ + (height/2)) {
    		String row = plugin.worldMap.get(z - startZ);
        	for (int x = startX; x < locX + (width/2); x++) {
        		Material material = (row.charAt(x - startX) == ' ')
        				? Material.LAPIS_BLOCK
        				: Material.DIRT;
        		world.getBlockAt(x, level, z).setType(material);
        	}
        	z++;
    	} else {
    		new Consumer(plugin, world, width, height, plugin.centerX, plugin.centerZ, startX, startZ).runTaskTimer(plugin, 10, 10);
    		this.cancel();
    	}
    }

}