package org.braekpo1nt.mctmanager.games.game.config;

import com.google.common.base.Preconditions;
import lombok.Getter;


@Getter
public class SoundDTO {
    /**
     * Is the resource location of the sound to play. Can be a built-in minecraft sound or a resource pack sound. See sound parameter explanation of default minecraft /playsound command
     */
    private String sound;
    /**
     * see volume parameter explanation of default minecraft /playsound command (defaults to 1)
     */
    private float volume = 1;
    /**
     * see pitch parameter explanation of default minecraft /playsound command (defaults to 1)
     */
    private float pitch = 1;
    
    public void isValid() {
        Preconditions.checkArgument(sound != null, "sound can't be null");
    }
}
