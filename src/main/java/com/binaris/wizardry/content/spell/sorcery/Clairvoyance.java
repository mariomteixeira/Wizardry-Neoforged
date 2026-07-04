package com.binaris.wizardry.content.spell.sorcery;

import com.binaris.wizardry.api.content.item.ICastItem;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.core.networking.s2c.ClairvoyanceS2C;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Clairvoyance extends Spell {

    /** Ticks each path particle takes to move from one path point to the next. */
    public static final int PARTICLE_MOVEMENT_INTERVAL = 45;

    @Override
    public boolean cast(PlayerCastContext ctx) {
        var caster = ctx.caster();
        WizardDataHolder data = caster.getData(EBAttachments.WIZARD_DATA);

        if (!caster.isShiftKeyDown()) {
            String dimension = data.getClairvoyanceDimension();
            BlockPos location = data.getClairvoyancePos();
            String currentDimension = ctx.world().dimension().location().toString();

            if (dimension != null && currentDimension.equals(dimension)) {
                if (location != null) {
                    if (!ctx.world().isClientSide) {
                        caster.displayClientMessage(Component.translatable("spell.ebwizardry.clairvoyance.searching"), true);

                        double range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

                        // An arbitrary pathfinder mob to borrow ground navigation from (1.12.2 approach)
                        Zombie pathfinderMob = new Zombie(ctx.world());
                        pathfinderMob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(range);
                        pathfinderMob.setPos(caster.getX(), caster.getY(), caster.getZ());
                        pathfinderMob.setPathfindingMalus(PathType.WATER, 0.0F);
                        pathfinderMob.setOnGround(true);

                        Path path = pathfinderMob.getNavigation().createPath(location, 0);
                        pathfinderMob.discard();

                        if (path != null && path.getEndNode() != null) {
                            Node end = path.getEndNode();
                            if (end.x == location.getX() && end.y == location.getY() && end.z == location.getZ()) {
                                this.playSound(ctx.world(), caster, ctx.castingTicks(), -1);

                                if (caster instanceof ServerPlayer serverPlayer) {
                                    List<BlockPos> points = new ArrayList<>();
                                    for (int i = 0; i < path.getNodeCount(); i++) {
                                        Node node = path.getNode(i);
                                        points.add(new BlockPos(node.x, node.y, node.z));
                                    }
                                    Services.NETWORK_HELPER.sendTo(serverPlayer,
                                            new ClairvoyanceS2C(points, ctx.modifiers().get(SpellModifiers.DURATION)));
                                }
                                return true;
                            }
                        }

                        caster.displayClientMessage(Component.translatable("spell.ebwizardry.clairvoyance.outofrange"), true);
                    }
                } else if (!ctx.world().isClientSide) {
                    caster.displayClientMessage(Component.translatable("spell.ebwizardry.clairvoyance.undefined"), true);
                }
            } else if (!ctx.world().isClientSide) {
                caster.displayClientMessage(Component.translatable("spell.ebwizardry.clairvoyance.wrongdimension"), true);
            }
        }

        // Fixes the problem with the sound not playing for the client of the caster
        if (ctx.world().isClientSide) this.playSound(ctx.world(), caster, ctx.castingTicks(), -1);

        return false;
    }

    // Sneak-right-click with clairvoyance selected remembers the location (1.12.2 parity)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) return;

        var stack = event.getItemStack();
        if (stack.getItem() instanceof ICastItem castItem && castItem.getCurrentSpell(stack) instanceof Clairvoyance) {
            WizardDataHolder data = event.getEntity().getData(EBAttachments.WIZARD_DATA);

            BlockPos pos = event.getPos().relative(event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace());
            data.setClairvoyanceLocation(pos, event.getLevel().dimension().location().toString());

            if (!event.getLevel().isClientSide) {
                event.getEntity().displayClientMessage(
                        Component.translatable("spell.ebwizardry.clairvoyance.confirm"), true);
            }
            event.setCanceled(true);
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.SORCERY, SpellType.UTILITY, SpellAction.POINT_UP, 20, 10, 100)
                .add(DefaultProperties.RANGE, 256f)
                .add(DefaultProperties.DURATION, 1800)
                .build();
    }
}
