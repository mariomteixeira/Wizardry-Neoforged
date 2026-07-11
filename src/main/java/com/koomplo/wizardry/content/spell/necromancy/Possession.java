package com.koomplo.wizardry.content.spell.necromancy;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.capabilities.WizardDataHolder;
import com.koomplo.wizardry.content.entity.living.DecoyEntity;
import com.koomplo.wizardry.content.entity.projectile.DarknessOrbEntity;
import com.koomplo.wizardry.content.entity.projectile.IceShardEntity;
import com.koomplo.wizardry.content.entity.projectile.LargeMagicFireballEntity;
import com.koomplo.wizardry.content.entity.projectile.LightningDiscEntity;
import com.koomplo.wizardry.content.entity.projectile.MagicFireballEntity;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.core.networking.s2c.PossessionS2C;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.EBAttachments;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** "Become thy enemy" — the caster takes over the body of the struck creature (1.12.2 Possession). */
public class Possession extends RaySpell {

    /** The NBT tag name for storing the possessed flag in the target's persistent data (rendering only). */
    public static final String NBT_KEY = "possessed";
    /** The NBT tag name for storing the possessor's previous inventory in their persistent data. */
    public static final String INVENTORY_NBT_KEY = "prevInventory";

    public static final SpellProperty<Float> CRITICAL_HEALTH = SpellProperty.floatProperty("critical_health");

    private static final int PROJECTILE_COOLDOWN = 30;

    private static final Map<net.minecraft.world.entity.EntityType<?>, Function<Level, ? extends Projectile>> PROJECTILES = new HashMap<>();
    private static final Set<Class<? extends Mob>> BLACKLIST = new HashSet<>();

    private static final ResourceLocation SPEED_MODIFIER_ID = WizardryMainMod.location("possession_speed");
    private static final ResourceLocation DAMAGE_MODIFIER_ID = WizardryMainMod.location("possession_damage");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = WizardryMainMod.location("possession_knockback");

    static {
        PROJECTILES.put(net.minecraft.world.entity.EntityType.SNOW_GOLEM,
                l -> new Snowball(net.minecraft.world.entity.EntityType.SNOWBALL, l)); // Woooo snowballs!
        PROJECTILES.put(net.minecraft.world.entity.EntityType.BLAZE, MagicFireballEntity::new);
        PROJECTILES.put(net.minecraft.world.entity.EntityType.GHAST, LargeMagicFireballEntity::new);
        PROJECTILES.put(com.koomplo.wizardry.setup.registries.EBEntities.ICE_WRAITH.get(), IceShardEntity::new);
        PROJECTILES.put(com.koomplo.wizardry.setup.registries.EBEntities.SHADOW_WRAITH.get(), DarknessOrbEntity::new);
        PROJECTILES.put(com.koomplo.wizardry.setup.registries.EBEntities.STORM_ELEMENTAL.get(), LightningDiscEntity::new);
        PROJECTILES.put(net.minecraft.world.entity.EntityType.WITCH,
                l -> new ThrownPotion(net.minecraft.world.entity.EntityType.POTION, l));

        BLACKLIST.add(DecoyEntity.class);
    }

    @Override
    public boolean requiresPacket() {
        return false; // Has its own packet
    }

    @Override
    public boolean canCastByEntity() {
        return false;
    }

    @Override
    public boolean canCastByLocation() {
        return false;
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        Entity target = entityHit.getEntity();

        if (target instanceof Mob mob && !BLACKLIST.contains(target.getClass())
                && ctx.caster() instanceof Player player && !isPossessing(player)) {

            if (!player.isCreative() && player.getHealth() <= property(CRITICAL_HEALTH)) {
                player.displayClientMessage(Component.translatable("spell.ebwizardry.possession.insufficienthealth",
                        target.getName()), true);
                return false;
            }

            if (!ctx.world().isClientSide) {
                int duration = (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION));
                return possess(player, mob, duration);
            }
            return true;
        }

        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    // ================================================ Core logic ================================================

    public boolean possess(Player possessor, Mob target, int duration) {

        if (possessor.isShiftKeyDown()) return false;

        WizardDataHolder data = possessor.getData(EBAttachments.WIZARD_DATA);

        data.setPossessee(target);
        data.setPossessionTimer(duration);

        possessor.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());

        target.stopRiding();
        target.discard();
        target.setNoAi(true);
        target.setTarget(null);
        target.getPersistentData().putBoolean(NBT_KEY, true);

        // Flying mobs grant flight
        if (target instanceof FlyingMob || target instanceof FlyingAnimal) {
            possessor.getAbilities().mayfly = true;
            possessor.getAbilities().flying = true;
            possessor.onUpdateAbilities();
        }

        // Attribute inheritance: the player's value becomes the target's value
        // TODO marco 6: pular atributos vindos do equipamento do mob (1.12.2 evitava herdar a espada do piglin)
        inheritAttribute(possessor, target, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, possessor.getAbilities().getWalkingSpeed() / 0.1f);
        inheritAttribute(possessor, target, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, 1);
        inheritAttribute(possessor, target, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID, 1);

        if (!possessor.level().isClientSide) {

            // Mobs are dumb: if a player possesses something they're like "Huh?! Where'd you go?"
            for (Mob creature : EntityUtil.getEntitiesWithinRadius(16, possessor.getX(), possessor.getY(), possessor.getZ(),
                    possessor.level(), Mob.class)) {
                if (creature.getTarget() == possessor && !creature.canAttackType(target.getType())) {
                    creature.setTarget(null);
                }
            }

            // Inventory and items
            possessor.getPersistentData().put(INVENTORY_NBT_KEY, possessor.getInventory().save(new ListTag()));
            possessor.getInventory().clearContent();

            ItemStack stack = target.getMainHandItem().copy();

            if (stack.getItem() instanceof BowItem) {
                Holder<Enchantment> infinity = possessor.level().registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY);
                stack.enchant(infinity, 1);
                ItemStack arrow = new ItemStack(Items.ARROW);
                if (target instanceof Stray) {
                    arrow = new ItemStack(Items.TIPPED_ARROW);
                    arrow.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(Potions.SLOWNESS));
                }
                possessor.setItemInHand(InteractionHand.OFF_HAND, arrow);
            }

            possessor.setItemSlot(EquipmentSlot.MAINHAND, stack);
            possessor.getInventory().selected = 0;
            possessor.inventoryMenu.broadcastChanges();

            // Notify clients (dimension-wide covers tracking + self)
            Services.NETWORK_HELPER.sendToDimension(possessor.getServer(),
                    PossessionS2C.start(possessor, target, duration), possessor.level().dimension());
        }

        return true;
    }

    private static void inheritAttribute(Player possessor, Mob target, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                         ResourceLocation modifierId, float playerValueDivisor) {
        AttributeInstance targetInstance = target.getAttribute(attribute);
        AttributeInstance playerInstance = possessor.getAttribute(attribute);
        if (targetInstance == null || playerInstance == null) return;

        double targetValue = targetInstance.getValue();
        double currentValue = playerInstance.getValue() / playerValueDivisor;
        if (currentValue == 0) return;

        playerInstance.removeModifier(modifierId);
        playerInstance.addTransientModifier(new AttributeModifier(modifierId,
                targetValue / currentValue - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public void endPossession(Player player) {

        WizardDataHolder data = player.getData(EBAttachments.WIZARD_DATA);
        Mob victim = data.getPossessee();

        // Reverts the possessed entity back to normal
        if (victim != null) {
            victim.revive();
            victim.setNoAi(false);
            victim.getPersistentData().remove(NBT_KEY);
            victim.setPos(player.getX(), player.getY(), player.getZ());

            if (!player.level().isClientSide) {
                player.level().addFreshEntity(victim);

                for (var effect : player.getActiveEffects()) {
                    victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect));
                }
            }
        }

        // Reverts the player back to normal
        player.removeAllEffects();

        data.setPossessionTimer(0);
        data.setPossessee(null);

        if (!player.getAbilities().instabuild) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        removeIfPresent(player, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID);
        removeIfPresent(player, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID);
        removeIfPresent(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID);

        if (player instanceof ServerPlayer serverPlayer) {
            player.getInventory().clearContent();
            if (player.getPersistentData().contains(INVENTORY_NBT_KEY)) {
                player.getInventory().load(player.getPersistentData().getList(INVENTORY_NBT_KEY, Tag.TAG_COMPOUND));
                player.getPersistentData().remove(INVENTORY_NBT_KEY);
            }
            player.inventoryMenu.broadcastChanges();

            playSuffixedSound(player.level(), player, "end");

            Services.NETWORK_HELPER.sendToDimension(serverPlayer.getServer(),
                    PossessionS2C.end(player), player.level().dimension());
        }
    }

    private static void removeIfPresent(Player player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    @Nullable
    public static Mob getPossessee(Player player) {
        return player.getData(EBAttachments.WIZARD_DATA).getPossessee();
    }

    public static boolean isPossessing(Player player) {
        return getPossessee(player) != null;
    }

    // ================================================ Ticking ================================================

    public static void onLivingTick(com.koomplo.wizardry.api.content.event.EBLivingTick event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) return;

        WizardDataHolder data = player.getData(EBAttachments.WIZARD_DATA);
        Mob possessee = data.getPossessee();
        int timer = data.getPossessionTimer();

        data.setPossessionShootCooldown(Math.max(data.getPossessionShootCooldown() - 1, 0));

        if (possessee != null) {
            // Keep the possessed body in step with the player (it isn't in the world, but mob logic still runs)
            possessee.setPos(player.getX(), player.getY(), player.getZ());
            possessee.setDeltaMovement(player.getDeltaMovement());
            possessee.setOnGround(player.onGround());
            possessee.setYRot(player.getYRot());
            possessee.setXRot(player.getXRot());
            possessee.tickCount++;

            if (possessee.getHealth() <= 0) {
                possession().endPossession(player);
                return;
            }

            // Abilities (1.12.2 addAbility lambdas)
            if (possessee instanceof Spider || possessee instanceof CaveSpider) {
                if (player.horizontalCollision) player.setDeltaMovement(player.getDeltaMovement().x, 0.2, player.getDeltaMovement().z);
            }
            if (possessee instanceof Chicken && !player.onGround() && player.getDeltaMovement().y < 0) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0.6, 1));
            }
            if (!possessee.fireImmune() && player.isOnFire()) {
                // (fire-immune possessees extinguish the player in 1.12.2 — inverted guard kept verbatim)
            } else if (player.isOnFire() && possessee.fireImmune()) {
                player.clearFire();
            }

            if (timer > 0) {
                if (!player.isShiftKeyDown()) {
                    data.setPossessionTimer(timer - 1);
                } else {
                    possession().endPossession(player);
                }
            } else {
                possession().endPossession(player);
            }
        }
    }

    private static Possession possession() {
        return (Possession) com.koomplo.wizardry.setup.registries.Spells.POSSESSION;
    }

    // ================================================ Event handlers ================================================

    /** Damage to a possessing player is diverted through the possessed body; the player takes half. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return;

        Mob possessee = getPossessee(player);
        if (possessee == null) return;

        event.setCanceled(true);

        possessee.hurt(event.getSource(), event.getAmount());

        if (!player.getAbilities().instabuild) {
            player.hurt(player.damageSources().fellOutOfWorld(), event.getAmount() / 2);
        }

        float critical = com.koomplo.wizardry.setup.registries.Spells.POSSESSION.property(CRITICAL_HEALTH);
        if (player.getHealth() <= critical) {
            player.setHealth(critical);
            possession().endPossession(player);
        }
    }

    /** Mobs can't target a player possessing something they wouldn't normally attack. */
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player && event.getEntity() instanceof Mob attacker) {
            Mob possessee = getPossessee(player);
            if (possessee != null && !attacker.canAttackType(possessee.getType())) {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && isPossessing(player)) {
            possession().endPossession(player); // Just in case, to make sure the player drops their items
        }
    }

    /** Called via packets to shoot a projectile if the entity currently possessed by the given player can do so. */
    public static void shootProjectile(Player possessor) {

        WizardDataHolder data = possessor.getData(EBAttachments.WIZARD_DATA);
        if (data.getPossessionShootCooldown() > 0) return;

        Mob possessee = getPossessee(possessor);
        if (possessee == null) return;

        if (possessee instanceof Creeper creeper) {
            possession().endPossession(possessor);
            creeper.ignite(); // Aaaaaaand.... RUN!
        }

        Function<Level, ? extends Projectile> factory = PROJECTILES.get(possessee.getType());

        if (factory != null) {
            Projectile projectile = factory.apply(possessor.level());
            Vec3 look = possessor.getLookAngle();
            projectile.setPos(possessor.getX() + look.x, possessor.getY() + possessor.getEyeHeight() + look.y, possessor.getZ() + look.z);
            projectile.setOwner(possessor);
            projectile.shoot(look.x, look.y, look.z, 1.6f, EntityUtil.getDefaultAimingError(possessor.level().getDifficulty()));

            if (projectile instanceof ThrownPotion potion) {
                potion.setItem(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HARMING));
            }

            possessor.level().addFreshEntity(projectile);
        }

        data.setPossessionShootCooldown(PROJECTILE_COOLDOWN);
    }

    /** Melee attacks are blocked for mobs with no melee AI (and creepers, which oddly have one). */
    public static boolean shouldBlockMeleeAttack(Player player) {
        Mob possessee = getPossessee(player);
        if (possessee == null) return false;
        if (possessee instanceof Creeper) return true;
        return possessee.goalSelector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() instanceof MeleeAttackGoal);
    }

    private void playSuffixedSound(Level world, LivingEntity entity, String suffix) {
        SoundEvent sound = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                getLocation().getNamespace(), "spell." + getLocation().getPath() + "." + suffix));
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, getVolume(), getPitch());
    }

    @Override
    protected void playSound(Level world, LivingEntity entity, int castTicks, int duration) {
        playSuffixedSound(world, entity, "possess");
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.NECROMANCY, SpellType.ALTERATION, SpellAction.POINT, 100, 15, 300)
                .add(DefaultProperties.RANGE, 8f)
                .add(DefaultProperties.EFFECT_DURATION, 600)
                .add(CRITICAL_HEALTH, 1f)
                .build();
    }
}
