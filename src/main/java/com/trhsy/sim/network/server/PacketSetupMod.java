package com.trhsy.sim.network.server;

import com.trhsy.sim.loader.ModSimLoader;
import com.trhsy.sim.loader.NetWorkLoader;
import com.trhsy.sim.loader.SimmodeStart;
import com.trhsy.sim.network.client.PacketUpdateMoney;
import com.trhsy.sim.npcCode.store.SimStore;
import com.trhsy.sim.util.Onboarding;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @ClassName PacketSetupMod
 * @Description todo
 * @Author Tian
 * @Date 2022/9/2515:27
 **/
public class PacketSetupMod implements IMessage {
    public int gamemode;
    public PacketSetupMod() {
    }
    public PacketSetupMod(int gamemode) {
        this.gamemode = gamemode;
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        this.gamemode = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.gamemode);
    }

    public static class Handler implements IMessageHandler<PacketSetupMod, IMessage> {
        public Handler() {
        }

        @Override
        public IMessage onMessage(PacketSetupMod message, MessageContext ctx) {
            try {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    this.handle(message, ctx);
                });
            } catch (Exception var4) {
                StackTraceElement element = var4.getStackTrace()[0];
                ModSimLoader.log.error("PacketSetupMod出错了：" + var4.getMessage() + "行数：" + element.getLineNumber());
            }

            return null;
        }

        private void handle(PacketSetupMod message, MessageContext ctx) {
            // P11(S1): 全局模式只能由 OP 修改；单人游戏保留本地设置能力。
            EntityPlayerMP player = ctx.getServerHandler().player;
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            boolean singleplayer = server != null && server.isSinglePlayer();
            if (!singleplayer && (player == null || !player.canUseCommand(2, "sim"))) {
                ModSimLoader.log.warn("非 OP 玩家尝试修改模拟城市模式，已拒绝: {}", player == null ? "null" : player.getName());
                // 回发权威状态，让客户端从“本地已改”恢复，避免界面看起来反复任命。
                if (player != null) {
                    NetWorkLoader.net.sendTo(new PacketUpdateMoney(), player);
                }
                return;
            }
            if (message.gamemode != 0 && message.gamemode != 1 && message.gamemode != 999) {
                ModSimLoader.log.warn("非法模拟城市模式: {}", message.gamemode);
                if (player != null) {
                    NetWorkLoader.net.sendTo(new PacketUpdateMoney(), player);
                }
                return;
            }

            int previous = ModSimLoader.gamemode;
            ModSimLoader.gamemode = message.gamemode;
            ModSimLoader.sim_is_running = message.gamemode != 999;
            SimmodeStart.gamemodeDirty = true;
            if (message.gamemode != 999) {
                // 任命过就永远记住；之后切到“不启动”也不会再要求重新接受任命。
                ModSimLoader.appointed = true;
            }

            // 立即写入 SimStore，不能等世界保存或 15 秒后的延迟加载。
            if (player != null) {
                net.minecraft.world.World saveWorld = server != null ? server.getWorld(0) : player.world;
                SimStore store = SimStore.get(saveWorld);
                if (store != null) {
                    store.gamemode = ModSimLoader.gamemode;
                    store.appointed = ModSimLoader.appointed;
                    if (SimmodeStart.stateLoaded) {
                        store.money = ModSimLoader.money;
                        store.dayOfWeek = ModSimLoader.dayOfWeek;
                        store.gameDay = ModSimLoader.gameDay;
                    }
                    store.markDirty();
                }
            }

            // 第一次从“未启动”进入城市时发一套工具；只有真的发了才提示。
            if (player != null && previous == 999 && message.gamemode != 999) {
                if (Onboarding.giveStarterKit(player)) {
                    Onboarding.send(player, "container.sim.kit.given");
                    Onboarding.send(player, "container.sim.tip.start");
                    Onboarding.send(player, "container.sim.tip.materials");
                }
            }

            // 回执：客户端以服务端状态为准。
            if (player != null) {
                NetWorkLoader.net.sendTo(new PacketUpdateMoney(), player);
            }
        }
    }
}
