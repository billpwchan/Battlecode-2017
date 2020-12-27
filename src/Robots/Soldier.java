package Robots;

import battlecode.common.*;
import Helpers.HelperMethods;
import Helpers.Movement;

/**
 * Class for soldier robot.
 */
public class Soldier {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Soldier(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                int enemyTankX = rc.readBroadcast(3);
                int enemyTankY = rc.readBroadcast(4);

                if (enemyTankX != -1 && enemyTankY != -1) {
                    MapLocation enemyTankLoc = new MapLocation(enemyTankX, enemyTankY);

                    // See if tank is in area, if not clear broadcast
                    if (rc.getLocation().distanceTo(enemyTankLoc) > 5) {
                        for (RobotInfo robot : enemyRobots) {
                            if (robot.getType() == RobotType.TANK) {
                                rc.broadcast(3, (int) robot.getLocation().x);
                                rc.broadcast(4, (int) robot.getLocation().y);

                            }
                            // This will run a bunch but will not matter overall if there is a tank in range,
                            // make this more efficient is bytecode is an issue
                            else {
                                rc.broadcast(3, -1);
                                rc.broadcast(4, -1);
                            }
                        }
                    }

                    move.stayInLocationRange(enemyTankLoc,
                            (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                }
                // If there are some enemy robots in area
                else if (enemyRobots.length > 0) {
                    move.stayInLocationRange(enemyRobots[0].getLocation(),
                            (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                }
                else {
                    move.move();
                }

                shootNearby();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                try {
                    rc.setIndicatorDot(rc.getLocation(), 66, 134, 244);
                }
                catch (Exception f) { }
                e.printStackTrace();
            }
        }
    }

    public static void shootNearby() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            if (rc.canFireSingleShot()) {
                RobotInfo lowestHealthRobot = enemyRobots[0];
                for (RobotInfo robot : enemyRobots) {
                    if (lowestHealthRobot.health > robot.health) {
                        lowestHealthRobot = robot;
                    }
                }
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(lowestHealthRobot.getLocation()));
            }
        }
    }
}
