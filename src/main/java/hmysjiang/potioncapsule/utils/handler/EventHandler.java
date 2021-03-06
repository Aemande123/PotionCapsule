package hmysjiang.potioncapsule.utils.handler;

import java.util.Random;

import hmysjiang.potioncapsule.configs.CommonConfigs;
import hmysjiang.potioncapsule.container.ContainerPendant;
import hmysjiang.potioncapsule.init.ModBlocks;
import hmysjiang.potioncapsule.init.ModItems;
import hmysjiang.potioncapsule.items.ItemSpecialCapsule;
import hmysjiang.potioncapsule.items.ItemSpecialCapsule.EnumSpecialType;
import hmysjiang.potioncapsule.potions.effects.EffectNekomimiParadise;
import hmysjiang.potioncapsule.utils.helper.InventoryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.CapabilityItemHandler;

@EventBusSubscriber(bus=EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	private static Random rand = new Random();
	private static DamageSource dmg = new DamageSource("pc_void_lost");
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			ItemStack pendant = InventoryHelper.findStackFromPlayerInventory(player.inventory, new ItemStack(ModItems.PENDANT));
			if (!(event.getSource().equals(DamageSource.OUT_OF_WORLD) && player.posZ < -60))
				return;
			if (player.openContainer != null && player.openContainer instanceof ContainerPendant && ((ContainerPendant) player.openContainer).getStack() == pendant)
				return;
			if (!pendant.isEmpty()) {
				pendant.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
					for (int i = 8 ; i<11 ; i++) {
						ItemStack stack = handler.getStackInSlot(i);
						if (stack.getItem() == ItemSpecialCapsule.getCapsuleInstance(EnumSpecialType.LOST_CHRISTMAS)) {
							player.getCombatTracker().trackDamage(dmg, player.getHealth(), 0.0F);
							return;
						}
					}
				});
			}
		}
	}
	
	@SubscribeEvent
	public static void onCactusGrow(CropGrowEvent.Post event) {
		if (CommonConfigs.worldgen_cactiFragSpawnRate.get() == 0)
			return;
		if (event.getState().getBlock() == Blocks.CACTUS) {
			World world = (World) event.getWorld();
			BlockPos pos = event.getPos();
			if (!world.getBlockState(pos.up()).isAir(world, pos.up()) || !world.getBlockState(pos.up(2)).isAir(world, pos.up(2)))
				return;
			int tall = 1;
			for (BlockPos p = pos.down() ; world.getBlockState(p).getBlock() == Blocks.CACTUS ; tall++, p = p.down());
			if (tall <= CommonConfigs.worldgen_cactiFragSpawnHeight.get() && event.getState().get(CactusBlock.AGE) >= 12 && rand.nextInt(CommonConfigs.worldgen_cactiFragSpawnRate.get()) == 0) {
				world.setBlockState(pos.up(), Blocks.CACTUS.getDefaultState());
                BlockState blockstate = event.getState().with(CactusBlock.AGE, Integer.valueOf(0));
                world.setBlockState(pos, blockstate, 4);
                blockstate.neighborChanged(world, pos.up(), Blocks.CACTUS, pos, false);
                world.setBlockState(pos.up(2), ModBlocks.TINY_CACTI.getDefaultState());
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityJoin(EntityJoinWorldEvent event) {
		Entity ent = event.getEntity();
		if (ent instanceof CreeperEntity) {
			CreeperEntity creeper = (CreeperEntity) ent;
			creeper.goalSelector.addGoal(3, new AvoidEntityGoal<>(creeper, LivingEntity.class, living -> { 
				return living.getActivePotionEffect(EffectNekomimiParadise.INSTANCE) != null; 
			}, 8.0F, 1.0D, 1.2D, EntityPredicates.CAN_AI_TARGET::test));
		}
		else if (ent instanceof CatEntity) {
			((CatEntity) ent).goalSelector.addGoal(3, new EffectNekomimiParadise.CatNekomimiGoal((CatEntity) ent));
		}
		else if (ent instanceof OcelotEntity) {
			((OcelotEntity) ent).goalSelector.addGoal(3, new EffectNekomimiParadise.OcelotNekomimiGoal((OcelotEntity) ent));
		}
	}
	
	@SubscribeEvent
	public static void onEntityUpdate(LivingUpdateEvent event) {
		if (CommonConfigs.misc_catDropFur.get() && !event.getEntityLiving().world.isRemote) {
			if (event.getEntityLiving() instanceof CatEntity || event.getEntityLiving() instanceof OcelotEntity) {
				LivingEntity living = event.getEntityLiving();
				if (rand.nextInt((living.isSprinting() ? 5 : 8) * 60 * 20) == 0) {
					living.entityDropItem(ModItems.CAT_FUR);
				}
			}
		}
	}
	
}
