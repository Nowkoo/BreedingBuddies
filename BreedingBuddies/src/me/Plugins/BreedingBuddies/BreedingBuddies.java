package me.Plugins.BreedingBuddies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Plugins.BreedingBuddies.Configurables.CustomItems;
import me.Plugins.BreedingBuddies.Configurables.Messages;
import me.Plugins.BreedingBuddies.Configurables.Numbers;
import me.Plugins.BreedingBuddies.Events.DayChangeEvent;
import me.Plugins.BreedingBuddies.Listeners.AnimalCareListener;
import me.Plugins.BreedingBuddies.Listeners.AnimalDeathListener;
import me.Plugins.BreedingBuddies.Listeners.AnvilRenameListener;
import me.Plugins.BreedingBuddies.Listeners.BreedingListener;
import me.Plugins.BreedingBuddies.Listeners.BundleCollector;
import me.Plugins.BreedingBuddies.Listeners.BundleOpener;
import me.Plugins.BreedingBuddies.Listeners.ChunkCreation;
import me.Plugins.BreedingBuddies.Listeners.DayChangeListener;
import me.Plugins.BreedingBuddies.Listeners.FarmReport;
import me.Plugins.BreedingBuddies.Listeners.TamingListener;
import me.Plugins.BreedingBuddies.Listeners.InventoryManager;

public class BreedingBuddies extends JavaPlugin {private FileConfiguration bundlesConfig = null;
    private File bundlesFile = null;
    private FileConfiguration itemsConfig = null;
    private File itemsFile = null;
    private FileConfiguration messagesConfig = null;
    private File messagesFile = null;
    private FileConfiguration numbersConfig = null;
    private File numbersFile = null;

    @Override
    public void onEnable() {
    	PluginData.loadAllData();
        createAndLoadConfigs();
        registerListeners();
        new DayChangeScheduler(this, Numbers.startingDayTime).startScheduler();
        this.getCommand("breedingbuddies").setExecutor(this);
        startAutoSaveTask();
    }

    @Override
    public void onDisable() {
//    	Cleanser.removeDisappearedAnimals();
    	PluginData.saveAllData();
    }
    
    private void startAutoSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                PluginData.saveAllData();
            }
        }.runTaskTimer(this, 0, 20 * 60 * 10);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("breedingbuddies")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                createAndLoadConfigs();
                sender.sendMessage(ChatColor.GREEN + "[BreedingBuddies]" + ChatColor.YELLOW + " Config reloaded successfully!");
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("stablechunk")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Chunk chunk = player.getLocation().getChunk();
                    boolean isStable = ChunkManager.isStableChunk(chunk);
                    sender.sendMessage(ChatColor.GREEN + "Stable chunk: " + isStable);
                    return true;
                }
            } else if (args.length > 0 && args[0].equalsIgnoreCase("changeday")) {
                Bukkit.getServer().getPluginManager().callEvent(new DayChangeEvent());
                sender.sendMessage(ChatColor.GREEN + "[BreedingBuddies]" + ChatColor.YELLOW + " Day changed!");
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("spawnanimal")) {
                if (args.length == 4 && sender instanceof Player) {
                    String entityType = args[1].toUpperCase();
                    int friendship = Integer.parseInt(args[2]);
                    int genetics = Integer.parseInt(args[3]);

                    if (!bundlesConfig.contains(entityType)) {
                        sender.sendMessage(ChatColor.RED + "Invalid entity type specified in config.");
                        return false;
                    }

                    Player player = (Player) sender;
                    EntityType type = EntityType.valueOf(entityType);
                    Entity entity = player.getWorld().spawnEntity(player.getLocation(), type);
                    UUID entityUuid = entity.getUniqueId();
                    
                    FarmAnimal farmAnimal = new FarmAnimal(entityUuid, "???", player.getUniqueId(), genetics, friendship);
                    farmAnimal.setState(AnimalStates.SPAWNED);
                    UnownedAnimalsManager.addUnownedAnimal(farmAnimal);
                    sender.sendMessage(ChatColor.GREEN + "[BreedingBuddies]" + ChatColor.YELLOW + " Animal spawned successfully!");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /breedingbuddies spawnanimal <entityType> <friendship> <genetics>");
                    return false;
                } 
            } else if (args.length > 0 && args[0].equalsIgnoreCase("savedata")) {
            	PluginData.saveAllData();
            } else if (args.length > 0 && args[0].equalsIgnoreCase("loaddata")) {
            	PluginData.loadAllData();
            } else if (args.length > 0 && args[0].equalsIgnoreCase("fix")) {

                // Recorrer todos los animales cargados
                for (List<FarmAnimal> animals : PluginData.getPlayerAnimals().values()) {
                    for (FarmAnimal animal : animals) {
                        animal.setCared(false);
                        animal.setFed(false);
                        animal.setState(AnimalStates.HAPPY);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void createAndLoadConfigs() {
        createAndLoadBundleConfig();
        createAndLoadItemConfig();
        createAndLoadMessageConfig();
        createAndLoadNumbersConfig();
        loadConfigs();
        new AnimalUtils(bundlesConfig);
    }

    private void createAndLoadBundleConfig() {
        bundlesFile = new File(getDataFolder(), "bundles.yaml");
        if (!bundlesFile.exists()) {
            createConfigFile(bundlesFile, getDefaultBundleConfig());
        }
        bundlesConfig = YamlConfiguration.loadConfiguration(bundlesFile);
    }

    private void createAndLoadItemConfig() {
        itemsFile = new File(getDataFolder(), "items.yaml");
        if (!itemsFile.exists()) {
            createConfigFile(itemsFile, getDefaultItemsConfig());
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    private void createAndLoadMessageConfig() {
        messagesFile = new File(getDataFolder(), "messages.yaml");
        if (!messagesFile.exists()) {
            createConfigFile(messagesFile, getDefaultMessageConfig());
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void createAndLoadNumbersConfig() {
        numbersFile = new File(getDataFolder(), "numbers.yaml");
        if (!numbersFile.exists()) {
            createConfigFile(numbersFile, getDefaultNumbersConfig());
        }
        numbersConfig = YamlConfiguration.loadConfiguration(numbersFile);
    }

    private void createConfigFile(File file, String content) {
        try {
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfigs() {
        Numbers.loadConfig(numbersConfig);
        Messages.loadConfig(messagesConfig);
        CustomItems.loadConfig(itemsConfig);
        BundleCollector.setBundlesConfig(bundlesConfig);
        BundleOpener.setBundlesConfig(bundlesConfig);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BreedingListener(), this);
        getServer().getPluginManager().registerEvents(new TamingListener(this), this);
        getServer().getPluginManager().registerEvents(new DayChangeListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryManager(), this);
        getServer().getPluginManager().registerEvents(new AnimalCareListener(), this);
        getServer().getPluginManager().registerEvents(new AnimalDeathListener(), this);
        getServer().getPluginManager().registerEvents(new FarmReport(), this);
        getServer().getPluginManager().registerEvents(new ChunkCreation(), this);
        getServer().getPluginManager().registerEvents(new BundleOpener(this), this);
        getServer().getPluginManager().registerEvents(new BundleCollector(this), this);
        getServer().getPluginManager().registerEvents(new AnvilRenameListener(this), this);
        getServer().getPluginManager().registerEvents(new PluginData(), this);
    }

    // Default configurations

    private String getDefaultBundleConfig() {
        return "COW:\n" +
                "  common:\n" +
                "    bundleItem:\n" +
                "      type: \"vanilla\"\n" +
                "      itemID: \"LEATHER\"\n" +
                "    prizes:\n" +
                "      prize1:\n" +
                "        type: \"vanilla\"\n" +
                "        itemID: \"BEEF\"\n" +
                "        amount: 5\n" +
                "        weight: 15\n" +
                "      prize2:\n" +
                "        type: \"vanilla\"\n" +
                "        itemID: \"MILK_BUCKET\"\n" +
                "        amount: 1\n" +
                "        weight: 5\n" +
                "  rare:\n" +
                "    bundleItem:\n" +
                "      type: \"mmoitem\"\n" +
                "      mmoitemType: \"HEAD\"\n" +
                "      mmoitemID: \"COW_HEAD\"\n" +
                "    prizes:\n" +
                "      prize1:\n" +
                "        type: \"mmoitem\"\n" +
                "        mmoitemType: \"MILK_BUCKET\"\n" +
                "        mmoitemID: \"MYTHICAL_MILK\"\n" +
                "        amount: 1\n" +
                "        weight: 2\n";
    }

    private String getDefaultItemsConfig() {
        return "# Item configuration (id of mmoitems allowed)\n" +
                "items:\n" +
                "  tamingItem: \"carrot\"\n" +
                "  coownershipItem: \"paper\"\n" +
                "  universalFeed: \"wheat\"\n" +
                "  caringItem: \"golden_apple\"\n" +
                "  farmReportItem: \"book\"\n" +
                "  stableChunkItem: \"oak_fence\"\n" +
                "  collectorsItem: \"hopper\"";
    }

    private String getDefaultMessageConfig() {
        return "# Messages configuration. % will be the name of the animal.\n" +
                "messages:\n" +
                "  inventoryName: \"Animal Info\"\n" +
                "  nameFirst: \"No name found on item: pick a name using an anvil.\"\n" +
                "  ownerFirst: \"This animal must be tamed before it can be co-owned.\"\n" +
                "  invalidAnimalOrNotFound: \"Invalid animal or not found.\"\n" +
                "  alreadyOwner: \"Already an owner of %s.\"\n" +
                "  nowCoowner: \"Congratulations! You are now a proud co-owner of %s!\"\n" +
                "  ownershipShared: \"The ownership of %s has been successfully shared.\"\n" +
                "  useOnOwnedAnimal: \"Please use this item on an animal you own.\"\n" +
                "  onlyOwnerCanShare: \"Only %s's owner can share its ownership.\"\n" +
                "  nowOwner: \"You have successfully tamed %s and are now its proud owner!\"\n" +
                "  notOwner: \"You aren't the owner of this animal.\"\n" +
                "  alreadyFed: \"%s has already been fed.\"\n" +
                "  alreadyCared: \"%s has already been taken care of.\"\n" +
                "  animalsOut: \"-Some animals have slept outside the farm boundaries. Make sure all are secured to avoid risks.\"\n" +
                "  spaceNeeded: \"-Some of your animals need more space. Consider expanding their areas to improve their well-being.\"\n" +
                "  waterNeeded: \"-There are animals without easy access to water. Ensuring all have sufficient water available is crucial.\"\n" +
                "  animalsFed: \"-Animals fed: %s.\"\n" +
                "  animalsCared: \"-Animals taken care of: %s.\"\n" +
                "  animalsOwned: \"-Total animals owned: %s.\"\n" +
                "  lacksSpace: \"Make space in the inventory first!\"\n" +
                "  stableChunk: \"This chunk is now a stable chunk.\"\n" +
                "  alreadyStableChunk: \"This chunk is already a stable chunk.\"\n" +
                "  alreadyLinked: \"Already linked to %s.\"\n";
    }

    private String getDefaultNumbersConfig() {
        return "# Numbers Configuration\n" +
                "maxFriendshipAndGenetics: 10000 #It will always display 5 hearts on the animal menu, so each heart will be max/5 always.\n" +
                "initialGeneticMax: 20.0 #Maximum of genetic points that animals found in the wild can have.\n" +
                "friendshipByFeeding: 1\n" +
                "friendshipByCaring: 1\n" +
                "friendshipLostNotFeeding: 1\n" +
                "friendshipLostNotCaring: 1\n" +
                "maxAnimalsPerChunk: 10\n" +
                "maxIrlDaysOut: 3\n" +
                "maxIrlDaysChunkAbandoned: 5\n" +
                "geneticVarianceMultiplier: 1.0 #Set to 1 it won't do anything. By changing it, the amount of genetics that babies can improve from parents will be modified.\n" +
                "friendshipInfluenceMultiplier: 1.0 #Default friendship influence when breeding is set to 5%. This value is a multiplier of that 5%.\n" +
                "geneticDivisorMultiplierToRetardProgression: 1.0 #The higher the average genetics of the parents is, the slower genetics will improve on babies when breeding. It multiplies a divisor, never set to 0!!\n" +
                "hoursBetweenRewards: 6\n" +
                "startingDayTime: 12\n";
    }
}
