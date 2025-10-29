package org.braekpo1nt.mctmanager.ui.glow;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.UIManager;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GlowManager extends SimplePacketListenerAbstract implements UIManager {
    
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
         * removes the given target from this viewer
         * @param target the target to remove
         */
        public void removeTarget(UUID target) {
            targets.remove(target);
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
        
        public String debugToString(Map<UUID, PlayerData> playerDataMap) {
            StringBuilder builder = new StringBuilder();
            for (UUID targetUUID : targets) {
                String targetName = playerDataMap.get(targetUUID).getPlayer().getName();
                builder
                        .append(player.getName())
                        .append("->")
                        .append(targetName)
                ;
            }
            return builder.toString();
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
    
    private void unregisterListeners() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
    }
    
    @Override
    public void cleanup() {
        for (PlayerData playerData : playerDatas.values()) {
            Player target = playerData.getPlayer();
            List<EntityData<?>> entityMetadata = getEntityMetadata(target, false);
            for (UUID viewerUUID : playerData.getViewersList()) {
                Player viewer = playerDatas.get(viewerUUID).getPlayer();
                sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
            }
        }
        unregisterListeners();
        mapper.clear();
    }
    
    /**
     * Utility method to create an EntityData with the desired glowing status.
     * For use in an ENTITY_METADATA packet.
     * @param entity the entity which may or may not glow
     * @param glowing whether the entity should be glowing
     * @return an entity metadata which indicates that the given entity is or is not glowing
     */
    private static List<EntityData<?>> getEntityMetadata(Entity entity, boolean glowing) {
        byte trueEntityDataByte = UIUtils.getTrueEntityDataByte(entity, glowing);
        return Collections.singletonList(new EntityData<>(0, EntityDataTypes.BYTE, trueEntityDataByte));
    }
    
    /**
     * Send a new packet ENTITY_METADATA packet to the viewer with the given
     * target entity ID.
     * @param viewer the receiver of the packet
     * @param targetEntityId the entity ID of the entity which should be glowing
     * @param entityMetadata the metadata which includes a flag indicating the glowing status
     */
    private static void sendGlowingPacket(Player viewer, int targetEntityId, List<EntityData<?>> entityMetadata) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                targetEntityId,
                entityMetadata
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
    
    /**
     * Add a participant to this manager to be referenced as a viewer or target
     * @param participant the participant to add to this manager
     * @deprecated use {@link #showPlayer(Participant)}
     */
    @Deprecated
    public void addPlayer(Participant participant) {
        showPlayer(participant.getPlayer());
    }
    
    /**
     * Add a player to this manager to be referenced as a viewer or target
     * @param player the player to add to this manager
     * @deprecated use {@link #showPlayer(Player)}
     */
    @Deprecated
    public void addPlayer(Player player) {
        showPlayer(player);
    }
    
    @Override
    public void showPlayer(@NotNull Player player) {
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
     * Must be a player contained in this manager
     * @param target the target (the Participant who should glow).
     * Must reference a player contained in this manager.
     */
    public void showGlowing(Player viewer, Participant target) {
        showGlowing(viewer.getUniqueId(), target.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewer the viewer (the Participant who should see the target glowing)
     * Must reference a player contained in this manager
     * @param target the target (the Participant who should glow).
     * Must be a player contained in this manager.
     */
    public void showGlowing(Participant viewer, Player target) {
        showGlowing(viewer.getUniqueId(), target.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewer the viewer (the Participant who should see the target glowing)
     * Must reference a player contained in this manager
     * @param target the target (the Participant who should glow).
     * Must reference a player contained in this manager.
     */
    public void showGlowing(Participant viewer, Participant target) {
        showGlowing(viewer.getUniqueId(), target.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewer the viewer (the player who should see the target glowing)
     * Must be a player contained in this manager
     * @param target the target (the player who should glow).
     * Must be a player contained in this manager.
     */
    public void showGlowing(Player viewer, Player target) {
        showGlowing(viewer.getUniqueId(), target.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewerUUID the UUID of the viewer.
     * Must be a player contained in this manager
     * @param targetUUID the UUID of the target (the player who should glow).
     * Must be a player contained in this manager.
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
        
        List<EntityData<?>> entityMetadata = getEntityMetadata(target, true);
        sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
    }
    
    /**
     * Hide the target's glowing effect from the viewer
     * @param viewer the viewer.
     * Must be a player contained in this manager
     * @param target the target (the player who should glow).
     * Must be a player contained in this manager.
     */
    public void hideGlowing(Player viewer, Participant target) {
        hideGlowing(viewer.getUniqueId(), viewer.getName(), target.getUniqueId(), target.getName());
    }
    
    /**
     * Hide the target's glowing effect from the viewer
     * @param viewer the viewer.
     * Must be a player contained in this manager
     * @param target the target (the player who should glow).
     * Must be a player contained in this manager.
     */
    public void hideGlowing(Participant viewer, Participant target) {
        hideGlowing(viewer.getUniqueId(), viewer.getName(), target.getUniqueId(), target.getName());
    }
    
    /**
     * Hide the target's glowing effect from the viewer
     * @param viewerUUID the UUID of the viewer.
     * Must be a player contained in this manager
     * @param targetUUID the UUID of the target (the player who should glow).
     * Must be a player contained in this manager.
     */
    public void hideGlowing(UUID viewerUUID, String viewerName, UUID targetUUID, String targetName) {
        PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
        if (viewerPlayerData == null) {
            UIUtils.logUIError("Viewer %s is not in this manager (UUID %s)", viewerName, viewerUUID);
            return;
        }
        PlayerData targetPlayerData = playerDatas.get(targetUUID);
        if (targetPlayerData == null) {
            UIUtils.logUIError("Target %s is not in this manager (UUID %s)", targetName, targetUUID);
            return;
        }
        
        if (!viewerPlayerData.canSee(targetUUID)) {
            UIUtils.logUIError("Viewer %s doesn't see target %s glowing (viewerUUID=%s, targetUUID=%s)", viewerName, targetName, viewerUUID, targetUUID);
            return;
        }
        // (no need to check the viewers because they are maintained in tandem)
        
        Player target = targetPlayerData.getPlayer();
        Player viewer = viewerPlayerData.getPlayer();
        viewerPlayerData.removeTarget(targetUUID);
        targetPlayerData.removeViewer(viewerUUID);
        
        List<EntityData<?>> entityMetadata = getEntityMetadata(target, false);
        sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
    }
    
    /**
     * Remove the given participant from this manager. They will stop glowing and stop
     * seeing others glow.
     * @param participant the participant to remove
     * @deprecated use {@link #hidePlayer(Participant)}
     */
    @Deprecated
    public void removePlayer(Participant participant) {
        hidePlayer(participant.getPlayer());
    }
    
    /**
     * Remove the given player from this manager. They will stop glowing and stop
     * seeing others glow.
     * @param player the player to remove
     * @deprecated use {@link #hidePlayer(Player)}
     */
    @Deprecated
    public void removePlayer(Player player) {
        hidePlayer(player);
    }
    
    @Override
    public void hidePlayer(@NotNull Player player) {
        PlayerData removedPlayerData = playerDatas.remove(player.getUniqueId());
        if (removedPlayerData == null) {
            UIUtils.logUIError("Player %s does not exist in this manager", player.getName());
            return;
        }
        mapper.remove(player.getEntityId());
        
        Player removedPlayer = removedPlayerData.getPlayer();
        List<EntityData<?>> removedPlayerMetadata = getEntityMetadata(removedPlayer, false);
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
            List<EntityData<?>> targetMetadata = getEntityMetadata(target, false);
            sendGlowingPacket(removedPlayer, target.getEntityId(), targetMetadata);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
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
            UUID targetUUID = mapper.get(entityId);
            if (targetUUID == null) {
                // if the packet entity is not in the mapper, then it's a player in this manager, and we don't need to proceed
                return;
            }
            if (!viewerPlayerData.canSee(targetUUID)) {
                // if the viewer can't see the target's glow effect, then do not proceed
                return;
            }
            List<EntityData<?>> entityMetadata = packet.getEntityMetadata();
            // the unchecked cast warning is suppressed by the annotation on this method
            EntityData<Byte> baseEntityData = (EntityData<Byte>) entityMetadata.stream()
                    .filter(entityData -> entityData.getIndex() == 0 && entityData.getType() == EntityDataTypes.BYTE)
                    .findFirst()
                    .orElse(null);
            if (baseEntityData == null) {
                return;
            }
            // at this point, we're making changes to the packet, so mark it to be re-encoded
            event.markForReEncode(true);
            // if the base entity data is included in this packet, 
            // we need to make sure that the "glowing" flag is set to true
            byte flags = baseEntityData.getValue();
            flags |= (byte) 0x40;
            baseEntityData.setValue(flags);
        }
        if (event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY)) {
            UUID viewerUUID = event.getUser().getUUID();
            PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
            if (viewerPlayerData == null) {
                // if the receiver of the packet is not in this manager, then do not proceed
                return;
            }
            WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);
            int entityId = packet.getEntityId();
            UUID targetUUID = mapper.get(entityId);
            if (targetUUID == null) {
                // if the packet entity is not in the mapper, then it's a player in this manager, and we don't need to proceed
                return;
            }
            if (!viewerPlayerData.canSee(targetUUID)) {
                // if the viewer can't see the target's glow effect, then do not proceed
                return;
            }
            PlayerData targetPlayerData = playerDatas.get(targetUUID);
            if (targetPlayerData == null) {
                // this should never happen, if it does something is wrong
                return;
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                List<EntityData<?>> initialUpdateMetadata = getEntityMetadata(targetPlayerData.getPlayer(), true);
                sendGlowingPacket(viewerPlayerData.getPlayer(), entityId, initialUpdateMetadata);
            });
        }
    }
    
    public String debugToString() {
        StringBuilder builder = new StringBuilder("Viewer->Target:\n");
        for (PlayerData playerData : playerDatas.values()) {
            builder
                    .append(playerData.debugToString(playerDatas))
                    .append("\n")
            ;
        }
        return builder.toString();
    }
}
