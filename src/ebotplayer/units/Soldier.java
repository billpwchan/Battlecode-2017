package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.BulletUnit;
/**
 * Created by ebot on 1/10/17.
 */
public class Soldier extends BulletUnit {
    public Soldier(RobotController rc) {
        super(rc);
        while(true) {
            try {
                c.vp();
                a.bullet();
                c.shake();
                if (!rc.hasAttacked()) {
                    m.wander(45, 7);
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
