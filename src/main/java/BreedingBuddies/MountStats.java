package BreedingBuddies;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class MountStats {
    public final double minHealth, maxHealth, minSpeed, maxSpeed, minJump, maxJump;
    public static final double defMinHealth = 15, defMaxHealth = 30, defMinSpeed = 0.1125, defMaxSpeed = 0.23, defMinJump =0.4, defMaxJump = 0.6;
    private static FileConfiguration bundlesConfig;
    private static FileConfiguration numbersConfig;

    public MountStats(double minHealth, double maxHealth,
                      double minSpeed, double maxSpeed,
                      double minJump, double maxJump) {
        this.minHealth = minHealth;
        this.maxHealth = maxHealth;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.minJump = minJump;
        this.maxJump = maxJump;
    }

    public static MountStats getMountStats(String animalKey) {
        System.out.println("[DEBUG] bundlesConfig secciones disponibles: " + bundlesConfig.getKeys(true));
        System.out.println("[DEBUG] Buscando sección: " + animalKey + ".mountStats");
        System.out.println("[DEBUG] Existe? " + bundlesConfig.contains(animalKey + ".mountStats"));

        ConfigurationSection section = bundlesConfig.getConfigurationSection(animalKey + ".mountStats");
        if (section == null) {
            return getDefaultMountStats();
        }

        return new MountStats(
                section.getDouble("minHealth", numbersConfig.getDouble("defaultMinMountHealth", defMinHealth)),
                section.getDouble("maxHealth", numbersConfig.getDouble("defaultMaxMountHealth", defMaxHealth)),
                section.getDouble("minSpeed", numbersConfig.getDouble("defaultMinMountSpeed", defMinSpeed)),
                section.getDouble("maxSpeed", numbersConfig.getDouble("defaultMaxMountSpeed", defMaxSpeed)),
                section.getDouble("minJump", numbersConfig.getDouble("defaultMinMountJump", defMinJump)),
                section.getDouble("maxJump", numbersConfig.getDouble("defaultMaxMountJump", defMaxJump))
        );
    }

    public static MountStats getDefaultMountStats() {
        ConfigurationSection section = numbersConfig.getConfigurationSection("defaultMountStats");
        if (section == null) return new MountStats(
                defMinHealth, defMaxHealth, defMinSpeed, defMaxSpeed, defMinJump, defMaxJump
        );

        return new MountStats(
                section.getDouble("defaultMinMountHealth", defMinHealth),
                section.getDouble("defaultMaxMountHealth", defMaxHealth),
                section.getDouble("defaultMinMountSpeed", defMinSpeed),
                section.getDouble("defaultMaxMountSpeed", defMaxSpeed),
                section.getDouble("defaultMinMountJump", defMinJump),
                section.getDouble("defaultMaxMountJump", defMaxJump)
        );
    }

    public static void setConfigs(FileConfiguration bundles, FileConfiguration numbers) {
        bundlesConfig = bundles;
        numbersConfig = numbers;
    }

    public double getMinHealth() {
        return minHealth;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMinJump() {
        return minJump;
    }

    public double getMaxJump() {
        return maxJump;
    }
}
