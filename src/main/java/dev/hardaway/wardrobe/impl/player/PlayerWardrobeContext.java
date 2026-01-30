package dev.hardaway.wardrobe.impl.player;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerWardrobeContext implements WardrobeContext {

    private final Player player;
    private final PlayerSettings playerSettings;
    private final PlayerWardrobe component;
    private final Map<String, Cosmetic> appliedCosmetics;
    private final Map<String, ModelAttachment> attachments;
    protected final Set<String> slotsToHide;
    protected final Set<CosmeticType> hiddenTypes;
    private Model playerModel;

    public PlayerWardrobeContext(Player player, PlayerSettings playerSettings, PlayerWardrobe component, Map<String, Cosmetic> appliedCosmetics, Map<String, ModelAttachment> attachments, Set<String> slotsToHide, Model playerModel) {
        this.player = player;
        this.playerSettings = playerSettings;
        this.component = component;
        this.appliedCosmetics = appliedCosmetics;
        this.playerModel = playerModel;
        this.attachments = attachments;
        this.slotsToHide = slotsToHide;
        this.hiddenTypes = new HashSet<>(component.getHiddenCosmeticTypes());
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public PlayerSettings getPlayerSettings() {
        return playerSettings;
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
    public Map<String, Cosmetic> getCosmeticMap() {
        return appliedCosmetics;
    }

    @Override
    public Collection<CosmeticType> getHiddenTypes() {
        return hiddenTypes;
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

    public Set<String> getHiddenSlots() {
        return slotsToHide;
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
