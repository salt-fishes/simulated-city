package com.trhsy.sim.network.client;

import com.trhsy.sim.loader.ModSimClientLoader;
import com.trhsy.sim.loader.ModSimLoader;
import com.trhsy.sim.npcCode.build.BuildingInfo;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端把城市建筑列表发给客户端。
 */
public class PacketReturnBuildingList implements IMessage {
    private static final int MAX_BUILDINGS = 1000;

    public List<BuildingInfo> buildings = new ArrayList<BuildingInfo>();

    public PacketReturnBuildingList() {
    }

    public PacketReturnBuildingList(List<BuildingInfo> buildings) {
        this.buildings = buildings == null ? new ArrayList<BuildingInfo>() : buildings;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.buildings.clear();
        try {
            int count = buf.readInt();
            if (count < 0 || count > MAX_BUILDINGS) {
                return;
            }
            for (int i = 0; i < count; i++) {
                String id = ByteBufUtils.readUTF8String(buf);
                String name = ByteBufUtils.readUTF8String(buf);
                String type = ByteBufUtils.readUTF8String(buf);
                String jobType = ByteBufUtils.readUTF8String(buf);
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                int dimension = buf.readInt();
                int occupants = buf.readInt();
                this.buildings.add(new BuildingInfo(id, name, type, jobType, x, y, z, dimension, occupants));
            }
        } catch (Exception e) {
            StackTraceElement element = e.getStackTrace()[0];
            ModSimLoader.log.error("PacketReturnBuildingList读取出错了：" + e.getMessage() + "行数：" + element.getLineNumber());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int count = Math.min(this.buildings == null ? 0 : this.buildings.size(), MAX_BUILDINGS);
        buf.writeInt(count);
        for (int i = 0; i < count; i++) {
            BuildingInfo info = this.buildings.get(i);
            if (info == null) {
                info = new BuildingInfo("", "", "", "", 0.0D, 0.0D, 0.0D, 0, 0);
            }
            ByteBufUtils.writeUTF8String(buf, info.id == null ? "" : info.id);
            ByteBufUtils.writeUTF8String(buf, info.name == null ? "" : info.name);
            ByteBufUtils.writeUTF8String(buf, info.type == null ? "" : info.type);
            ByteBufUtils.writeUTF8String(buf, info.jobType == null ? "" : info.jobType);
            buf.writeDouble(info.x);
            buf.writeDouble(info.y);
            buf.writeDouble(info.z);
            buf.writeInt(info.dimension);
            buf.writeInt(info.occupants);
        }
    }

    public static class Handler implements IMessageHandler<PacketReturnBuildingList, IMessage> {
        public Handler() {
        }

        @Override
        public IMessage onMessage(PacketReturnBuildingList message, MessageContext ctx) {
            try {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    this.handle(message, ctx);
                });
            } catch (Exception e) {
                StackTraceElement element = e.getStackTrace()[0];
                ModSimLoader.log.error("PacketReturnBuildingList出错了：" + e.getMessage() + "行数：" + element.getLineNumber());
            }
            return null;
        }

        private void handle(PacketReturnBuildingList message, MessageContext ctx) {
            ModSimClientLoader.applyBuildingList(message.buildings);
        }
    }
}
