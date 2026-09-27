package com.fpsboost;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.particle.ParticlesMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a set of render-setting tweaks that trade visual fidelity for
 * frame rate, and can flip them back to whatever the player had before.
 *
 * Verified against the actual 1.21.11 GameOptions class (via reflection
 * dump), not guessed:
 *  - SimpleOption lives in net.minecraft.client.option (correct package).
 *  - ParticlesMode lives in net.minecraft.particle (NOT client.option -
 *    this was the actual root cause of every earlier compile failure).
 *  - GraphicsMode has no plain getter (only applyGraphicsMode(..) as a
 *    setter), so it's intentionally left out of this toggle.
 */
public final class PerformanceMode {
    private static final Logger LOGGER = LoggerFactory.getLogger("fpsboost");

    private static boolean active = false;

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
            prevParticles = options.getParticles().getValue();
            prevClouds = options.getCloudRenderMode().getValue();
            prevViewDistance = options.getViewDistance().getValue();
            prevEntityDistance = options.getEntityDistanceScaling().getValue();
            prevEntityShadows = options.getEntityShadows().getValue();

            setOption(options.getParticles(), ParticlesMode.MINIMAL);
            setOption(options.getCloudRenderMode(), CloudRenderMode.OFF);
            setOption(options.getEntityDistanceScaling(), Math.min(prevEntityDistance, 0.75));
            setOption(options.getEntityShadows(), false);
            setOption(options.getViewDistance(), Math.min(prevViewDistance, 10));

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
            setOption(options.getParticles(), prevParticles);
            setOption(options.getCloudRenderMode(), prevClouds);
            setOption(options.getEntityDistanceScaling(), prevEntityDistance);
            setOption(options.getEntityShadows(), prevEntityShadows);
            setOption(options.getViewDistance(), prevViewDistance);

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
