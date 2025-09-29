package com.beacmc.beacmcauth.api.social.keyboard;

import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Keyboard {

    List<List<Button>> buttons;
}
