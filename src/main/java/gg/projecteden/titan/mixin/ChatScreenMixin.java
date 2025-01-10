package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.discord.PlayerStates;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static gg.projecteden.titan.utils.Utils.isOnEden;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {

    @Shadow protected TextFieldWidget chatField;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!isOnEden())
            return;
        if (!ConfigItem.CHAT_CHANNEL_RENDER.getValue())
            return;

        PlayerStates.ChatChannel channel = detectQuickMessage();
        if (channel == null)
            channel = PlayerStates.getChannel();

        if (channel == PlayerStates.ChatChannel.UNKNOWN)
            return;

        context.fill(0, this.height - 14, 2, this.height - 2, channel.getColor());
    }

    @Unique
    private PlayerStates.ChatChannel detectQuickMessage() {
        String chat = this.chatField.getText().trim();

        if (chat.startsWith("/ch qm ")) {
            String[] parts = chat.split(" ", 4);
            if (parts.length >= 3) {
                String shortcut = parts[2];
                return Arrays.stream(PlayerStates.ChatChannel.values())
                        .filter(channel -> shortcut.equals(channel.getShortcut()))
                        .findFirst()
                        .orElse(null);
            }
        }

        if (chat.startsWith("/ch ")) {
            String[] parts = chat.split(" ", 3);
            if (parts.length == 3) {
                String shortcut = parts[1];
                return Arrays.stream(PlayerStates.ChatChannel.values())
                        .filter(channel -> shortcut.equals(channel.getShortcut()))
                        .findFirst()
                        .orElse(null);
            }
        }

        if (chat.startsWith("/msg "))
            return PlayerStates.ChatChannel.PRIVATE;

        return null;
    }

}
