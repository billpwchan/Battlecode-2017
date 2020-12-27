package ebotplayer.units.base;

import battlecode.common.*;
import ebotplayer.util.*;
/**
 * Created by ebot on 1/20/17.
 */
public class BulletUnit extends Unit {
    public static Attack a;
    public BulletUnit(RobotController rc)  {
        super(rc);
        a = new Attack(rc);
    }
}