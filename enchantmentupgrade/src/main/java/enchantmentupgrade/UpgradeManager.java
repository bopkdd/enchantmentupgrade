package enchantmentupgrade;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;

public class UpgradeManager {

    private static final String UPGRADE_PROGRESS_KEY = "EnchantmentUpgrade:Progress";
    // 基础成长阈值系数。耐久度损耗达到最大耐久的 80% 时成长一次
    private static final double THRESHOLD_MULTIPLIER = 0.8;

    public static void handleItemDamage(ItemStack stack, int damageAmount) {
        if (damageAmount <= 0) return;
        if (!stack.isDamageable()) return;
        
        // 如果没有可成长的附魔，不增加进度
        if (!hasUpgradableEnchantments(stack)) {
            return;
        }

        int currentProgress = getProgress(stack);
        currentProgress += damageAmount;

        int maxDamage = stack.getMaxDamage();
        int threshold = (int) Math.ceil(maxDamage * THRESHOLD_MULTIPLIER);
        if (threshold <= 0) threshold = 1;

        if (currentProgress >= threshold) {
            upgradeRandomEnchantment(stack);
            // 扣除阈值，保留溢出的进度
            currentProgress -= threshold;
        }

        setProgress(stack, currentProgress);
    }

    private static boolean hasUpgradableEnchantments(ItemStack stack) {
        // 1.21+ 中，附魔通过 component 管理
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        for (var entry : enchantments.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();
            if (level < enchantment.value().getMaxLevel() && enchantment.value().getMaxLevel() > 1) {
                return true;
            }
        }
        return false;
    }

    private static void upgradeRandomEnchantment(ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        Map<RegistryEntry<Enchantment>, Integer> upgradableEnchantments = new HashMap<>();

        for (var entry : enchantments.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();
            // 剔除最高等级为 1 的附魔（如经验修补、精准采集）
            if (level < enchantment.value().getMaxLevel() && enchantment.value().getMaxLevel() > 1) {
                upgradableEnchantments.put(enchantment, level);
            }
        }

        if (!upgradableEnchantments.isEmpty()) {
            var keys = upgradableEnchantments.keySet().toArray(new RegistryEntry[0]);
            RegistryEntry<Enchantment> selectedEnchantment = keys[Random.create().nextInt(keys.length)];
            int newLevel = upgradableEnchantments.get(selectedEnchantment) + 1;

            // 1.21+ 更新附魔
            stack.addEnchantment(selectedEnchantment, newLevel);
        }
    }

    public static int getProgress(ItemStack stack) {
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        return nbt.getInt(UPGRADE_PROGRESS_KEY).orElse(0);
    }

    public static void setProgress(ItemStack stack, int progress) {
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        nbt.putInt(UPGRADE_PROGRESS_KEY, progress);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}
