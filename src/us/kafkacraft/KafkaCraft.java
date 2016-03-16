package us.kafkacraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class KafkaCraft extends JavaPlugin implements CommandExecutor {
	// Map must be 80 lines
	List<String> worldMap = new ArrayList<String>();
	int centerX, centerZ;
	
    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	getLogger().info("Enable");
    	getCommand("kafka").setExecutor(this);
    	
    	String line = null;
    	JSONParser parser = new JSONParser();
    	BufferedReader in = new BufferedReader(new InputStreamReader(getResource("world.txt")));
    	try {
    		// First line gives location of 0,0 lat/lon
    		line = in.readLine();
    		JSONObject jsonObject = (JSONObject)parser.parse(line);
    		centerX = ((Number)jsonObject.get("centerX")).intValue();
    		centerZ = ((Number)jsonObject.get("centerZ")).intValue();    		
    		
			while ((line = in.readLine()) != null) {
				worldMap.add(line);
			}
		} catch (ParseException pe) {
			// TODO Auto-generated catch block
			pe.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	getLogger().info("Read "+worldMap.size()+" lines of world map");    	
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("kafka")) {
    		if (!(sender instanceof Player)) {
    			sender.sendMessage("This command can only be run by a player.");
    		} else {
    			new MapMaker(this, (Player)sender).runTaskTimer(this, 10, 10);
        		return true;
    		}
    	}
    	
    	return false; 
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {
    	getLogger().info("Disable");
    }
}
