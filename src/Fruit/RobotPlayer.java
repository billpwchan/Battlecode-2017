package Fruit;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    
    //Channels
    static int GARDENER_CHANNEL = 3;
    static int XCOORDENEMY = 2;
    static int YCOORDENEMY = 4;
    static int SHOULDCOME = 5;
    static int PRODUCTIONCHANNEL = 6;
    static int SCOUT_CHANNEL = 7;
    static int XMINMAP = 8;
    static int XMAXMAP = 9;
    static int YMINMAP = 10;
    static int YMAXMAP = 11;
    static int GARDENER_DISTRESS = 12;
    static int SOLDIER_CHANNEL = 13;
    static int GARDENER_X = 14;
    static int GARDENER_Y = 15;
    static int PLANTED_CHANNEL = 16;
    //Max number of bots
    static int GARDENER_MAX = 25;
    static int SCOUT_MAX = 5;
    
    static int COME = 1;
    static int DONTCOME = 0;
    static int DISTRESSED = 1;
    static int NOTDISTRESSED = 0;
    static float SETTLERADIUS = (float)4.5;
    
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
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
                break;
            case TANK:
                runTank();
                break;
        }
    }
    
    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        Direction moveDirection = randomDirection();
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                int areaOfMap = (rc.readBroadcast(XMAXMAP)-rc.readBroadcast(XMINMAP))*(rc.readBroadcast(YMAXMAP)-rc.readBroadcast(YMINMAP));
                RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                MapLocation[] locRobots = locEnemyRobotsNearby(robots);
                if(rc.readBroadcast(SHOULDCOME)==DONTCOME)
                    broadcastIfSenseEnemy();
                
                //write down number of gardeners
                //System.out.println(numGardHired);
                int numGard = rc.readBroadcast(GARDENER_CHANNEL);
                
                // Generate a random direction
                Direction dir = randomDirection();
                
                // Hire a gardener if enough slots in a random direction
                if (robots.length == 0 && rc.readBroadcast(GARDENER_DISTRESS) == 0 && numGard < 1 && rc.getRoundNum()<50 && rc.readBroadcast(PLANTED_CHANNEL) == 0) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, numGard + 1);
                    rc.broadcast(PLANTED_CHANNEL, 1);
                } else if (robots.length == 0 && rc.readBroadcast(GARDENER_DISTRESS) == 0 && numGard < GARDENER_MAX && rc.canHireGardener(dir) && rc.getRoundNum() > 50 && rc.readBroadcast(PLANTED_CHANNEL) == 0) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, numGard + 1);
                    rc.broadcast(PLANTED_CHANNEL, 1);
                }
                
                //Move randomly or retreat
                if(robots.length>0) {
                    if(!runAwayBravely(locRobots)) {
                        wander();
                    }
                } else {
                    wander();
                }
                
                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);
                
                //Donate bullets, see if can win
                if (rc.getTeamBullets() >= 500) rc.donate(100);
                canWin();
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
                
                
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
                
            }
        }
    }
    
    static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");
        MapLocation settleLoc = null;
        boolean settled = false;
        MapLocation[] enemy= rc.getInitialArchonLocations(rc.getTeam().opponent());
        Direction dir = rc.getLocation().directionTo(enemy[0]);
        int check = 0;
        int age = 0;
        int birthDay = 0;
        boolean squareSpawn = false;
        boolean circleSpawn = true;
        MapLocation target = tryFindSpot();
        boolean broadcastedDeath = false;
        Direction spawningDirection = figureOutDirection();
        Direction squaringDirection = spawningDirection.rotateRightDegrees(90);
        spawningDirection = figureOutDirection();
        boolean didIDistress = false;
        boolean broadcastPlanted = false;
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);
                Direction directionToArchon = rc.getLocation().directionTo(enemy[0]);
                //If no nearby archons or gardeners, settle
                if(!senseIfFriendlyArchonNear()) {
                    settled = true;
                    circleSpawn = true;
                }
                //If not settled, wander
                if(!settled) {
                    if (!tryMove(dir)) {
                        dir = dir.rotateRightDegrees((float)(Math.random()*180));
                        tryMove(dir);
                    }
                }
                //If no nearby enemies and settled and after 50 rounds, plant trees around
                if (circleSpawn && robots.length==0 && settled && circleSpawn && rc.readBroadcast(GARDENER_DISTRESS) == 0 && rc.getRoundNum() > 50) {
                    if(!tryPlantTree(directionToArchon.opposite(), 60, 2) && !broadcastPlanted) {
                        rc.broadcast(PLANTED_CHANNEL, 0);
                        broadcastPlanted = true;
                    }
                }
                //If low health, set broadcast to build a gardener
                if(rc.getHealth()<15) {
                    rc.broadcast(PLANTED_CHANNEL, 0);
                }
                // Always try to water
                tryWaterTree();
                //If sense nearby enemy, send a distress call
                if(robots.length>0 && rc.getHealth() > 5 && rc.readBroadcast(GARDENER_DISTRESS) == 0) {
                    rc.broadcast(GARDENER_DISTRESS, 1);
                    rc.broadcast(GARDENER_X, (int)rc.getLocation().x);
                    rc.broadcast(GARDENER_Y, (int)rc.getLocation().y);
                    rc.setIndicatorDot(rc.getLocation(), 0, 0, 0);
                    didIDistress = true;
                } else if (robots.length == 0 && rc.readBroadcast(GARDENER_DISTRESS) == 1 && didIDistress) {
                    rc.broadcast(GARDENER_DISTRESS, 0);
                    didIDistress = false;
                } else if (rc.getHealth() <5 && robots.length > 0) {
                    rc.broadcast(GARDENER_DISTRESS, 0);
                    didIDistress = false;
                }
                
                //If round # below 100 && at least 100 bullets, ensure 3 scouts and 1 soldier out at all times
                if(rc.readBroadcast(SOLDIER_CHANNEL) < 1  && rc.getRoundNum() < 100 && rc.getTeamBullets()>100) {
                    if(tryBuildRobot(RobotType.SOLDIER, directionToArchon)) {
                        rc.broadcast(SOLDIER_CHANNEL, rc.readBroadcast(SOLDIER_CHANNEL) + 1);
                    }
                }
                if(rc.readBroadcast(SCOUT_CHANNEL) < 3 && rc.getRoundNum() < 100 && rc.getTeamBullets()>100) {
                    if(tryBuildRobot(RobotType.SCOUT, directionToArchon)) {
                        rc.broadcast(SCOUT_CHANNEL, rc.readBroadcast(SCOUT_CHANNEL)+1);
                    }
                }
                //Build soldier if sense nearby enemies, otherwise hire lumbs if nearby trees
                if(robots.length>0) {
                    tryBuildRobot(RobotType.SOLDIER, directionToArchon);
                    
                } else {
                    if(birthDay%10 ==0)
                        hireLumberjacks(directionToArchon);
                }
                //If someone senses robots nearby, start producing tanks, reset production channel
                if (rc.readBroadcast(PRODUCTIONCHANNEL) > 0) {
                    tryBuildRobot(RobotType.TANK, directionToArchon);
                    rc.broadcast(PRODUCTIONCHANNEL, 0);
                }
                //If you have fully planted, let the archon know so it can produce more gardeners
                
                
                //Broadcast death if near death
                if (!broadcastedDeath)
                    broadcastedDeath = checkBroadcastDeath(GARDENER_CHANNEL);
                birthDay++;
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
                
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void runSoldier() throws GameActionException {
        Direction dir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                //Sense nearby robos
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                //if nearby robots, trigger production from gardeners
                if(robots.length > 0) {
                    rc.broadcast(PRODUCTIONCHANNEL, rc.readBroadcast(PRODUCTIONCHANNEL)+1);
                }
                //Establish location of broadcast
                float xcoord = rc.readBroadcast(XCOORDENEMY);
                float ycoord = rc.readBroadcast(YCOORDENEMY);
                float xcoordGard = rc.readBroadcast(GARDENER_X);
                float ycoordGard = rc.readBroadcast(GARDENER_Y);
                MapLocation enemyLocation = new MapLocation(xcoord, ycoord);
                MapLocation gardLoc = new MapLocation(xcoordGard, ycoordGard);
                rc.setIndicatorDot(enemyLocation, 0, 0, 0);
                //Check for gardeners distressing
                if (rc.readBroadcast(GARDENER_DISTRESS)==1) {
                    rc.setIndicatorLine(rc.getLocation(), gardLoc, 0, 0, 0);
                    tryMoveToLoc(gardLoc);
                    if(rc.getLocation().isWithinDistance(gardLoc, 3) && robots.length==0) {
                        rc.broadcast(GARDENER_DISTRESS, 0);
                    }
                }
                //if nearby robots, trigger production from gardeners
                if(robots.length > 0) {
                    rc.broadcast(PRODUCTIONCHANNEL, rc.readBroadcast(PRODUCTIONCHANNEL)+1);
                }
                
                //If broadcast says come, go there, otherwise if u sense enemies, broadcast andmove randomly
                else if (rc.readBroadcast(SHOULDCOME) == COME) {
                    goToDirectAvoidCollision(enemyLocation);
                    if(rc.getLocation().isWithinDistance(enemyLocation, 6) && robots.length==0) {
                        rc.broadcast(SHOULDCOME, DONTCOME);
                    }
                } else {
                    broadcastIfSenseEnemy();
                    if(rc.canMove(dir)&&!rc.hasMoved()) {
                        rc.move(dir);
                    } else {
                        dir = dir.rotateRightDegrees((float)(Math.random()*180));
                        tryMove(dir);
                    }
                }
                //If there are nearby robots, shoot in their direction
                if (robots.length > 0) {
                    strafeAndShoot(robots[0]);
                }
                
                /*broadcastIfSenseEnemy();
                 System.out.println(rc.readBroadcast(5));
                 MapLocation myLocation = rc.getLocation();
                 
                 // See if there are any nearby enemy robots
                 RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                 MapLocation broadcastLocation = null;
                 // If there are some...
                 if (rc.readBroadcast(5) == DONTCOME) {
	                if (robots.length > 0) {
                 //Broadcast your location for other soldiers to go to
                 rc.broadcast(XCOORDENEMY, (int)robots[0].getLocation().x);
                 rc.broadcast(YCOORDENEMY, (int)robots[0].getLocation().y);
                 rc.broadcast(5, COME);
                 // And we have enough bullets, and haven't attacked yet this turn...
                 strafeAndShoot(robots[0]);
	                }
                 } else {
                	if(robots.length == 0) {
                 rc.broadcast(5, DONTCOME);
                	}
                 }
                 
                 broadcastLocation = new MapLocation(rc.readBroadcast(XCOORDENEMY),rc.readBroadcast(YCOORDENEMY));
                 // Try to move to a preexisting broadcast location, otherwise move randomly
                 if (!rc.hasMoved()){
                	if (rc.readBroadcast(5)==DONTCOME){
                 if(!tryMove(dir)) {
                 dir = dir.rotateRightDegrees((float)(Math.random()*180));
                 tryMove(dir);
                 }
	                } else {
                 goToDirectAvoidCollision(broadcastLocation);
	                }
                 }
                 
                 // Try to move to a broadcast location, otherwise move randomly
                 
                 
                 // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                 */                Clock.yield();
                
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();
        Direction dir = new Direction(0);
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                if (rc.readBroadcast(SHOULDCOME)==DONTCOME) {
                    broadcastIfSenseEnemy();
                }
                
                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                
                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);
                    
                    // If there is a robot, move towards it
                    if (robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);
                        
                        tryMove(toEnemy);
                    }else if (rc.senseNearbyTrees(-1, Team.NEUTRAL).length>0) {
                        TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
                        Direction toTree = rc.getLocation().directionTo(trees[0].getLocation());
                        tryMove(toTree);
                        for (TreeInfo t: trees){
                            if (t.getTeam() == Team.NEUTRAL && rc.canChop(t.getID()))
                                rc.chop(t.getID());
                        }
                    }
                    
                    if(!rc.hasAttacked() && !rc.hasMoved())
                        // Move Randomly
                        if(!tryMove(dir)) {
                            dir = dir.rotateRightDegrees((float)(Math.random()*180));
                            tryMove(dir);
                        }
                    
                    
                    
                    
                    // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                    Clock.yield();
                }
            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void runScout() throws GameActionException {
        System.out.println("I'm a scout!");
        boolean broadcastedDeath = false;
        Direction dir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        
        while (true) {
            try {
                //Shakes nearby trees, tries to move to nearby trees if it cant reach
                shakeNearbyTrees();
                RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                //Targeting gardeners + scouts
                RobotInfo gardenerToKill = null;
                for (RobotInfo robot: robots) {
                    if (robot.getType()==RobotType.GARDENER) {
                        gardenerToKill = robot;
                        break;
                    }
                }
                //If gardener/scout nearby
                if (gardenerToKill != null) {
                    //Go to Robots
                    if(!rc.hasMoved() && !tryMove(gardenerToKill.getLocation()) && rc.canFireSingleShot())
                        rc.fireSingleShot(rc.getLocation().directionTo(gardenerToKill.getLocation()));
                } //Otherwise random explore
                else {
                    if(rc.canMove(dir)&&!rc.hasMoved()) {
                        rc.move(dir);
                    } else if (!rc.hasMoved()) {
                        dir = dir.rotateRightDegrees((float)(Math.random()*180));
                        tryMove(dir);
                    }
                }
                //if nearby robots, trigger production from gardeners
                if(robots.length > 0) {
                    rc.broadcast(PRODUCTIONCHANNEL, rc.readBroadcast(PRODUCTIONCHANNEL)+1);
                }
                
                //If units are already going to an area, dont broadcast your location with enemies
                if(rc.readBroadcast(SHOULDCOME)==DONTCOME)
                    broadcastIfSenseEnemy();
                
                // Broadcast if near death
                if (!broadcastedDeath)
                    broadcastedDeath = checkBroadcastDeath(SCOUT_CHANNEL);
                
                Clock.yield();
                
                
                
                
                /*Team enemy = rc.getTeam().opponent();
                 RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                 if(robots.length>0&& stayStill == false) {
                	if(tryMove(robots[0].getLocation())==false) {
                 stayStill = true;
                	}
                 } else if (robots.length==0) {
                	stayStill = false;
                 }
                 
                 
                 broadcastIfSenseEnemy();
                 MapLocation[] broadcastingRobots = rc.senseBroadcastingRobotLocations();
                 if(rc.canMove(dir)&&!rc.hasMoved()) {
                 rc.move(dir);
                 } else {
                 dir = dir.rotateRightDegrees((float)(Math.random()*180));
                 tryMove(dir);
                 }
                 if(rc.senseNearbyTrees().length>0) {
                 shakeNearbyTrees();
                 }
                 if (robots.length > 0) {
                 //Broadcast your location for other soldiers to go to
                 rc.broadcast(XCOORDENEMY, (int)robots[0].getLocation().x);
                 rc.broadcast(YCOORDENEMY, (int)robots[0].getLocation().y);
                 rc.broadcast(5, COME);
                 if (rc.canFireSingleShot()) {
                 // ...Then fire a bullet in the direction of the enemy.
                 System.out.println("lol");
                 rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                 }
                 }else {
                 rc.broadcast(5, DONTCOME);
                 }*/
            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void runTank() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();
        Direction dir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                //If no current broadcasting location, broadcast if sense enemy
                if(rc.readBroadcast(SHOULDCOME)==DONTCOME)
                    broadcastIfSenseEnemy();
                
                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                
                //Nearby trees in a super close vicinity
                TreeInfo[] friendlyTrees = rc.senseNearbyTrees((float)2.5, rc.getTeam());
                
                
                //Obtain broadcast location
                int xcoord = rc.readBroadcast(XCOORDENEMY);
                int ycoord = rc.readBroadcast(YCOORDENEMY);
                MapLocation broadcastLocation = new MapLocation(xcoord, ycoord);
                
                //If nearby robots, strafe and shoot, otherwise if location distress go to location
                //Otherwise random explore
                if(robots.length>0) {
                    strafeAndShoot(robots[0]);
                } else if(friendlyTrees.length ==0){
                    if (rc.readBroadcast(SHOULDCOME)==COME) {
                        if(rc.canMove(broadcastLocation)) {
                            rc.move(broadcastLocation);
                        }else {
                            tryMoveToLoc(broadcastLocation);
                        }
                    } else if (!rc.hasMoved()) {
                        dir = dir.rotateRightDegrees((float)(Math.random()*180));
                        tryMove(dir);
                    }
                } else {
                    if(rc.canMove(rc.getLocation().directionTo(friendlyTrees[0].getLocation()).opposite())) {
                        rc.move(rc.getLocation().directionTo(friendlyTrees[0].getLocation()).opposite());
                    }
                }
                
                //If you are near broadcast Location and no nearby enemies, turn off the distress
                if (rc.readBroadcast(SHOULDCOME) == COME && rc.getLocation().isWithinDistance(broadcastLocation, 4) && robots.length > 0) {
                    rc.broadcast(SHOULDCOME, DONTCOME);
                }
                
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
                
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
    
    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,4);
    }
    
    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        
        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        
        // Now try a bunch of similar angles
        int currentCheck = 1;
        
        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }
        
        // A move never happened, so return false.
        return false;
    }
    
    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();
        
        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;
        
        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);
        
        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }
        
        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)
        
        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    
    static void wander() throws GameActionException {
        try {
            Direction dir = randomDirection();
            tryMove(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Try to plant a tree in multiple directions; if unable, return false.
     *
     */
    static boolean tryPlantTree(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        if(rc.getTeamBullets() < 50) {
            return true;
        }
        // First, try intended direction
        if (rc.canPlantTree(dir)) {
            rc.plantTree(dir);
            return true;
        }
        
        // Now try a bunch of similar angles
        int currentCheck = 1;
        
        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canPlantTree(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.plantTree(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canPlantTree(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.plantTree(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }
        
        // Planting never happened, so return false.
        return false;
    }
    
    /**
     * Try planting the corners of a box.
     * @param target = the center location
     */
    static int tryPlantCorners(int check, MapLocation target) throws GameActionException{
        if (check == 0 || check == 1 || check == 8) {
            if(rc.canMove(Direction.getNorth()))
                rc.move(Direction.getNorth());
            if (check == 1)
                tryPlantTree(Direction.getWest().rotateRightDegrees(2), 2, 5);
            return check + 1;
        }
        else if (check == 2){
            tryPlantTree(Direction.getEast().rotateLeftDegrees(2), 2, 5);
            return check + 1;
        }
        else if (check == 7){
            tryPlantTree(Direction.getEast().rotateRightDegrees(2), 2, 5);
            return check + 1;
        }
        else if (check == 4 || check == 9){
            if (rc.canMove(target))
                rc.move(target);
            return check + 1;
        }
        else if (check == 3 || check == 5 || check == 6){
            if (rc.canMove(Direction.getSouth()))
                rc.move(Direction.getSouth());
            if (check == 6)
                tryPlantTree(Direction.getWest().rotateLeftDegrees(2), 2, 5);
            return check + 1;
        }
        return 0;
    }
    
    /**
     * Try planting trees in a square formation.
     *
     */
    static void tryPlantSquare(Direction a) throws GameActionException{
        int num = 0;
        Direction dir = a;
        boolean planted = tryPlantTree(dir, 2, 10);
        while (num <= 1 && !planted){
            dir = dir.rotateLeftDegrees(90);
            planted = tryPlantTree(dir, 2, 10);
            if (planted) return;
            num++;
        }
    }
    
    /**
     * Figure out which direction to make the square opening
     *
     */
    static Direction figureOutDirection() throws GameActionException{
        Direction initialArchonDir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        if(initialArchonDir.degreesBetween(Direction.getNorth())<90) {
            return Direction.getNorth();
        } else {
            return Direction.getSouth();
        }
    }
    
    /**
     * Try to water a nearby tree if it needs watering.
     *
     */
    static boolean tryWaterTree() throws GameActionException{
        TreeInfo[] trees = rc.senseNearbyTrees(-1, rc.getTeam());
        for (TreeInfo t : trees){
            if (rc.canWater(t.getID()) && t.getHealth() < 45){
                rc.water(t.getID());
                //System.out.println("watered!");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Attempts to move towards a given location that contains an object,
     * while avoiding small obstacles direction in the path.
     *
     */
    static boolean tryMoveToLoc(MapLocation loc) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(loc);
        return tryMove(dir);
    }
    
    /**
     * Checks the need to broadcast death, and does so if needed.
     * Returns true if broadcasted.
     *
     * Precondition: points to a channel that records the active number of a certain type of robot.
     * @throws GameActionException
     */
    static boolean checkBroadcastDeath(int channel) throws GameActionException{
        if (rc.getHealth() <= 5 ) {
            int prevNumBots = rc.readBroadcast(channel);
            rc.broadcast(channel, prevNumBots - 1);
            return true;
        }
        return false;
    }
    
    /**
     * Hires lumberjacks when there are nearby neutral trees
     *
     * @throws GameActionException
     */
    static void hireLumberjacks(Direction dir) throws GameActionException {
        TreeInfo[] nearby = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        if (nearby.length > 0) {
            tryBuildRobot(RobotType.LUMBERJACK, dir);
        }
    }
    
    /**
     * Returns true and immediately donates all bullets if we can win in that
     * turn. Returns false otherwise
     *
     * @return
     * @throws GameActionException
     */
    static boolean canWin() throws GameActionException {
        float difference = 1000 - rc.getTeamVictoryPoints();
        if ((rc.getTeamBullets() / 10) >= difference) {
            rc.donate(rc.getTeamBullets());
            return true;
        } else
            return false;
    }
    
    static void goToDirectAvoidCollision(MapLocation dest) throws GameActionException{
        tryMove(rc.getLocation().directionTo(dest),10,90);
    }
    
    static void goToDirectAvoidCollision(float x, float y) throws GameActionException {
        MapLocation dest = new MapLocation(x,y);
        goToDirectAvoidCollision(dest);
    }
    
    static boolean tryBuildRobot(RobotType robo, Direction dir) throws GameActionException {
        // First, try intended direction
        if (rc.canBuildRobot(robo, dir)) {
            rc.buildRobot(robo, dir);
            return true;
        }
        
        // Now try a bunch of similar angles
        int currentCheck = 1;
        int checksPerSide = 180;
        int degreeOffset = 1;
        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canBuildRobot(robo,dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.buildRobot(robo,dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canBuildRobot(robo,dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.buildRobot(robo,dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }
        return false;
        
    }
    
    static void produceRandom(Direction dir) throws GameActionException {
        int choose = (int) (Math.random() * 99);
        if (choose < 30 && rc.canBuildRobot(RobotType.SCOUT, dir))
            tryBuildRobot(RobotType.SCOUT, dir);
        if (choose < 90 && rc.canBuildRobot(RobotType.SOLDIER, dir))
            tryBuildRobot(RobotType.SOLDIER, dir);
        //		else if (choose < 75 && rc.canBuildRobot(RobotType.LUMBERJACK, dir))
        //			tryBuildRobot(RobotType.LUMBERJACK, dir);
        else if (rc.canBuildRobot(RobotType.TANK, dir)) {
            tryBuildRobot(RobotType.TANK ,dir);
        }
    }
    
    static boolean shakeNearbyTrees() throws GameActionException{
        TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        
        int randomIndex = (int)(Math.random()*nearbyTrees.length);
        //System.out.println(randomIndex);
        for (TreeInfo trees: nearbyTrees) {
            if(trees.getContainedBullets()>0) {
                //System.out.println("shaking");
                tryMoveToLoc(trees.getLocation());
                rc.shake(trees.getLocation());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return true if there's enough space to settle.
     */
    static boolean settleDown() throws GameActionException {
        if(!senseIfFriendlyArchonNear())
            return !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), SETTLERADIUS);
        return false;
    }
    
    /**
     * See if there are enemy/neutral trees nearby- chop them if possible, otherwise
     * move to them if possible.
     */
    static void killTheTrees(TreeInfo[] trees) throws GameActionException {
        if (trees.length > 0){
            if (!rc.hasAttacked()){
                for (TreeInfo t: trees){
                    if (rc.canChop(t.getID())){ 
                        rc.chop(t.getID());
                        break;
                    }
                }
            }
            if (!rc.hasMoved()){
                Direction toTree = rc.getLocation().directionTo(trees[0].getLocation());
                tryMove(toTree);
            }
        }
    }
    
    static MapLocation tryFindSpot() throws GameActionException{
        float circleRadius = 3 + GameConstants.GENERAL_SPAWN_OFFSET;
        Direction dir = new Direction(0);
        int checks = 1;
        while (checks <= 8){
            MapLocation endLoc = rc.getLocation().add(dir, 1);
            if (rc.onTheMap(endLoc) && !rc.isLocationOccupied(endLoc)){
                if (!rc.isCircleOccupiedExceptByThisRobot(endLoc, (float)2.5)) {
                    return endLoc;
                }
            }
            dir = dir.rotateLeftDegrees(45);
            checks++;
        }
        return null;
    }
    
    static boolean broadcastIfSenseEnemy() throws GameActionException {
        if(rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length>0) {
            int enemy = rc.readBroadcast(PRODUCTIONCHANNEL);
            rc.broadcast(XCOORDENEMY, (int)rc.senseNearbyRobots(-1, rc.getTeam().opponent())[0].getLocation().x);
            rc.broadcast(YCOORDENEMY, (int)rc.senseNearbyRobots(-1, rc.getTeam().opponent())[0].getLocation().y);
            rc.broadcast(SHOULDCOME, COME);
            rc.broadcast(PRODUCTIONCHANNEL, enemy);
            return true;
        } 
        System.out.println("dontcome");
        rc.broadcast(SHOULDCOME, DONTCOME);
        return false;
    }
    
    static void strafeAndShoot(RobotInfo enemy) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(enemy.getLocation());
        if (!rc.hasMoved()&& !tryMove(dir.rotateLeftDegrees(90)))
            tryMove(dir.rotateRightDegrees(90));
        if(rc.canFirePentadShot()) {
            rc.firePentadShot(dir);
        } else if (rc.canFireTriadShot()) {
            rc.fireTriadShot(dir);
        } else if (rc.canFireSingleShot()) {
            rc.fireSingleShot(dir);
        }
    }
    
    static void squarePlant(int i) throws GameActionException {
        if(i == 0) {
            rc.move(Direction.getNorth(),1);
        }
        if(i == 1) {
            rc.move(Direction.getNorth(),1);
            rc.plantTree(Direction.getWest());
        }
        if(i == 2) {
            rc.plantTree(Direction.getEast());
        }
        if(i == 3) {
            rc.move(Direction.getSouth(),1);
        }
        if(i == 4) {
            rc.move(Direction.getSouth(),1);
            rc.plantTree(Direction.getWest());
        }
        if(i == 5) {
            rc.plantTree(Direction.getEast());
        }
        if(i == 6) {
            rc.move(Direction.getSouth(),1);
        }
        if(i == 7) {
            rc.move(Direction.getSouth(),1);
            rc.plantTree(Direction.getEast());
        }
        if(i==8) {
            rc.plantTree(Direction.getWest());
        }
        if(i==9) {
            rc.move(Direction.getNorth(),1);
        }
        if(i==10) {
            rc.move(Direction.getNorth(),1);
            rc.plantTree(Direction.getNorth());
        }
        
    }
    
    static void goIntoTree() throws GameActionException{
        TreeInfo[] trees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if(trees.length>0 && rc.canMove(trees[0].getLocation()))
            rc.move(trees[0].getLocation());
    }
    
    static boolean tryMove(MapLocation enemyLocation) throws GameActionException {
        if (rc.canMove(enemyLocation)) {
            rc.move(enemyLocation);
            return true;
        } else {
            goIntoTree();
            return false;
        }
    }
    
    static float distanceAwayFromLocation (MapLocation location) throws GameActionException {
        float xcoordMe = rc.getLocation().x;
        float ycoordMe = rc.getLocation().y;
        float xcoordLoc = location.x;
        float ycoordLoc = location.y;
        return (float) (Math.sqrt((xcoordMe-xcoordLoc)*(xcoordMe-xcoordLoc)-(ycoordMe-ycoordLoc)*(ycoordMe-ycoordLoc)));
    }
    
    /**
     * MapLocation of enemy robots
     */
    static MapLocation[] locEnemyRobotsNearby(RobotInfo[] enemies) throws GameActionException {
        MapLocation[] locations = new MapLocation[enemies.length];
        for (int i = 0; i<enemies.length; i++) {
            locations[i] = enemies[i].getLocation();
        }
        return locations;
    }
    
    /**
     * Archon retreat code
     * @param enemies  location of enemies
     * @throws GameActionException
     */
    static boolean runAwayBravely(MapLocation[] enemies) throws GameActionException {
        if(enemies.length == 1){ //only one enemy
            Direction dir = new Direction(enemies[0].x, enemies[0].y); //direction of the enemy
            return tryMove(dir.opposite()); //go in the opposite direction
        }
        else{
            MapLocation ml = getCenterOfMass(enemies);
            Direction dir = new Direction(ml.x, ml.y); //direction of the CoM of the enemy
            return tryMove(dir.opposite()); //go in the opposite direction
        }
        
        
    }
    
    /**
     * Calculates the center of mass of the enemies, based off their locations
     * @param enemies location of enemies
     * @return the map location of the center of mass
     * @throws GameActionException
     */
    static MapLocation getCenterOfMass(MapLocation[] enemies) throws GameActionException {
        MapLocation com = new MapLocation(0,0); //center of mass
        float sumx = 0; //sum of x coordinates of enemies
        float sumy =0; //sum of y coordinates of enemies
        
        //calculate center of mass, x coordinate
        for(MapLocation ml : enemies){
            sumx += ml.x;
            sumy += ml.y;
        }
        
        float cx = sumx / enemies.length; //actual calculation of CoM
        float cy = sumy / enemies.length;
        
        com = com.translate(cx,cy); //translates so the point is actually the CoM
        
        return com;
    }
    /**
     * returns true if you sense an archon or gardener nearby
     * @return
     * @throws GameActionException
     */
    static boolean senseIfFriendlyArchonNear() throws GameActionException {
        RobotInfo[] friendlyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo robots: friendlyRobots) {
            if (robots.getType()==RobotType.ARCHON ||
                robots.getType() == RobotType.GARDENER) {
                return true;
            }
        }
        return false;
    }
    static void updateMap() throws GameActionException {
        if(rc.getLocation().x < rc.readBroadcast(XMINMAP)) {
            rc.broadcast(XMINMAP, (int)rc.getLocation().x);
        }
        if(rc.getLocation().x > rc.readBroadcast(XMAXMAP)) {
            rc.broadcast(XMINMAP, (int)rc.getLocation().x);
        }
        if(rc.getLocation().y < rc.readBroadcast(YMINMAP)) {
            rc.broadcast(YMINMAP, (int)rc.getLocation().y);
        }
        if(rc.getLocation().y > rc.readBroadcast(YMAXMAP)) {
            rc.broadcast(YMAXMAP, (int)rc.getLocation().y);
        }
    }
}
