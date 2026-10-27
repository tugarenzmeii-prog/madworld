package com.madworld.event;

import com.madworld.item.ModItems;
import com.madworld.util.MadUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Таблицы наград. */
public final class Rewards {
    private Rewards() {}

    private record Entry(Item item, int min, int max, int weight) {}

    private static final Entry[] CHEST = {
            new Entry(Items.DIAMOND, 1, 2, 6), new Entry(Items.EMERALD, 3, 10, 12), new Entry(Items.GOLDEN_APPLE, 1, 2, 8),
            new Entry(Items.IRON_INGOT, 6, 16, 16), new Entry(Items.GOLD_INGOT, 4, 12, 14), new Entry(Items.EXPERIENCE_BOTTLE, 4, 12, 12),
            new Entry(Items.COOKED_BEEF, 8, 16, 12), new Entry(Items.ARROW, 16, 32, 8),
            new Entry(ModItems.LUCKY_DIE, 1, 2, 8), new Entry(ModItems.FORBIDDEN_POTATO, 1, 1, 5),
            new Entry(ModItems.SPEED_SOUP, 1, 2, 6), new Entry(ModItems.BANANA_GRENADE, 3, 8, 8),
            new Entry(ModItems.CHAOS_REMOTE, 1, 1, 2), new Entry(ModItems.TELEPORT_STICK, 1, 1, 3),
            new Entry(ModItems.PIG_MAGNET, 1, 1, 3), new Entry(ModItems.APOCALYPSE_HORN, 1, 1, 2),
            new Entry(Items.NETHERITE_SCRAP, 1, 1, 1), new Entry(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1)
    };

    private static final Entry[] PIG_GIFT = {
            new Entry(Items.IRON_INGOT, 1, 3, 20), new Entry(Items.GOLD_INGOT, 1, 3, 20), new Entry(Items.BREAD, 2, 4, 14),
            new Entry(Items.CARROT, 2, 5, 12), new Entry(Items.EMERALD, 1, 2, 10), new Entry(Items.GOLDEN_CARROT, 1, 3, 10),
            new Entry(Items.EXPERIENCE_BOTTLE, 1, 3, 10), new Entry(Items.ARROW, 4, 8, 8), new Entry(Items.DIAMOND, 1, 1, 2)
    };

    private static ItemStack roll(Entry[] table) {
        int total = 0;
        for (Entry e : table) total += e.weight;
        int r = MadUtil.rng().nextInt(total);
        for (Entry e : table) {
            r -= e.weight;
            if (r < 0) return new ItemStack(e.item, e.min + MadUtil.rng().nextInt(e.max - e.min + 1));
        }
        return new ItemStack(Items.DIRT);
    }

    public static ItemStack chestReward() { return roll(CHEST); }

    public static ItemStack pigGift() { return roll(PIG_GIFT); }
}
