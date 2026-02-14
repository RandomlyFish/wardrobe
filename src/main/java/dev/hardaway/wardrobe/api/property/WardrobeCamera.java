package dev.hardaway.wardrobe.api.property;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class WardrobeCamera {
    public static final WardrobeCamera DEFAULT_CAMERA = new WardrobeCamera();
    private static final ServerCameraSettings DEFAULT_SETTINGS = new ServerCameraSettings();

    public static final BuilderCodec<WardrobeCamera> CODEC = BuilderCodec.builder(WardrobeCamera.class, WardrobeCamera::new)
            .append(
                    new KeyedCodec<>("X", Codec.FLOAT),
                    (data, s) -> data.x = s,
                    data -> data.x
            )
            .addValidator(Validators.insideRange(-10F, 10F))
            .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
            .add()

            .append(
                    new KeyedCodec<>("Y", Codec.FLOAT),
                    (data, s) -> data.y = s,
                    data -> data.y
            )
            .addValidator(Validators.insideRange(-10F, 10F))
            .add()

            .append(
                    new KeyedCodec<>("Z", Codec.FLOAT),
                    (data, s) -> data.z = s,
                    data -> data.z
            )
            .addValidator(Validators.insideRange(-10F, 10F))
            .add()

            .append(
                    new KeyedCodec<>("Distance", Codec.FLOAT),
                    (data, s) -> data.distance = s,
                    data -> data.distance
            )
            .addValidator(Validators.insideRange(-10F, 10F))
            .metadata(new UIPropertyTitle("Distance")).documentation("The camera's distance from the player.")
            .add()

            .append(
                    new KeyedCodec<>("EyeOffset", Codec.BOOLEAN),
                    (data, s) -> data.eyeOffset = s,
                    data -> data.eyeOffset
            )
            .metadata(new UIPropertyTitle("Eye Offset")).documentation("If the camera's position should be offset to match the player's eye position.")
            .add()

            .append(
                    new KeyedCodec<>("Yaw", Codec.FLOAT),
                    (data, s) -> data.yaw = s,
                    data -> data.yaw
            )
            .addValidator(Validators.insideRange(-360F, 360F))
            .metadata(new UIPropertyTitle("Yaw")).documentation("Added onto the player's body rotation yaw.")
            .add()

            .append(
                    new KeyedCodec<>("Pitch", Codec.FLOAT),
                    (data, s) -> data.pitch = s,
                    data -> data.pitch
            )
            .addValidator(Validators.insideRange(-360F, 360F))
            .add()

            .append(
                    new KeyedCodec<>("Roll", Codec.FLOAT),
                    (data, s) -> data.roll = s,
                    data -> data.roll
            )
            .addValidator(Validators.insideRange(-360F, 360F))
            .add()

            .build();

    private float distance = 3;
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private boolean eyeOffset = true;
    private float yaw = 180;
    private float pitch = 0;
    private float roll = 0;

    protected WardrobeCamera() {
    }

    public ServerCameraSettings toServerSettings(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
        float yaw = componentAccessor.getComponent(ref, TransformComponent.getComponentType()).getRotation().y;
        ServerCameraSettings settings = new ServerCameraSettings(DEFAULT_SETTINGS);
        settings.distance = this.distance;
        settings.positionOffset = new Position(this.x, this.y, this.z);
        settings.eyeOffset = this.eyeOffset;
        settings.rotation = new Direction((float) Math.toRadians(this.yaw) + yaw, (float) Math.toRadians(this.pitch), (float) Math.toRadians(this.roll));
        return settings;
    }

    static {
        DEFAULT_SETTINGS.isFirstPerson = false;
        DEFAULT_SETTINGS.positionLerpSpeed = 0.15F;
        DEFAULT_SETTINGS.rotationType = RotationType.Custom;
        DEFAULT_SETTINGS.rotationLerpSpeed = 0.15F;
        DEFAULT_SETTINGS.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
        DEFAULT_SETTINGS.positionType = PositionType.AttachedToPlusOffset;
    }
}
