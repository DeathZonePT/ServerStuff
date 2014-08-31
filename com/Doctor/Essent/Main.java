package com.Doctor.Essent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Main extends JavaPlugin implements Listener{


	int time = 10;
	int taskid = 0;

	public static HashMap<Player, Boolean> seeSpam = new HashMap<Player, Boolean>();
	public static HashMap<Player, Boolean> canTalk = new HashMap<Player, Boolean>();

	public static int join;
	public static int leave;

	public static boolean move = true;

	public static List<String> bannedWords = new ArrayList<String>();

	public static String wMessage;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		bannedWords = getConfig().getStringList("bannedWords");
		wMessage = getConfig().getString("wMessage");
		join = getConfig().getInt("join");
		leave = getConfig().getInt("leave");
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8]: Created by TheDoctor2014"));
		for(Player p : Bukkit.getOnlinePlayers()){
			canTalk.put(p, true);
			seeSpam.put(p, false);
		}
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	}

	@EventHandler
	public void join(PlayerJoinEvent e){
		if(e.getPlayer().hasPermission("serverstuff.see")){
			seeSpam.put(e.getPlayer(), true);
		}else{
			seeSpam.put(e.getPlayer(), false);
		}
		canTalk.put(e.getPlayer(), true);

		if(join == 0){
			e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', "&6[&a+&6] " + e.getPlayer().getName()));
		}else if(join == 2){
			e.setJoinMessage(null);
		}
	}



	@EventHandler
	public void leave(PlayerQuitEvent e){
		if(join == 0){
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "&6[&4-&6] " + e.getPlayer().getName()));
		}else if(join == 2){
			e.setQuitMessage(null);
		}
	}

	@EventHandler
	public void onChat(final AsyncPlayerChatEvent e){
		if(getConfig().getBoolean("anti")){
			if(!e.getPlayer().hasPermission("serverstuff.chatbypass")){
				if(canTalk.get(e.getPlayer())){
					String msg = ChatColor.stripColor(e.getMessage());
					for(String s : bannedWords){
						String y = s.toLowerCase();
						if(msg.toLowerCase().contains(s.toLowerCase())){
							for (int i = 0; i < s.length(); i++){
								char c = s.charAt(i);        
								c = '*';
								s = s.replace(s.charAt(i), c);
							}
							msg = msg.toLowerCase().replace(y, s);
						}
					}

					e.setMessage(msg);
					canTalk.put(e.getPlayer(), false);
					freePlayer(e.getPlayer());
				}else{
					e.setCancelled(true);

					e.setMessage("");
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8]: &7You can only talk once every 3 seconds."));
				}
			}
		}
	}

	private final static Pattern ipPattern = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
	private final static Pattern ipPattern2 = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\,([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\,([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\,([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
	private final static Pattern ipPattern4 = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\{dot}([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\{dot}([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\{dot}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
	private final static Pattern ipPattern5 = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\[dot]([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\[dot]([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\[dot]([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

	@EventHandler(priority = EventPriority.HIGHEST) // Makes your method Highest priority
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		if(!chat.getPlayer().hasPermission("serverstuff.advertisebypass")){
			Player player = chat.getPlayer();
			String message = chat.getMessage();
			if(ipPattern.matcher(message).find() ||
					ipPattern2.matcher(message).find() ||

					ipPattern4.matcher(message).find() ||
					ipPattern5.matcher(message).find() 

					|| message.contains(".com") || message.contains(".org") || message.contains(".net") || message.contains(".us") || message.contains(".uk")
					|| message.contains(",com") || message.contains(",org") || message.contains(",net") || message.contains(",us") || message.contains(",uk")) {
				chat.setCancelled(true);
				chat.setMessage("");
				player.sendMessage(ChatColor.RED + "DO NOT ADVERTISE!");
				for(Player p : Bukkit.getOnlinePlayers()){
					if(seeSpam.get(p)){
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8]:&7 " + player.getName() + " Attempted to advertise an link or ip: " + ChatColor.YELLOW + message));
					}
				}
			}
		}
	}





	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {        
		if(getConfig().getBoolean("commandBlock")){
			if(!e.getPlayer().isOp()){
				if(e.getMessage().toLowerCase().startsWith("/plot")){
					return;
				}
				if((e.getMessage().equalsIgnoreCase("/bukkit:?")) ||
						(e.getMessage().equalsIgnoreCase("/bukkit:pl")) || 
						(e.getMessage().toLowerCase().startsWith("/?")) || 
						(e.getMessage().toLowerCase().startsWith("/pl")) ||
						(e.getMessage().toLowerCase().startsWith("/bukkit:pl")) || 
						(e.getMessage().toLowerCase().startsWith("/bukkit:plugins")) ||
						(e.getMessage().toLowerCase().startsWith("/ver")) ||
						(e.getMessage().toLowerCase().startsWith("/bukkit:ver")) ||
						(e.getMessage().toLowerCase().startsWith("/bukkit:version")) ||
						(e.getMessage().toLowerCase().startsWith("/version"))
						){
					e.setCancelled(true);
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8] &7You do not have permission to do that!"));
				}
			}
		}
		if(getConfig().getBoolean("w")){
			if(e.getMessage().equalsIgnoreCase("/w")){
				e.setCancelled(true);
				e.getPlayer().chat(wMessage);
			}
		}
	}


	@EventHandler
	public void onMove(PlayerMoveEvent e){
		if(move == false){
			e.setTo(e.getFrom());
		}
	}


	public static void freePlayer(final 
			Player p){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("ServerStuff"), new Runnable(){
			@Override
			public void run(){
				canTalk.put(p, true);
			}
		}, 60L);
	}	
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args){

		if(sender instanceof Player){
			Player p = (Player)sender;
			if(label.equalsIgnoreCase("ss")){
				if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
					if(!(sender instanceof Player)){
						// If player is not an instanceof Player, then it's the console sending the command.
						reloadConfig();
						saveConfig();
						bannedWords = getConfig().getStringList("bannedWords");
						wMessage = getConfig().getString("wMessage");
						join = getConfig().getInt("join");
						leave = getConfig().getInt("leave");
						Bukkit.getLogger().info("[ServerStuff] Config Reloaded! ");
						// Returns true because the console can execute anything.
						return true;
					} else if (p.hasPermission("serverstuff.reload")) {
						// If it's not the console, then it's a player. Check their permission and execute if they have.
						reloadConfig();
						saveConfig();
						bannedWords = getConfig().getStringList("bannedWords");
						wMessage = getConfig().getString("wMessage");
						join = getConfig().getInt("join");
						leave = getConfig().getInt("leave");
						p.sendMessage(ChatColor.GREEN + "[ServerStuff] Config reloaded!");

						System.out.println("[ServerStuff] Config reloaded!");
						return true;
						// Return true if this plugin executed this command.
					} else {
						return false;
						// Returns false because the player didn't have permission, so it wasn't executed.

					}
				}
			}



			if(getConfig().getBoolean("chatClear")){
				if(label.equalsIgnoreCase("clearchat")){
					if(p.hasPermission("serverstuff.clear") || p.isOp()){
						for(int i = 0; i < 150; i++){
							for(Player p1 : Bukkit.getServer().getOnlinePlayers()){
								p1.sendMessage(" ");
							}
						}
						Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&eServer &7// &aThe whole chat got cleared by &7" + p.getName()));
					}else{
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8] &7You do not have permission to do that!"));
					}
				}
			}
			if(label.equalsIgnoreCase("spamview")){
				if(p.hasPermission("serverstuff.see")){
					if(seeSpam.get(p)){
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8] &7Spam View Disabled"));
						seeSpam.put(p, false);
					}else{
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8] &7Spam View Enabled"));
						seeSpam.put(p, true);
					}
				}else{
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&eServer&8] &7You do not have permission to do that!"));
				}
			}
		}else{
			if(getConfig().getBoolean("chatClear")){
				if(label.equalsIgnoreCase("clearchat")){
					for(int i = 0; i < 150; i++){
						for(Player p1 : Bukkit.getServer().getOnlinePlayers()){
							p1.sendMessage(" ");
						}
					}
					Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&eServer &7// &aThe whole chat got cleared by &7Console"));

				}
			}

		}
		if(label.equalsIgnoreCase("w")){
			//nothing..
		}
		if(label.equalsIgnoreCase("sendplayers")){
			if(((Player)sender).isOp()){
				if(args.length>0){
					move = false;
					for(final Player p : Bukkit.getOnlinePlayers()){
						p.setNoDamageTicks(200);
						p.sendMessage(ChatColor.YELLOW + "You are being moved to " + args[0] + " by " + ((Player)sender).getName() + " in 10 seconds!");
						p.setAllowFlight(true);
						time = 10;
						taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
							@Override
							public void run(){
								if(time>0){
									
									time--;
									p.sendMessage(ChatColor.YELLOW + "You are being moved to " + args[0] + " by " + ((Player)sender).getName() + " in " + time + " seconds!");
								}else{
									p.setAllowFlight(false);
									ByteArrayDataOutput out = ByteStreams.newDataOutput();
									out.writeUTF("Connect");
									out.writeUTF(args[0]);
									p.sendPluginMessage(Bukkit.getPluginManager().getPlugin("ServerStuff"), "BungeeCord", out.toByteArray());
									Bukkit.getScheduler().cancelTask(taskid);
									move = true;
								}
							}
						}
						, 0L, 20);
					}

				}
			}
		}
		return false;

	}

}