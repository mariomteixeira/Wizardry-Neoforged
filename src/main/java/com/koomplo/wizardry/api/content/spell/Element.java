package com.koomplo.wizardry.api.content.spell;

import com.koomplo.wizardry.core.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Element {
    private final ChatFormatting color;
    private final int[] colors;
    private final boolean npcSelectable;
    private String descriptionId;
    private ResourceLocation location;
    private ResourceLocation icon;

    public Element(ChatFormatting colour, int... colors) {
        this(colour, true, colors);
    }

    /**
     * @param npcSelectable false para elementos só de classificação: wizards NPC nunca o sorteiam
     *                      (não há armor/wand/cristal correspondentes).
     */
    public Element(ChatFormatting colour, boolean npcSelectable, int... colors) {
        this.color = colour;
        this.npcSelectable = npcSelectable;
        this.colors = colors;
    }

    public boolean isNpcSelectable() {
        return npcSelectable;
    }

    public int[] getColors() {
        return colors;
    }

    // ===================================================
    // LOCATION AND FORMATTING
    // ==================================================

    /**
     * Will return the description for the spell (e.g. "Fireball")
     */
    public Component getDescriptionFormatted() {
        return Component.translatable(getOrCreateDescriptionId()).withStyle(this.color);
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null)
            this.descriptionId = Util.makeDescriptionId("element", Services.REGISTRY_UTIL.getElement(this));
        return this.descriptionId;
    }

    /**
     * Will return the description ID for the spell (e.g. "spell.ebwizardry.fireball")
     * if you want the location instead, use {@link #getLocation()}
     */
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    protected ResourceLocation getOrCreateLocation() {
        if (this.location == null) this.location = Services.REGISTRY_UTIL.getElement(this);
        return this.location;
    }

    /**
     * Will return the location for the spell (e.g. "ebwizardry:fireball")
     */
    public ResourceLocation getLocation() {
        return this.getOrCreateLocation();
    }

    /**
     * Will return true if the spell is registered at the given location
     */
    public final boolean is(ResourceLocation location) {
        return location.equals(getLocation());
    }

    /**
     * Will return true if the spell is registered at the given location
     */
    public final boolean is(String location) {
        return location.equals(getLocation().toString());
    }


    public String getName() {
        return getLocation().getPath();
    }

    public ChatFormatting getColor() {
        return color;
    }

    public Component getWizardName() {
        return Component.translatable(getDescriptionId() + ".wizard");
    }

    public ResourceLocation getIconId() {
        if (icon == null)
            icon = ResourceLocation.fromNamespaceAndPath(getOrCreateLocation().getNamespace(), "textures/gui/container/element_icon_" + getOrCreateLocation().getPath() + ".png");
        return icon;
    }
}
