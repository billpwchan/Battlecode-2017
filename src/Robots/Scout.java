package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import battlecode.common.*;

import java.util.ArrayList;

/**
 * Class for scout robot
 */
public class Scout {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Scout(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            try {
                MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam().opponent());
                MapLocation myArchon = archons[0];

                for (MapLocation archonLoc : archons) {
                    if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                        myArchon = archonLoc;
                    }
                }

                sense();
                move.stayInLocationRange(myArchon, 10, 30);
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }

    public static void sense() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());

        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.TANK) {
                // Broadcast to tank channels
                rc.broadcast(3, (int) robot.getLocation().x);
                rc.broadcast(4, (int) robot.getLocation().y);
            }
            else if (robot.getType() == RobotType.SOLDIER) {
                // Broadcast to soldier channels
                rc.broadcast(5, (int) robot.getLocation().x);
                rc.broadcast(6, (int) robot.getLocation().y);
            }
            else if (robot.getType() == RobotType.GARDENER) {
                // Broadcast to gardener channels
                rc.broadcast(7, (int) robot.getLocation().x);
                rc.broadcast(8, (int) robot.getLocation().y);
            }
        }

        if (enemyTrees.length > 0) {
            // Broadcast to gardener channels
            rc.broadcast(7, (int) enemyTrees[0].getLocation().x);
            rc.broadcast(8, (int) enemyTrees[0].getLocation().y);
        }
    }
}
