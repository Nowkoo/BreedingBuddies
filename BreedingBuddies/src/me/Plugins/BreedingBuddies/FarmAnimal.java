package me.Plugins.BreedingBuddies;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;

import me.Plugins.BreedingBuddies.Configurables.Numbers;

public class FarmAnimal {
	private UUID uuid;
    private String name;
    private ArrayList<UUID> ownersUuids = new ArrayList<>();
	private int geneticPoints;
    private AnimalStates state;
    private boolean fed = false;
    private boolean cared = false;
    private boolean sleptInStable = true;
    private boolean spaceNeeded = false;
    private boolean waterNeeded = false;
    private int daysOut = 0;
    private Date lastRewardCollected;

    public FarmAnimal(UUID uuid, String name, UUID uuidOwner) {
        this.uuid = uuid;
        this.name = name;
        this.ownersUuids.add(uuidOwner);
        this.geneticPoints = new Random().nextInt(Numbers.initialGeneticMax);
        this.state = AnimalStates.HAPPY;
    }
    
    public FarmAnimal(UUID uuid, String name, UUID uuidOwner, int genetics) {
        this.uuid = uuid;
        this.name = name;
        this.ownersUuids.add(uuidOwner);
        this.geneticPoints = genetics;
        this.state = AnimalStates.HAPPY;
    }
    
    public FarmAnimal(UUID uuid, String name, UUID uuidOwner, int genetics, int friendship) {
        this.uuid = uuid;
        this.name = name;
        this.geneticPoints = genetics;
        this.friendshipPoints = friendship;
        this.state = AnimalStates.UNOWNED;
    }

    public FarmAnimal(UUID uuid, int geneticPoints) {
        this.uuid = uuid;
        this.name = "???";
        this.geneticPoints = geneticPoints;
        this.state = AnimalStates.UNOWNED;
    }
    
    public FarmAnimal (UUID animalUUID, String name, List<UUID> owners) {
    	this.uuid = animalUUID;
    	this.name = name;
    	this.ownersUuids = (ArrayList<UUID>) owners;
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
        
        if (friendshipPoints > Numbers.maxFriendshipAndGenetics) {
        	friendshipPoints = Numbers.maxFriendshipAndGenetics;
        }
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

    public boolean isSleptInStable() {
		return sleptInStable;
	}

	public void setSleptInStable(boolean sleptInStable) {
		this.sleptInStable = sleptInStable;
	}

	public int getDaysOut() {
        return daysOut;
    }

    public void incrementDaysOut() {
        daysOut++;
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

    public String timeForNextReward() {
    	if (state != AnimalStates.HAPPY) {
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

	public boolean isAnimalHappy() {
		if (!sleptInStable) return false;
		return fed && cared && !waterNeeded && !spaceNeeded;
	}

	public boolean isSpaceNeeded() {
		return spaceNeeded;
	}

	public void setSpaceNeeded(boolean spaceNeeded) {
		this.spaceNeeded = spaceNeeded;
	}

	public boolean isWaterNeeded() {
		return waterNeeded;
	}

	public void setWaterNeeded(boolean waterNeeded) {
		this.waterNeeded = waterNeeded;
	}
	

//    private boolean isAdultAnimal() {
//        if (entity instanceof Ageable) {
//            Ageable ageable = (Ageable) entity;
//            return ageable.getAge() >= 0;
//        }
//        return false;
//    }
}