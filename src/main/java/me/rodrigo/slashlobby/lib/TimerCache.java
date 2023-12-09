package me.rodrigo.slashlobby.lib;

import java.util.*;

public class TimerCache
{
    private UUID uuid;
    private long LastUsed;
    private Parser parser;

    public TimerCache setUuid(final UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public TimerCache setLastUsed(final long lastUsed) {
        this.LastUsed = lastUsed;
        return this;
    }

    public TimerCache setParser(final Parser parser) {
        this.parser = parser;
        return this;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public long getLastUsed() {
        return this.LastUsed;
    }

    public Parser getParser() {
        return this.parser;
    }

    public int getAmplifier() {
        final String asString = this.parser.AsString("cooldown.unit", new int[0]);
        int amplifier = 0;
        switch (asString) {
            case "MINUTES": {
                amplifier = 60000;
                break;
            }
            case "HOURS": {
                amplifier = 3600000;
                break;
            }
            case "DAYS": {
                amplifier = 86400000;
                break;
            }
            default: {
                amplifier = 1000;
                break;
            }
        }
        return amplifier;
    }

    public boolean canUse() {
        return System.currentTimeMillis() - this.LastUsed >= Double.parseDouble(this.parser.AsObject("cooldown.value").toString()) * this.getAmplifier();
    }

    public TimerCache registerNewUsage() {
        this.LastUsed = System.currentTimeMillis();
        return this;
    }
}
