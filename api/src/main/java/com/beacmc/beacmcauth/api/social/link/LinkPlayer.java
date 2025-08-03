package com.beacmc.beacmcauth.api.social.link;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.social.SocialType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class LinkPlayer<ID> {

    SocialType socialType;
    ProtectedPlayer player;
    ID id;
    String code;
}
