package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Responsible for the decay of blocks over time in spleef
 */
public class DecayManager implements Listener {
    
    private final Main plugin;
    private final SpleefStorageUtil storageUtil;
    private final SpleefRound spleefRound;
    private final Random random = new Random();
    private int decayTaskId;
    /**
     * the number of participants who are alive
     */
    private long aliveCount;
    /**
     * the percent of participants who are alive
     */
    private double alivePercent;
    /**
     * the index of the currently active stage
     */
    private int currentStageIndex;
    /**
     * the currently active stage
     */
    private DecayStage currentStage;
    /**
     * the seconds left in the stage
     */
    private int secondsLeft;
    
    public DecayManager(Main plugin, SpleefStorageUtil storageUtil, SpleefRound spleefRound) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
        this.spleefRound = spleefRound;
    }
    
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        currentStageIndex = -1;
        currentStage = null;
        secondsLeft = 0;
        startNextStage();
        startDecayTask();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        currentStageIndex = 0;
        currentStage = null;
        secondsLeft = 0;
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(decayTaskId);
    }
    
    private void startDecayTask() {
        this.decayTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                // if time is up, or num of living players is below threshold
                // the final stage will continue on forever, regardless of the value of the duration or minParticipants
                if ((secondsLeft <= 0 || aliveCount < currentStage.getMinParticipants()) || alivePercent < currentStage.getMinParticipantsPercent()) {
                    if (hasNextStage()) {
                        startNextStage();
                        return;
                    }
                    // otherwise, the final stage continues indefinitely
                }
                secondsLeft--;
                
                for (DecayStage.LayerInfo layerInfo : currentStage.getLayerInfos()) {
                    randomlyRemoveDecayingBlocks(layerInfo.getDecayingBlocks(), layerInfo.getDecayingBlockRate());
                    randomlyDecaySolidBlocks(layerInfo.getSolidBlocks(), layerInfo.getDecayingBlocks(), layerInfo.getSolidBlockRate());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * @return true if there is a next stage, false if the current stage is the last stage
     */
    private boolean hasNextStage() {
        return currentStageIndex + 1 < storageUtil.getConfig2().getStages().size();
    }
    
    /**
     * starts the next decay stage
     */
    private void startNextStage() {
        if (currentStage != null) {
            currentStage.clearBlocks();
        }
        currentStageIndex++;
        currentStage = storageUtil.getConfig2().getStages().get(currentStageIndex);
        for (DecayStage.LayerInfo layerInfo : currentStage.getLayerInfos()) {
            BoundingBox decayLayer = storageUtil.getConfig2().getDecayLayers().get(layerInfo.getIndex());
            layerInfo.setSolidBlocks(BlockPlacementUtils.getBlocks(storageUtil.getConfig2().getWorld(), decayLayer, Collections.singletonList(storageUtil.getConfig2().getLayerBlock())));
            layerInfo.setDecayingBlocks(BlockPlacementUtils.getBlocks(storageUtil.getConfig2().getWorld(), decayLayer, Collections.singletonList(storageUtil.getConfig2().getDecayBlock())));
        }
        secondsLeft = currentStage.getDuration();
        if (currentStage.getStartMessage() != null) {
            spleefRound.messageAllParticipants(Component.text(currentStage.getStartMessage())
                    .color(NamedTextColor.DARK_RED));
        }
        spleefRound.setShouldGivePowerups(currentStage.shouldGivePowerups());
    }
    
    /**
     * Changes n blocks in the given list to the decaying material, where n is the given count. Each randomly chosen block is removed from the solidBlocks list and added to the decayingBlocks list.
     * @param solidBlocks the list to decay a random subset of
     * @param decayingBlocks the list to add the decaying block to
     * @param count how many blocks to decay from the given list. If count is less than solidBlocks.size(), , then all the blocks that are left will be decayed. 
     */
    private void randomlyDecaySolidBlocks(List<Block> solidBlocks, List<Block> decayingBlocks, int count) {
        for (int i = 0; i < Math.min(count, solidBlocks.size()); i++) {
            int indexToDecay = random.nextInt(solidBlocks.size());
            Block newDecayingBlock = solidBlocks.get(indexToDecay);
            newDecayingBlock.setType(storageUtil.getConfig2().getDecayBlock());
            solidBlocks.remove(indexToDecay);
            decayingBlocks.add(newDecayingBlock);
        }
    }
    
    /**
     * Changes n blocks in the given list to air, where n is the given count. Each randomly chosen block is removed from the decayingBlocks list.
     * @param decayingBlocks the list to remove a random subset of
     * @param count how many blocks to decay from the given list. If count is less than decayingBlocks.size(), then all the blocks that are left will be removed. 
     */
    private void randomlyRemoveDecayingBlocks(List<Block> decayingBlocks, int count) {
        for (int i = 0; i < Math.min(count, decayingBlocks.size()); i++) {
            int indexToDecay = random.nextInt(decayingBlocks.size());
            Block randomCoarseDirtBlock = decayingBlocks.get(indexToDecay);
            randomCoarseDirtBlock.setType(Material.AIR);
            decayingBlocks.remove(indexToDecay);
        }
    }
    
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (currentStage == null) {
            return;
        }
        Block block = event.getBlock();
        Material blockType = block.getType();
        if (!blockType.equals(storageUtil.getConfig2().getDecayBlock()) && !blockType.equals(storageUtil.getConfig2().getLayerBlock())) {
            return;
        }
        Location blockLocation = block.getLocation();
        if (!blockLocation.getWorld().equals(storageUtil.getConfig2().getWorld())) {
            return;
        }
        Vector blockVector = blockLocation.toVector();
        DecayStage.LayerInfo layerBlockIsIn = getLayerBlockIsIn(blockVector);
        if (layerBlockIsIn == null) {
            return;
        }
        if (blockType.equals(storageUtil.getConfig2().getLayerBlock())) {
            layerBlockIsIn.getSolidBlocks().add(block);
            return;
        }
        if (blockType.equals(storageUtil.getConfig2().getDecayBlock())) {
            layerBlockIsIn.getDecayingBlocks().add(block);
        }
    }
    
    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        if (currentStage == null) {
            return;
        }
        Block block = event.getBlock();
        Material blockType = block.getType();
        if (!blockType.equals(storageUtil.getConfig2().getDecayBlock()) && !blockType.equals(storageUtil.getConfig2().getLayerBlock())) {
            return;
        }
        Location blockLocation = block.getLocation();
        if (!blockLocation.getWorld().equals(storageUtil.getConfig2().getWorld())) {
            return;
        }
        Vector blockVector = blockLocation.toVector();
        DecayStage.LayerInfo layerBlockIsIn = getLayerBlockIsIn(blockVector);
        if (layerBlockIsIn == null) {
            return;
        }
        if (blockType.equals(storageUtil.getConfig2().getLayerBlock())) {
            layerBlockIsIn.getSolidBlocks().remove(block);
            return;
        }
        if (blockType.equals(storageUtil.getConfig2().getDecayBlock())) {
            layerBlockIsIn.getDecayingBlocks().remove(block);
        }
    }
    
    /**
     * Checks if the given block location is in one of the current stage's decay layers. If it is,
     * then we return that layer. If not, we return null. 
     * @param blockVector the location of the block to check
     * @return the LayerInfo object from the {@link DecayManager#currentStage} which the block is in. Null if the block is not in any of the layers. 
     */
    private @Nullable DecayStage.LayerInfo getLayerBlockIsIn(Vector blockVector) {
        for (DecayStage.LayerInfo layerInfo : currentStage.getLayerInfos()) {
            BoundingBox decayLayer = storageUtil.getConfig2().getDecayLayers().get(layerInfo.getIndex());
            if (decayLayer.contains(blockVector)) {
                return layerInfo;
            }
        }
        return null;
    }
    
    public void setAliveCount(long aliveCount) {
        this.aliveCount = aliveCount;
    }
    
    public void setAlivePercent(double alivePercent) {
        this.alivePercent = alivePercent;
    }
}
