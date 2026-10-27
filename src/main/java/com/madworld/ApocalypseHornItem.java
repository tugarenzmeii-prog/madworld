package com.madworld.item;

import com.madworld.entity.PigEmperorEntity;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import com.madworld.util.Tasks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Рог апокалипсиса: смешная тревога — все мобы в радиусе 24 блоков 8 секунд разбегаются от игрока. */
public class ApocalypseHornItem extends Item {
    public ApocalypseHornItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity p)) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(stack, 600);
        MadUtil.sound(sw, p.getX(), p.getY(), p.getZ(), SoundEvents.EVENT_RAID_HORN, SoundCategory.PLAYERS, 8.0f, 0.8f);

        p.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 40, 10));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Все прочь! Это не учения!").formatted(Formatting.YELLOW)));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("ТРЕВОГА!").formatted(Formatting.RED, Formatting.BOLD)));

        final double ox = p.getX(), oz = p.getZ();
        List<UUID> ids = new ArrayList<>();
        for (MobEntity m : sw.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(24), e -> e.isAlive() && !(e instanceof PigEmperorEntity))) {
            if (ids.size() >= 30) break;
            ids.add(m.getUuid());
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 160, 1));
        }
        Tasks.repeat(160, 10, age -> {
            for (UUID id : ids) {
                Entity e = sw.getEntity(id);
                if (!(e instanceof MobEntity m) || !m.isAlive()) continue;
                double dx = m.getX() - ox, dz = m.getZ() - oz;
                double d = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                m.setTarget(null);
                m.getNavigation().startMovingTo(m.getX() + dx / d * 12, m.getY(), m.getZ() + dz / d * 12, 1.6);
            }
        }, null);
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.5);
        return ActionResult.SUCCESS;
    }
}
