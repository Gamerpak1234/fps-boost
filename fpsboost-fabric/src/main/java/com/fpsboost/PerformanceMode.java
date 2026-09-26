package com.fpsboost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticleStatus;
import net.minecraft.client.option.SimpleOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a set of render-setting tweaks that trade visual fidelity for
 * frame rate, and can flip them back to whatever the player had before.
 *
 * NOTE: Minecraft's 1.21.2+ options-screen rewrite renamed several
 * GameOptions accessor methods (dropping the "get" prefix, e.g.
 * getViewDistance() -> viewDistance()). This file uses the new
 * no-prefix names; if a build error says a method isn't found here,
 * that's the exact detail to fix next.
 */
public final class PerformanceMode {
    private static final Logger LOGGER = LoggerFactory.getLogger("fpsboost");

    private static boolean active = false;

    private static GraphicsMode prevGraphics;
    private static ParticleStatus prevParticles;
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
            prevGraphics = options.graphicsMode().getValue();
            prevParticles = options.particles().getValue();
            prevClouds = options.cloudRenderMode().getValue();
            prevViewDistance = options.viewDistance().getValue();
            prevEntityDistance = options.entityDistanceScaling().getValue();
            prevEntityShadows = options.entityShadows().getValue();

            setOption(options.graphicsMode(), GraphicsMode.FAST);
            setOption(options.particles(), ParticleStatus.MINIMAL);
            setOption(options.cloudRenderMode(), CloudRenderMode.OFF);
            setOption(options.entityDistanceScaling(), Math.min(prevEntityDistance, 0.75));
            setOption(options.entityShadows(), false);
            setOption(options.viewDistance(), Math.min(prevViewDistance, 10));

            active = true;
            LOGGER.info("[fpsboost] Performance mode ON");
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
            setOption(options.graphicsMode(), prevGraphics);
            setOption(options.particles(), prevParticles);
            setOption(options.cloudRenderMode(), prevClouds);
            setOption(options.entityDistanceScaling(), prevEntityDistance);
            setOption(options.entityShadows(), prevEntityShadows);
            setOption(options.viewDistance(), prevViewDistance);

            active = false;
            LOGGER.info("[fpsboost] Performance mode OFF");
        } catch (Exception e) {
            LOGGER.error("[fpsboost] Failed to disable performance mode: {}", e.getMessage());
        }
    }

    private static <T> void setOption(SimpleOption<T> option, T value) {
        option.setValue(value);
    }
}
