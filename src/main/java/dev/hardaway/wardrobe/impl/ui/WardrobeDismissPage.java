package dev.hardaway.wardrobe.impl.ui;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.system.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeDismissPage extends InteractiveCustomUIPage<WardrobeDismissPage.PageEventData> {
    private final PlayerWardrobe wardrobe;

    public WardrobeDismissPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, PlayerWardrobe wardrobe) {
        super(playerRef, lifetime, PageEventData.CODEC);
        this.wardrobe = wardrobe;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("Wardrobe/Pages/UnsavedChanges.ui");

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Save", WardrobePage.MenuAction.Save.getEvent());
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Discard", WardrobePage.MenuAction.Discard.getEvent());
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageEventData data) {
        super.handleDataEvent(ref, store, data);

        PlayerWardrobe currentWardrobe = store.getComponent(ref, PlayerWardrobeComponent.getComponentType());

        if (WardrobePage.MenuAction.Discard.equals(data.action)) {
            if (wardrobe != null) {
                store.putComponent(ref, PlayerWardrobeComponent.getComponentType(), (PlayerWardrobeComponent) wardrobe);
            } else if (currentWardrobe != null) {
                currentWardrobe.clearCosmetics();
                currentWardrobe.rebuild();
            }
        }

        close();
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        super.onDismiss(ref, store);
        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson, false, null));
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("Action", new EnumCodec<>(WardrobePage.MenuAction.class)), (e, v) -> e.action = v, e -> e.action).add()
                .build();

        private WardrobePage.MenuAction action;
    }
}
