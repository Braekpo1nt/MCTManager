package org.braekpo1nt.mctmanager.games.game.example.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.boundingbox.BlockBoxRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRenderer;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActiveState extends ExampleStateBase {
    
    private final @Nullable Timer flyTimer;
    private @Nullable Timer playingTimer;
    private int flyTaskId;
    private final BoundingBox noFlyZone;
    private final BoundingBoxRenderer noFlyZoneRenderer;
    
    public ActiveState(ExampleGame context) {
        super(context);
        Vector s = context.getConfig().getStartingLocation().toVector();
        this.noFlyZone = new BoundingBox(
                s.getX() - 5,
                s.getY(),
                s.getZ() - 5,
                s.getX() + 5,
                s.getY() + 70,
                s.getZ() + 5
        );
        this.noFlyZoneRenderer = BlockBoxRenderer.builder()
                .world(context.getConfig().getWorld())
                .boundingBox(noFlyZone)
                .blockData(Material.RED_STAINED_GLASS.createBlockData())
                .build();
        Audience.audience(
                Audience.audience(context.getParticipants().values()),
                Audience.audience(context.getAdmins())
        ).sendMessage(Component.empty()
                .append(Component.text("The ActiveState has begun")));
        flyTimer = context.getTimerManager().start(Timer.builder()
                        .duration(10)
                        .withSidebar(context.getSidebar(), "timer")
                        .withSidebar(context.getAdminSidebar(), "timer")
                        .sidebarPrefix(Component.text("Flying in: "))
                        .onCompletion(() -> {
                            noFlyZoneRenderer.show();
                            for (ExampleParticipant participant : context.getParticipants().values()) {
                                participant.teleport(participant.getLocation().add(new Vector(0, 100, 0)));
                            }
                            flyTaskId = context.getPlugin().getServer().getScheduler().runTaskLater(context.getPlugin(), () -> {
                                for (ExampleParticipant participant : context.getParticipants().values()) {
                                    participant.getPlayer().setGliding(true);
                                    participant.setGliding(true);
                                }
                            }, 10L).getTaskId();
                            playingTimer = context.getTimerManager().start(Timer.builder()
                                    .duration(60)
                                    .withSidebar(context.getSidebar(), "timer")
                                    .withSidebar(context.getAdminSidebar(), "timer")
                                    .sidebarPrefix(Component.text("Playing: "))
                                    .onCompletion(() -> {
                                        noFlyZoneRenderer.hide();
                                        context.setState(new GameOverState(context));
                                    })
                                    .build());
                        })
                .build());
    }
    
    @Override
    public void cleanup() {
        if (flyTimer != null) {
            flyTimer.cancel();
        }
        if (playingTimer != null) {
            playingTimer.cancel();
        }
        context.getPlugin().getServer().getScheduler().cancelTask(flyTaskId);
        noFlyZoneRenderer.hide();
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ExampleParticipant participant) {
        if (participant.isGliding()) {
            handleGliding(event, participant);
            return;
        }
        int fromY = event.getFrom().getBlockY();
        int toY = event.getTo().getBlockY();
        int diff = toY - fromY;
        if (diff < 1) {
            return;
        }
        context.awardPoints(participant, context.getConfig().getJumpScore());
        Main.logf("%s jumped %d block(s)", participant.getName(), diff);
    }
    
    private void handleGliding(@NotNull PlayerMoveEvent event, @NotNull ExampleParticipant participant) {
        if (shouldStopGliding(participant)) {
            participant.getPlayer().setGliding(false); // this has to come first, or the setGliding will trigger the canceled event
            participant.setGliding(false);
            participant.sendMessage("Landed");
            return;
        }
        
        Location to = event.getTo();
        Location from = event.getFrom();
        if (!noFlyZone.contains(to.toVector())) {
            Main.logf("old dist=%s", from.distance(to));
            return;
        }
        Vector closestToTo = closestPointOnRectangle(
                noFlyZone.getMinX(), noFlyZone.getMinZ(),
                noFlyZone.getMaxX(), noFlyZone.getMaxZ(),
                to.x(), to.z()
        ).toVector(to.y());
        Vector dir = closestToTo.clone().subtract(from.toVector()).normalize();
        Vector newToVec = from.toVector().add(dir);
        
        Location newToLoc = new Location(
                to.getWorld(),
                newToVec.getX(),
                to.y(),
                newToVec.getZ(),
                to.getYaw(),
                to.getPitch()
        );
        Main.logf("old dist=%s new dist=%s", from.distance(to), from.distance(newToLoc));
        event.setTo(newToLoc);
        
    }
    
    public record Point2D(double x, double z) {
        public Vector toVector(double y) {
            return new Vector(x, y, z);
        }
    }
    
    public static Point2D closestPointOnRectangle(double minX, double minZ,
                                                  double maxX, double maxZ,
                                                  double tx, double tz) {
        double distLeft   = tx - minX;
        double distRight  = maxX - tx;
        double distTop    = tz - minZ;
        double distBottom = maxZ - tz;
        
        double minDist = Math.min(Math.min(distLeft, distRight),
                Math.min(distTop, distBottom));
        
        if (minDist == distLeft) {
            return new Point2D(minX, tz);
        } else if (minDist == distRight) {
            return new Point2D(maxX, tz);
        } else if (minDist == distTop) {
            return new Point2D(tx, minZ);
        } else {
            return new Point2D(tx, maxZ);
        }
    }
    
    public boolean shouldStopGliding(@NotNull ExampleParticipant participant) {
        Location location = participant.getLocation();
        Location solidBlockBelow = BlockPlacementUtils.getSolidBlockBelow(location);
        if (solidBlockBelow.equals(location)) {
            return false;
        }
        return !(solidBlockBelow.distance(location) > 1);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ExampleParticipant participant) {
        // do nothing
        Main.logf("%s was damaged %s", participant.getName(), event.getDamage());
    }
    
    @Override
    public void onParticipantToggleGlide(EntityToggleGlideEvent event, ExampleParticipant participant) {
        if (!participant.isGliding()) {
            return;
        }
        event.setCancelled(true);
    }
}
