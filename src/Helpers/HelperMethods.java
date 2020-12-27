package Helpers;

import battlecode.common.*;

import java.util.Random;

/**
 * Class for common helper methods
 */
public class HelperMethods {
    static RobotController rc;
    static Random rand;

    public HelperMethods(RobotController rc) {
        rand = new Random(rc.getID());
        this.rc = rc;
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return new Direction((float)HelperMethods.randomNum() * 2 * (float)Math.PI);
    }

//    // Iterates through all given bullets to see if they will collide with given location,
//    // return the distance from the location to the bullets as an array
//    public static ArrayList<Float> isMoveSafe(MapLocation loc, BulletInfo[] bullets) {
//        ArrayList<Float> bulletDistances = new ArrayList<>();
//        for (BulletInfo bullet : bullets) {
//            if (willCollideWithMe(loc, bullet)) {
//                bulletDistances.add(loc.distanceTo(bullet.getLocation()));
//            }
//        }
//        return bulletDistances;
//    }

//    // safeStep alredy checks to see if robot can stay in current location, so this method assumes that
//    // current location is unsafe
//    public static Direction bulletDodge(BulletInfo[] bullets) throws GameActionException {
//        ArrayList<Float> canStayStill = isMoveSafe(rc.getLocation(), bullets);
//        if (canStayStill.size() == 0 || canStayStill.get(0) > 5) {
//            return null;
//        }
//        else {
//            Direction[] dirs = RobotPlayer.getDirList();
//            float farthestDist = 0;
//            Direction toGoDir = null;
//            // For all directions, find which one is the farthest away from all bullets
//            for (Direction dir : dirs) {
//                float allDists = 0;
//                for (BulletInfo bullet : bullets) {
//                    allDists = allDists + rc.getLocation().add(dir, rc.getType().strideRadius).distanceTo(bullet.getLocation());
//                }
//                if (allDists > farthestDist && rc.canMove(dir)) {
//                    farthestDist = allDists;
//                    toGoDir = dir;
//                }
//            }
//
//            if (toGoDir == null) {
//                return null;
//            }
//
//            return safeStep(toGoDir, bullets);
//        }
//    }

//    // Bullet dodge algorithm for movement, does the following:
//    // - Finds the actual direction robot is going in
//    // - Checks if bullets in range 4 will collide with new player location
//    //    - If there are, check if it is safe to stay still
//    //         - If it is safe to stay still, do so
//    //         - If it is not safe to stay still, iterate through all directions tryMove style to find a safe one
//    //    - If there aren't, move in normal direction
//    public static Direction safeStep(Direction direction, BulletInfo[] bullets) throws GameActionException {
//        Direction actualDir = tryDirs(direction);
//
//        //rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(actualDir).add(actualDir).add(actualDir),244, 66, 66);
//
//        if (actualDir == null) {
//            return null;
//        }
//
//        float strideRadius = rc.getType().strideRadius;
//        ArrayList<Float> canMoveInDir = isMoveSafe(rc.getLocation().add(actualDir, strideRadius), bullets);
//
//        if (canMoveInDir.size() > 0 && canMoveInDir.get(0) <= 5) {
//            ArrayList<Float> canStayStill = isMoveSafe(rc.getLocation(), bullets);
//            if (canStayStill.size() == 0 || canStayStill.get(0) > 5) {
//                return actualDir;
//            }
//            else {
//                // Since we will be moving to the new locations, add 1 unit to bullet range
//                ArrayList<Float> canMoveInOppDir = isMoveSafe(rc.getLocation().add(actualDir.opposite(), strideRadius), bullets);
//                if ((canMoveInOppDir.size() == 0 || canMoveInOppDir.get(0) > 6) && rc.canMove(actualDir.opposite())) {
//                    rc.move(actualDir.opposite());
//                    return actualDir.opposite();
//                }
//                else {
//                    int currentCheck = 1;
//
//                    while(currentCheck<=checksPerSide) {
//                        Direction newDir = actualDir.rotateLeftDegrees(degreeOffset*currentCheck);
//                        ArrayList<Float> tryAngle = isMoveSafe(rc.getLocation().add(newDir, strideRadius), bullets);
//                        // Try the offset of the left side, if no bullets will collide or those bullets are
//                        // a ways away, return the new direction
//                        if((tryAngle.size() == 0 || tryAngle.get(0) > 6) && rc.canMove(newDir)) {
//                            rc.move(newDir);
//                            return newDir;
//                        }
//
//                        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newDir).add(newDir).add(newDir),66,244,241);
//
//
//                        newDir = actualDir.rotateRightDegrees(degreeOffset*currentCheck);
//                        tryAngle = isMoveSafe(rc.getLocation().add(newDir, strideRadius), bullets);
//                        // Try the offset on the right side
//                        if((tryAngle.size() == 0 || tryAngle.get(0) > 6) && rc.canMove(newDir)) {
//                            rc.move(newDir);
//                            return newDir;
//                        }
//
//                        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newDir).add(newDir).add(newDir),66,244,241);
//
//                        // No move performed, try slightly further
//                        currentCheck++;
//                    }
//
//                    // If nothing is safe, just stand still
//                    rc.setIndicatorDot(rc.getLocation(), 66, 244, 119);
//                    return null;
//                }
//            }
//        }
//        else {
//            System.out.println(actualDir);
//            rc.move(actualDir);
//            return actualDir;
//        }
//    }
//
//    public static Direction dodgeBullets(Direction dir, int degreeOffset, int checksPerSide) throws GameActionException {
//        // Find nearby bullets
//        BulletInfo[] bullets = rc.senseNearbyBullets();
//
//        if (bullets.length > 0) {
//            // List for bullets on collision course with robot
//            ArrayList<BulletInfo> bulletsWillCollide = new ArrayList<>();
//
//            // Initialize bulletsWillCollide
//            for (BulletInfo bullet : bullets) {
//                if (willCollideWithMe(rc.getLocation(), bullet)) {
//                    bulletsWillCollide.add(bullet);
//                }
//            }
//
//            // If there are bullets in range...
//            if (bulletsWillCollide.size() > 1) {
//                // Find the closest bullet
//                BulletInfo closestBullet = bulletsWillCollide.get(0);
//                BulletInfo secondClosestBullet = bulletsWillCollide.get(1);
//
//                // Find direction that will move the robot out of the closest bullet path and away from second
//                // closest bullet and move.
//                Direction closestBulletDirection = closestBullet.getDir();
//                MapLocation leftOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateLeftDegrees(90), rc.getType().strideRadius);
//                MapLocation rightOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateRightDegrees(90), rc.getType().strideRadius);
//
//                if (secondClosestBullet.getLocation().distanceTo(leftOfClosestBullet) >
//                        secondClosestBullet.getLocation().distanceTo(rightOfClosestBullet)) {
//                    return fullMove(closestBulletDirection.rotateLeftDegrees(90));
//                } else {
//                    return fullMove(closestBulletDirection.rotateRightDegrees(90));
//                }
//            } else if (bulletsWillCollide.size() == 1) {
//                BulletInfo secondClosest = bullets[0];
//                if (secondClosest.getID() == bulletsWillCollide.get(0).getID() && bullets.length > 1) {
//                    secondClosest = bullets[1];
//                }
//
//                Direction closestBulletDirection = bulletsWillCollide.get(0).getDir();
//                MapLocation leftOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateLeftDegrees(90), rc.getType().strideRadius);
//                MapLocation rightOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateRightDegrees(90), rc.getType().strideRadius);
//
//                if (secondClosest.getLocation().distanceTo(leftOfClosestBullet) >
//                        secondClosest.getLocation().distanceTo(rightOfClosestBullet)) {
//                    return fullMove(closestBulletDirection.rotateLeftDegrees(90));
//                } else {
//                    return fullMove(closestBulletDirection.rotateRightDegrees(90));
//                }
//            }
//            else {
//                for (BulletInfo bullet : bullets) {
//                    if (willCollideWithMe(rc.getLocation().add(dir, rc.getType().strideRadius), bullet)) {
//                        return dir;
//                    }
//                }
//                // After looping through each bullet, nothing will collide with me if I move forwards
//                // TODO: the second tryMove function could move you into the path of a bullet
//                return fullMove(dir);
//            }
//        }
//        else {
//            return null;
//        }
//    }
//
//    /**
//     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
//     *
//     * @param dir The intended direction of movement
//     * @return true if a move was performed
//     * @throws GameActionException
//     */
//    public static Direction tryMove(Direction dir) throws GameActionException {
//        BulletInfo[] bullets = rc.senseNearbyBullets();
//
//        Direction results = safeStep(dir, bullets);
//
//        if (results == null) {
//            results = bulletDodge(bullets);
//            if (results == null) {
//                return dir;
//            }
//            return results;
//        }
//        else {
//            return results;
//        }
//    }

    //public static Direction findOpenLocation(MapLocation loc) {

    //}

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    public static boolean willCollideWithMe(MapLocation loc, BulletInfo bullet) {
        MapLocation myLocation = loc;

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

    public static float randomNum() {
        return rand.nextFloat();
    }

//    // Keeps robot in range and returns the direction it moved in
//    public static Direction stayInLocationRange(Direction goingDir, MapLocation myLoc, int minDist, int maxDist) throws GameActionException {
//        Direction togo = goingDir;
//        float archonDist = rc.getLocation().distanceTo(myLoc);
//        // Calculate random variation for direction to move in
//        float toRotate = (float) (randomNum() * 140) - 70;
//        // Check if robot has gone too far from archon
//        if (archonDist >= maxDist) {
//            if (!(rc.getLocation().add(goingDir).distanceTo(myLoc) < archonDist)) {
//                Direction archonDir = rc.getLocation().directionTo(myLoc);
//                togo = archonDir.rotateLeftDegrees(toRotate);
//            }
//        }
//        // Check if robot has come too close to archon
//        else if (archonDist <= minDist) {
//            if (!(rc.getLocation().add(goingDir).distanceTo(myLoc) >= archonDist)) {
//                Direction archonDir = rc.getLocation().directionTo(myLoc).opposite();
//                togo = archonDir.rotateLeftDegrees(toRotate);
//            }
//        }
//
//        Direction tryMoveResult = tryMove(togo);
//
//        // Only receive null if tryMove cannot go anywhere, so try going opposite direction
//        if (tryMoveResult == null) {
//            togo = togo.opposite();
//        }
//        else {
//            togo = tryMoveResult;
//        }
//
//        return togo;
//    }
}