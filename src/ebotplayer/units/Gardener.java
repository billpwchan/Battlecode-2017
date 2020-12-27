package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.Unit;
import ebotplayer.util.*;
/**
 * Created by ebot on 1/10/17.
 */
public class Gardener extends Unit {
    private Direction[] plantD;
    private boolean planter;
    public Gardener(RobotController rc) {
        super(rc);
        plantD = new Direction[6];
        for (int i = 0 ; i < 6 ; i++) {
            plantD[i] = new Direction(0 + i * ((float)Math.PI / 3) );
        }
        while(true) {
            try {
                c.vp();
                u.sendAliveSignal();
                c.shake();
                water();
                bUnit(RobotType.SOLDIER);
                bUnit(RobotType.LUMBERJACK);
                plant();
                if (rc.senseNearbyTrees((float)1.1, rc.getTeam()).length < 1) {
                    m.wander(30, 12);
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
    private void plant() throws GameActionException {
        for (Direction d : plantD) {
            if (rc.canPlantTree(d)) {
                rc.plantTree(d);
                break;
            }
        }
    }
    private void water() throws GameActionException {
        TreeInfo[] fTrees = rc.senseNearbyTrees(rc.getType().sensorRadius, rc.getTeam());
        if (fTrees.length > 0) {
            float lowestH = 50;
            int tID = 0;
            for(TreeInfo t : fTrees) {
                if (t.getHealth() < lowestH && rc.canWater(t.getID())) {
                    lowestH = t.getHealth();
                    tID = t.getID();
                }
            }
            if (rc.canWater(tID)) {
                rc.water(tID);
                System.out.println("watered");
            }
        }
    }
    private void bUnit(RobotType type) throws GameActionException {
        Direction bDirection = Tools.randomDirection();
        if (rc.canBuildRobot(type, bDirection)) {
            rc.buildRobot(type, bDirection);
        }
    }
}

