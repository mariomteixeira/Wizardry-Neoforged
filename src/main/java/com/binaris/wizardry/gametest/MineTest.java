package com.binaris.wizardry.gametest;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.core.gametest.GST;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@SuppressWarnings("unused")
@PrefixGameTestTemplate(false)
@GameTestHolder(WizardryMainMod.MOD_ID)
public class MineTest {

    @GameTest(template = "empty_3x3x3")
    public static void mineBreaksBlock(GameTestHelper helper) {
        BlockPos target = new BlockPos(1, 1, 1);
        helper.setBlock(target, Blocks.STONE);

        Player player = GST.mockPlayer(helper, new Vec3(1.5, 2.0, 1.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, EBItems.NOVICE_WAND.get().getDefaultInstance());
        player.setXRot(90); // olhar reto para baixo, direto no bloco
        player.setYRot(0);

        boolean result = Spells.MINE.cast(new PlayerCastContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, 0, new SpellModifiers()));

        GST.assertTrue(helper, "Mine cast should return true when aimed at breakable stone", result);
        helper.succeedWhen(() -> helper.assertBlockNotPresent(Blocks.STONE, target));
    }
}
