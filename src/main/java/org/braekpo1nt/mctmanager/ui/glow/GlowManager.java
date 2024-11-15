package org.braekpo1nt.mctmanager.ui.glow;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class GlowManager extends SimplePacketListenerAbstract {
    
    private final Plugin plugin;
    
    public GlowManager(Plugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class PlayerData {
        @EqualsAndHashCode.Include
        @Getter
        private final Player player;
        /**
         * UUIDs of entities which this player should appear to glow to
         */
        private final Set<UUID> viewers = new HashSet<>();
        /**
         * UUIDs of entities that should appear to glow to this player
         */
        private final Set<UUID> targets = new HashSet<>();
        /**
         * This has a very particular use. When players first log in, the ENTITY_METADATA 
         * packet send automatically by the server does not contain the 0-index byte 
         * data (the glowing information). Thus, the default behavior of 
         * {@link #onPacketPlaySend(PacketPlaySendEvent)} is to ignore the packet. 
         * However, this results in a player who should be glowing, but is not. 
         * If the player crouches (or otherwise updates their base "Entity" metadata) 
         * then they start glowing. 
         * <br>
         * Desired behavior: Players log in and are glowing to their viewers
         * Actual behavior: Players log in and don't glow until they update their 
         * entity data (crouch, sprint, swim, fly with elytra, etc.)
         * <br>
         * Solution: if {@link #onPacketPlaySend(PacketPlaySendEvent)} recieves an 
         * ENTITY_METADATA packet for a given target, it checks to see if the viewer's 
         * PlayerData.requiresInitialUpdate(targetUUID) is true. If so, it sends a 
         * special update to ensure the player is glowing. 
         */
        private final Set<UUID> initiallyUpdatedTargets = new HashSet<>();
        
        /**
         * Add a viewer who should see this target as glowing
         * @param viewer the viewer to add
         */
        public void addViewer(UUID viewer) {
            viewers.add(viewer);
        }
        
        /**
         * Remove a viewer who previously saw this target glowing, and should
         * not anymore
         * @param viewer the viewer to remove
         */
        public void removeViewer(UUID viewer) {
            viewers.remove(viewer);
        }
        
        /**
         * Whether this @param viewer the viewer who may see this glowing
         * @return true if this target can be seen glowing by the given viewer,
         * false otherwise
         */
        public boolean canBeSeen(UUID viewer) {
            return viewers.contains(viewer);
        }
        
        /**
         * @return the UUIDs of the viewers who can see this target glowing
         */
        public Set<UUID> getViewersList() {
            return viewers;
        }
        
        /**
         * Add a target that this viewer should see glowing
         * @param target the target
         */
        public void addTarget(UUID target) {
            targets.add(target);
        }
        
        /**
         * removes the given target from this viewer. This also un-marks their initial update,
         * so that if they are added again they must send their initial update.
         * @param target the target to remove
         */
        public void removeTarget(UUID target) {
            targets.remove(target);
            initiallyUpdatedTargets.remove(target);
        }
        
        /**
         * @return the UUIDs of the targets this viewer should see glowing
         */
        public Set<UUID> getTargetsList() {
            return targets;
        }
        
        /**
         * @param target the target who may be glowing to this viewer
         * @return true if this viewer can see the given target glowing
         */
        public boolean canSee(UUID target) {
            return targets.contains(target);
        }
        
        /**
         * Can be thought of as "is this the first time this viewer is
         * viewing this target?"
         *
         * @param target the target
         * @return true if this target requires an initial update, false otherwise
         */
        public boolean requiresInitialUpdate(UUID target) {
            return !initiallyUpdatedTargets.contains(target);
        }
        
        /**
         * Mark that this target has been initialized for this viewer. Successive calls
         * to {@link #requiresInitialUpdate(UUID)} after this will return false, until
         * the target is removed. 
         * @param target a target that this viewer can see. If this target is not visible
         *               to this viewer, nothing happens.
         */
        public void initialUpdate(UUID target) {
            if (!targets.contains(target)) {
                return;
            }
            initiallyUpdatedTargets.add(target);
        }
        
    }
    
    /**
     * Maps each player's Entity ID to their UUID, so that we don't have to switch threads during
     * the packet listener in order to see which entity it's referencing.
     */
    private final Map<Integer, UUID> mapper = new HashMap<>();
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    public void registerListeners() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }
    
    public void unregisterListeners() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
    }
    
    public void clear() {
        for (PlayerData playerData : playerDatas.values()) {
            Player target = playerData.getPlayer();
            List<EntityData> entityMetadata = getEntityMetadata(target, false);
            for (UUID viewerUUID : playerData.getViewersList()) {
                Player viewer = playerDatas.get(viewerUUID).getPlayer();
                sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
            }
        }
        mapper.clear();
    }
    
    /**
     * Utility method to create an EntityData with the desired glowing status.
     * For use in an ENTITY_METADATA packet.
     * @param entity the entity which may or may not glow
     * @param glowing whether the entity should be glowing
     * @return an entity metadata which indicates that the given entity is or is not glowing
     */
    private static List<EntityData> getEntityMetadata(Entity entity, boolean glowing) {
        byte trueEntityDataByte = UIUtils.getTrueEntityDataByte(entity, glowing);
        return Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, trueEntityDataByte));
    }
    
    /**
     * Send a new packet ENTITY_METADATA packet to the viewer with the given 
     * target entity ID. 
     * @param viewer the receiver of the packet
     * @param targetEntityId the entity ID of the entity which should be glowing
     * @param entityMetadata the metadata which includes a flag indicating the glowing status
     */
    private static void sendGlowingPacket(Player viewer, int targetEntityId, List<EntityData> entityMetadata) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                targetEntityId,
                entityMetadata
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
    
    public void addPlayer(Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            UIUtils.logUIError("Player %s already exists in this manager", player.getName());
            return;
        }
        playerDatas.put(player.getUniqueId(), new PlayerData(player));
        mapper.put(player.getEntityId(), player.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewer the viewer (the player who should see the target glowing)
     *               Must be a player contained in this manager
     * @param target the target (the player who should glow). 
     *               Must be a player contained in this manager.
     */
    public void showGlowing(Player viewer, Player target) {
        showGlowing(viewer.getUniqueId(), target.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewerUUID the UUID of the viewer. 
     *               Must be a player contained in this manager
     * @param targetUUID the UUID of the target (the player who should glow). 
     *               Must be a player contained in this manager.
     */
    public void showGlowing(UUID viewerUUID, UUID targetUUID) {
        PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
        if (viewerPlayerData == null) {
            UIUtils.logUIError("Viewer player with UUID %s is not in this manager", viewerUUID);
            return;
        }
        PlayerData targetPlayerData = playerDatas.get(targetUUID);
        if (targetPlayerData == null) {
            UIUtils.logUIError("Target player with UUID %s is not in this manager", targetUUID);
            return;
        }
        
        if (viewerPlayerData.canSee(targetUUID)) {
            UIUtils.logUIError("Viewer with UUID %s can already see target with UUID %s glowing", viewerUUID, targetUUID);
            return;
        }
        // (no need to check the viewers because they are maintained in tandem)
        
        Player target = targetPlayerData.getPlayer();
        Player viewer = viewerPlayerData.getPlayer();
        viewerPlayerData.addTarget(targetUUID);
        targetPlayerData.addViewer(viewerUUID);
        
        List<EntityData> entityMetadata = getEntityMetadata(target, true);
        sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
    }
    
    /**
     * Hide the target's glowing effect from the viewer
     * @param viewerUUID the UUID of the viewer. 
     *               Must be a player contained in this manager
     * @param targetUUID the UUID of the target (the player who should glow). 
     *               Must be a player contained in this manager.
     */
    public void hideGlowing(UUID viewerUUID, UUID targetUUID) {
        PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
        if (viewerPlayerData == null) {
            UIUtils.logUIError("Viewer player with UUID %s is not in this manager", viewerUUID);
            return;
        }
        PlayerData targetPlayerData = playerDatas.get(targetUUID);
        if (targetPlayerData == null) {
            UIUtils.logUIError("Target player with UUID %s is not in this manager", targetUUID);
            return;
        }
        
        if (!viewerPlayerData.canSee(targetUUID)) {
            UIUtils.logUIError("Viewer with UUID %s doesn't see target with UUID %s glowing", viewerUUID, targetUUID);
            return;
        }
        // (no need to check the viewers because they are maintained in tandem)
        
        Player target = targetPlayerData.getPlayer();
        Player viewer = viewerPlayerData.getPlayer();
        viewerPlayerData.removeTarget(targetUUID);
        targetPlayerData.removeViewer(viewerUUID);
        
        List<EntityData> entityMetadata = getEntityMetadata(target, false);
        sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
    }
    
    /**
     * Remove the given player from this manager. They will stop glowing and stop
     * seeing others glow.
     * @param player the player to remove
     */
    public void removePlayer(Player player) {
        PlayerData removedPlayerData = playerDatas.remove(player.getUniqueId());
        if (removedPlayerData == null) {
            UIUtils.logUIError("Player %s does not exist in this manager", player.getName());
            return;
        }
        mapper.remove(player.getEntityId());
        
        Player removedPlayer = removedPlayerData.getPlayer();
        List<EntityData> removedPlayerMetadata = getEntityMetadata(removedPlayer, false);
        // removed player should no longer glow. iterate through viewers
        // and update their packets:
        UUID removedUUID = removedPlayer.getUniqueId();
        for (UUID viewerUUID : removedPlayerData.getViewersList()) {
            PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
            Player viewer = viewerPlayerData.getPlayer();
            viewerPlayerData.removeTarget(removedUUID);
            sendGlowingPacket(viewer, removedPlayer.getEntityId(), removedPlayerMetadata);
        }
        
        // removed player should no longer see glowing. iterate through targets
        // and update their packets:
        for (UUID targetUUID : removedPlayerData.getTargetsList()) {
            PlayerData targetPlayerData = playerDatas.get(targetUUID);
            Player target = targetPlayerData.getPlayer();
            targetPlayerData.removeViewer(removedUUID);
            List<EntityData> targetMetadata = getEntityMetadata(target, false);
            sendGlowingPacket(removedPlayer, target.getEntityId(), targetMetadata);
        }
    }
    
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            
            UUID viewerUUID = event.getUser().getUUID();
            PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
            if (viewerPlayerData == null) {
                // if the receiver of the packet is not in this manager, then do not proceed
                return;
            }
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            int entityId = packet.getEntityId();
            Player player = event.getPlayer();
            UUID targetUUID = mapper.get(entityId);
            if (targetUUID == null) {
                // if the packet entity is not in the mapper, then it's a player in this manager, and we don't need to proceed
                return;
            }
            if (!viewerPlayerData.canSee(targetUUID)) {
                // if the viewer can't see the target's glow effect, then do not proceed
                return;
            }
            List<EntityData> entityMetadata = packet.getEntityMetadata();
            EntityData baseEntityData = entityMetadata.stream().filter(entityData -> entityData.getIndex() == 0 && entityData.getType() == EntityDataTypes.BYTE).findFirst().orElse(null);
            if (baseEntityData == null) {
                if (viewerPlayerData.requiresInitialUpdate(targetUUID)) {
                    viewerPlayerData.initialUpdate(targetUUID);
                    PlayerData targetPlayerData = playerDatas.get(targetUUID);
                    if (targetPlayerData == null) {
                        // this should never happen, if it does something is wrong
                        return;
                    }
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        List<EntityData> initialUpdateMetadata = getEntityMetadata(targetPlayerData.getPlayer(), true);
                        sendGlowingPacket(viewerPlayerData.getPlayer(), entityId, initialUpdateMetadata);
                    });
                }
                return;
            }
            // at this point, we're making changes to the packet, so mark it to be re-encoded
            event.markForReEncode(true);
            // if the base entity data is included in this packet, 
            // we need to make sure that the "glowing" flag is set to true
            byte flags = (byte) baseEntityData.getValue();
            flags |= (byte) 0x40;
            baseEntityData.setValue(flags);
        }
    }
}
