package dev.hardaway.wardrobe.impl.menu;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class WardrobeMenuEvents {
    public record Open(PlayerRef playerRef, WardrobePage menu) implements IEvent<Void> {
    }

    public record Close(PlayerRef playerRef) implements IEvent<Void> {
    }
}
