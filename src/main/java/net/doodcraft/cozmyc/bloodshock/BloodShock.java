package net.doodcraft.cozmyc.bloodshock;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BloodShock extends BloodAbility implements AddonAbility {

    public static final Map<UUID, UUID> bloodBent = new ConcurrentHashMap<>();
    public static final Iterable<EntityType> bloodless = Bloodbending.getBloodlessEntities();

    private long cooldown;
    private boolean controlMovements;
    private long duration;
    private double liftHeight;
    private double range;
    private double throwPower;
    private double throwHeight;

    private AbilityState currentState;
    private boolean expired;
    private Set<Entity> lifted;
    private Map<LivingEntity, Vector> relativePositions;

    public BloodShock(Player player) {
        super(player);

        if (this.bPlayer.canBloodbend() && !this.bPlayer.isOnCooldown(this)) {
            initializeFields();
            start();
        }
    }

    private void initializeFields() {
        this.cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Cozmyc.BloodShock.Cooldown");
        this.controlMovements = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Cozmyc.BloodShock.ControlMovements");
        this.duration = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Cozmyc.BloodShock.Duration");
        this.liftHeight = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.BloodShock.LiftHeight");
        this.range = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.BloodShock.Range");
        this.throwPower = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.BloodShock.ThrowPower");
        this.throwHeight = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.BloodShock.ThrowHeight");

        this.currentState = AbilityState.START;
        this.expired = false;
        this.lifted = new HashSet<>();
        this.relativePositions = new ConcurrentHashMap<>();

        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
            if (this.currentState != AbilityState.STOP) {
                if (this.duration > 0) this.expired = true;
            }
        }, (int) (20 * (this.duration / 1000)));
    }

    public static boolean isInProtectedRegion(Player player, Entity entity) {
        if (entity == null || entity.isDead()) return true;

        return RegionProtection.isRegionProtected(player, entity.getLocation());
    }

    @Override
    public String getInstructions() {
        return ConfigManager.defaultConfig.get().getString("ExtraAbilities.Cozmyc.BloodShock.Language.Instructions");
    }

    @Override
    public String getDescription() {
        return ConfigManager.defaultConfig.get().getString("ExtraAbilities.Cozmyc.BloodShock.Language.Description");
    }

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new BloodShockListener(), ProjectKorra.plugin);

        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.ControlMovements", true);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.Cooldown", 5000);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.Duration", 6000);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.LiftHeight", 2.5);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.ThrowPower", 2.0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.ThrowHeight", 0.7);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.Range", 10);

        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.NightOnly", true);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.FullMoonOnly", false);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.UndeadMobs", true);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.OtherBloodbenders", false);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.Bloodless", false);

        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.Language.Description", "Extremely skilled bloodbenders have demonstrated the ability of taking full control of any living being in their surrounding area. This ability grants the user mass crowd control, allowing them to forcefully make entities mimic the bender's movements until they are lifted into the air and launched away upon release.");
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.BloodShock.Language.Instructions", "Hold Shift to activate, Left Click to lift entities, release Shift to launch.");

        ConfigManager.defaultConfig.save();
    }

    @Override
    public void stop() {
    }

    @Override
    public String getAuthor() {
        return "LuxaelNi, Cozmyc";
    }

    @Override
    public String getVersion() {
        return "1.0.6";
    }

    @Override
    public void progress() {
        if (shouldRemoveAbility()) {
            releaseBloodbentEntities(false);
            this.bPlayer.addCooldown(this);
            remove();
        } else {
            if (this.currentState == AbilityState.CLICKED) {
                liftBloodbentEntities();
            } else {
                bloodbendEntitiesInRange();
                if (this.controlMovements) updateBloodbentEntitiesPosition();
            }
            removeOutOfRangeEntities();
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return this.cooldown;
    }

    @Override
    public String getName() {
        return "BloodShock";
    }

    @Override
    public Location getLocation() {
        return this.player.getLocation();
    }

    private void removeOutOfRangeEntities() {
        Iterator<Map.Entry<UUID, UUID>> iterator = bloodBent.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();

            LivingEntity entity = (LivingEntity) Bukkit.getEntity(entry.getKey());
            if (entity == null || entity.isDead()) {
                iterator.remove();
                continue;
            }

            UUID playerId = entry.getValue();
            if (playerId.equals(this.player.getUniqueId())
                    && this.player.getLocation().distance(entity.getLocation()) > this.range) {
                releaseBloodbentEntity(entity, false);
                iterator.remove();
            }
        }
    }

    private boolean shouldRemoveAbility() {
        if (this.expired || this.player == null || !this.player.isOnline() || !this.player.isSneaking()) {
            this.currentState = AbilityState.STOP;
            return true;
        }

        if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)
                || !this.bPlayer.getBoundAbilityName().equalsIgnoreCase("BloodShock")) {
            this.currentState = AbilityState.STOP;
            return true;
        }

        return false;
    }

    private void liftBloodbentEntities() {
        List<UUID> remove = new ArrayList<>();

        for (UUID uuid : bloodBent.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) {
                remove.add(uuid);
                continue;
            }

            if (!this.lifted.contains(entity)) {
                if (bloodBent.get(uuid).equals(this.player.getUniqueId())) {
                    Location currentLocation = entity.getLocation().clone();
                    double targetY = currentLocation.getY() + this.liftHeight;
                    while (currentLocation.getY() < targetY) {
                        currentLocation.setY(currentLocation.getY() + 1);
                        Location headLocation = currentLocation.clone().add(0, 1, 0);
                        if (headLocation.getBlock().getType().isSolid()) {
                            break;
                        }
                        moveEntity(entity, currentLocation);
                    }
                    this.lifted.add(entity);
                }
            }
        }

        for (UUID uuid : remove) {
            bloodBent.remove(uuid);
        }
    }

    private void bloodbendEntitiesInRange() {
        List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.range);

        boolean canBendUndead = ConfigManager.defaultConfig.get()
                .getBoolean("ExtraAbilities.Cozmyc.BloodShock.UndeadMobs");

        boolean canBendOtherBloodbenders = ConfigManager.defaultConfig.get()
                .getBoolean("ExtraAbilities.Cozmyc.BloodShock.OtherBloodbenders");

        boolean canBendBloodless = ConfigManager.defaultConfig.get()
                .getBoolean("ExtraAbilities.Cozmyc.BloodShock.Bloodless");

        for (Entity entity : entities) {
            if (entity == null) continue;
            if (entity.isDead()) continue;
            if (!(entity instanceof LivingEntity)) continue;

            UUID uuid = entity.getUniqueId();

            if (bloodBent.containsKey(uuid)) continue;
            if (uuid.equals(this.player.getUniqueId())) continue;
            if (!canBendBloodless && isBloodless(entity)) continue;
            if (!canBendUndead && GeneralMethods.isUndead(entity)) continue;
            if (isInProtectedRegion(this.player, entity)) continue;

            double distance = entity.getLocation().distance(this.player.getLocation());
            if (distance > this.range - 0.5) continue;

            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;
                BendingPlayer targetBendingPlayer = BendingPlayer.getBendingPlayer(targetPlayer);

                if (Commands.invincible.contains(targetPlayer.getName())) continue;
                if (targetPlayer.getGameMode() == GameMode.SPECTATOR || targetPlayer.getGameMode() == GameMode.CREATIVE) continue;
                if (targetBendingPlayer.canBloodbend() && !canBendOtherBloodbenders) continue;
                if (!targetBendingPlayer.canBeBloodbent()) continue;
            }

            bloodbendEntity((LivingEntity) entity);
        }

        removeOutOfRangeEntities();
    }

    public boolean isBloodless(Entity entity) {
        if (entity == null) return true;

        EntityType entityType = entity.getType();

        for (EntityType type : bloodless) {
            if (type.name().equals(entityType.name())) {
                return true;
            }
        }

        return false;
    }

    private void bloodbendEntity(LivingEntity entity) {
        if (entity == null || entity.isDead()) return;

        if (bloodBent.containsKey(entity.getUniqueId())) return;
        bloodBent.put(entity.getUniqueId(), this.player.getUniqueId());

        DamageHandler.damageEntity(entity, this.player, 0.0, this);

        entity.setGravity(false);

        if (!(entity instanceof Player)) {
            entity.setAI(false);
        } else {
            for (Ability ability : ElementalAbility.getAbilitiesByInstances()) {
                if (ability.getPlayer().getUniqueId() == entity.getUniqueId()) {
                    ability.remove();
                }
            }
        }

        stopEntity(entity);
    }

    private void updateBloodbentEntitiesPosition() {
        Map<LivingEntity, Location> move = new ConcurrentHashMap<>();

        for (Map.Entry<LivingEntity, Vector> entry : this.relativePositions.entrySet()) {
            LivingEntity entity = entry.getKey();

            if (entity == null || entity.isDead()) continue;

            Vector relativePosition = entry.getValue();
            Location targetLocation = this.player.getLocation().clone().add(relativePosition);
            move.put(entity, targetLocation);
        }

        for (Map.Entry<LivingEntity, Location> entry : move.entrySet()) {
            moveEntity(entry.getKey(), entry.getValue());
        }
    }

    private void stopEntity(LivingEntity entity) {
        entity.setVelocity(new Vector(0, 0, 0));

        Location loc = entity.getLocation().clone();
        Vector relativePosition = loc.toVector().subtract(this.player.getLocation().toVector());

        this.relativePositions.put(entity, relativePosition);

        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
            if (this.currentState == AbilityState.STOP || this.currentState == AbilityState.CLICKED) return;

            final Vector vec = new Vector();
            entity.setVelocity(vec);
            loc.setYaw(entity.getLocation().getYaw());
            loc.setPitch(entity.getLocation().getPitch());

            moveEntity(entity, loc);
        }, 1L);
    }

    private void moveEntity(Entity entity, Location loc) {
        Location adjustedLocation = entity.getLocation().clone();

        adjustedLocation.setPitch(loc.getPitch());
        adjustedLocation.setYaw(loc.getYaw());

        entity.teleport(adjustedLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN);

        Location partialLocationX = adjustedLocation.clone();
        partialLocationX.setX(loc.getX());

        if (isSafeLocation(partialLocationX)) {
            adjustedLocation.setX(loc.getX());
        } else {
            Vector relativePosition = adjustedLocation.toVector().subtract(this.player.getLocation().toVector());
            this.relativePositions.put((LivingEntity) entity, relativePosition);
        }

        Location partialLocationZ = adjustedLocation.clone();
        partialLocationZ.setZ(loc.getZ());
        if (isSafeLocation(partialLocationZ)) {
            adjustedLocation.setZ(loc.getZ());
        } else {
            Vector relativePosition = adjustedLocation.toVector().subtract(this.player.getLocation().toVector());
            this.relativePositions.put((LivingEntity) entity, relativePosition);
        }

        Location partialLocationY = adjustedLocation.clone();
        partialLocationY.setY(loc.getY());
        if (isSafeLocation(partialLocationY)) {
            adjustedLocation.setY(loc.getY());
        } else {
            this.relativePositions.remove((LivingEntity) entity);
            Vector relativePosition = adjustedLocation.toVector().subtract(this.player.getLocation().toVector());
            this.relativePositions.put((LivingEntity) entity, relativePosition);
        }

        entity.teleport(adjustedLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    private boolean isSafeLocation(Location location) {
        if (location.getBlock().getType().isSolid() || location.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
            return false;
        }

        double margin = 0.3;

        for (double x = -margin; x <= margin; x += margin) {
            for (double z = -margin; z <= margin; z += margin) {
                Location offsetLocation = location.clone().add(x, 0, z);
                Location offsetLocationAbove = offsetLocation.clone().add(0, 1, 0);

                if (offsetLocation.getBlock().getType().isSolid() || offsetLocationAbove.getBlock().getType().isSolid()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void handleClick() {
        if (currentState == AbilityState.START) {
            this.currentState = AbilityState.CLICKED;
        }
    }

    public void handleReleaseShift() {
        releaseBloodbentEntities(currentState == AbilityState.CLICKED);
        remove();
    }

    public void releaseBloodbentEntities(boolean launch) {
        List<UUID> remove = new ArrayList<>();

        for (UUID uuid : new HashSet<>(bloodBent.keySet())) {
            LivingEntity entity = (LivingEntity) Bukkit.getEntity(uuid);

            if (entity == null || entity.isDead()) {
                remove.add(uuid);
                continue;
            }

            if (bloodBent.get(uuid).equals(this.player.getUniqueId())) {
                releaseBloodbentEntity(entity, launch);
            }
        }

        for (UUID uuid : remove) {
            bloodBent.remove(uuid);
        }

        if (this.lifted != null) this.lifted.clear();
        if (this.relativePositions != null) this.relativePositions.clear();
    }

    private void releaseBloodbentEntity(LivingEntity entity, boolean launch) {
        if (entity == null || entity.isDead()) return;

        entity.setGravity(true);

        if (!(entity instanceof Player)) {
            entity.setAI(true);
        }

        if (launch && this.lifted.contains(entity)) {
            Vector playerVector = this.player.getLocation().toVector();
            Vector direction = entity.getLocation().toVector().subtract(playerVector).normalize().setY(this.throwHeight);
            entity.setVelocity(direction.multiply(this.throwPower));
        }

        UUID uuid = entity.getUniqueId();

        bloodBent.remove(uuid);
        this.lifted.remove(entity);
        this.relativePositions.remove(entity);
    }
}