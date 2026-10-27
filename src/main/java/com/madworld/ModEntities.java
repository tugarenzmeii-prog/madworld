package com.madworld.entity;

import com.madworld.MadWorldMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModEntities {
    public static final EntityType<CreeperCourierEntity> CREEPER_COURIER = register("creeper_courier",
            EntityType.Builder.create(CreeperCourierEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 1.7f));
    public static final EntityType<ZombieDancerEntity> ZOMBIE_DANCER = register("zombie_dancer",
            EntityType.Builder.create(ZombieDancerEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f));
    public static final EntityType<VillageMadmanEntity> VILLAGE_MADMAN = register("village_madman",
            EntityType.Builder.create(VillageMadmanEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 1.95f));
    public static final EntityType<NinjaChickenEntity> NINJA_CHICKEN = register("ninja_chicken",
            EntityType.Builder.create(NinjaChickenEntity::new, SpawnGroup.CREATURE).dimensions(0.5f, 0.8f));
    public static final EntityType<EliteSkeletonEntity> ELITE_SKELETON = register("elite_skeleton",
            EntityType.Builder.create(EliteSkeletonEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.99f));
    public static final EntityType<PigEmperorEntity> PIG_EMPEROR = register("pig_emperor",
            EntityType.Builder.create(PigEmperorEntity::new, SpawnGroup.MONSTER).dimensions(1.0f, 1.3f).maxTrackingRange(10));
    public static final EntityType<BananaGrenadeEntity> BANANA_GRENADE = register("banana_grenade",
            EntityType.Builder.<BananaGrenadeEntity>create(BananaGrenadeEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f).maxTrackingRange(4).trackingTickInterval(10));
    public static final EntityType<GoldenPotatoEntity> GOLDEN_POTATO = register("golden_potato",
            EntityType.Builder.<GoldenPotatoEntity>create(GoldenPotatoEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f).maxTrackingRange(4).trackingTickInterval(10));

    private ModEntities() {}

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, MadWorldMod.id(name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(CREEPER_COURIER, CreeperCourierEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ZOMBIE_DANCER, ZombieDancerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(VILLAGE_MADMAN, VillageMadmanEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(NINJA_CHICKEN, NinjaChickenEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ELITE_SKELETON, EliteSkeletonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PIG_EMPEROR, PigEmperorEntity.createAttributes());
    }
}
