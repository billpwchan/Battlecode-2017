package ebotplayer;

import battlecode.common.*;
import ebotplayer.units.*;
/**
 * Created by ebot on 1/10/17.
 */
public class RobotPlayer {
    public static void run(RobotController rc) {
        switch(rc.getType()) {
            case ARCHON:
                Archon a = new Archon(rc);
                break;
            case GARDENER:
                Gardener b = new Gardener(rc);
                break;
            case LUMBERJACK:
                Lumberjack c = new Lumberjack(rc);
                break;
            case SOLDIER:
                Soldier d = new Soldier(rc);
                break;
            case TANK:
                Tank e = new Tank(rc);
                break;
            case SCOUT:
                Scout f = new Scout(rc);
                break;
        }
    }
}
