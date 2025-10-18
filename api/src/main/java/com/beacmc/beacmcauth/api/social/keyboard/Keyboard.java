package com.beacmc.beacmcauth.api.social.keyboard;

import com.beacmc.beacmcauth.api.social.keyboard.button.Button;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Keyboard {

    List<List<Button>> buttons;
    boolean inline;
}
