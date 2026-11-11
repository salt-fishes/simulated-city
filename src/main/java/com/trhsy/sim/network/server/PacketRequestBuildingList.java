package com.trhsy.sim.network.server;

import com.trhsy.sim.loader.ModSimLoader;
import com.trhsy.sim.loader.NetWorkLoader;
import com.trhsy.sim.network.client.PacketReturnBuildingList;
import com.trhsy.sim.npcCode.build.Building;
import com.trhsy.sim.npcCode.build.BuildingInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 客户端请求城市建筑列表（建筑传送 GUI 使用）。
 */
public class PacketRequestBuildingList implements IMessage {

    public PacketRequestBuildingList() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<PacketRequestBuildingList, IMessage> {
        public Handler() {
        }

        @Override
        public IMessage onMessage(PacketRequestBuildingList message, MessageContext ctx) {
            try {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    this.handle(message, ctx);
                });
            } catch (Exception e) {
                StackTraceElement element = e.getStackTrace()[0];
                ModSimLoader.log.error("PacketRequestBuildingList出错了：" + e.getMessage() + "行数：" + element.getLineNumber());
            }
            return null;
        }

        private void handle(PacketRequestBuildingList message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null) {
                return;
            }
            List<BuildingInfo> infos = new ArrayList<BuildingInfo>();
            for (Building building : ModSimLoader.buildings) {
                BuildingInfo info = BuildingInfo.fromBuilding(building);
                if (info != null) {
                    infos.add(info);
                }
            }
            // 城市列表按名称排序，客户端也能直接使用这个顺序。
            infos.sort(new Comparator<BuildingInfo>() {
                @Override
                public int compare(BuildingInfo a, BuildingInfo b) {
                    String left = a.name == null ? "" : a.name;
                    String right = b.name == null ? "" : b.name;
                    int byName = String.CASE_INSENSITIVE_ORDER.compare(left, right);
                    if (byName != 0) {
                        return byName;
                    }
                    return Double.compare(a.x, b.x);
                }
            });
            NetWorkLoader.net.sendTo(new PacketReturnBuildingList(infos), player);
        }
    }
}
