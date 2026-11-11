package com.trhsy.sim.network.server;

import com.trhsy.sim.loader.ModSimLoader;
import com.trhsy.sim.npcCode.V3;
import com.trhsy.sim.npcCode.build.Building;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 玩家点击建筑传送列表后，由服务端执行传送。
 *
 * <p>服务端统一做目标校验和安全落点计算，客户端只负责发建筑 UUID。</p>
 */
public class PacketTeleportToBuilding implements IMessage {
    private String buildingId;

    public PacketTeleportToBuilding() {
    }

    public PacketTeleportToBuilding(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buildingId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.buildingId == null ? "" : this.buildingId);
    }

    public static class Handler implements IMessageHandler<PacketTeleportToBuilding, IMessage> {
        public Handler() {
        }

        @Override
        public IMessage onMessage(PacketTeleportToBuilding message, MessageContext ctx) {
            try {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    this.handle(message, ctx);
                });
            } catch (Exception e) {
                StackTraceElement element = e.getStackTrace()[0];
                ModSimLoader.log.error("PacketTeleportToBuilding出错了：" + e.getMessage() + "行数：" + element.getLineNumber());
            }
            return null;
        }

        private void handle(PacketTeleportToBuilding message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null) {
                return;
            }
            if (message.buildingId == null || message.buildingId.isEmpty()) {
                player.sendStatusMessage(new TextComponentTranslation("container.sim.teleport.fail", new Object[0]), true);
                return;
            }
            Building building = ModSimLoader.getBuildingByUUID(message.buildingId);
            if (building == null) {
                player.sendStatusMessage(new TextComponentTranslation("container.sim.teleport.fail", new Object[0]), true);
                return;
            }
            V3 target = building.livingXYZ != null ? building.livingXYZ : building.controlXYZ;
            if (target == null) {
                player.sendStatusMessage(new TextComponentTranslation("container.sim.teleport.no_target", new Object[0]), true);
                return;
            }
            int targetDimension = target.dimension != 0 ? target.dimension : building.dimension;
            if (targetDimension != player.dimension) {
                player.sendStatusMessage(new TextComponentTranslation("container.sim.teleport.other_dimension", new Object[0]), true);
                return;
            }
            World world = player.world;
            BlockPos base = new BlockPos(target.x, target.y, target.z);
            // 先确保目标区块在服务端已加载，避免 setPositionAndUpdate 后实体落在未加载区块。
            world.getChunkFromBlockCoords(base);
            BlockPos safe = findSafeSpot(world, base);
            if (safe == null) {
                player.sendStatusMessage(new TextComponentTranslation("container.sim.teleport.fail", new Object[0]), true);
                return;
            }
            player.dismountRidingEntity();
            player.motionX = 0.0D;
            player.motionY = 0.0D;
            player.motionZ = 0.0D;
            player.fallDistance = 0.0F;
            player.setPositionAndUpdate(safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D);
            player.fallDistance = 0.0F;
            player.closeScreen();
            player.sendStatusMessage(new TextComponentTranslation("container.sim.teleport.done",
                    building.buildingName == null ? "" : building.buildingName), true);
        }

        /**
         * 从目标点向上/向下找两格高、脚下有实心方块的落点。
         */
        private static BlockPos findSafeSpot(World world, BlockPos base) {
            for (int offset = 0; offset <= 4; offset++) {
                BlockPos up = base.up(offset);
                if (isSafe(world, up)) {
                    return up;
                }
                if (offset > 0) {
                    BlockPos down = base.down(offset);
                    if (isSafe(world, down)) {
                        return down;
                    }
                }
            }
            return null;
        }

        private static boolean isSafe(World world, BlockPos feet) {
            return isPassable(world, feet)
                    && isPassable(world, feet.up())
                    && isGround(world, feet.down());
        }

        private static boolean isPassable(World world, BlockPos pos) {
            IBlockState state = world.getBlockState(pos);
            return state.getBlock().isAir(state, world, pos) || state.getBlock().isPassable(world, pos);
        }

        private static boolean isGround(World world, BlockPos pos) {
            IBlockState state = world.getBlockState(pos);
            return state.getMaterial().isSolid() && !state.getBlock().isPassable(world, pos);
        }
    }
}
