package dev.hardaway.wardrobe.impl.system;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PlayerWardrobeContext implements WardrobeContext {

    private final Player player;
    private final PlayerWardrobeComponent component;
    private Model playerModel;
    private final Map<WardrobeCosmeticSlot, ModelAttachment> attachments;
    protected final Set<String> groupsToHide;

    public PlayerWardrobeContext(Player player, PlayerWardrobeComponent component, Model playerModel, Map<WardrobeCosmeticSlot, ModelAttachment> attachments, Set<String> groupsToHide) {
        this.player = player;
        this.component = component;
        this.playerModel = playerModel;
        this.attachments = attachments;
        this.groupsToHide = groupsToHide;
    }

    public Player getPlayer() {
        return player;
    }

    public Collection<PlayerCosmetic> getCosmetics() {
        return this.component.getCosmetics();
    }

    @Nullable
    public PlayerCosmetic getCosmetic(WardrobeCosmeticSlot group) {
        return this.component.getCosmetic(group);
    }


    public Map<WardrobeCosmeticSlot, ModelAttachment> getModelAttachments() {
        return Collections.unmodifiableMap(this.attachments);
    }

    public void addAttachment(WardrobeCosmeticSlot group, ModelAttachment attachment) {
        this.attachments.put(group, attachment);
    }

    @Override
    public void hideGroup(String group) {
        this.groupsToHide.add(group);
    }

    public Model getPlayerModel() {
        return playerModel;
    }

    public void setPlayerModel(Model playerModel) {
        this.playerModel = playerModel;
    }
}
