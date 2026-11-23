package com.hbm.computers.blocks;

import java.util.HashMap;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockToilet extends Block {

	@SideOnly(Side.CLIENT)
	private IIcon iconNormal;

	@SideOnly(Side.CLIENT)
	private IIcon iconSkibidi;

	@SideOnly(Side.CLIENT)
	private static Map<String, ISound> activeSounds = new HashMap<String, ISound>();

	public BlockToilet() {
		super(Material.rock);
		this.setBlockName("ntm_toilet");
		this.setCreativeTab(MainRegistry.blockTab);
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.iconNormal = reg.registerIcon(RefStrings.MODID + ":toilet_normal");
		this.iconSkibidi = reg.registerIcon(RefStrings.MODID + ":toilet_skibidi");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return meta == 1 ? this.iconSkibidi : this.iconNormal;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		int meta = world.getBlockMetadata(x, y, z);

		if (meta == 0) {
			ItemStack held = player.getCurrentEquippedItem();

			if (held != null && held.getItem() == Item.getItemFromBlock(ModBlocks.bobblehead) && held.getItemDamage() == BobbleType.BOB.ordinal()) {
				if (!world.isRemote) {
					world.setBlockMetadataWithNotify(x, y, z, 1, 2);

					if (!player.capabilities.isCreativeMode) {
						held.stackSize--;
					}

					player.triggerAchievement(MainRegistry.achSkibidiBob);
				} else {
					playToiletSound(world, x, y, z);
				}
				return true;
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void playToiletSound(World world, int x, int y, int z) {
		stopToiletSound(world, x, y, z);
		String key = world.provider.dimensionId + ":" + x + ":" + y + ":" + z;
		ISound sound = PositionedSoundRecord.func_147675_a(new ResourceLocation(RefStrings.MODID + ":block.skibidi_bob"), x + 0.5F, y + 0.5F, z + 0.5F);
		Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		activeSounds.put(key, sound);
	}

	@SideOnly(Side.CLIENT)
	private void stopToiletSound(World world, int x, int y, int z) {
		String key = world.provider.dimensionId + ":" + x + ":" + y + ":" + z;
		ISound sound = activeSounds.remove(key);
		if (sound != null) {
			Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if (world.isRemote) {
			stopToiletSound(world, x, y, z);
		}
		super.breakBlock(world, x, y, z, block, meta);
	}
}