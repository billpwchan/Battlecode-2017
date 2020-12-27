package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import battlecode.common.*;

public class Tank {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Tank(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() throws GameActionException{


            while (true) {
                try {
                    rc.broadcast(0,(int)rc.getLocation().x);
                    rc.broadcast(1,(int)rc.getLocation().y);
                    MapLocation[] blocs = rc.senseBroadcastingRobotLocations();  //broadcasting locations
                    RobotInfo[] nlocs = rc.senseNearbyRobots(10, rc.getTeam().opponent());  //nearby locations
                    MapLocation floc = rc.getLocation();
                    MapLocation Archon = rc.getInitialArchonLocations(rc.getTeam())[0];
                    MapLocation enemyArchon = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];

                    if (nlocs.length == 0) {
                        //System.out.println("Tank code");
                        //major flaw: if no enemy robots are broadcasting, tank will move towards its furthest ally
                        if(blocs.length != 0){
                            for (MapLocation loc : blocs) {
                                if (Archon.distanceTo(loc) > Archon.distanceTo(floc)) {
                                    floc = loc;
                                }
                            }
                            Direction enemyDir = rc.getLocation().directionTo(floc);
                            if(enemyDir != null){
                                move.moveToLoc(floc);
                            }
                            else{
                                move.move();
                            }
                        }else{
                            move.moveToLoc(enemyArchon);
                        }
                    } else {
                        if (rc.canFireTriadShot()) {
                            rc.fireTriadShot(rc.getLocation().directionTo(nlocs[nlocs.length - 1].getLocation()));
                        }
                        if (rc.canFireSingleShot()){
                            rc.fireSingleShot(rc.getLocation().directionTo(nlocs[nlocs.length - 1].getLocation()));
                        }
                        move.stayInLocationRange(nlocs[0].getLocation(),
                                (int) rc.getType().sensorRadius - 1, (int) rc.getType().sensorRadius);
                    }
                    Clock.yield();
                }catch(Exception e) {
                    System.out.println("Tank Exception");
                    e.printStackTrace();
                }
            }
    }
}
