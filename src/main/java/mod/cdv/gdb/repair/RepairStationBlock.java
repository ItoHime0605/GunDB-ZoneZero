package mod.cdv.gdb.repair;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Two-block-high station: ONE item and one visible block model.
 * The top half reserves physical space, with the full model attached to the lower half.
 * The independent RepairStationMenu does not use AnvilMenu.
 */
public class RepairStationBlock extends DoublePlantBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<RepairStationBlock> CODEC = simpleCodec(RepairStationBlock::new);

    private static final VoxelShape TABLE = merge(
        Block.box(1, 0, 1, 3.2, 12, 3.2),
        Block.box(12.8, 0, 1, 15, 12, 3.2),
        Block.box(1, 0, 12.8, 3.2, 12, 15),
        Block.box(12.8, 0, 12.8, 15, 12, 15),
        Block.box(0, 11.7, 0, 16, 14, 16),
        Block.box(1.2, 2.6, 1.2, 14.8, 3.4, 14.8)
    );

    // Do not call the Shapes.or(VoxelShape[]) signature incompatible with 1.21.1.
    private static VoxelShape merge(VoxelShape first, VoxelShape... rest) {
        VoxelShape merged = first;
        for (VoxelShape shape : rest) merged = Shapes.or(merged, shape);
        return merged;
    }

    private static final VoxelShape[] LOWER_SHAPES = new VoxelShape[4];
    private static final VoxelShape[] UPPER_SHAPES = new VoxelShape[4];

    static {
        LOWER_SHAPES[0] = Shapes.or(TABLE, Block.box(1, 14, 13, 15, 16, 16));
        LOWER_SHAPES[1] = Shapes.or(TABLE, Block.box(0, 14, 1, 3, 16, 15));
        LOWER_SHAPES[2] = Shapes.or(TABLE, Block.box(1, 14, 0, 15, 16, 3));
        LOWER_SHAPES[3] = Shapes.or(TABLE, Block.box(13, 14, 1, 16, 16, 15));
        UPPER_SHAPES[0] = Block.box(1, 0, 13, 15, 12.2, 16);
        UPPER_SHAPES[1] = Block.box(0, 0, 1, 3, 12.2, 15);
        UPPER_SHAPES[2] = Block.box(1, 0, 0, 15, 12.2, 3);
        UPPER_SHAPES[3] = Block.box(13, 0, 1, 16, 12.2, 15);
    }

    public RepairStationBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(HALF, DoubleBlockHalf.LOWER)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState base = super.getStateForPlacement(context);
        return base == null ? null : base.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) return true;
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level,
                                  BlockPos pos, CollisionContext context) {
        int index = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
            ? UPPER_SHAPES[index] : LOWER_SHAPES[index];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                           BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
            ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState base = level.getBlockState(basePos);
        if (!base.is(this) || base.getValue(HALF) != DoubleBlockHalf.LOWER) return null;
        return new SimpleMenuProvider(
            (windowId, inventory, player) -> new RepairStationMenu(
                windowId, inventory, ContainerLevelAccess.create(level, basePos)),
            Component.translatable("container.gundb.gun_repair_station")
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider == null) return InteractionResult.FAIL;
            serverPlayer.openMenu(provider);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
