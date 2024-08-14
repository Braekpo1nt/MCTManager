package org.braekpo1nt.mctmanager.utils;

import lombok.Getter;

@Getter
public enum LogType {
    CANCEL_DEATH_EVENT("cancelDeathEvent");
    
    private final String id;
    
    LogType(String id) {
        this.id = id;
    }
}
