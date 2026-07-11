package com.koomplo.wizardry.client;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.event.EBClientTickEvent;
import com.koomplo.wizardry.content.spell.necromancy.Possession;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Liga/desliga os post-shaders do 1.12.2 conforme o estado do player local.
 * Os programas phosphor/deconverge foram removidos do vanilla moderno, então são
 * shipados sob o namespace ebwizardry (GLSL do client 1.12.2 em #version 150).
 * Addons registram shaders próprios com {@link #register} no init de client
 * (maior prioridade vence quando mais de uma condição está ativa).
 */
public final class PostEffects {

    public static final ResourceLocation POSSESSION = WizardryMainMod.location("shaders/post/possession.json");
    public static final ResourceLocation SIXTH_SENSE = WizardryMainMod.location("shaders/post/sixth_sense.json");
    public static final ResourceLocation SLOW_TIME = WizardryMainMod.location("shaders/post/slow_time.json");
    public static final ResourceLocation TRANSIENCE = WizardryMainMod.location("shaders/post/transience.json");

    /**
     * @param blinkOnToggle 1.12.2: sixth sense/transience piscam ao ligar E desligar; possession só ao ligar
     */
    private record Entry(int priority, Predicate<LocalPlayer> condition, ResourceLocation shader,
                         boolean blinkOnToggle, boolean blinkOffToggle) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        register(400, Possession::isPossessing, POSSESSION, true, false);
        register(300, p -> p.hasEffect(EBMobEffects.holder(EBMobEffects.SIXTH_SENSE)), SIXTH_SENSE, true, true);
        register(200, p -> p.hasEffect(EBMobEffects.holder(EBMobEffects.SLOW_TIME)), SLOW_TIME, false, false);
        register(100, p -> p.hasEffect(EBMobEffects.holder(EBMobEffects.TRANSIENCE)), TRANSIENCE, true, true);
    }

    /** Registra um shader condicional (client init; sem blink nos toggles). */
    public static void register(int priority, Predicate<LocalPlayer> condition, ResourceLocation shader) {
        register(priority, condition, shader, false, false);
    }

    public static synchronized void register(int priority, Predicate<LocalPlayer> condition, ResourceLocation shader,
                                             boolean blinkOnToggle, boolean blinkOffToggle) {
        ENTRIES.add(new Entry(priority, condition, shader, blinkOnToggle, blinkOffToggle));
        ENTRIES.sort(Comparator.comparingInt(Entry::priority).reversed());
    }

    private static Entry active;

    public static void onClientTick(EBClientTickEvent event) {
        Minecraft mc = event.getMinecraft();
        GameRenderer renderer = mc.gameRenderer;
        Entry desired = desiredEntry(mc);

        if (desired == null) {
            if (active != null) {
                if (isOurs(renderer)) renderer.shutdownEffect();
                if (active.blinkOffToggle()) ScreenOverlays.playBlinkEffect();
                active = null;
            }
        } else if (active != desired || !isOurs(renderer)) {
            // Não atropela shaders de terceiros nem os de espectador (creeper/spider/invert)
            if (renderer.currentEffect() == null || isOurs(renderer)) {
                boolean firstLoad = active == null;
                renderer.loadEffect(desired.shader());
                active = desired;
                if (firstLoad && desired.blinkOnToggle()) ScreenOverlays.playBlinkEffect();
            }
        }
    }

    private static Entry desiredEntry(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.getCameraEntity() != player) return null;
        for (Entry entry : ENTRIES) {
            if (entry.condition().test(player)) return entry;
        }
        return null;
    }

    private static boolean isOurs(GameRenderer renderer) {
        PostChain current = renderer.currentEffect();
        if (current == null) return false;
        String name = current.getName();
        return ENTRIES.stream().anyMatch(entry -> entry.shader().toString().equals(name));
    }

    private PostEffects() {
    }
}
