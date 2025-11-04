package org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.sound;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.sound.Sound;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;


@Data
@Builder
public class SoundDTO implements Validatable {
    /**
     * Is the resource location of the sound to play. Can be a built-in minecraft sound or a resource pack sound. See
     * sound parameter explanation of default minecraft /playsound command
     */
    private NamespacedKeyDTO namespacedKey;
    /**
     * see volume parameter explanation of default minecraft /playsound command (defaults to 1)
     */
    @Builder.Default
    private float volume = 1;
    /**
     * see pitch parameter explanation of default minecraft /playsound command (defaults to 1)
     */
    @Builder.Default
    private float pitch = 1;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(namespacedKey, "namespacedKey");
        namespacedKey.validate(validator.path("namespacedKey"));
    }
    
    public Sound toSound() {
        return Sound.sound()
                .type(namespacedKey.toNamespacedKey())
                .volume(this.volume)
                .pitch(this.pitch)
                .build();
    }
    
    public static SoundDTO fromSound(Sound sound) {
        return SoundDTO.builder()
                .namespacedKey(new NamespacedKeyDTO(sound.name().namespace(), sound.name().value()))
                .volume(sound.volume())
                .pitch(sound.pitch())
                .build();
    }
    
    /**
     * Convenience method to get just the key of the NamespacedKey DTO of this SoundDTO
     * @return the key of the NamespacedKeyDTO of this soundDTO
     */
    public String getKey() {
        return namespacedKey.getKey();
    }
}
