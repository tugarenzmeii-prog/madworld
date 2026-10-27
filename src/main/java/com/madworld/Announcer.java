package com.madworld.event;

import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.madworld.util.MadUtil;

import java.util.List;

/** Уведомления на экране: большой заголовок + подзаголовок + сообщение в чат. */
public final class Announcer {
    private Announcer() {}

    public static void announce(List<ServerPlayerEntity> players, String title, String subtitle) {
        Text t = Text.literal(title).formatted(Formatting.GOLD, Formatting.BOLD);
        Text s = Text.literal(subtitle).formatted(Formatting.YELLOW);
        for (ServerPlayerEntity p : players) {
            p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
            p.networkHandler.sendPacket(new SubtitleS2CPacket(s));
            p.networkHandler.sendPacket(new TitleS2CPacket(t));
            p.sendMessage(Text.literal("★ ").formatted(Formatting.RED).append(t).append(Text.literal(" — ").formatted(Formatting.GRAY)).append(s), false);
            MadUtil.sound(p.getEntityWorld(), p.getX(), p.getY(), p.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.8f, 1.0f);
        }
    }

    public static void say(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg), false);
    }
}
