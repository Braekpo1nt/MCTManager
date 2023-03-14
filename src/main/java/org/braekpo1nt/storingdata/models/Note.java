package org.braekpo1nt.storingdata.models;

import java.util.Date;
import java.util.UUID;

public class Note {

    public String getId() {
        return id;
    }

    private String id;
    
    private String playerName;
    private String message;
    private Date dateCreated;

    public Note(String playerName, String message) {
        this.playerName = playerName;
        this.message = message;
        this.dateCreated = new Date();
        this.id = UUID.randomUUID().toString();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
