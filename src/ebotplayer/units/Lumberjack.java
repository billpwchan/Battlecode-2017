package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.Unit;
/**
 * Created by ebot on 1/10/17.
 */
public class Lumberjack extends Unit{
    private int choppingID;
    public Lumberjack(RobotController rc) {
        super(rc);
        while(true) {
            try {
                c.vp();
                strike();
                if (canChop()) {
                    chop();
                } else {
                    m.wander(45, 7);
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }
    private boolean canChop() {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
        if (trees.length > 0) {
            for (TreeInfo ctree : trees) {
                if (rc.canChop(trees[0].location) && (trees[0].containedBullets > 0 || trees[0].containedRobot != null)) {
                    choppingID = trees[0].ID;
                    return true;
                }
            } for (TreeInfo ctree : trees) { //repeated the for-loop so that if no bullet or robot trees are nearby, they'll jsut cut down a regular one
                //should probably find a more elegant solution
                //or maybe we should just have them cut down all trees regardless or only special trees?
                if (rc.canChop(trees[0].location)) {
                    choppingID = trees[0].ID;
                    return true;
                }
            }
        }
        return false;
    }
    private void chop() throws GameActionException {
        if (rc.canChop(choppingID)) {
            rc.chop(choppingID);
            System.out.println("chopped");
        }
    }
    private void strike() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().sensorRadius, c.enemy());
        if (robots.length > 0) {
            if (rc.canStrike()) {
                rc.strike();
                System.out.println("struck");
            }
        }
    }
}
