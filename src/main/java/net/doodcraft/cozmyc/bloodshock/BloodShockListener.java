package net.doodcraft.cozmyc.bloodshock;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class BloodShockListener implements Listener {

    @EventHandler
    public void onActivate(AbilityStartEvent event) {
        if (BloodShock.bloodBent.containsKey(event.getAbility().getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (BloodShock.bloodBent.containsKey(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to == null) return;
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setTo(from.setDirection(to.getDirection()));
            }
        }
    }

    @EventHandler
    public void onBendingReload(BendingReloadEvent event) {
        Collection<CoreAbility> abilities = ElementalAbility.getAbilitiesByInstances();
        for (CoreAbility ability : abilities) {
            if (ability.getName().equals("BloodShock")) {
                ((BloodShock) ability).releaseBloodbentEntities(false);
                ability.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (noPermission(player)) return;
        if (event.isSneaking()) {
            new BloodShock(player);
        } else {
            Collection<CoreAbility> abilities = ElementalAbility.getAbilitiesByInstances();
            for (CoreAbility ability : abilities) {
                if (ability.getName().equals("BloodShock") && ability.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId())) {
                    BloodShock bloodShock = (BloodShock) ability;
                    bloodShock.handleReleaseShift();
                    BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
                    bPlayer.addCooldown(ability);
                }
            }
        }
    }

    private boolean noPermission(Player player) {
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("BloodShock")) return true;
        boolean useGlobalConfig = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Cozmyc.BloodShock.UseBloodbendingAbilityConfig");
        boolean onlyAtNight = ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight") && useGlobalConfig;
        boolean onlyDuringFullMoon = ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon") && useGlobalConfig;
        boolean isFullMoon = ElementalAbility.isFullMoon(player.getWorld());
        boolean isDay = ElementalAbility.isDay(player.getWorld());
        if (!isFullMoon && onlyDuringFullMoon) return true;
        if (isDay && onlyAtNight) return true;
        return BloodShock.isInProtectedRegion(player, player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        Collection<CoreAbility> abilities = ElementalAbility.getAbilitiesByInstances();
        for (CoreAbility ability : abilities) {
            if (ability.getName().equals("BloodShock") && ability.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                BloodShock bloodShock = (BloodShock) ability;
                bloodShock.handleClick();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Collection<CoreAbility> abilities = ElementalAbility.getAbilitiesByInstances();
        for (CoreAbility ability : abilities) {
            if (ability.getName().equals("BloodShock") && ability.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                BloodShock bloodShock = (BloodShock) ability;
                bloodShock.releaseBloodbentEntities(false);
                bloodShock.remove();
            }
        }

        for (Map.Entry<UUID, UUID> entry : BloodShock.bloodBent.entrySet()) {
            if (entry.getValue().equals(player.getUniqueId())) {
                LivingEntity entity = (LivingEntity) player.getServer().getEntity(entry.getKey());
                if (entity != null) {
                    entity.setGravity(true);
                    if (!(entity instanceof Player)) {
                        entity.setAI(true);
                    }
                }
                BloodShock.bloodBent.remove(entry.getKey());
            }

            if (entry.getKey().equals(player.getUniqueId())) {
                player.setGravity(true);
                BloodShock.bloodBent.remove(entry.getKey());
            }
        }
    }
}
