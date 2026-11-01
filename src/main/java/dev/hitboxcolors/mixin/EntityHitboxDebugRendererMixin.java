package dev.hitboxcolors.mixin;

import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 1.21.11 (Yarn): hitboxes are drawn as gizmos in
 * EntityHitboxDebugRenderer#drawHitbox(Entity, float, boolean).
 * We remember which entity is being drawn and swap the colour ints
 * passed to the gizmo calls (box style, lines, arrows).
 */
@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {

    @Unique private static final int HC_RED  = 0xFFFF0000;
    @Unique private static final int HC_BLUE = 0xFF0066FF;
    @Unique private static Entity hc$current;

    @Inject(method = "drawHitbox", at = @At("HEAD"))
    private void hc$start(Entity entity, float tickProgress, boolean inLocalServer, CallbackInfo ci) {
        hc$current = entity;
    }

    @Inject(method = "drawHitbox", at = @At("RETURN"))
    private void hc$end(Entity entity, float tickProgress, boolean inLocalServer, CallbackInfo ci) {
        hc$current = null;
    }

    @Unique
    private static int hc$color(int original) {
        Entity e = hc$current;
        if (e == null) return original;
        EntityType<?> t = e.getType();
        if (t == EntityType.ENDER_PEARL) return HC_RED;
        if (t == EntityType.WIND_CHARGE || t == EntityType.BREEZE_WIND_CHARGE) return HC_BLUE;
        return original;
    }

    // DrawStyle.stroked(int) -> used for the boxes
    @ModifyArg(method = "drawHitbox", require = 0,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/DrawStyle;stroked(I)Lnet/minecraft/client/render/DrawStyle;"),
        index = 0)
    private int hc$stroked(int color) { return hc$color(color); }

    @ModifyArg(method = "drawHitbox", require = 0,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/DrawStyle;stroked(IF)Lnet/minecraft/client/render/DrawStyle;"),
        index = 0)
    private int hc$strokedWidth(int color) { return hc$color(color); }

    // GizmoDrawing.line / arrow -> eye line and look direction
    @ModifyArg(method = "drawHitbox", require = 0,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/debug/gizmo/GizmoDrawing;line(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;I)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;"),
        index = 2)
    private int hc$line(int color) { return hc$color(color); }

    @ModifyArg(method = "drawHitbox", require = 0,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/debug/gizmo/GizmoDrawing;line(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;IF)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;"),
        index = 2)
    private int hc$lineWidth(int color) { return hc$color(color); }

    @ModifyArg(method = "drawHitbox", require = 0,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/debug/gizmo/GizmoDrawing;arrow(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;I)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;"),
        index = 2)
    private int hc$arrow(int color) { return hc$color(color); }

    @ModifyArg(method = "drawHitbox", require = 0,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/debug/gizmo/GizmoDrawing;arrow(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;IF)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;"),
        index = 2)
    private int hc$arrowWidth(int color) { return hc$color(color); }
}
