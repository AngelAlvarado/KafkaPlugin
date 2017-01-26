package us.kafkacraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KafkaCraft extends JavaPlugin implements CommandExecutor {
	String topic;

    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	getLogger().info("Enable");
    	getCommand("kafka").setExecutor(this);
    }

    // args[0] mapName (without any file extension, defaults to 'world'),
    // args[1] Kafka topic (defaults to 'log').
    // args[2] makeMap indicates whether to make the map (defaults to true). This latter allows you to reuse worlds,
    // resubscribing without the overhead of re-placing all the map blocks
    // args[4] Kafka host (default to localhost)
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String map      = (args.length > 0) ? (args[0] + ".txt") : "world.txt";
		this.topic      = (args.length > 1) ? args[1] : "log";
		Boolean makeMap = (args.length > 2) ? Boolean.valueOf(args[2]) : true;
        String kafkaHost = (args.length > 3) ? (args[3]) : "localhost";
    	if (cmd.getName().equalsIgnoreCase("kafka")) {
    		if (!(sender instanceof Player)) {
    			sender.sendMessage("This command can only be run by a player.");
    		} else {
    			new MapMaker(this, (Player)sender, map, makeMap, kafkaHost).runTaskTimer(this, 10, 10);
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
