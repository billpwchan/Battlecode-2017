package ebotplayer.util;

import battlecode.common.*;
/**
 * Created by ebot on 1/12/17.
 */
public class Attack {
    private RobotController rc;
    private Common c;
    public Attack(RobotController rc) {
        this.rc = rc;
        c = new Common(rc);
    }
    public void bullet() throws GameActionException {
       RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().sensorRadius, c.enemy());
       if (robots.length > 0) {
           if (rc.canFirePentadShot()) {
               rc.firePentadShot(rc.getLocation().directionTo(robots[0].location));
           } else if (rc.canFireTriadShot()) {
               rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
           } else if (rc.canFireSingleShot()) {
               rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
           }
       }
    }
}
