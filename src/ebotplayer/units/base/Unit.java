package ebotplayer.units.base;

import battlecode.common.*;
import ebotplayer.util.*;
/**
 * Created by ebot on 1/20/17.
 */
public class Unit {
    public RobotController rc;
    public static Common c;
    public static Movement m;
    public static UnitCount u;
    public Unit(RobotController rc) {
        this.rc = rc;
        c = new Common(rc);
        m = new Movement(rc);
        u = new UnitCount(rc);
    }
}
