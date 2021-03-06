package hmysjiang.potioncapsule.network.packets;

import java.util.function.Supplier;

import hmysjiang.potioncapsule.PotionCapsule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SPacketVisualExplosion {
	
	public SPacketVisualExplosion() {}

	public static void encode(SPacketVisualExplosion message, PacketBuffer buf) {}

	public static SPacketVisualExplosion decode(PacketBuffer buf) {
		return new SPacketVisualExplosion();
	}

	public static void handle(SPacketVisualExplosion message, Supplier<Context> ctx) {
		PlayerEntity player = PotionCapsule.proxy.getPlayer();
		if (player != null) {
			player.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.posX, player.posY, player.posZ, 1.0D, 0.0D, 0.0D);
			player.world.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, (1.0F + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.2F) * 0.7F, true);
		}
		ctx.get().setPacketHandled(true);
	}
	
}
