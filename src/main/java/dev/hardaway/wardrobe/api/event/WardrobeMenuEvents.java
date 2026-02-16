package dev.hardaway.wardrobe.api.event;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hardaway.wardrobe.impl.menu.WardrobePage;

public class WardrobeMenuEvents {
    public record Open(PlayerRef playerRef, WardrobePage menu) implements IEvent<Void> {
    }

    public record Close(PlayerRef playerRef) implements IEvent<Void> {
    }
}
