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
    private int decayTaskId;
    private long aliveCount;
    
    public DecayManager(Main plugin, SpleefStorageUtil storageUtil, SpleefRound spleefRound) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
        this.spleefRound = spleefRound;
    }
    
    public void start() {
        startDecayTask();
    }
    
    public void stop() {
        cancelAllTasks();
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(decayTaskId);
    }
    
    private void startDecayTask() {
        List<DecayStage> stages = storageUtil.getStages();
        this.decayTaskId = new BukkitRunnable() {
            private final Random random = new Random();
            private int currentStageIndex = 0;
            private DecayStage currentStage = stages.get(currentStageIndex);
            private int secondsLeft = currentStage.duration();
            @Override
            public void run() {
                // if time is up, or num of living players is below threshold
                // the final stage will continue on forever, regardless of the value of the duration or minParticipants
                if ((secondsLeft <= 0 || aliveCount < currentStage.minParticipants())) {
                    // if this is not the final stage in the list
                    if (currentStageIndex + 1 < stages.size()) {
                        // move to the next stage
                        currentStageIndex++;
                        currentStage = stages.get(currentStageIndex);
                        secondsLeft = currentStage.duration();
                        if (currentStage.startMessage() != null) {
                            spleefRound.messageAllParticipants(Component.text(currentStage.startMessage())
                                    .color(NamedTextColor.DARK_RED));
                        }
                        return;
                    }
                    // otherwise, the final stage continues indefinitely
                }
                secondsLeft--;
                
                for (DecayStage.LayerInfo layerInfo : currentStage.layerInfos()) {
                    BoundingBox decayLayer = storageUtil.getDecayLayers().get(layerInfo.index());
                    decayLayer(decayLayer, layerInfo.blocksPerSecond());
                }
            }
            
            /**
             *
             * @param decayLayer the area to decay within
             * @param blocks the number of blocks to decay
             */
            private void decayLayer(BoundingBox decayLayer, int blocks) {
                List<Block> coarseDirtBlocks = getCoarseDirtBlocks(decayLayer);
                List<Block> dirtBlocks = getDirtBlocks(decayLayer);
                
                // Decay coarse dirt blocks to air
                if (!coarseDirtBlocks.isEmpty()) {
                    for (int i = 0; i < blocks; i++) {
                        Block randomCoarseDirtBlock = coarseDirtBlocks.get(random.nextInt(coarseDirtBlocks.size()));
                        randomCoarseDirtBlock.setType(Material.AIR);
                    }
                }
                
                // Decay dirt blocks to coarse dirt
                if (!dirtBlocks.isEmpty()) {
                    for (int i = 0; i < blocks; i++) {
                        Block randomDirtBlock = dirtBlocks.get(random.nextInt(dirtBlocks.size()));
                        randomDirtBlock.setType(storageUtil.getDecayBlock());
                    }
                }
            }
            
            private List<Block> getDirtBlocks(BoundingBox layer) {
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
            
            private List<Block> getCoarseDirtBlocks(BoundingBox layer) {
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
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void setAliveCount(long aliveCount) {
        this.aliveCount = aliveCount;
    }
}
