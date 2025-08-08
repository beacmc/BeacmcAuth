package com.beacmc.beacmcauth.core.social.types.vkontakte;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.SocialPlayer;
import com.beacmc.beacmcauth.api.social.SocialType;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import org.jetbrains.annotations.Nullable;

import java.util.Random;


public class VkontaktePlayer implements SocialPlayer<Integer, Integer> {

    private final BeacmcAuth plugin;
    private final Integer id;
    private final GroupActor groupActor;
    private final Random random;
    private final VkontakteSocial vkontakteSocial;

    public VkontaktePlayer(BeacmcAuth plugin, Integer id) {
        this.plugin = plugin;
        this.id = id;
        this.vkontakteSocial = (VkontakteSocial) plugin.getSocialManager().getSocialByType(SocialType.VKONTAKTE);
        this.groupActor = vkontakteSocial.getVkApiPlugin().getVkApiProvider().getActor();
        this.random = new Random();
    }

    @Override
    public void sendPrivateMessage(String message, @Nullable Object keyboard) {
        try {
            VkApiClient client = vkontakteSocial.getVkApiPlugin().getVkApiProvider().getVkApiClient();
            MessagesSendQuery query = client.messages().send(groupActor).message(message);


            if (keyboard instanceof Keyboard vkKeyboard) {
                query.keyboard(vkKeyboard);
            }

            query.peerId(getID())
                    .randomId(random.nextInt())
                    .execute();
        } catch (ClientException | ApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public boolean checkAccountLink(ProtectedPlayer player) {
        return getID().equals(player.getVkontakte());
    }

    @Override
    public Integer getOriginalSocialPlayer() {
        return id;
    }
}
