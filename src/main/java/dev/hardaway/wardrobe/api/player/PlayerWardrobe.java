package dev.hardaway.wardrobe.api.player;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

import javax.annotation.Nullable;
import java.util.Collection;

public interface PlayerWardrobe {
    static ComponentType<EntityStore, PlayerWardrobeComponent> getComponentType() {
        return PlayerWardrobeComponent.getComponentType();
    }

    Collection<PlayerCosmetic> getCosmetics();

    boolean hasCosmetic(String id);

    default boolean hasCosmetic(Cosmetic cosmetic) {
        return this.hasCosmetic(cosmetic.getId());
    }

    PlayerCosmetic getCosmetic(String groupId);

    @Nullable
    default PlayerCosmetic getCosmetic(WardrobeCosmeticSlot group) {
        return this.getCosmetic(group.getId());
    }

    void setCosmetic(String groupId, @Nullable PlayerCosmetic cosmetic);

    default void setCosmetic(WardrobeCosmeticSlot group, @Nullable PlayerCosmetic cosmetic) {
        this.setCosmetic(group.getId(), cosmetic);
    }

    default void removeCosmetic(String groupId) {
        this.setCosmetic(groupId, null);
    }

    default void removeCosmetic(WardrobeCosmeticSlot group) {
        this.removeCosmetic(group.getId());
    }

    void clearCosmetics();

    void rebuild();

    PlayerWardrobe clone();
}
