package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PlayerWardrobeContext implements WardrobeContext {

    private final Player player;
    private final PlayerWardrobe component;
    private final Map<String, WardrobeCosmetic> appliedCosmetics;
    private final Map<String, ModelAttachment> attachments;
    protected final Set<String> slotsToHide;
    private Model playerModel;

    public PlayerWardrobeContext(Player player, PlayerWardrobe component, Map<String, WardrobeCosmetic> appliedCosmetics, Map<String, ModelAttachment> attachments, Set<String> slotsToHide, Model playerModel) {
        this.player = player;
        this.component = component;
        this.appliedCosmetics = appliedCosmetics;
        this.playerModel = playerModel;
        this.attachments = attachments;
        this.slotsToHide = slotsToHide;
    }

    public Player getPlayer() {
        return player;
    }

    public Collection<PlayerCosmetic> getCosmetics() {
        return this.component.getCosmetics();
    }

    @Nullable
    @Override
    public PlayerCosmetic getCosmetic(String slot) {
        return this.component.getCosmetic(slot);
    }

    @Override
    public Map<String, WardrobeCosmetic> getCosmeticMap() {
        return appliedCosmetics;
    }

    @Nullable
    public PlayerCosmetic getCosmetic(WardrobeCosmeticSlot slot) {
        return this.component.getCosmetic(slot);
    }


    public Map<String, ModelAttachment> getModelAttachments() {
        return Collections.unmodifiableMap(this.attachments);
    }

    public void addAttachment(String slot, ModelAttachment attachment) {
        this.attachments.put(slot, attachment);
    }

    @Override
    public void hideSlots(String... slots) {
        this.slotsToHide.addAll(Set.of(slots)); // gross
    }

    public Model getPlayerModel() {
        return playerModel;
    }

    public void setPlayerModel(Model playerModel) {
        this.playerModel = playerModel;
    }
}
