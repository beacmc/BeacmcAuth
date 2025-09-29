package com.beacmc.beacmcauth.api.social.keyboard.button;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Button {

    String label;
    String callbackData;
    ButtonType type;
}
