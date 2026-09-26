package com.fpsboost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.util.SimpleOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a set of render-setting tweaks that trade visual fidelity for
 * frame rate, and can flip them back to whatever the player had before.
 *
 * This is intentionally simple (direct option edits) rather than a mixin,
 * so it stays compatible across Minecraft point releases without needing
 * to hook into the renderer itself.
 */
public final class PerformanceMode {
    private static final Logger LOGGER = LoggerFactory.getLogger("fpsboost");

    private static boolean active = false;

    // Remembers the player's previous settings so toggling off restores them.
    private static GraphicsMode prevGraphics;
    private static ParticlesMode prevParticles;
    private static CloudRenderMode prevClouds;
    private static int prevViewDistance;
    private static double prevEntityDistance;
    private static boolean prevEntityShadows;

    private PerformanceMode() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public static void enable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        GameOptions options = client.options;

        try {
            prevGraphics = options.getGraphicsMode().getValue();
            prevParticles = options.getParticles().getValue();
            prevClouds = options.getCloudRenderMode().getValue();
            prevViewDistance = options.getViewDistance().getValue();
            prevEntityDistance = options.getEntityDistanceScaling().getValue();
            prevEntityShadows = options.getEntityShadows().getValue();

            setOption(options.getGraphicsMode(), GraphicsMode.FAST);
            setOption(options.getParticles(), ParticlesMode.MINIMAL);
            setOption(options.getCloudRenderMode(), CloudRenderMode.OFF);
            setOption(options.getEntityDistanceScaling(), Math.min(prevEntityDistance, 0.75));
            setOption(options.getEntityShadows(), false);
            // Clamp view distance rather than forcing it lower than the player's
            // own preference, so this never *increases* memory/CPU load.
            setOption(options.getViewDistance(), Math.min(prevViewDistance, 10));

            active = true;
            LOGGER.info("[fpsboost] Performance mode ON");
            if (client.player != null) {
                client.player.sendMessage(
                        net.minecraft.text.Text.literal("[FPS Boost] Performance mode ON"), true);
            }
        } catch (Exception e) {
            LOGGER.error("[fpsboost] Failed to enable performance mode: {}", e.getMessage());
        }
    }

    public static void disable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !active) {
            return;
        }
        GameOptions options = client.options;

        try {
            setOption(options.getGraphicsMode(), prevGraphics);
            setOption(options.getParticles(), prevParticles);
            setOption(options.getCloudRenderMode(), prevClouds);
            setOption(options.getEntityDistanceScaling(), prevEntityDistance);
            setOption(options.getEntityShadows(), prevEntityShadows);
            setOption(options.getViewDistance(), prevViewDistance);

            active = false;
            LOGGER.info("[fpsboost] Performance mode OFF");
            if (client.player != null) {
                client.player.sendMessage(
                        net.minecraft.text.Text.literal("[FPS Boost] Performance mode OFF"), true);
            }
        } catch (Exception e) {
            LOGGER.error("[fpsboost] Failed to disable performance mode: {}", e.getMessage());
        }
    }

    private static <T> void setOption(SimpleOption<T> option, T value) {
        option.setValue(value);
    }
}
