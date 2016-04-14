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
    private final KafkaCraft plugin;
    private final World world;
    private KafkaConsumer<String, String> consumer;
    private final JSONParser parser;
    private final MapMaker mapMaker;
    private double latScale, lonScale;
    
    public Consumer(KafkaCraft plugin, World world, MapMaker mapMaker) {
    	this.plugin = plugin;
    	this.world = world;
    	this.mapMaker = mapMaker;
    	
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
        consumer.subscribe(Arrays.asList(plugin.topic));
        
        Bukkit.broadcastMessage("Subscribed to "+plugin.topic+" topic");
        
        latScale = mapMaker.height / (mapMaker.bottomRightLat - mapMaker.topLeftLat);
        lonScale = mapMaker.width / (mapMaker.bottomRightLon - mapMaker.topLeftLon);
        
        plugin.getLogger().info("latScale: "+latScale+", lonScale: "+lonScale);
    }

    @Override
    public void run() {
        ConsumerRecords<String, String> records = consumer.poll(0);
        for (ConsumerRecord<String, String> record : records) {
        	plugin.getLogger().info("Received " + record.value());
        	try {
        		JSONObject jsonObject = (JSONObject)parser.parse(record.value());
        		
        		if (jsonObject.containsKey("verb")) {
	        		Bukkit.broadcastMessage(jsonObject.get("verb") + " " + 
	        				jsonObject.get("request") + " from " + 
	        				jsonObject.get("clientip"));
        		} else if (jsonObject.containsKey("VERB")) {
	        		Bukkit.broadcastMessage(jsonObject.get("VERB") + " " + 
	        				jsonObject.get("REQUEST") + " from " + 
	        				jsonObject.get("CLIENTIP"));
        		}
        		
        		double lat = 0.0, lon = 0.0;
        		boolean haveLatLon = false;
        		if (jsonObject.containsKey("LAT") && jsonObject.containsKey("LON")) {
            		lat = ((Number)jsonObject.get("LAT")).doubleValue();
            		lon = ((Number)jsonObject.get("LON")).doubleValue();
            		haveLatLon = true;
        		} else if (jsonObject.containsKey("lat") && jsonObject.containsKey("lon")) {
            		lat = ((Number)jsonObject.get("lat")).doubleValue();
            		lon = ((Number)jsonObject.get("lon")).doubleValue();
            		haveLatLon = true;
        		} 
        		
        		if (haveLatLon) {
            		// Default to sand
            		Material material = Material.SAND;
            		
            		// Make small plots gravel
            		double area = 0.0;
            		boolean haveArea = false;
            		if (jsonObject.containsKey("AREA")) {
            			area = ((Number)jsonObject.get("AREA")).doubleValue();
            		} else if (jsonObject.containsKey("area")) {
            			area = ((Number)jsonObject.get("area")).doubleValue();
            		}
            		
            		if (haveArea && area < 10000) {
        				material = Material.GRAVEL;
            		}
             		
            		if (lat < mapMaker.topLeftLat && lat > mapMaker.bottomRightLat &&
            			lon > mapMaker.topLeftLon && lon < mapMaker.bottomRightLon) {
                     		int blockX = mapMaker.originX + (int)(lonScale * (lon - mapMaker.topLeftLon));
                     		int blockZ = mapMaker.originZ + (int)(latScale * (lat - mapMaker.topLeftLat));
                     		plugin.getLogger().info("Dropping block at "+blockX+", 100, "+blockZ);
                     		world.getBlockAt(blockX, 100, blockZ).setType(material);            				
            		}
        		}        		
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
    }

}