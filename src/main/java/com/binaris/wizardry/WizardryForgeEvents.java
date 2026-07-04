package com.binaris.wizardry;

import com.binaris.wizardry.api.content.event.*;
import com.binaris.wizardry.setup.registries.RegisterFunction;
import com.binaris.wizardry.capabilities.*;
import com.binaris.wizardry.core.PropertiesForgeDataManager;
import com.binaris.wizardry.core.config.EBCommonConfig;
import com.binaris.wizardry.core.event.WizardryEventBus;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.core.registry.EBRegistries;
import com.binaris.wizardry.network.ArcaneLockSyncPacketS2C;
import com.binaris.wizardry.setup.registries.*;
import com.binaris.wizardry.setup.registries.client.EBParticleProviders;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import com.binaris.wizardry.setup.registries.client.EBRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

/**
 * This class contains
 * - {@link ForgeBusEvents} : Events that are fired by Forge
 * - {@link ModBusEvents} : Events that are fired by the mod
 * - {@link ModBusEventsClient} : Events that are fired by the mod on the client
 */
public class WizardryForgeEvents {

    @EventBusSubscriber(modid = WizardryMainMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onServerAboutToStart(ServerAboutToStartEvent event) {
            WizardryEventBus.getInstance().fire(new EBServerLoad(event.getServer()));
        }

        @SubscribeEvent
        public static void onWorldLoadEvent(final LevelEvent.Load event) {
            if (event.getLevel().isClientSide()) return;
            WizardryEventBus.getInstance().fire(new EBServerLevelLoadEvent((ServerLevel) event.getLevel()));
        }

        @SubscribeEvent
        public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
            WizardryEventBus.getInstance().fire(new EBPlayerJoinServerEvent(event.getEntity(), event.getEntity().getServer()));
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                player.getData(EBAttachments.WIZARD_DATA).sync();
                player.getData(EBAttachments.CAST_COMMAND_DATA).sync();
                player.getData(EBAttachments.SPELL_MANAGER_DATA).sync();
            }
        }

        @SubscribeEvent
        public static void onLootTableLoadEvent(LootTableLoadEvent event) {
            com.binaris.wizardry.setup.registries.EBLootTables.applyInjections((location, pool) -> {
                if (event.getName().equals(location)) {
                    event.getTable().addPool(pool);
                }
            });
        }

        @SubscribeEvent
        public static void registerReloadListeners(AddReloadListenerEvent event) {
            event.addListener(new PropertiesForgeDataManager());
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            EBCommands.COMMANDS_TO_REGISTER.forEach(c -> c.accept(event.getDispatcher(), event.getBuildContext()));
            if (Services.PLATFORM.isDevelopmentEnvironment() || EBCommonConfig.ENABLE_DEBUG_COMMANDS.get()) {
                EBCommands.DEBUG_COMMANDS.forEach(c -> c.accept(event.getDispatcher(), event.getBuildContext()));
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            if (WizardryEventBus.getInstance().fire(new EBPlayerInteractEntityEvent(event.getEntity(), event.getTarget())))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onBlockUse(PlayerInteractEvent.RightClickBlock event) {
            if (WizardryEventBus.getInstance().fire(new EBPlayerUseBlockEvent(event.getEntity(), event.getLevel(), event.getPos(), event.getHand())))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            if (WizardryEventBus.getInstance().fire(new EBPlayerBreakBlockEvent(event.getPlayer(), (Level) event.getLevel(), event.getPos())))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onChunkWatch(ChunkWatchEvent.Watch event) {
            // Sync ArcaneLock data for all block entities in the chunk when a player starts watching it
            ServerLevel level = event.getLevel();
            LevelChunk chunk = level.getChunk(event.getPos().x, event.getPos().z);

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof BaseContainerBlockEntity) {
                    ArcaneLockDataHolder data = blockEntity.getData(EBAttachments.ARCANE_LOCK_DATA);
                    if (data.isArcaneLocked()) {
                        // Send sync packet to the player
                        ArcaneLockSyncPacketS2C packet =
                                new ArcaneLockSyncPacketS2C(blockEntity.getBlockPos(), data.serializeNBT(level.registryAccess()));
                        Services.NETWORK_HELPER.sendTo(event.getPlayer(), packet);
                    }
                }
            }
        }

        // Note: death-copy for WizardDataHolder/SpellManagerDataHolder/CastCommandDataHolder is handled
        // automatically by NeoForge (AttachmentType.Builder#copyOnDeath, see EBAttachments), which internally
        // subscribes to PlayerEvent.Clone and copies serializable attachments flagged copyOnDeath. No manual
        // clone handler is needed here anymore.

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            // Sync all capabilities to client after respawn to ensure the client has the correct data
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                player.getData(EBAttachments.WIZARD_DATA).sync();
                player.getData(EBAttachments.SPELL_MANAGER_DATA).sync();
                player.getData(EBAttachments.CAST_COMMAND_DATA).sync();
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            // Sync all capabilities to client after dimension change to ensure the client has the correct data
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                player.getData(EBAttachments.WIZARD_DATA).sync();
                player.getData(EBAttachments.SPELL_MANAGER_DATA).sync();
                player.getData(EBAttachments.CAST_COMMAND_DATA).sync();
            }
        }
    }

    @EventBusSubscriber(modid = WizardryMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            if (event.getRegistryKey() == Registries.MOB_EFFECT)
                register(event, EBMobEffects::register);
            else if (event.getRegistryKey() == Registries.BLOCK)
                register(event, EBBlocks::register);
            else if (event.getRegistryKey() == Registries.BLOCK_ENTITY_TYPE)
                register(event, EBBlockEntities::register);
            else if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB)
                register(event, EBCreativeTabs::register);
            else if (event.getRegistryKey() == Registries.ENTITY_TYPE)
                register(event, EBEntities::register);
            else if (event.getRegistryKey() == Registries.ARMOR_MATERIAL)
                register(event, EBArmorMaterials::register);
            else if (event.getRegistryKey() == Registries.ITEM)
                register(event, EBItems::register);
            else if (event.getRegistryKey() == Registries.PARTICLE_TYPE) register(event, EBParticles::registerType);
            else if (event.getRegistryKey() == Registries.SOUND_EVENT)
                register(event, EBSounds::register);
            else if (event.getRegistryKey() == Registries.LOOT_FUNCTION_TYPE)
                register(event, EBLootFunctions::register);
            else if (event.getRegistryKey() == Registries.MENU)
                register(event, EBMenus::register);
            else if (event.getRegistryKey() == Registries.ATTRIBUTE)
                register(event, EBAttributes::register);
            else if (event.getRegistryKey() == Registries.RECIPE_TYPE)
                register(event, EBRecipeTypes::register);
            else if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER)
                register(event, EBRecipeTypes::registerSerializers);
            else if (event.getRegistryKey() == EBRegistries.ELEMENT) registerForge(event, Elements::registerNull);
            else if (event.getRegistryKey() == EBRegistries.TIER)
                registerForge(event, SpellTiers::registerNull);
            else if (event.getRegistryKey() == EBRegistries.SPELL)
                registerForge(event, Spells::registerNull);
            else if (event.getRegistryKey() == Registries.TRIGGER_TYPE)
                register(event, EBAdvancementTriggers::register);
        }

        /**
         * Helps to register custom registries and keeping this system
         */
        @SuppressWarnings("unchecked")
        private static <T> void registerForge(RegisterEvent event, Consumer<RegisterFunction<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register((net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>>) event.getRegistryKey(), id, () -> value));
        }

        private static <T> void register(RegisterEvent event, Consumer<RegisterFunction<T>> consumer) {
            consumer.accept((registry, id, value) -> event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void createEntityAttributes(EntityAttributeCreationEvent event) {
            EBEntities.registerAttributes(event::put);
        }

        @SubscribeEvent
        public static void registerSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
            EBEntities.registerSpawns(event);
        }

        @SubscribeEvent
        public static void registerTests(RegisterGameTestsEvent event) {
        }

        @SubscribeEvent
        public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
            e.getTypes().forEach(entity -> EBAttributes.getAttributes().forEach(attribute -> e.add(entity, EBAttributes.holder(attribute))));
        }
    }

    @EventBusSubscriber(modid = WizardryMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEventsClient {
        @SubscribeEvent
        public static void registerProviders(RegisterParticleProvidersEvent event) {
            EBParticleProviders.registerProvider((type, provider) ->
                    event.registerSpriteSet(type.get(), provider::apply)
            );
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            EBRenderers.createEntityLayers(event::registerLayerDefinition);
        }
    }
}
