package iblis.player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class PlayerUtils {

	public static final int MAX_SPRINT_SPEED = 64;
	public static final Map<Integer,Integer> sprintingButtonCounterState = new HashMap<Integer,Integer>();
	
	private final static String[] ATTRIBUTES_AFFECTED_BY_CRAFTING_SKILL = new String[] {
			SharedMonsterAttributes.ARMOR.getName(), 
			SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),
			SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
			SharedIblisAttributes.PROJECTILE_DAMAGE.getName()};

	public static boolean isCharacteristicsCouldBeRaised(EntityPlayer player) {
		for (PlayerCharacteristics characteristic : PlayerCharacteristics.values()) {
			if (isCharacteristicCouldBeRaised(characteristic, player)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCharacteristicCouldBeRaised(PlayerCharacteristics characteristic, EntityPlayer player) {
		int experienceLevel = player.experienceLevel;
		long characteristicLevel = characteristic.getCurrentLevel(player);
		if (characteristicLevel <= experienceLevel
				&& characteristic.getCurrentValue(player) < characteristic.getMaxValue(player)) {
			return true;
		}
		return false;
	}

	public static double getQualityModifierValue(double skillValue, ItemStack output1,
			EntityEquipmentSlot slot, String attributeName, boolean additive) {
		double baseValue = 0;
		Iterator<AttributeModifier> ami = output1.getAttributeModifiers(slot).get(attributeName).iterator();
		while (ami.hasNext()) {
			AttributeModifier am = ami.next();
			baseValue += am.getAmount();
		}
		if (isAttributeAffectedByCraftingSkill(attributeName)) {
			if(additive)
				return baseValue + skillValue * 0.1d;
			if (skillValue < 0d)
				return baseValue / (1d - skillValue * 0.2d);
			if (skillValue > 0d)
				return baseValue * skillValue * 0.1d + baseValue;
		}
		return baseValue;
	}

	private static boolean isAttributeAffectedByCraftingSkill(String attributeNameIn) {
		for (String attributeName : ATTRIBUTES_AFFECTED_BY_CRAFTING_SKILL)
			if (attributeNameIn.equals(attributeName))
				return true;
		return false;
	}
	
	public static void applySprintingSpeedModifier(EntityPlayer player, int sprintCounter){
		IAttributeInstance msi = player.getAttributeMap()
				.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
		msi.removeModifier(SharedIblisAttributes.SPRINTING_SPEED_MODIFIER);
		if (sprintCounter > 0 && PlayerSkills.RUNNING.enabled) {
			double ss = (PlayerSkills.RUNNING.getFullSkillValue(player) - 0.1) * 0.1;
			if (ss > 0)
				ss = ss * sprintCounter / PlayerUtils.MAX_SPRINT_SPEED;
			msi.applyModifier(new AttributeModifier(SharedIblisAttributes.SPRINTING_SPEED_MODIFIER,
						"Sprinting speed boost", ss, 2));
		}
	}

	public static void saveSprintButtonCounterState(EntityPlayer player, int sprintButtonCounter) {
		sprintingButtonCounterState.put(player.getEntityId(), sprintButtonCounter);
	}
	
	public static int getSprintButtonCounterState(EntityPlayer player) {
		Integer state = sprintingButtonCounterState.get(player.getEntityId());
		if(state!=null)
			return state.intValue();
		return 0;
	}

	public static boolean canJump(EntityPlayer player) {
		return player.getFoodStats().getFoodLevel() >= 6;
	}

}
