package com.madworld.block;

import com.madworld.MadWorldMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;

public final class ModBlocks {
    public static final Block GOLDEN_TOILET;
    public static final Item GOLDEN_TOILET_ITEM;

    static {
        RegistryKey<Block> bk = RegistryKey.of(RegistryKeys.BLOCK, MadWorldMod.id("golden_toilet"));
        GOLDEN_TOILET = Registry.register(Registries.BLOCK, bk, new GoldenToiletBlock(
                AbstractBlock.Settings.create().registryKey(bk).mapColor(MapColor.GOLD).strength(2.0f, 6.0f)
                        .sounds(BlockSoundGroup.METAL).nonOpaque()));
        RegistryKey<Item> ik = RegistryKey.of(RegistryKeys.ITEM, MadWorldMod.id("golden_toilet"));
        GOLDEN_TOILET_ITEM = Registry.register(Registries.ITEM, ik, new BlockItem(GOLDEN_TOILET,
                new Item.Settings().registryKey(ik).useBlockPrefixedTranslationKey().rarity(Rarity.RARE)));
    }

    private ModBlocks() {}

    public static void init() {
        // загрузка класса запускает статическую регистрацию
    }
}
