package enchantmentupgrade;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class EnchantmentUpgrade implements ModInitializer {

    @Override
    public void onInitialize() {
        // 注册命令
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("enchantmentupgrade")
                .then(CommandManager.literal("progress")
                        .executes(this::getProgress)));
    }

    private int getProgress(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ItemStack stack = source.getPlayer().getMainHandStack();
        if (stack.isEmpty() || !stack.isDamageable()) {
            source.sendMessage(Text.literal("You are not holding a damageable item."));
            return 0;
        }
        int progress = UpgradeManager.getProgress(stack);
        int maxDamage = stack.getMaxDamage();
        int threshold = (int) Math.ceil(maxDamage * 0.8); // 根据 UpgradeManager 中的设定
        if (threshold <= 0) threshold = 1;
        
        source.sendMessage(Text.literal("Current enchantment upgrade progress: " + progress + "/" + threshold));
        return 1;
    }
}
