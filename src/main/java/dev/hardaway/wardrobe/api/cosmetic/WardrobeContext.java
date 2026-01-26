package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface WardrobeContext {

    Player getPlayer();

    PlayerSettings getPlayerSettings();

    Map<String, Cosmetic> getCosmeticMap();

    Collection<PlayerCosmetic> getCosmetics();

    @Nullable
    PlayerCosmetic getCosmetic(String slot);

    @Nullable
    default PlayerCosmetic getCosmetic(WardrobeCosmeticSlot slot) {
        return this.getCosmetic(slot.getId());
    }

    Map<String, ModelAttachment> getModelAttachments();

    void addAttachment(String slot, ModelAttachment attachment);

    void hideSlots(String... slots);

    Model getPlayerModel();

    void setPlayerModel(Model playerModel);
}
