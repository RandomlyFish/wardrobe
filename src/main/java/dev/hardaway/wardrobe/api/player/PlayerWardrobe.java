package dev.hardaway.wardrobe.api.player;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeGroup;

import javax.annotation.Nullable;
import java.util.Collection;

public interface PlayerWardrobe {

    static ComponentType<EntityStore, ? extends PlayerWardrobe> getComponentType() {
        return WardrobePlugin.get().getPlayerWardrobeComponentType();
    }

    Collection<PlayerCosmetic> getCosmetics();

    @Nullable
    PlayerCosmetic getCosmetic(WardrobeGroup group);

    void setCosmetic(WardrobeGroup group, @Nullable PlayerCosmetic cosmetic);

    void clearCosmetics();

    void rebuild();
}
