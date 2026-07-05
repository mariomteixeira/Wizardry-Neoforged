package com.koomplo.wizardry.client;

import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.client.EBBlockEntityRenderers;
import com.koomplo.wizardry.setup.registries.client.EBItemProperties;
import com.koomplo.wizardry.setup.registries.client.EBRenderers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class WizardryForgeClient {

    @SuppressWarnings({"unchecked", "deprecation"})
    public static void clientSetup(final FMLClientSetupEvent event) {
        WizardryClientMod.init();
        EBClientEventHelper.register();
        SpellAction.register();
        EBRenderers.registerRenderers();
        EBRenderers.getRenderers().forEach((entity, renderer) ->
                EntityRenderers.register(entity.get(), (EntityRendererProvider<Entity>) renderer)
        );
        EBItemProperties.register();

        // Menu screens are registered on the mod bus via RegisterMenuScreensEvent (see ForgeModClientEvents)

        EBBlockEntityRenderers.init();
        EBBlockEntityRenderers.register(BlockEntityRenderers::register
        );

        // Set render types for blocks that need transparency/cutout
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.VANISHING_COBWEB.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.CRYSTAL_FLOWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.POTTED_CRYSTAL_FLOWER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.OAK_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.SPRUCE_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.BIRCH_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.JUNGLE_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.ACACIA_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.DARK_OAK_LECTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(EBBlocks.IMBUEMENT_ALTAR.get(), RenderType.cutoutMipped());
        });
    }
}
