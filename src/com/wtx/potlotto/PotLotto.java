package com.wtx.potlotto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.wtx.potlotto.commands.LotteryCommand;
import com.wtx.potlotto.config.Config;
import com.wtx.potlotto.config.ConfigManager;

import net.md_5.bungee.api.ChatColor;

public class PotLotto extends JavaPlugin implements Listener {

	private static PotLotto instance;

	private Inventory inventory;

	public static int i;

	public static final String prefix = ChatColor.RED + "PotLotto" + ChatColor.GOLD + " || " + ChatColor.AQUA;

	private List<ItemStack> potContents = new ArrayList<ItemStack>();

	private HashMap<UUID, List<ItemStack>> playersAndItems = new HashMap<UUID, List<ItemStack>>();
	private HashMap<Material, Integer> itemsAndPrices = new HashMap<Material, Integer>();
	private HashMap<Material, Boolean> itemsAndAloud = new HashMap<Material, Boolean>();

	private ConfigManager configManager;
	private Config config;
	private Config valueCfg;

	private int chance = 0;
	private int winningsWorth = 0;
	private int ORIGINAL_POT_TIME = 300;
	public int timePerPot = 0;
	
	private Long sleepTime = 5000L;

	public static String[] Syntax = { "/pot pot <Shows the current pot>", "/pot buyin <Adds the item you are holding to the pot>",
			"/pot time <Shows when the next lotto is>" };

	private final String invName = ChatColor.RED + "¬Lottery Pot¬";
	
	public void setORIGINAL_POT_TIME(int oRIGINAL_POT_TIME) {
		ORIGINAL_POT_TIME = oRIGINAL_POT_TIME;
	}
	
	public void setValues()
	{
		this.setORIGINAL_POT_TIME(this.getValueCfg().getInt("POT_LENGTH"));
		this.setSleepTime(Long.parseLong(this.getValueCfg().getString("DELAY_BETWEEN_NEW_POT")));
		
	}

	public Inventory getInventory() {
		return inventory;
	}
	
	public Config getValueCfg() {
		return valueCfg;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public static int getPotTask() {
		return i;
	}

	public static void setI(int i) {
		PotLotto.i = i;
	}

	public HashMap<UUID, List<ItemStack>> getPlayersAndItems() {
		return playersAndItems;
	}

	public int getChance() {
		return chance;
	}
	
	public void setChance(int newChance) {
		this.chance = newChance;
	}

	public int getWinningsWorth() {
		return winningsWorth;
	}

	public void setWinningsWorth(int winningsWorth) {
		this.winningsWorth = winningsWorth;
	}

	public Long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(Long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public int getTimePerPot() {
		return timePerPot;
	}

	public void setTimePerPot(int timePerPot) {
		this.timePerPot = timePerPot;
	}

	public static String[] getSyntax() {
		return Syntax;
	}

	public static void setSyntax(String[] syntax) {
		Syntax = syntax;
	}

	public boolean isBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	public static String getPrefix() {
		return prefix;
	}

	public int getORIGINAL_POT_TIME() {
		return ORIGINAL_POT_TIME;
	}

	public String getInvName() {
		return invName;
	}

	public static void setInstance(PotLotto instance) {
		PotLotto.instance = instance;
	}

	private boolean broadcast = true;

	public HashMap<Material, Integer> getItemsAndPrices() {
		return itemsAndPrices;
	}
	
	public int getPotWorth() {
		return winningsWorth;
	}

	public HashMap<Material, Boolean> getItemsAndAloud() {
		return itemsAndAloud;
	}

	public static PotLotto getInstance() {
		return instance;
	}

	public Inventory getPotInventory() {
		return inventory;
	}

	public List<ItemStack> getPotContents() {
		return potContents;
	}

	private final void setCommandExecuters() {
		this.getCommand("pot").setExecutor(new LotteryCommand());
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public Config getDataCfg() {
		return config;
	}

	@Override
	public void onEnable() {
		if (instance == null) {
			instance = this;
		}

		configManager = new ConfigManager(this);
		config = configManager.getNewConfig("Settings.yml",
				new String[] { "This is where all prices are stored for all items for LottoPot" });
		this.getDataCfg().saveConfig();
		
		valueCfg = configManager.getNewConfig("Config.yml");
		
		try {
			
			this.setValues();
			
		} catch (Exception ex)
		{
			this.getValueCfg().set("POT_LENGTH", this.ORIGINAL_POT_TIME);
			this.getValueCfg().set("DELAY_BETWEEN_NEW_POT", this.sleepTime);
		}
		
		this.getValueCfg().saveConfig();

		this.setConfig();
		setCommandExecuters();
		this.getServer().getPluginManager().registerEvents(this, this);
		this.startNewPot();
		this.loadCfg();
		
		this.setTimePerPot(this.getORIGINAL_POT_TIME());
		
		this.getLogger().log(Level.FINE, "Sleep value: " + this.getSleepTime());
		this.getLogger().log(Level.FINE, "Pot delay: " + this.getORIGINAL_POT_TIME());
	}

	public void setConfig() {
		for (Material material : Material.values()) {
			this.getDataCfg().set("Prices. " + material.name(), 100);
			this.getDataCfg().set("Allowed. " + material.name(), true);
		}

		this.getDataCfg().saveConfig();
	}

	public void loadCfg() {
		for (Material material : Material.values()) {
			this.getItemsAndPrices().put(material, this.getDataCfg().getInt("Price. " + material.name()));
		}

		this.getLogger().log(Level.INFO, "SUCCESSFULLY IMPORTED PRICE DATA FOR ITEMS");
	}

	@Override
	public void onDisable() {

	}

	public String untilNextPot() {
		if (this.timePerPot > 1200) {
			return String.valueOf(
					TimeUnit.SECONDS.toMinutes(Integer.parseInt(getSecondsFromTicks(this.timePerPot))) + " minutes.");
		} else {
			return String.valueOf(this.getSecondsFromTicks(this.timePerPot) + " seconds");
		}

	}

	public String getSecondsFromTicks(int value) {
		String time = null;

		int seconds = value / 20;

		time = String.valueOf(seconds);

		return time;
	}

	public boolean isLottoPlayer(Player player) {
		return this.playersAndItems.containsKey(player.getUniqueId());
	}
	
	public Player randomise(List<Player> p)
	{
		Player player;
		player = (Player) p.toArray()[ThreadLocalRandom.current().nextInt(p.size())];
		
		return player;
	}

	public Player firePot() {
		Player player = null;

		List<Player> possibleWinner = new ArrayList<Player>();

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (this.isLottoPlayer(p)) {
				possibleWinner.add(p);
			}
		}
		
		for (int i = 0; i < 7; i++)
		{
			player = randomise(possibleWinner);
		}
		
		if (!(player.isOnline()))
		{
			player = randomise(possibleWinner);
		}
		
		
		try {
			
			for (ItemStack item : this.getPotContents()) {
				ItemStack removeLore = item;
				ItemMeta im = removeLore.getItemMeta();
				im.setLore(null);
				removeLore.setItemMeta(im);
				player.getInventory().addItem(removeLore);
			}
			
			player.sendMessage(prefix + ChatColor.RED + "You have been awarded your winnings!");
			
		} catch (Exception ex) { }
		
		this.setChance(possibleWinner.size());

		return player;
	}

	@SuppressWarnings("deprecation")
	public void startNewPot() {

		if (this.getInventory() != null) {
			this.setInventory(null);
		}

		this.setInventory(this.getServer().createInventory(null, 54, this.getInvName()));
		this.setChance(0);
		this.setWinningsWorth(0);
		this.setTimePerPot(this.getORIGINAL_POT_TIME());
		this.getPotContents().clear();
		this.getPotInventory().clear();
		this.getPlayersAndItems().clear();

		Bukkit.broadcastMessage(prefix + ChatColor.BOLD + "A new pot is starting! Type /pot for more information.");

		i = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {

				timePerPot -= 20;

				switch (getSecondsFromTicks(timePerPot)) {
				case "60":
					Bukkit.broadcastMessage(prefix + "The pot is ending in " + ChatColor.RED + "1 minute!");
					break;
				case "5":
					Bukkit.broadcastMessage(
							prefix + "The pot is ending in " + ChatColor.RED + "5" + ChatColor.AQUA + " seconds!");
					break;
				case "4":
					Bukkit.broadcastMessage(
							prefix + "The pot is ending in " + ChatColor.RED + "4" + ChatColor.AQUA + " seconds!");
					break;
				case "3":
					Bukkit.broadcastMessage(
							prefix + "The pot is ending in " + ChatColor.RED + "3" + ChatColor.AQUA + " seconds!");
					break;
				case "2":
					Bukkit.broadcastMessage(
							prefix + "The pot is ending in " + ChatColor.RED + "2" + ChatColor.AQUA + " seconds!");
					break;
				case "1":
					Bukkit.broadcastMessage(
							prefix + "The pot is ending in " + ChatColor.RED + "1" + ChatColor.AQUA + " seconds!");
					break;
				case "0":

					Bukkit.getScheduler().cancelTask(i);

					if ((getPotContents().size() == 0) && playersAndItems.size() == 0) {
						Bukkit.broadcastMessage(prefix + ChatColor.RED
								+ "There were no winners this time. No players entered the pot.");
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						startNewPot();
					} else {
						
						String winner = "";
						
						try {
							
							winner = firePot().getDisplayName();
							
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
																		
						Bukkit.broadcastMessage(prefix + ChatColor.RED + winner + ChatColor.AQUA
								+ " won the pot containing " + ChatColor.RED + getPotContents().size() + ChatColor.AQUA
								+ " items" + ChatColor.RED + " ($" + winningsWorth + " worth) " + ChatColor.AQUA
								+ ". A total of " + ChatColor.RED + playersAndItems.keySet().size() + ChatColor.AQUA
								+ " players competed. They had a " + ChatColor.RED + "1" + ChatColor.AQUA + " in "
								+ ChatColor.RED + chance + ChatColor.AQUA + " chance of winning");
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						startNewPot();
					}

				}

			}

		}, 20, 20);

	}

	public void addItemsToPot(ItemStack item, Player player) {

		List<ItemStack> items = new ArrayList<ItemStack>();

		if (!(this.getDataCfg().getBoolean("Allowed. " + item.getType()))) {
			player.sendMessage(prefix + "This item is not allowed to be added to the pot!");
			return;
		}

		if (this.getPlayersAndItems().get(player.getUniqueId()) != null) {
			items = this.getPlayersAndItems().get(player.getUniqueId());

			items.add(item);
			this.getPlayersAndItems().replace(player.getUniqueId(), this.playersAndItems.get(player.getUniqueId()), items);

		} else {
			items.add(item);
			this.getPlayersAndItems().put(player.getUniqueId(), items);
		}

		this.getPotContents().add(item);
		this.winningsWorth += this.getDataCfg().getInt("Prices. " + item.getType().name());
		this.getPotInventory().addItem(item);
		player.getInventory().remove(item);
		player.sendMessage(
				prefix + "You have added your " + ChatColor.RED + item.getType() + ChatColor.AQUA + " to the pot!");

		if (broadcast) {
			this.getServer().broadcastMessage(
					prefix + ChatColor.GREEN + player.getDisplayName() + ChatColor.AQUA + " added an item to the pot!");
		}
	}

	public boolean isPotInventory(Inventory inventory) {
		return inventory.getName() == this.getInventory().getName();
	}

	public void showPot(Player player) {

		if (this.getPotInventory() == null) {
			player.sendMessage(prefix + "There is no pot active!");
			return;
		}

		player.openInventory(this.getPotInventory());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {

		if (this.isPotInventory(event.getInventory())) {
			event.setCancelled(true);
		}
	}

}
