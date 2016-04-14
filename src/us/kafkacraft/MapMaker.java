package us.kafkacraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MapMaker extends BukkitRunnable {
	List<String> worldMap;
	
	// TBD - set from ascii art
	int level = 0,
		z,
	    width,		// width of map in blocks
	    height,		// height of map in blocks
	    centerX,	// center of map in blocks
	    centerZ,
	    originX,	// Top left block coord
	    originZ;
	
	double topLeftLat,
		   topLeftLon,
		   bottomRightLat,
		   bottomRightLon;
			
    private final World world;
    private final KafkaCraft plugin;
	private final Boolean makeMap;
    
    private List<String> makeMap(String map) {
    	List<String> m = new ArrayList<String>();
    	String line = null;
    	JSONParser parser = new JSONParser();
    	BufferedReader in = new BufferedReader(new InputStreamReader(plugin.getResource(map)));
    	try {
    		// First line gives location of 0,0 lat/lon or top left/bottom right
    		line = in.readLine();
    		JSONObject jsonObject = (JSONObject)parser.parse(line);
			topLeftLat = ((Number)jsonObject.get("topLeftLat")).doubleValue();
			topLeftLon = ((Number)jsonObject.get("topLeftLon")).doubleValue();
			bottomRightLat = ((Number)jsonObject.get("bottomRightLat")).doubleValue();
			bottomRightLon = ((Number)jsonObject.get("bottomRightLon")).doubleValue();
    		
    		if (bottomRightLon < topLeftLon) {
    			bottomRightLon += 360.0;
    		}
    		
			while ((line = in.readLine()) != null) {
				m.add(line);
			}
		} catch (ParseException pe) {
			// TODO Auto-generated catch block
			pe.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	plugin.getLogger().info("Read "+m.size()+" lines of world map");
    	
    	return m;
    }
    
    public MapMaker(KafkaCraft plugin, Player player, String map, Boolean makeMap) {
    	this.plugin = plugin;
    	this.makeMap = makeMap;
    	
    	plugin.getLogger().info("Map: "+map+", makeMap: "+makeMap);
    	
    	worldMap = makeMap(map);
    	
    	width = worldMap.get(0).length();
    	height = worldMap.size();
    			
    	Location loc = player.getLocation();
    	world = loc.getWorld();
    	centerX = 0;//loc.getBlockX();
    	centerZ = 0;//loc.getBlockZ();
    	for (int x = centerX - (width/2); x < centerX + (width/2); x++) {
        	for (int z = centerZ - (height/2); z < centerZ + (height/2); z++) {
        		Block b = world.getHighestBlockAt(x, z);
        		level = Math.max(level, b.getY());
        	}
    	}
    	
    	plugin.getLogger().info("Highest block is at y = "+level);

    	level += 5;
    	
    	originX = centerX - (width/2);
    	originZ = centerZ - (height/2);
    	z = originZ;
    	
    	player.setFlying(true);
    	loc.setX(centerX);
    	loc.setZ(centerZ + (height/2));
    	loc.setY(level + 50);
    	loc.setPitch(55);
    	loc.setYaw(180);
    	player.teleport(loc);
    }

    @Override
    public void run() {
    	
    	if (makeMap && (z < centerZ + (height/2))) {
    		plugin.getLogger().info("Drawing row at z = "+z);
    		String row = worldMap.get(z - originZ);
        	for (int x = originX; x < centerX + (width/2); x++) {
        		Material material = (row.charAt(x - originX) == ' ')
        				? Material.LAPIS_BLOCK
        				: Material.EMERALD_BLOCK;
        		world.getBlockAt(x, level, z).setType(material);
        	}
        	z++;
    	} else {
    		new Consumer(plugin, world, this).runTaskTimer(plugin, 10, 10);
    		this.cancel();
    	}
    }

}