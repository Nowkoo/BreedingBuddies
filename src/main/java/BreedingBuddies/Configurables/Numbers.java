package BreedingBuddies.Configurables;

import org.bukkit.configuration.file.FileConfiguration;

public class Numbers {
	public static int maxFriendshipAndGenetics;
    public static int initialGeneticMax;
    public static int friendshipByFeeding;
    public static int friendshipByCaring;
    public static int friendshipLostNotFeeding;
    public static int friendshipLostNotCaring;
    public static int maxAnimalsPerChunk;
    public static int maxIrlDaysOut;
    public static int maxIrlDaysChunkAbandoned;
    public static double geneticVarianceMultiplier;
    public static double friendshipInfluenceMultiplier;
    public static double geneticDivisorMultiplierToRetardProgression;
    public static double hoursBetweenRewards;
    public static int startingDayTime;
    public static int minMountHealth;
    public static double minMountSpeed;

    public static double minMountJump;
    public static int maxMountHealth;
    public static double maxMountSpeed;

    public static double maxMountJump;
    public static double mountNerfDivisor;
    public static boolean nerfMountStats;

    public static void loadConfig(FileConfiguration config) {
        maxFriendshipAndGenetics = config.getInt("maxFriendshipAndGenetics");
        initialGeneticMax = config.getInt("initialGeneticMax");
        friendshipByFeeding = config.getInt("friendshipByFeeding");
        friendshipByCaring = config.getInt("friendshipByCaring");
        friendshipLostNotFeeding = config.getInt("friendshipLostNotFeeding");
        friendshipLostNotCaring = config.getInt("friendshipLostNotCaring");
        maxAnimalsPerChunk = config.getInt("maxAnimalsPerChunk");
        maxIrlDaysOut = config.getInt("maxIrlDaysOut");
        maxIrlDaysChunkAbandoned = config.getInt("maxIrlDaysChunkAbandoned");
        geneticVarianceMultiplier = config.getDouble("geneticVarianceMultiplier");
        friendshipInfluenceMultiplier = config.getDouble("friendshipInfluenceMultiplier");
        geneticDivisorMultiplierToRetardProgression = config.getDouble("geneticDivisorMultiplierToRetardProgression");
        hoursBetweenRewards = config.getDouble("hoursBetweenRewards");
        startingDayTime = config.getInt("startingDayTime");
        mountNerfDivisor = config.getDouble("mountNerfDivisor");
        nerfMountStats = config.getBoolean("nerfMountStats");
    }
}
