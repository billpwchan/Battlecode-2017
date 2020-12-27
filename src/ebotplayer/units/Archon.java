package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.Unit;
import ebotplayer.util.*;
/**
 * Created by ebot on 1/10/17.
 */
public class Archon extends Unit {
    public Archon(RobotController rc) {
        super(rc);
        while(true) {
            try {
                c.vp();
                if (u.unitCount(RobotType.GARDENER) < 10) {
                    hGardener();
                }
                c.shake();
                m.wander(45,7);
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
    private void hGardener() throws GameActionException {
        if (rc.hasRobotBuildRequirements(RobotType.GARDENER)) {
            Direction bDirection = Tools.randomDirection();
            int i = 0;
            while (!rc.canHireGardener(bDirection) && i < 3) {
                bDirection = Tools.randomDirection();
                i++;
            } if (rc.canHireGardener(bDirection)) {
                rc.hireGardener(bDirection);
            }
        }
    }
}
