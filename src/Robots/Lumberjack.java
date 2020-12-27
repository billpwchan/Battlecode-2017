package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import battlecode.common.*;

/**
 * Class for lumberjack robot.
 */
public class Lumberjack {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Lumberjack(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() throws GameActionException {
        while (true) {
            try {
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && rc.canStrike()) {
                        rc.strike();
                        Direction chase = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(chase)) {
                            rc.move(chase);
                        }
                        break;
                    }
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                for (TreeInfo t : trees) {
                    if (rc.canChop(t.getLocation()) && t.getTeam() != rc.getTeam()) {
                        rc.chop(t.getLocation());
                        break;
                    }
                }
                if (! rc.hasAttacked()) {
                    move.move();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
