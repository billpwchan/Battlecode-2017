package ebotplayer.util;

import battlecode.common.*;
/**
 * Created by ebot on 1/20/17.
 */
public class UnitCount {
    private RobotController rc;
    public UnitCount(RobotController rc) {
        this.rc = rc;
    }
    public void sendAliveSignal() throws GameActionException {
        rc.broadcast(getPreChannel(rc.getType()), rc.readBroadcast(getPreChannel(rc.getType())) + 1);
    }
    public int unitCount(RobotType type)  throws GameActionException {
        int amountOfUnit = pollPreAmount(type);
        if (amountOfUnit == 0) {
            amountOfUnit = pollAmount(type);
        }
        System.out.println("There are " + amountOfUnit + " " + type + " in the game!");

        rc.broadcast(getChannel(type), amountOfUnit);
        rc.broadcast(getPreChannel(type), 0);
        return amountOfUnit;
    }
    private int pollPreAmount(RobotType type) throws GameActionException{
        return rc.readBroadcast(getPreChannel(type));
    }
    private int pollAmount(RobotType type) throws GameActionException{
        return rc.readBroadcast(getChannel(type));
    }
    //cheat-sheet
    //Gardener = 0, 1
    //Soldier = 2, 3
    //Lumberjack = 4, 5
    //Scout = 6, 7
    //Tank = 8, 9
    private static int getPreChannel(RobotType type) {
        switch(type) {
            case GARDENER:
                return 0;
            case SOLDIER:
                return 2;
            case LUMBERJACK:
                return 4;
            case SCOUT:
                return 6;
            case TANK:
                return 8;
        }
        return 0;
    }
    private static int getChannel(RobotType type) {
        switch(type) {
            case GARDENER:
                return 1;
            case SOLDIER:
                return 3;
            case LUMBERJACK:
                return 5;
            case SCOUT:
                return 7;
            case TANK:
                return 9;
        }
        return 0;
    }
}
