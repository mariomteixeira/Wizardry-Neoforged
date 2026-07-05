package com.koomplo.wizardry.datagen.provider;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.setup.registries.EBItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class EBArtifactDocsProvider extends ArtifactDocsProvider {
    public EBArtifactDocsProvider(PackOutput output) {
        super(output, WizardryMainMod.MOD_ID, "/resources/img/artifact/");
    }

    @Override
    protected void buildArtifacts(@NotNull Consumer<Item> consumer) {
        EBItems.getArtifacts().keySet().forEach(i -> consumer.accept(i.get()));
    }
}
