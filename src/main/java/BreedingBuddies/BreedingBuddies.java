package BreedingBuddies;

import BreedingBuddies.Configurables.CustomItems;
import BreedingBuddies.Configurables.Messages;
import BreedingBuddies.Configurables.Numbers;
import BreedingBuddies.Events.DayChangeEvent;
import BreedingBuddies.Listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BreedingBuddies extends JavaPlugin {private FileConfiguration bundlesConfig = null;
    private static BreedingBuddies instance;
    private File bundlesFile = null;
    private FileConfiguration itemsConfig = null;
    private File itemsFile = null;
    private FileConfiguration messagesConfig = null;
    private File messagesFile = null;
    private FileConfiguration numbersConfig = null;
    private File numbersFile = null;

    @Override
    public void onEnable() {
    	PluginData.loadStableChunks();
        createAndLoadConfigs();
        registerListeners();
        new DayChangeScheduler(this, Numbers.startingDayTime).startScheduler();
        this.getCommand("breedingbuddies").setExecutor(this);
        startAutoSaveTask();
        instance = this;
    }

    @Override
    public void onDisable() {
//    	Cleanser.removeDisappearedAnimals();
    	PluginData.saveAllData();
    }

    public static BreedingBuddies getInstance() {
        return instance;
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
                    
                    FarmAnimal farmAnimal = new FarmAnimal(entityUuid, entityType, player.getUniqueId(), entity, genetics, friendship);
                    farmAnimal.setState(AnimalStates.SPAWNED);
                    UnownedAnimalsManager.addUnownedAnimal(farmAnimal);
                    sender.sendMessage(ChatColor.GREEN + "[BreedingBuddies]" + ChatColor.YELLOW + " Animal spawned successfully!");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /breedingbuddies spawnanimal <entityType> <friendship> <genetics>");
                    return false;
                } 
            } else if (args.length > 0 && args[0].equalsIgnoreCase("spawnmount")) {
                if (args.length == 6 && sender instanceof Player) {
                    String entityType = args[1].toUpperCase();
                    int friendship = Integer.parseInt(args[2]);
                    double health = Integer.parseInt(args[3]);
                    double speed = Double.parseDouble(args[4]);
                    double jump = Double.parseDouble(args[5]);

                    EntityType type;
                    try {
                        type = EntityType.valueOf(entityType);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid entity type.");
                        return false;
                    }

                    if (!AbstractHorse.class.isAssignableFrom(type.getEntityClass())) {
                        sender.sendMessage(ChatColor.RED + "Entity type must be a mount (like HORSE, DONKEY, etc).");
                        return false;
                    }

                    Player player = (Player) sender;
                    Entity entity = player.getWorld().spawnEntity(player.getLocation(), type);

                    if (!(entity instanceof AbstractHorse horse)) {
                        sender.sendMessage(ChatColor.RED + "Spawned entity is not a mount.");
                        return false;
                    }

                    UUID entityUuid = entity.getUniqueId();
                    FarmAnimal farmAnimal = new FarmAnimal(entityUuid, entityType, player.getUniqueId(), entity, 0, friendship);
                    farmAnimal.setState(AnimalStates.SPAWNED);
                    farmAnimal.setFriendshipPoints(friendship);
                    UnownedAnimalsManager.addUnownedAnimal(farmAnimal);

                    // Aplica stats
                    horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                    horse.setHealth(health);
                    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
                    horse.setJumpStrength(jump);
                    horse.addScoreboardTag("bb.isNerfed");

                    sender.sendMessage(ChatColor.GREEN + "[BreedingBuddies]" + ChatColor.YELLOW + " Animal spawned successfully!");
                    return true;

                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /breedingbuddies spawnmount <entityType> <friendship> <health> <speed> <jump>");
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
        numbersFile = new File(getDataFolder(), "config.yaml");
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
        FarmAnimal.setBundlesConfig(bundlesConfig);
        BundleOpener.setBundlesConfig(bundlesConfig);
        MountStats.setConfigs(bundlesConfig, numbersConfig);
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
        getServer().getPluginManager().registerEvents(new MountListener(), this);
        getServer().getPluginManager().registerEvents(new NerfMountListener(), this);
    }

    // Default configurations

    private String getDefaultBundleConfig() {
        return """
# ========== HORSE CONFIGURATION ==========
HORSE:
  mountStats:                     # Stats applied when used as a mount
    minHealth: 15                 # Minimum possible health
    maxHealth: 30                 # Maximum possible health
    minSpeed: 0.1125              # Minimum movement speed
    maxSpeed: 0.3375              # Maximum movement speed
    minJump: 0.4                  # Minimum jump height
    maxJump: 1.0                  # Maximum jump height
  bundlesEnabled: false           # Horses do not drop bundles

# ========== COW CONFIGURATION ==========
COW:
  bundlesEnabled: true            # Bundles can be obtained from cows
  bundles:

    # ----- Common Bundle -----
    common:
      bundleItem:                 # The item given to the player
        type: "mmoitem"           # Item comes from MMOItems
        mmoitemType: "PETS"
        mmoitemID: "COMMON_ANIMAL_BUNDLE"
      prizes:                     # Possible rewards from this bundle
        prize1:
          type: "vanilla"         # Minecraft vanilla item
          itemID: "BEEF"
          amount: 4
          weight: 50              # Probability weight
        prize2:
          type: "mmoitem"
          mmoitemType: "MATERIALS"
          mmoitemID: "LEATHER"
          amount: 4
          weight: 50

    # ----- Rare Bundle -----
    rare:
      bundleItem:
        type: "mmoitem"
        mmoitemType: "PETS"
        mmoitemID: "RARE_ANIMAL_BUNDLE"
      prizes:
        prize1:
          type: "vanilla"
          itemID: "BEEF"
          amount: 8
          weight: 45
        prize2:
          type: "mmoitem"
          mmoitemType: "MATERIALS"
          mmoitemID: "LEATHER"
          amount: 8
          weight: 45
        prize3:
          type: "mmoitem"
          mmoitemType: "MATERIALS"
          mmoitemID: "RARE_LEATHER"
          amount: 1
          weight: 5
        prize4:
          type: "mmoitem"
          mmoitemType: "MATERIALS"
          mmoitemID: "EPIC_LEATHER"
          amount: 1
          weight: 5
""";
    }

    private String getDefaultItemsConfig() {
        return """
# Configuration of allowed items (vanilla or mmoitems IDs) used for various animal actions
items:
  tamingItem: "TAMING_ITEM"             # Item used to tame an animal.
  coownershipItem: "COOWNERSHIP_ITEM"   # Item to share ownership of an animal with another player.
  universalFeed: "UNIVERSAL_FEED"       # Item used to feed any kind of animal.
  caringItem: "CARING_ITEM"             # Item to care for or pamper an animal.
  farmReportItem: "FARMREPORT_ITEM"    # Item that shows a report or summary of the farm.
  stableChunkItem: "STABLECHUNK_ITEM"  # Item to claim a chunk as a stable (farm animals should spend the night in stable chunks).
  collectorsItem: "COLLECTOR_ITEM"     # Item used to collect rewards from animals, like wool, leather, etc.
  mountStatsItem: "CARROT_ON_A_STICK"  # Item to display mount stats or status.
  neuterItem: "SHEARS"                  # Item to neuter or sterilize animals (e.g., shears).
""";
    }

    private String getDefaultMessageConfig() {
        return """
# Messages configuration. %s will be replaced by the animal's name.
messages:
  inventoryName: "Animal Info"  # Title shown on the animal information screen.
  
  nameFirst: "§6No name found on item: pick a name using an anvil."
    # Shown when the animal item doesn't have a name yet, prompting the player to name it.

  ownerFirst: "§6This animal must be tamed before it can be co-owned."
    # Displayed when trying to share ownership but the animal is not tamed yet.

  invalidAnimalOrNotFound: "§6You cannot tame this!"
    # Message when the player attempts to tame an invalid or untamable animal.

  alreadyOwner: "§6Already an owner of %s."
    # Notifies the player they already own the specified animal (%s).

  nowCoowner: "§6Congratulations! You are now a proud co-owner of %s!"
    # Confirmation message when the player becomes a co-owner of the animal.

  ownershipShared: "§6The ownership of %s has been successfully shared."
    # Indicates that the animal's ownership has been shared with another player.

  useOnOwnedAnimal: "§6Please use this item on an animal you own."
    # Informs the player that the item they are trying to use requires ownership of the animal.

  onlyOwnerCanShare: "§6Only %s's owner can share its ownership."
    # Warns that only the main owner can share the ownership of the animal.

  nowOwner: "§6You have successfully tamed %s and are now its proud owner!"
    # Confirmation that the player tamed the animal and became its owner.

  notOwner: "§6You aren't the owner of this animal."
    # Shown when a player tries to perform an owner-only action on an animal they don't own.

  alreadyFed: "§6%s has already been fed."
    # Indicates the animal has already received food for this period.

  alreadyCared: "§6%s has already been taken care of."
    # Indicates the animal has already received care (e.g., petting) for this period.

  animalsOut: "§7§oSome animals have slept outside the farm boundaries. Make sure to secure them."
    # Warning that some animals are outside the designated farm area.

  spaceNeeded: "§7§oSome of your animals need more space. Consider expanding your farm plot."
    # Suggestion to increase farm space because animals are crowded.

  waterNeeded: "§7§oThere are animals without easy access to water."
    # Warning that some animals lack proper water supply.

  animalsFed: "§e-§oAnimals fed:§r %s."
    # Report of how many animals have been fed.

  animalsCared: "§e-§oAnimals taken care of:§r %s."
    # Report of how many animals have been cared for.

  animalsOwned: "§e-§oTotal animals owned:§r %s."
    # Displays the total count of animals owned.

  lacksSpace: "§6Make space in the inventory first!"
    # Warning that the player's inventory is full and space is needed.

  stableChunk: "§6This chunk is now a stable chunk."
    # Confirmation that a game chunk is designated as a stable (farm) area.

  alreadyStableChunk: "§6This chunk is already a stable chunk."
    # Message if trying to set a chunk as stable but it already is.

  alreadyLinked: "§6Already linked to %s."
    # Indicates that the animal or object is already linked to the specified target.

  neuteredHorse: "§6Neutered horses can't breed."
    # Info message that horses that are neutered cannot breed.
""";
    }

    private String getDefaultNumbersConfig() {
        return """
# ========== GENERAL BALANCE ==========
maxFriendshipAndGenetics: 10000   # Total points for friendship/genetics. Always shows 5 hearts (max/5 = points per heart).
initialGeneticMax: 200            # Max genetic points wild animals can start with.

# ========== INTERACTION ==========
friendshipByFeeding: 200          # Friendship gained by feeding.
friendshipByCaring: 200           # Friendship gained by caring (e.g., petting).
friendshipLostNotFeeding: 50      # Friendship lost for not feeding regularly.
friendshipLostNotCaring: 50       # Friendship lost for not caring/interacting.

# ========== ENVIRONMENT LIMITS ==========
maxAnimalsPerChunk: 20            # Max animals allowed per chunk.
maxIrlDaysOut: 3                  # Real-life days before animal starts losing friendship due to not sleeping in a farm plot.
maxIrlDaysChunkAbandoned: 5       # Number of real-life days a farm chunk can stay empty before it stops being considered a farm chunk.

# ========== BREEDING & GENETICS ==========
geneticVarianceMultiplier: 0.4    # Modifies how much genetics can vary from parents. 1 = no effect.
friendshipInfluenceMultiplier: 0.4 # Scales how much parents' friendship affects baby genetics (base is 5% of average friendship).
geneticDivisorMultiplierToRetardProgression: 0.4  # Higher parent stats = slower baby improvement. Multiplies divisor (never set to 0!).

# ========== REWARDS & TIME ==========
hoursBetweenRewards: 8            # Real-life hours between reward eligibility.
startingDayTime: 21               # In-game time at day start (21 = night).

# ========== MOUNT RELATED ==========
defaultMountStats:
  minHealth: 15
  maxHealth: 30
  minSpeed: 0.1125
  maxSpeed: 0.3375
  minJump: 0.4
  maxJump: 1.0

nerfMountStats: false             # Whether to nerf mount stats automatically.
mountNerfDivisor: 2.0             # Applied if nerfMountStats is true; divides the stats.
""";
    }
}
