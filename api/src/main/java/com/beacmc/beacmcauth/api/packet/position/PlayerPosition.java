package com.beacmc.beacmcauth.api.packet.position;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PlayerPosition {

    private double x;
    private double y;
    private double z;

    public Vector3i asVector3i() {
        return new Vector3i((int) x, (int) y, (int) z);
    }

    public Vector3d asVector3d() {
        return new Vector3d(x, y, z);
    }
}
