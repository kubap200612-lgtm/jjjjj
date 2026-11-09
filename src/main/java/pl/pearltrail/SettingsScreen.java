package pl.pearltrail;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/** Ekran ustawień smugi (domyślnie klawisz O). */
public class SettingsScreen extends Screen {

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Pearl Trail – ustawienia"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        TrailConfig cfg = TrailConfig.INSTANCE;
        // dwie kolumny: lewa = smuga, prawa = podświetlenie bloku
        int w = 180;
        int gap = 10;
        int left = this.width / 2 - w - gap / 2;
        int right = this.width / 2 + gap / 2;
        int top = 50;
        int step = 24;

        // ---- Smuga ----
        int y = top;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.enabled)
                .build(left, y, w, 20, Text.literal("Smuga"), (b, v) -> cfg.enabled = v));
        y += step;
        addDrawableChild(new ConfigSlider(left, y, w, 20, 0.05, 1.0, cfg.density,
                v -> "Gęstość: " + Math.round(v * 100) + "%",
                v -> cfg.density = (float) v, 0.05));
        y += step;
        addDrawableChild(new ConfigSlider(left, y, w, 20, 0.0, 10.0, cfg.fadeSeconds,
                v -> String.format("Czas po uderzeniu: %.1f s", v),
                v -> cfg.fadeSeconds = (float) v, 0.1));
        y += step;
        addDrawableChild(new ConfigSlider(left, y, w, 20, 0, 150, cfg.burstParticles,
                v -> "Wybuch: " + Math.round(v) + " cząsteczek",
                v -> cfg.burstParticles = (int) Math.round(v), 1));
        y += step;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.latestEnabled)
                .build(left, y, w, 20, Text.literal("Wyróżnij ostatnią"), (b, v) -> cfg.latestEnabled = v));
        y += step;
        addDrawableChild(colorButton(left, y, w, "Kolor ostatniej", cfg.latestColor, v -> cfg.latestColor = v));

        // ---- Podświetlenie bloku ----
        y = top;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.highlight)
                .build(right, y, w, 20, Text.literal("Podświetlenie"), (b, v) -> cfg.highlight = v));
        y += step;
        addDrawableChild(new ConfigSlider(right, y, w, 20, 1.0, 15.0, cfg.highlightSeconds,
                v -> String.format("Czas podświetlenia: %.1f s", v),
                v -> cfg.highlightSeconds = (float) v, 0.5));
        y += step;
        addDrawableChild(colorButton(right, y, w, "Kolor", cfg.highlightColor, v -> cfg.highlightColor = v));

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
                .dimensions(this.width / 2 - 100, top + step * 6 + 12, 200, 20).build());
    }

    private static CyclingButtonWidget<TrailConfig.HighlightColor> colorButton(
            int x, int y, int w, String name, TrailConfig.HighlightColor initial,
            java.util.function.Consumer<TrailConfig.HighlightColor> setter) {
        return CyclingButtonWidget.<TrailConfig.HighlightColor>builder(c -> Text.literal(c.label).withColor(c.rgb))
                .values(TrailConfig.HighlightColor.values())
                .initially(initial)
                .build(x, y, w, 20, Text.literal(name), (b, v) -> setter.accept(v));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xB04CFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Smuga"), this.width / 2 - 95, 38, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Blok uderzenia"), this.width / 2 + 95, 38, 0xFFFFFF);
    }

    @Override
    public void close() {
        TrailConfig.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    /** Suwak z zakresem min-max i zaokrąglaniem do kroku. */
    private static class ConfigSlider extends SliderWidget {
        private final double min, max, step;
        private final DoubleFunction<String> label;
        private final DoubleConsumer setter;

        ConfigSlider(int x, int y, int w, int h, double min, double max, double current,
                     DoubleFunction<String> label, DoubleConsumer setter, double step) {
            super(x, y, w, h, Text.empty(), MathHelper.clamp((current - min) / (max - min), 0, 1));
            this.min = min;
            this.max = max;
            this.step = step;
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        private double real() {
            double v = min + value * (max - min);
            return Math.round(v / step) * step;
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label.apply(real())));
        }

        @Override
        protected void applyValue() {
            setter.accept(real());
        }
    }
}
