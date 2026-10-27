package com.madworld.block;

import com.madworld.event.Rewards;
import com.madworld.item.ModItems;
import com.madworld.madness.MadnessManager;
import com.madworld.util.Advancements;
import com.madworld.util.MadUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Золотой унитаз: декоративный блок. ПКМ — спустить воду (иногда из бачка что-то выпадает; секретное достижение). */
public class GoldenToiletBlock extends HorizontalFacingBlock {
    public static final MapCodec<GoldenToiletBlock> CODEC = createCodec(GoldenToiletBlock::new);
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(2, 0, 2, 14, 9, 14),
            Block.createCuboidShape(4, 9, 11, 12, 16, 14));
    private static final Map<UUID, Long> LAST_FLUSH = new HashMap<>();

    public GoldenToiletBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld sw) || !(player instanceof ServerPlayerEntity sp)) return ActionResult.SUCCESS;
        MadUtil.sound(sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0f, 0.7f);
        MadUtil.sound(sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_VILLAGER_CELEBRATE, SoundCategory.BLOCKS, 1.0f, 1.3f);
        sw.spawnParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 25, 0.25, 0.1, 0.25, 0.1);
        Advancements.grant(sp, "secret_flush");
        long now = sw.getTime();
        Long last = LAST_FLUSH.get(sp.getUuid());
        if (last != null && now - last < 100) return ActionResult.SUCCESS;
        LAST_FLUSH.put(sp.getUuid(), now);
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.0);
        int r = MadUtil.rng().nextInt(100);
        if (r < 12) {
            MadUtil.drop(sw, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, Rewards.pigGift());
            sp.sendMessage(Text.literal("Из бачка что-то булькнуло и выпало."), true);
        } else if (r < 15) {
            MadUtil.drop(sw, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    new ItemStack(MadUtil.rng().nextBoolean() ? ModItems.LUCKY_DIE : ModItems.FORBIDDEN_POTATO));
            sp.sendMessage(Text.literal("Королевский слив! Бачок вернул подарок."), true);
        } else {
            sp.sendMessage(Text.literal("Бульк."), true);
        }
        return ActionResult.SUCCESS;
    }
}
