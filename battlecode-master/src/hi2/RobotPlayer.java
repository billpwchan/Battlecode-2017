package hi2;

import battlecode.common.*;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

/**
 * Created by Max_Inspiron15 on 1/10/2017.
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Direction[] dirList = new Direction[4];
    static Direction goingDir;
    static Random rand;


    static int count = 0;



    static List<String> idArr = new ArrayList<String>();

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        initDirList();
        rand = new Random(rc.getID());
        goingDir = randomDir();

        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
                runScout();
        }
    }

    // commonly used methods
    public static Direction randomDir() {
        return dirList[rand.nextInt(4)];
    }

    public static void initDirList() {
        for (int i = 0; i < 4; i++) {
            float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i) / 4);
            dirList[i] = new Direction(radians);
            System.out.println("made new direction " + dirList[i]);
        }
    }

    //wander with direction

    public static void wander() throws GameActionException {
        try {
            Direction dir = randomDir();
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void directionalTravel(Direction dir ){
        try {
            if (rc.canMove(dir)){
                rc.move(dir);
            }else{
                wander();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    // robots classes
    public static void runArchon() {

        while (true) {
            try {
                wander();
                //TODO count gardeners
                //try to build gardeners
                //can you build a gardener?
                if (Math.random() < .1 &&rc.canHireGardener(goingDir)){
                    rc.hireGardener(goingDir);
                    //tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
                }else{wander();}
                //System.out.println("bytecode usage is "+Clock.getBytecodeNum());
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void runGardener() throws GameActionException{
        count++;
        rc.donate(10);
        while (true) {
            try {

                wander();

                dodge();

                //dodge();
                //first try to plant trees
                tryToPlant();
                //now try to water trees
                tryToWater();
                tryToShake();


                //move around
                if (rc.canMove(goingDir)) {
                    rc.move(goingDir);
                } else {
                    goingDir = randomDir();
                }
                Clock.yield();

                if (rc.canBuildRobot(RobotType.LUMBERJACK, goingDir) && rc.getTeamBullets() >= 50){

                    rc.buildRobot(RobotType.LUMBERJACK, goingDir );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void runSoldier() {

    }

    public static void runScout(){

    }

    public static void runLumberjack() throws GameActionException {

        while (true) {
            try {

                dodge();

                MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());

                Direction enemyBase = rc.getLocation().directionTo(ml[0]);

                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {

                    float dist = rc.getLocation().distanceTo(b.getLocation());
                    if (b.getTeam() != rc.getTeam() && rc.canStrike() && dist <= GameConstants.LUMBERJACK_STRIKE_RADIUS + rc.getType().bodyRadius) {
                        rc.strike();

                        Direction dir = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                        break;
                    }
                }

                if (!ThereIsEnemyBotNearBy()) {


                    TreeInfo[] tree = rc.senseNearbyTrees();
                    for (TreeInfo t : tree) {

                        if (rc.canChop(t.getID()) && t.getTeam() != rc.getTeam()) {
                            rc.chop(t.getID());
                            break;
                        }
                    }
                }

                if (!rc.hasAttacked()) {
                    wanderWithDirection(enemyBase);
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public static boolean ThereIsEnemyBotNearBy(){
        RobotInfo[] bots = rc.senseNearbyRobots();

        for (RobotInfo b : bots){
            if (b.getTeam() != rc.getTeam()){
                return true;
            }
        }

        return false;
    }

    public static void tryToWater() throws GameActionException {
        if (rc.canWater()) {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (int i = 0; i < nearbyTrees.length; i++)
                if (nearbyTrees[i].getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) {
                    if (rc.canWater(nearbyTrees[i].getID())) {
                        rc.water(nearbyTrees[i].getID());
                        break;
                    }
                }
        }
    }
    public static void tryToShake() throws GameActionException {
        if (rc.canShake()) {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (int i = 0; i < nearbyTrees.length; i++){
                rc.shake(nearbyTrees[i].getID());
            }
        }
    }

    public static void tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
        if (rc.getTeamBullets() > moneyNeeded) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 4; i++) {
                if (rc.canBuildRobot(type, dirList[i])) {
                    rc.buildRobot(type, dirList[i]);
                    break;
                }
            }
        }
    }




    public static void tryToPlant() throws GameActionException {
        //try to build gardeners
        //can you build a gardener?
        if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 4; i++) {
                //only plant trees on a sub-grid
                MapLocation p = rc.getLocation().add(dirList[i], GameConstants.GENERAL_SPAWN_OFFSET + GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius);
                if (modGood(p.x, 6, 0.2f) && modGood(p.y, 6, 0.2f)) {
                    if (rc.canPlantTree(dirList[i])) {
                        rc.plantTree(dirList[i]);
                        break;
                    }
                }
            }
        }
    }

    public static  void negativefeedBack(RobotType type   ,int keepingConstant){

        // negative feedback system to hold constant

    }

    public static int getCount(RobotType type){
        //count the number of Robot
        return 0;
    }


    static boolean willCollideWithMe(BulletInfo bullet){
        MapLocation mylocation = rc.getLocation();

        //get relevant bullet information
        Direction propagationDirecion = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        //CAlculate bullet

        Direction directionToRobot = bulletLocation.directionTo(mylocation);
        float distToRobot = bulletLocation.distanceTo(mylocation);
        float theta = propagationDirecion.radiansBetween(directionToRobot);

        if (Math.abs(theta) >= Math.PI/2){
            return false;
        }

        float perpendicularDist = (float) Math.abs(distToRobot * Math.tan(theta));

        return (perpendicularDist <= rc.getType().bodyRadius);


    }

    //try slide step





    public static boolean modGood(float number, float spacing, float fraction) {
        return (number % spacing) < spacing * fraction;
    }

    static void wanderWithDirection(Direction dir) throws GameActionException{

        double rand = Math.random();

        if (0< rand  && rand <= 0.5 ){
            tryMove(dir);
        }
        if (0.5 < rand && rand <= 0.66666){
            tryMove(dir.rotateLeftDegrees(90));
        }
        if (0.66666 < rand && rand <= 0.866666){
            tryMove(dir.rotateLeftDegrees(180));
        }
        if (0.866666 < rand && rand <= 1){
            tryMove(dir.rotateLeftDegrees(270));
        }

    }
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (!rc.hasMoved() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(! rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }


    static boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }

    }
}