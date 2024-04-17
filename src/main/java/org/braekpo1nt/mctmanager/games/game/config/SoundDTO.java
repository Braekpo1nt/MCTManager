package org.braekpo1nt.mctmanager.games.game.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;


@Getter
public class SoundDTO {
    /**
     * Is the resource location of the sound to play. Can be a built-in minecraft sound or a resource pack sound. See sound parameter explanation of default minecraft /playsound command
     */
    private NamespacedKeyDTO namespacedKey;
    /**
     * see volume parameter explanation of default minecraft /playsound command (defaults to 1)
     */
    private float volume = 1;
    /**
     * see pitch parameter explanation of default minecraft /playsound command (defaults to 1)
     */
    private float pitch = 1;
    
    public void isValid() {
        Preconditions.checkArgument(namespacedKey != null, "namespacedKey can't be null");
        namespacedKey.isValid();
    }
    
    public Sound toSound() {
        return Sound.sound()
                .type(namespacedKey.toNamespacedKey())
                .volume(this.volume)
                .pitch(this.pitch)
                .build();
    }
}
