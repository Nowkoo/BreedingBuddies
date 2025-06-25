package BreedingBuddies;

import BreedingBuddies.Configurables.Numbers;
import BreedingBuddies.Listeners.BundleCollector;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class FarmAnimal {
	private UUID uuid;
    private String name;
    private ArrayList<UUID> ownersUuids = new ArrayList<>();
    private Entity entity;
	private int geneticPoints;
    private AnimalStates state;
    private boolean fed = false;
    private boolean cared = false;
    private Chunk sleptChunk;
    private int daysOut = 0;
    private Date lastRewardCollected;
    //private Date lastUpdated;

    private static FileConfiguration bundlesConfig;


    public FarmAnimal(UUID uuid, String name, UUID uuidOwner, Entity entity) {
        this.uuid = uuid;
        this.name = name;
        this.ownersUuids.add(uuidOwner);
        this.entity = entity;
        int geneticPoints = new Random().nextInt(Numbers.initialGeneticMax);
        calcGeneticsIfIsMount(entity, geneticPoints);
        this.state = AnimalStates.HAPPY;
    }
    
    public FarmAnimal(UUID uuid, String name, UUID uuidOwner, Entity entity, int genetics) {
        this.uuid = uuid;
        this.name = name;
        this.ownersUuids.add(uuidOwner);
        this.entity = entity;
        calcGeneticsIfIsMount(entity, genetics);
        this.state = AnimalStates.HAPPY;
    }
    
    public FarmAnimal(UUID uuid, String name, UUID uuidOwner, Entity entity, int genetics, int friendship) {
        this.uuid = uuid;
        this.name = "???";
        this.entity = entity;
        calcGeneticsIfIsMount(entity, genetics);
        this.friendshipPoints = friendship;
        this.state = AnimalStates.UNOWNED;
    }

    public FarmAnimal(UUID uuid, int geneticPoints, Entity entity) {
        this.uuid = uuid;
        this.name = "???";
        this.entity = entity;
        calcGeneticsIfIsMount(entity, geneticPoints);
        this.state = AnimalStates.UNOWNED;
    }
    
    public FarmAnimal (UUID animalUUID, String name, List<UUID> owners, Entity entity) {
    	this.uuid = animalUUID;
    	this.name = name;
    	this.ownersUuids = (ArrayList<UUID>) owners;
    	this.entity = entity;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<UUID> getOwnersUuids() {
        return ownersUuids;
    }

    public int getFriendshipPoints() {
        return friendshipPoints;
    }

    public void increaseFriendship(int amount) {
        friendshipPoints += amount;
    }

    public void decreaseFriendship(int amount) {
        friendshipPoints -= amount;
        if (friendshipPoints < 0) {
        	friendshipPoints = 0;
        }
    }

    public boolean getFed() {
        return fed;
    }

    public void setFed(boolean fed) {
        this.fed = fed;
    }

    public boolean getCared() {
        return cared;
    }

    public void setCared(boolean cared) {
        this.cared = cared;
    }

    public AnimalStates getState() {
        return state;
    }

    public void setState(AnimalStates state) {
        this.state = state;
    }

    public int getGeneticPoints() {
        return geneticPoints;
    }

    public Chunk getSleptChunk() {
        return sleptChunk;
    }

    public void setSleptChunk(Chunk sleptChunk) {
        this.sleptChunk = sleptChunk;
    }

    public int getDaysOut() {
        return daysOut;
    }

    public void incrementDaysOut() {
        daysOut++;
    }

    public Entity getEntity() {
        return entity;
    }

    public Date getLastRewardCollected() {
        return lastRewardCollected;
    }

    public void setLastRewardCollected(Date lastRewardCollected) {
        this.lastRewardCollected = lastRewardCollected;
    }

    public boolean canCollectReward() {
    	if (state != AnimalStates.HAPPY) {
        	return false;
        }
    	
        if (lastRewardCollected == null) {
            return true;
        }
        
        long timeSinceLastReward = System.currentTimeMillis() - lastRewardCollected.getTime();
        double hoursSinceLastReward = (double) timeSinceLastReward / (1000 * 60 * 60); // Convertir milisegundos a horas
        return hoursSinceLastReward >= Numbers.hoursBetweenRewards;
    }

    public static void setBundlesConfig(FileConfiguration bundlesConfig) {
        FarmAnimal.bundlesConfig = bundlesConfig;
    }

    public String timeForNextReward() {
        ConfigurationSection animalSection = bundlesConfig.getConfigurationSection(entity.getType().toString());
        if (state != AnimalStates.HAPPY || !animalSection.getBoolean("bundlesEnabled", false)) {
    		return "No bundle";
    	}
        long remainingTimeMillis = timeForNextRewardMillis();
        long hours = TimeUnit.MILLISECONDS.toHours(remainingTimeMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis - TimeUnit.HOURS.toMillis(hours));

        String timeString = String.format("%02d:%02d", hours, minutes);
        return timeString;
    }

    private long timeForNextRewardMillis() {
        if (lastRewardCollected == null) {
            return 0;
        }
        long timeSinceLastReward = System.currentTimeMillis() - lastRewardCollected.getTime();
        long remainingTimeMillis = (long) (Numbers.hoursBetweenRewards * 3600000 - timeSinceLastReward);
        return Math.max(remainingTimeMillis, 0);
    }

    void calcGeneticsIfIsMount(Entity entity, int geneticPoints)
    {
        if (MountUtils.isFullStatMount(entity)) {
            this.geneticPoints = MountUtils.getGenetics(entity);
        } else {
            this.geneticPoints = geneticPoints;
        }
    }

	public void resetDaysOut() {
		daysOut = 0;
	}
	
	private int friendshipPoints = 0;
    public void setFriendshipPoints(int friendshipPoints) {
		this.friendshipPoints = friendshipPoints;
	}

	public void setGeneticPoints(int geneticPoints) {
		this.geneticPoints = geneticPoints;
	}

	public void setDaysOut(int daysOut) {
		this.daysOut = daysOut;
	}

    public void setNeutered(Entity entity) {
        entity.getScoreboardTags().add("bb.isNeutered");
    }
    public boolean isNeutered(Entity entity) {
        return entity.getScoreboardTags().contains("bb.isNeutered");
    }
}