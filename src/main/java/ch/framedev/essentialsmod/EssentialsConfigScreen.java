package ch.framedev.essentialsmod;

import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EssentialsConfigScreen extends Screen {

    private final Screen parent;

    public EssentialsConfigScreen(Screen parent) {
        super(Component.literal("Essentials Mod Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        int startY = this.height / 6;
        int centerX = this.width / 2 - buttonWidth / 2;

        int currentY = startY;

        addToggleButton(centerX, currentY, "Enable Warps", EssentialsConfig.enableWarps, EssentialsConfig.enableWarps::set);
        currentY += spacing;
        addToggleButton(centerX, currentY, "Enable Back Command", EssentialsConfig.useBack, EssentialsConfig.useBack::set);
        currentY += spacing;
        addToggleButton(centerX, currentY, "Enable Limited Homes", EssentialsConfig.enableLimitedHomes, EssentialsConfig.enableLimitedHomes::set);
        currentY += spacing;

        this.addRenderableWidget(new AbstractSliderButton(centerX, currentY, buttonWidth, buttonHeight,
                Component.literal("Limit for Homes: " + EssentialsConfig.limitForHomes.get()),
                EssentialsConfig.limitForHomes.get() / 100.0) {
            @Override
            protected void updateMessage() {
                int limit = (int) (this.value * 100);
                this.setMessage(Component.literal("Limit for Homes: " + limit));
            }

            @Override
            protected void applyValue() {
                int limit = (int) (this.value * 100);
                EssentialsConfig.limitForHomes.set(limit);
            }
        });
        currentY += spacing;

        addToggleButton(centerX, currentY, "Enable Mute Other Player for Themselves", EssentialsConfig.muteOtherPlayerForSelf::get, EssentialsConfig.muteOtherPlayerForSelf::set);
        currentY += spacing;
        addToggleButton(centerX, currentY, "Enable Backpack", EssentialsConfig.enableBackPack::get, EssentialsConfig.enableBackPack::set);
        currentY += spacing;
        addToggleButton(centerX, currentY, "Enable Backpack Config Save", EssentialsConfig.enableBackPackSaveInConfig::get, EssentialsConfig.enableBackPackSaveInConfig::set);
        currentY += spacing;
        addToggleButton(centerX, currentY, "Enable Signs events as example [FREE]", EssentialsConfig.enableSigns::get, EssentialsConfig.enableSigns::set);
        currentY += spacing;
        addToggleButton(centerX, currentY, "Enable silent join and leave messages", EssentialsConfig.silentJoinLeave::get, EssentialsConfig.silentJoinLeave::set);
        currentY += spacing;

        this.addRenderableWidget(
                Button.builder(Component.literal("Config Format: " + EssentialsConfig.configSelection.get()), button -> {
                            List<String> options = List.of("json", "yaml");
                            String current = EssentialsConfig.configSelection.get();
                            int currentIndex = options.indexOf(current);
                            String newValue = options.get((currentIndex + 1) % options.size());
                            EssentialsConfig.configSelection.set(newValue);
                            button.setMessage(Component.literal("Config Format: " + newValue));
                        })
                        .bounds(centerX, currentY, buttonWidth, buttonHeight)
                        .build()
        );
        currentY += spacing;

        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), button -> {
                            EssentialsConfig.COMMON_CONFIG.save();
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(this.parent);
                            }
                        })
                        .bounds(centerX, currentY, buttonWidth, buttonHeight)
                        .build()
        );
    }

    private void addToggleButton(int x, int y, String label, java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
        this.addRenderableWidget(
                Button.builder(Component.literal(label + ": " + (getter.get() ? "On" : "Off")), button -> {
                            boolean newValue = !getter.get();
                            setter.accept(newValue);
                            button.setMessage(Component.literal(label + ": " + (newValue ? "On" : "Off")));
                        })
                        .bounds(x, y, 200, 20)
                        .build()
        );
    }

    @Mod.EventBusSubscriber(modid = "essentials", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EssentialsModClient {
        public static void registerConfigGui(FMLJavaModLoadingContext context) {
            context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new EssentialsConfigScreen(parent)));
        }
    }
}