package enchantmentupgrade.mixin;

import enchantmentupgrade.UpgradeManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    
    // 监听耐久度设置。这是最底层且稳定的耐久度修改方法
    @Inject(method = "setDamage", at = @At("HEAD"))
    private void onSetDamage(int newDamage, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        int currentDamage = stack.getDamage();
        
        // 如果新耐久损伤大于当前，说明正在消耗耐久（而非修复）
        if (newDamage > currentDamage) {
            int damageAmount = newDamage - currentDamage;
            UpgradeManager.handleItemDamage(stack, damageAmount);
        }
    }
}
