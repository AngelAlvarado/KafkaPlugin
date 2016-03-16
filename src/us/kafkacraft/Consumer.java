package us.kafkacraft;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Consumer extends BukkitRunnable {
	// TBD - set from ascii art
	int width,
	    height,
	    centerX,
	    centerZ,
	    originX,
	    originZ;
	
    private final KafkaCraft plugin;
    private final World world;
    private KafkaConsumer<String, String> consumer;
    private final JSONParser parser;
    
    public Consumer(KafkaCraft plugin, World world, int width, int height, int centerX, int centerZ, int originX, int originZ) {
    	this.plugin = plugin;
    	this.world = world;
    	this.width = width;
    	this.height = height;
    	this.centerX = centerX;
    	this.centerZ = centerZ;
    	this.originX = originX;
    	this.originZ = originZ;
    	
        parser = new JSONParser();
        
    	Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("log"));
        
        Bukkit.broadcastMessage("Subscribed to log topic");
    }

    @Override
    public void run() {
        ConsumerRecords<String, String> records = consumer.poll(0);
        for (ConsumerRecord<String, String> record : records) {
        	plugin.getLogger().info("Received " + record.value());
        	try {
        		JSONObject jsonObject = (JSONObject)parser.parse(record.value());
        		
        		Bukkit.broadcastMessage(jsonObject.get("verb") + " " + 
        				jsonObject.get("request") + " from " + 
        				jsonObject.get("clientip"));
        		
        		if (jsonObject.get("lat") != null && jsonObject.get("lon") != null) {
            		double lat = ((Number)jsonObject.get("lat")).doubleValue(),
             			   lon = ((Number)jsonObject.get("lon")).doubleValue();
             		
             		int blockX = originX + (int)(((((lon + 180) / 360) * width) + (centerX - (width / 2)))) % width;
             		int blockZ = originZ + (int)(((180 - lat) / 360) * (centerZ * 2));
             		plugin.getLogger().info("Dropping block at "+blockX+", 100, "+blockZ);
             		world.getBlockAt(blockX, 100, blockZ).setType(Material.SAND);
        		}        		
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
    }

}