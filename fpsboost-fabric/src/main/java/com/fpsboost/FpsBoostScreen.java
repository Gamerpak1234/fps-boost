package com.fpsboost;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * A small in-game menu (opened with the keybind) showing whether
 * Performance Mode is on, with a button to toggle it.
 */
public class FpsBoostScreen extends Screen {
    private final Screen parent;
    private ButtonWidget toggleButton;

    public FpsBoostScreen(Screen parent) {
        super(Text.literal("FPS Boost"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        toggleButton = ButtonWidget.builder(toggleLabel(), button -> {
            PerformanceMode.toggle();
            button.setMessage(toggleLabel());
        }).dimensions(centerX - 100, centerY - 20, 200, 20).build();
        this.addDrawableChild(toggleButton);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            this.close();
        }).dimensions(centerX - 100, centerY + 10, 200, 20).build());
    }

    private Text toggleLabel() {
        return Text.literal("Performance Mode: " + (PerformanceMode.isActive() ? "ON" : "OFF"));
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
