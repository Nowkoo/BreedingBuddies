package BreedingBuddies.Listeners;

import BreedingBuddies.*;
import BreedingBuddies.Configurables.CustomItems;
import BreedingBuddies.Configurables.Messages;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityMountEvent;

public class MountListener implements Listener {
    @EventHandler
    public void onMount(EntityMountEvent event) {
        Entity entity =  event.getMount();
        Player player = (Player) event.getEntity();
        if (entity instanceof Animals) {
            //se cancela el evento si el caballo tiene dueño y no es el que interacciona
            if (OwnershipManager.hasOwner(entity.getUniqueId()) && !OwnershipManager.isOwner(player.getUniqueId(), entity.getUniqueId())) {
                SoundManager.playAngryCowSound(entity);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if(event.getHand().equals(EquipmentSlot.HAND)) {
            if (MountUtils.isFullStatMount(entity)
                    && ItemUtils.getItemId(itemInHand).equalsIgnoreCase(CustomItems.neuterItem)
                    && OwnershipManager.isOwner(player.getUniqueId(), entity.getUniqueId())) {

                AbstractHorse abstractHorse = (AbstractHorse) entity;
                FarmAnimal animal = OwnershipManager.getAnimal(entity.getUniqueId(), player.getUniqueId());

                if (animal != null && !abstractHorse.isAdult() && !MountUtils.isNeutered(entity)) {
                    SoundManager.playFoalScream(entity);
                    MountUtils.setNeuteredFlag(entity, true);
                    event.setCancelled(true);
                }
            }
        }
    }
}
