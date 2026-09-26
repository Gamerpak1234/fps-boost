package com.fpsboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public final class FpsBoostClient implements ClientModInitializer {

    // As of 1.21.9, keybind categories are objects, not plain strings.
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("fpsboost", "keybinds"));

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fpsboost.menu",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_F10,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FpsBoostScreen(null));
                }
            }
        });
    }
}
