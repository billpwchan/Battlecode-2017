package ebotplayer.util;

import battlecode.common.*;
/**
 * Created by ebot on 1/10/17.
 */
public class Tools {
    public static Direction randomDirection() {
        return new Direction((float)Math.random() * (float)Math.PI * 2);
    }
}
