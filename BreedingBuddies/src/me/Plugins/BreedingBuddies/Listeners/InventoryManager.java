package me.Plugins.BreedingBuddies.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Plugins.BreedingBuddies.AnimalStates;
import me.Plugins.BreedingBuddies.FarmAnimal;
import me.Plugins.BreedingBuddies.OwnershipManager;
import me.Plugins.BreedingBuddies.PluginData;
import me.Plugins.BreedingBuddies.UnownedAnimalsManager;
import me.Plugins.BreedingBuddies.Configurables.Messages;
import me.Plugins.BreedingBuddies.Configurables.Numbers;

import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryManager implements Listener {    
    @EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		
		if (event.getRightClicked() instanceof Animals 
				&& event.getPlayer().isSneaking()
				&& itemInHand.getType() == Material.AIR) {
			
			FarmAnimal animal = OwnershipManager.getAnimal(entity.getUniqueId(), player.getUniqueId());
			if (animal != null) {
				openAnimalMenu(player, animal);
			} else {
				animal = UnownedAnimalsManager.getAnimal(entity.getUniqueId());
				if (animal != null) {
					openAnimalMenu(player, animal);
				} else if (player.hasPermission("breedingbuddies.use")) {
                    animal = OwnershipManager.getAnimal(entity.getUniqueId());
                    if (animal != null) {
                        openAnimalMenu(player, animal);
                    }
                }
			}
		}
	}
	
    public void openAnimalMenu(Player player, FarmAnimal farmAnimal) {
        String animalName = farmAnimal.getName();
        AnimalStates animalStatus = farmAnimal.getState();
        int friendshipPoints = farmAnimal.getFriendshipPoints();
        int geneticPoints = farmAnimal.getGeneticPoints();

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + Messages.inventoryName);

        // Primera fila: Nombre y Estado
        inv.setItem(3, createItem(Material.NAME_TAG, ChatColor.GOLD + "Name: " + animalName));
        inv.setItem(4, createItem(Material.CHEST, ChatColor.GOLD + "Next bundle: " + farmAnimal.timeForNextReward()));
        inv.setItem(5, createItem(Material.TOTEM_OF_UNDYING, ChatColor.GOLD + "State: " + animalStatus.toString()));

        // Segunda fila: Puntos de amistad
        int maxFriendshipPoints = Numbers.maxFriendshipAndGenetics;
        int friendshipPerHeart = maxFriendshipPoints / 5; // 5 corazones en total
        for (int i = 0; i < 5; i++) {
            if (friendshipPoints >= (i + 1) * friendshipPerHeart) {
                inv.setItem(11 + i, createItem(Material.RED_DYE, ChatColor.RED + "Friendship: " + friendshipPoints + "/" + maxFriendshipPoints));
            } else {
                inv.setItem(11 + i, createItem(Material.GRAY_DYE, ChatColor.RED + "Friendship: " + friendshipPoints + "/" + maxFriendshipPoints));
            }
        }

        // Tercera fila: Puntos de genética
        int maxGeneticPoints = Numbers.maxFriendshipAndGenetics;
        int geneticPerHeart = maxGeneticPoints / 5;
        for (int i = 0; i < 5; i++) {
            if (geneticPoints >= (i + 1) * geneticPerHeart) {
                inv.setItem(20 + i, createItem(Material.SLIME_SPAWN_EGG, ChatColor.YELLOW + "Genetics: " + geneticPoints + "/" + maxGeneticPoints));
            } else {
                inv.setItem(20 + i, createItem(Material.VEX_SPAWN_EGG, ChatColor.YELLOW + "Genetics: " + geneticPoints + "/" + maxGeneticPoints));
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    	if (event.getClickedInventory() == null) {
            return;
        }
    	
    	if (event.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + Messages.inventoryName)) {
            if (event.getClickedInventory().getSize() == 27) {
            	event.setCancelled(true);
            }
        }
    }
}
