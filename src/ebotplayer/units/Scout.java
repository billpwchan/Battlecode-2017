package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.BulletUnit;
/**
 * Created by ebot on 1/10/17.
 */
public class Scout extends BulletUnit {
    public Scout(RobotController rc) {
        super(rc);
        while(true) {
            try {
                c.vp();

                Clock.yield();
            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
}
