package ebotplayer.util;

import battlecode.common.*;
/**
 * Created by ebot on 1/12/17.
 */
public class Common {
    private RobotController rc;
    public Common(RobotController rc) {
        this.rc = rc;
    }
    public Team enemy() {
        return rc.getTeam().opponent();
    }
    public void shake() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
        if (trees.length > 0) {
            for (TreeInfo t : trees) {
                if (rc.canShake(t.location) && t.containedBullets > 0) {
                    rc.shake(t.location);
                    System.out.println("shook");
                    break;
                }
            }
        }
    }
    public void vp() throws GameActionException{
        if(rc.getTeamBullets() >= 10000) rc.donate(10000);
    }
}