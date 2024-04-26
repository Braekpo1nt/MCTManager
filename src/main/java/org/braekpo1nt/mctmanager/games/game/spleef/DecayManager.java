package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Responsible for the decay of blocks over time in spleef
 */
public class DecayManager {
    
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
        currentStageIndex = -1;
        currentStage = null;
        secondsLeft = 0;
        startNextStage();
        startDecayTask();
    }
    
    public void stop() {
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
                    decayRandomBlocks(layerInfo.getSolidBlocks(), storageUtil.getDecayBlock(), layerInfo.getBlocksPerSecond());
                    decayRandomBlocks(layerInfo.getDecayingBlocks(), Material.AIR, layerInfo.getBlocksPerSecond());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * @return true if there is a next stage, false if the current stage is the last stage
     */
    private boolean hasNextStage() {
        return currentStageIndex + 1 < storageUtil.getStages().size();
    }
    
    /**
     * starts the next decay stage
     */
    private void startNextStage() {
        if (currentStage != null) {
            currentStage.clearBlocks();
        }
        currentStageIndex++;
        currentStage = storageUtil.getStages().get(currentStageIndex);
        for (DecayStage.LayerInfo layerInfo : currentStage.getLayerInfos()) {
            BoundingBox decayLayer = storageUtil.getDecayLayers().get(layerInfo.getIndex());
            layerInfo.setSolidBlocks(getSolidBlocks(decayLayer));
            layerInfo.setDecayingBlocks(getDecayingBlocks(decayLayer));
        }
        secondsLeft = currentStage.getDuration();
        if (currentStage.getStartMessage() != null) {
            spleefRound.messageAllParticipants(Component.text(currentStage.getStartMessage())
                    .color(NamedTextColor.DARK_RED));
        }
        spleefRound.setShouldGivePowerups(currentStage.shouldGivePowerups());
    }
    
    /**
     * Changes n blocks in the given list to the given material, where n is the given count.. 
     * @param blocks the blocks to decay a random subset of
     * @param to the material to decay the blocks to
     * @param count how many blocks to decay from the given list. If count is less than blocks.size(), then blocks.size() blocks will be decayed.
     */
    private void decayRandomBlocks(List<Block> blocks, Material to, int count) {
        for (int i = 0; i < Math.min(count, blocks.size()); i++) {
            int indexToDecay = random.nextInt(blocks.size());
            Block randomCoarseDirtBlock = blocks.get(indexToDecay);
            randomCoarseDirtBlock.setType(to);
            blocks.remove(indexToDecay);
        }
    }
    
    private List<Block> getSolidBlocks(BoundingBox layer) {
        List<Block> dirtBlocks = new ArrayList<>();
        
        for (int x = layer.getMin().getBlockX(); x <= layer.getMaxX(); x++) {
            for (int y = layer.getMin().getBlockY(); y <= layer.getMaxY(); y++) {
                for (int z = layer.getMin().getBlockZ(); z <= layer.getMaxZ(); z++) {
                    Block block = storageUtil.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == storageUtil.getLayerBlock()) {
                        dirtBlocks.add(block);
                    }
                }
            }
        }
        
        return dirtBlocks;
    }
    
    private List<Block> getDecayingBlocks(BoundingBox layer) {
        List<Block> coarseDirtBlocks = new ArrayList<>();
        
        for (int x = layer.getMin().getBlockX(); x <= layer.getMaxX(); x++) {
            for (int y = layer.getMin().getBlockY(); y <= layer.getMaxY(); y++) {
                for (int z = layer.getMin().getBlockZ(); z <= layer.getMaxZ(); z++) {
                    Block block = storageUtil.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == storageUtil.getDecayBlock()) {
                        coarseDirtBlocks.add(block);
                    }
                }
            }
        }
        
        return coarseDirtBlocks;
    }
    
    public void setAliveCount(long aliveCount) {
        this.aliveCount = aliveCount;
    }
    
    public void setAlivePercent(double alivePercent) {
        this.alivePercent = alivePercent;
    }
}
