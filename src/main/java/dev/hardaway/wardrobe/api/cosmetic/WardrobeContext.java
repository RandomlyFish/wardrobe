package dev.hardaway.wardrobe.api.cosmetic;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface WardrobeContext {

    Player getPlayer();

    Collection<PlayerCosmetic> getCosmetics();

    @Nullable
    PlayerCosmetic getCosmetic(WardrobeGroup group);

    Map<WardrobeGroup, ModelAttachment> getModelAttachments();

    void addAttachment(WardrobeGroup group, ModelAttachment attachment);

    void hideGroup(WardrobeGroup group);

    Model getPlayerModel();

    void setPlayerModel(Model playerModel);
}
