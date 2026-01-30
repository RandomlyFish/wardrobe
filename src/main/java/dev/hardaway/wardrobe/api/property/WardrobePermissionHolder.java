package dev.hardaway.wardrobe.api.property;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface WardrobePermissionHolder {

    String getPermissionNode();

    default boolean hasPermission(@Nonnull UUID uuid) {
        String permissionNode = this.getPermissionNode();
        if (permissionNode == null || permissionNode.isBlank()) return true;
        return PermissionsModule.get().hasPermission(uuid, permissionNode);
    }
}
