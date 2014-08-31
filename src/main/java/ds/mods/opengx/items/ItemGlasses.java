package ds.mods.opengx.items;

import java.util.UUID;

import ds.mods.opengx.Glasses;
import ds.mods.opengx.OpenGX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemGlasses extends Item {
	public ItemGlasses()
	{
		setUnlocalizedName("opengx.glasses");
		setCreativeTab(OpenGX.tab);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_,
			EntityPlayer p_77659_3_) {
		return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
	}
	
	public static void create(EntityPlayer entity, ItemStack stack)
	{
		if (stack.stackTagCompound != null && stack.stackTagCompound.getBoolean("valid"))
		{
			Glasses g = Glasses.get(new UUID(stack.stackTagCompound.getLong("msb"),stack.stackTagCompound.getLong("lsb")), entity.worldObj);
			if (g == null)
				new Glasses(entity, stack);
			return;
		}
		//give the item a random uuid
		UUID rand = UUID.randomUUID();
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setBoolean("valid", true);
		stack.stackTagCompound.setLong("msb", rand.getMostSignificantBits());
		stack.stackTagCompound.setLong("lsb", rand.getLeastSignificantBits());
		//create the glasses
		new Glasses(entity, stack);
	}
	
	public static Glasses getGlasses(EntityPlayer entity, ItemStack stack)
	{
		create(entity,stack);
		return Glasses.get(new UUID(stack.stackTagCompound.getLong("msb"),stack.stackTagCompound.getLong("lsb")), entity.worldObj);
	}

	@Override
	public void onUpdate(ItemStack stack, World world,
			Entity entity, int p_77663_4_, boolean p_77663_5_) {
		if (entity instanceof EntityPlayer)
		{
			create(((EntityPlayer)entity),stack);
			Glasses g = Glasses.get(new UUID(stack.stackTagCompound.getLong("msb"),stack.stackTagCompound.getLong("lsb")), world);
			if (g != null) g.update();
		}
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player,
			ItemStack stack) {
		create(player,stack);
		Glasses g = Glasses.get(new UUID(stack.stackTagCompound.getLong("msb"),stack.stackTagCompound.getLong("lsb")), world);
		if (g != null) g.update();
	}

	@Override
	public void onCreated(ItemStack stack, World world,
			EntityPlayer entity) {
		create(entity,stack);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return armorType == 0;
	}
}
