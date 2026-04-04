package com.beacmc.beacmcauth.core.dialog.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.auth.AuthenticatingPlayer;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.dialog.DialogClickListener;
import com.beacmc.beacmcauth.api.model.ProtectedPlayer;
import com.beacmc.beacmcauth.api.server.player.ServerPlayer;
import com.beacmc.beacmcauth.core.config.dialog.SecretDialogSettings;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;

@RequiredArgsConstructor
public class SecretListener implements DialogClickListener {

    private final BeacmcAuth plugin;

    @Override
    public void onClick(User user, WrapperCommonClientCustomClickAction<?> clickAction) {
        final AuthManager authManager = plugin.getAuthManager();

        if (clickAction.getId().toString().equals("beacmcauth:secret-recovery")
                && authManager.isAuthenticating(user.getName().toLowerCase())) {

            ServerPlayer player = plugin.getProxy().getPlayer(user.getUUID());
            AuthenticatingPlayer authPlayer = authManager.getAuthPlayers()
                    .getCacheData(user.getName().toLowerCase());
            String question = authPlayer.getPlayer().getSecretQuestion();
            if (question != null) {
                Dialog dialog = buildDialogWithSecretQuestion(question);
                player.showDialog(dialog);
            } else {
                plugin.getServerLogger().debug("The player(%s) does not have a secret question"
                        .formatted(player.getName()));
                player.disconnect(plugin.getConfig().getMessages().getInternalError());
            }
        }

        else if (clickAction.getId().toString().equals("beacmcauth:confirm-secret-recovery")
                && authManager.isAuthenticating(user.getName().toLowerCase())
                && clickAction.getPayload() instanceof NBTCompound nbt) {

            Config config = plugin.getConfig();

            ServerPlayer player = plugin.getProxy().getPlayer(user.getUUID());
            AuthenticatingPlayer authPlayer = authManager.getAuthPlayers()
                    .getCacheData(user.getName().toLowerCase());
            ProtectedPlayer protectedPlayer = authPlayer.getPlayer();

            String secretAnswer = nbt.getStringTagValueOrDefault("answer", "");
            if (secretAnswer.isEmpty() || !protectedPlayer.checkSecretAnswer(secretAnswer)) {
                player.disconnect(config.getMessages().getInvalidSecretAnswerDisconnect());
                return;
            }

            try {
                String newPassword = CodeGenerator.generate(config.getRecoveryPasswordChars(), 8);
                plugin.getDatabase().getProtectedPlayerDao().createOrUpdate(protectedPlayer
                        .setPassword(BCrypt.hashpw(
                                newPassword,
                                BCrypt.gensalt(config.getBCryptRounds()))
                        ));
                player.disconnect(config.getMessages().getSecretAnswerSuccessUsedDisconnect()
                        .replace("%password%", newPassword));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Dialog buildDialogWithSecretQuestion(String question) {
        Map<String, ?> placeholders = Map.of("%secret_question%", question);
        SecretDialogSettings secretDialogSettings = (SecretDialogSettings) plugin.getConfig().getSecretDialogSettings();
        return plugin.getDialogManager().buildAndSave(
                getClass().getSimpleName(),
                (manager) -> secretDialogSettings.createDialog(
                        manager,
                        placeholders
                ),
                placeholders
        );
    }
}
