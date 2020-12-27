package Robots;

import Helpers.HelperMethods;
import Main.RobotPlayer;
import battlecode.common.*;

import static Helpers.HelperMethods.randomDirection;

/**
 * Class for archon robot.
 */
public class Archon {
    static RobotController rc;
    static HelperMethods helpers;

    public Archon(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            // Our team tank x location
            rc.broadcast(0, -1);

            // Our team tank y location
            rc.broadcast(1, -1);

            // Gardener count
            rc.broadcast(2, -1);

            // Tank x location
            rc.broadcast(3, -1);

            // Tank y location
            rc.broadcast(4, -1);

            // Soldier x location
            rc.broadcast(5, -1);

            // Soldier y location
            rc.broadcast(6, -1);

            // Gardener x location
            rc.broadcast(7, -1);

            // Gardener y location
            rc.broadcast(8, -1);

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction[] dirList = RobotPlayer.getDirList();
                Direction dir = dirList[0];
                for(Direction d : dirList){
                    if(rc.canBuildRobot(RobotType.TANK,d)){
                        dir = d;
                        break;
                    }
                }
                int numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
                if (rc.getRobotCount() < 10) {
                    if (rc.canHireGardener(dir) && (rc.getRobotCount() == numArchons || rc.getRobotCount() == numArchons + 1 || rc.getRobotCount() == numArchons + 3 || rc.getRobotCount() == numArchons + 5 || rc.getRobotCount() == numArchons + 7)) {
                        rc.hireGardener(dir);
                    }
                }else if (rc.getRobotCount() % 5 == 4 && rc.canHireGardener(dir)){
                        rc.hireGardener(dir);
                }

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}