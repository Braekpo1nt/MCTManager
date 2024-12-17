package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MCTPlayer {
    private UUID uniqueId;
    private int score;
    @SerializedName(value = "teamId", alternate = {"teamId"})
    private String teamId;
}
