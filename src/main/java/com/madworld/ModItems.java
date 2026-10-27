package com.madworld.item;

import com.madworld.MadWorldMod;
import com.madworld.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public final class ModItems {
    public static final Item BANANA_GRENADE = reg("banana_grenade", s -> new BananaGrenadeItem(s.maxCount(16)));
    public static final Item BANANA_PEEL = reg("banana_peel", s -> new Item(s.maxCount(64)));
    public static final Item CHAOS_REMOTE = reg("chaos_remote", s -> new ChaosRemoteItem(s.maxCount(1).rarity(Rarity.RARE)));
    public static final Item HAMMER_OF_FATE = reg("hammer_of_fate", s -> new Item(s.sword(ToolMaterial.GOLD, 6.0f, -3.2f).maxDamage(400).rarity(Rarity.EPIC)));
    public static final Item SPEED_SOUP = reg("speed_soup", s -> new SpeedSoupItem(s.maxCount(1)
            .food(new FoodComponent.Builder().nutrition(6).saturationModifier(0.6f).alwaysEdible().build())
            .useRemainder(Items.BOWL)));
    public static final Item LUCKY_DIE = reg("lucky_die", s -> new LuckyDieItem(s.maxCount(16).rarity(Rarity.UNCOMMON)));
    public static final Item APOCALYPSE_HORN = reg("apocalypse_horn", s -> new ApocalypseHornItem(s.maxCount(1).rarity(Rarity.RARE)));
    public static final Item TELEPORT_STICK = reg("teleport_stick", s -> new TeleportStickItem(s.maxCount(1).rarity(Rarity.RARE)));
    public static final Item GOLDEN_TOILET = ModBlocks.GOLDEN_TOILET_ITEM;
    public static final Item PIG_MAGNET = reg("pig_magnet", s -> new PigMagnetItem(s.maxCount(1).rarity(Rarity.RARE)));
    public static final Item FORBIDDEN_POTATO = reg("forbidden_potato", s -> new ForbiddenPotatoItem(s.maxCount(16).rarity(Rarity.EPIC)
            .enchantmentGlintOverride(true)
            .food(new FoodComponent.Builder().nutrition(3).saturationModifier(0.3f).alwaysEdible().build())));
    public static final Item GOLDEN_POTATO = reg("golden_potato", s -> new GoldenPotatoItem(s.maxCount(16).rarity(Rarity.UNCOMMON)));
    public static final Item EMPEROR_CROWN = reg("emperor_crown", s -> new Item(s.maxCount(1).rarity(Rarity.EPIC).enchantmentGlintOverride(true)));

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, MadWorldMod.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(CHAOS_REMOTE))
                    .displayName(Text.translatable("itemGroup.madworld"))
                    .entries((ctx, entries) -> {
                        entries.add(CHAOS_REMOTE);
                        entries.add(BANANA_GRENADE);
                        entries.add(BANANA_PEEL);
                        entries.add(HAMMER_OF_FATE);
                        entries.add(SPEED_SOUP);
                        entries.add(LUCKY_DIE);
                        entries.add(APOCALYPSE_HORN);
                        entries.add(TELEPORT_STICK);
                        entries.add(PIG_MAGNET);
                        entries.add(FORBIDDEN_POTATO);
                        entries.add(GOLDEN_POTATO);
                        entries.add(GOLDEN_TOILET);
                        entries.add(EMPEROR_CROWN);
                    }).build());

    private ModItems() {}

    private static <T extends Item> T reg(String name, Function<Item.Settings, T> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, MadWorldMod.id(name));
        return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().registryKey(key)));
    }

    public static void init() {
        // загрузка класса запускает статическую регистрацию
    }
}
