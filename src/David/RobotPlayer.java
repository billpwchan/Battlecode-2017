package David;


import battlecode.common.*;
import battlecode.server.GameState;

import javax.management.relation.RoleNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public strictfp class RobotPlayer {
	static final int ArchonPositionX = 99;
	static final int ArchonPositionY = 999;
	static final int GardenerPositionX = 100;
	static final int GardenerPositionY = 101;
	static final int Current_Gardener = 103;
	static final int GARDENER_CHANNEL = 104;
	static final int ARCHON_DIE = 107;
	static final int Solider_Die=108;
	static final int Archon_Distress=109;
	static final int OwnArchonPositionX = 110;
	static final int OwnArchonPositionY = 111;
	static final int EnemySoliderPositionX = 112;
	static final int EnemySoliderPositionY = 113;
	static RobotController rc;
	static Direction[] dirList = new Direction[4];
	static Direction goingDir;
	static Random rand;
	static int GARDENER_MAX;

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
			runScout(1);


		}
	}

	// call this method in rungardener, it will tell the archon that the gardener still exist
	public static void Gardener_Broadcast_Existance() throws GameActionException {
		int previous_Count = rc.readBroadcast(Current_Gardener);
		rc.broadcast(Current_Gardener, previous_Count + 1);

	}

	public static void enemyArchonDieCheck(RobotInfo b) throws GameActionException{
		if (b.getType()==RobotType.ARCHON && b.getHealth()<10){
			rc.broadcast(ARCHON_DIE, 1);
		}
	}


	public static void enemySoliderDieCheck(RobotInfo b ) throws GameActionException{
		if (b.getType()== RobotType.SOLDIER && b.getHealth()<10){
			rc.broadcast(Solider_Die, 1);
		}
	}


	public static void ArchonBroadcast(RobotInfo b) throws GameActionException {
		if (b.getType() == RobotType.ARCHON && b.getTeam() != rc.getTeam()) {
			rc.broadcast(ArchonPositionX, (int) (b.getLocation().x));
			rc.broadcast(ArchonPositionY, (int) (b.getLocation().y));
		}
	}

	public static void GardenerBroadcast(RobotInfo b) throws GameActionException {
		if (b.getType() == RobotType.GARDENER && b.getTeam() != rc.getTeam()) {
			rc.broadcast(GardenerPositionX, (int) (b.getLocation().x));
			rc.broadcast(GardenerPositionY, (int) (b.getLocation().y));
		}
	}

	public static void SoliderBroadcast ( RobotInfo b ) throws GameActionException{
		if (b.getType() == RobotType.SOLDIER && b.getTeam()!=rc.getTeam()){
			rc.broadcast(EnemySoliderPositionX, (int) (b.getLocation().x));
			rc.broadcast(EnemySoliderPositionY, (int) (b.getLocation().y));
			rc.broadcast(Solider_Die, 1);
		}
	}

	// to a location
	public static void directionalTravelToASpecificLocation(MapLocation destination) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(destination);


		double rand = Math.random();

		if (rand < 0.8) {
			if (!rc.hasMoved() && rc.canMove(dir)) {
				rc.move(dir);
			} else if (!rc.hasMoved() && !rc.canMove(dir)) {
				//check
				if (rc.canMove(dir.rotateLeftDegrees(90))) {
					rc.move(dir.rotateLeftDegrees(90));
				} else if (rc.canMove(dir.rotateLeftDegrees(90))) {
					rc.move(dir.rotateLeftDegrees(90));
				} else {
					if (rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
					}
				}
			}
		}
		if (0.8 <= rand && rand <= 0.9) {
			tryMove(dir.rotateLeftDegrees(90));
		}
		if (0.9 < rand && rand <= 1) {
			tryMove(dir.rotateLeftDegrees(180));
		}
	}

	// to a direction
	public static void directionalTravel(Direction dir) {
		try {

			if (!rc.hasMoved() && rc.canMove(dir)) {
				rc.move(dir);
			} else if (!rc.hasMoved() && !rc.canMove(dir)) {
				//check

				if (rc.canMove(dir.rotateLeftDegrees(90))) {
					rc.move(dir.rotateLeftDegrees(90));
				} else if (rc.canMove(dir.rotateLeftDegrees(90))) {
					rc.move(dir.rotateLeftDegrees(90));
				} else {
					if (rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


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
			if (rc.canMove(dir) && !rc.hasMoved()) {
				rc.move(dir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static boolean shakeNearbyTrees() throws GameActionException {
		TreeInfo[] nearTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);

		int rIndex = (int) (Math.random() * nearTrees.length);
		for (TreeInfo trees : nearTrees) {
			if (trees.getContainedBullets() > 0) {
				directionalTravelToASpecificLocation(trees.getLocation());
				rc.shake(trees.getLocation());
				return true;
			}
		}
		return false;
	}

	static boolean canwin() throws GameActionException {
		float difference = 1000 - rc.getTeamVictoryPoints();
		if ((rc.getTeamBullets() / 10) >= difference) {
			rc.donate(rc.getTeamBullets());
			return true;
		} else
			return false;
	}

	private static boolean checkIfSurroundinghaveGardener(MapLocation location) {
		RobotInfo[] bot = rc.senseNearbyRobots();
		if (bot.length != 0) {
			for (RobotInfo b : bot) {
				float distanceBetween = (b.getLocation()).distanceTo(rc.getLocation());
				if (b.getType() == RobotType.GARDENER && (b.getTeam() == rc.getTeam()) && distanceBetween <= 6) {
					return true;
				}
			}
		} else {
			return false;
		}


		return false;
	}

	static int checkNumTreesNearby() throws GameActionException {
		TreeInfo[] tree = rc.senseNearbyTrees(2, rc.getTeam());
		return tree.length;

	}
	static void plantTrees(Direction plantDir) throws GameActionException {
		if (rc.canPlantTree(plantDir)) {
			rc.plantTree(plantDir);
		} else if (rc.canPlantTree(plantDir.rotateLeftDegrees(60))) {
			rc.plantTree(plantDir.rotateLeftDegrees(60));
		} else if (rc.canPlantTree(plantDir.rotateLeftDegrees(120))) {
			rc.plantTree(plantDir.rotateLeftDegrees(120));
		} else if (rc.canPlantTree(plantDir.rotateLeftDegrees(180))) {
			rc.plantTree(plantDir.rotateLeftDegrees(180));
		} else if (rc.canPlantTree(plantDir.rotateLeftDegrees(240))) {
			rc.plantTree(plantDir.rotateLeftDegrees(240));
		} else {

		}
	}

	static void Gardener_buildRobot(RobotType type, Direction dir) throws GameActionException {
		if (rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(60))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(60));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(120))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(120));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(180))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(180));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(240))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(240));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(300))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(300));
		} else {

		}
	}



	private static MapLocation giveMapLocationOfArchon(final int X, final int Y) throws GameActionException {
		float positionX = rc.readBroadcast(X);
		float positionY = rc.readBroadcast(Y);

		MapLocation archonLocation = new MapLocation(positionX, positionY);

		return archonLocation;

	}


	private static boolean checkFriendlyFire(Direction dir) {
		RobotInfo[] bots = rc.senseNearbyRobots();
		for (RobotInfo b : bots) {
			if (b.getTeam() == rc.getTeam()) {
				if (rc.getLocation().directionTo(b.getLocation()).equals(dir)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean ThereIsEnemyBotNearBy() throws GameActionException {
		RobotInfo[] bots = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
		if (bots.length != 0) {
			return true;

		}else {
			return false;
		}
	}

	public static void tryToWater() throws GameActionException {
		if (rc.canWater()) {
			TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
			if (nearbyTrees.length != 0) {
				for (int i = 0; i < nearbyTrees.length; i++)
					if (nearbyTrees[i].getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) {
						if (rc.canWater(nearbyTrees[i].getID())) {
							rc.water(nearbyTrees[i].getID());
							break;
						}
					}
			} else {
			}
		}
	}

	public static void tryToShake() throws GameActionException {
		if (rc.canShake()) {
			TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
			if (nearbyTrees.length != 0) {
				for (int i = 0; i < nearbyTrees.length; i++) {
					rc.shake(nearbyTrees[i].getID());
					break;
				}
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


	static boolean willCollideWithMe(BulletInfo bullet) throws GameActionException {
		MapLocation mylocation = rc.getLocation();

		//get relevant bullet information
		Direction propagationDirecion = bullet.dir;
		MapLocation bulletLocation = bullet.location;

		//CAlculate bullet

		Direction directionToRobot = bulletLocation.directionTo(mylocation);
		float distToRobot = bulletLocation.distanceTo(mylocation);
		float theta = propagationDirecion.radiansBetween(directionToRobot);

		if (Math.abs(theta) >= Math.PI / 2) {
			return false;
		}

		float perpendicularDist = (float) Math.abs(distToRobot * Math.tan(theta));

		return (perpendicularDist <= rc.getType().bodyRadius);
	}

	static boolean scoutIntheTree() throws GameActionException{
		TreeInfo[] trees = rc.senseNearbyTrees();
		for (TreeInfo t : trees){
			if (t.getLocation().equals(rc.getLocation())){
				return true;
			}
		}
		return false;
	}


	static boolean tryMove(Direction dir) throws GameActionException {
		return tryMove(dir, 30, 3);
	}

	static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

		if (rc.getType()==RobotType.SCOUT&&scoutIntheTree()){
			return true;
		}
		// First, try intended direction
		if (!rc.hasMoved() && rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}

		// Now try a bunch of similar angles
		//boolean moved = rc.hasMoved();
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			if (!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
				rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
				return true;
			}
			// Try the offset on the right side
			if (!rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
				rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
				return true;
			} else {

			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		return false;
	}


	static boolean trySidestep(BulletInfo bullet) throws GameActionException {

		Direction towards = bullet.getDir();
		MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
		MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

		return (tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
	}

	static void dodge() throws GameActionException {
		BulletInfo[] bullets = rc.senseNearbyBullets();
		if (bullets.length != 0) {
			for (BulletInfo bi : bullets) {
				if (willCollideWithMe(bi)) {
					trySidestep(bi);
				}
			}
		}
	}

	static void soliderShoot(RobotInfo e, int enemyNum) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(e.getLocation());
        if (!rc.hasMoved()&& !tryMove(dir.rotateLeftDegrees(90)))
            tryMove(dir.rotateRightDegrees(90));
		if (!rc.hasMoved()&& !tryMove(dir.rotateLeftDegrees(90)))
			directionalTravel(dir.rotateRightDegrees(90));
		if(rc.canFirePentadShot()&&enemyNum!=1) {
			rc.firePentadShot(dir);
		} else if (rc.canFireSingleShot()) {
			rc.fireSingleShot(dir);
		}
	}

	public static int CheckHowManySidesCanBuildtrees(Direction dir) throws GameActionException {
		int numberOfTreesCanBeBuilt = 0;

		MapLocation centerLocation = new MapLocation(rc.getLocation().x, rc.getLocation().y);
		RobotInfo[] bots = rc.senseNearbyRobots(2, rc.getTeam());
		for (int i = 0; i < 6; i++) {
			if (rc.isLocationOccupied(centerLocation.add(dir, 2)) == false ) {
				numberOfTreesCanBeBuilt += 1;
			}
			dir = dir.rotateLeftDegrees(60);
		}
		System.out.println(numberOfTreesCanBeBuilt);
		if (bots.length != 0) {
			for (RobotInfo b : bots) {
				if (b.getType() == RobotType.LUMBERJACK || b.getType() == RobotType.SCOUT || b.getType() == RobotType.SOLDIER ) {
					numberOfTreesCanBeBuilt += 1;
				}

			}
		}
		System.out.println("****" + numberOfTreesCanBeBuilt);

		return numberOfTreesCanBeBuilt;

	}

	public static boolean onlyscoutandarchon(RobotInfo []  b) throws GameActionException {
		int scout_count = 0;
		int robot_count = 0;
		int archon_count = 0;
		for (RobotInfo bot : b) {
			if (bot.getType() == RobotType.SCOUT ) {
				scout_count += 1;
				robot_count += 1;
			} else if (bot.getType() == RobotType.ARCHON){
				archon_count += 1;
				robot_count += 1;
			}
			else {
				robot_count += 1;
			}
		}
		if ((scout_count + archon_count == robot_count) || (archon_count == robot_count) || (scout_count == robot_count)) {
			return true;
		} else {
			return false;
		}
	}

	static void gardenerbuild(RobotType type, Direction dir) throws GameActionException {

		if (rc.canBuildRobot(type, dir.rotateLeftDegrees(60))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(60));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(120))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(120));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(180))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(180));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(240))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(240));
		} else if (rc.canBuildRobot(type, dir.rotateLeftDegrees(300))) {
			rc.buildRobot(type, dir.rotateLeftDegrees(300));
		}else{
			System.out.print("I can't build any fking gardener!!");
		}
	}

	public static void end_game() throws GameActionException {
		int game_round = rc.getRoundLimit();
		int current_round = rc.getRoundNum();

		if (game_round == current_round +1) {
			rc.donate(rc.getTeamBullets());
		}
	}

	// ROBOT CLASSES!!!!
	public static void runArchon() throws GameActionException {
		goingDir = randomDir();
		MapLocation[] ownarchon = rc.getInitialArchonLocations(rc.getTeam());
		int ownarchonnumber = ownarchon.length;
		Direction dir = randomDir();
		if (ownarchonnumber == 1) {
			GARDENER_MAX = 5;
		} else if (ownarchonnumber == 2) {
			GARDENER_MAX = 10;
		} else if (ownarchonnumber == 3) {
			GARDENER_MAX = 15;
		}

		while (true) {
			try {
				RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
				if (rc.getHealth() < 300 && robots.length > 0) {
					rc.broadcast(Archon_Distress, 1);
				}
				int numGard = rc.readBroadcast(GARDENER_CHANNEL);

				if ((robots.length == 0 || onlyscoutandarchon(robots)) && numGard < 1 && rc.getRoundNum() < 75) {
					if (!rc.canHireGardener(dir)){
						gardenerbuild(RobotType.GARDENER,dir);
					} else {
						rc.hireGardener(dir);
					}
					rc.broadcast(GARDENER_CHANNEL, numGard + 1);
				} else if ((robots.length == 0 || onlyscoutandarchon(robots)) && numGard < GARDENER_MAX && rc.canHireGardener(dir) && rc.getRoundNum() > 75) {
					if (!rc.canHireGardener(dir)){
						gardenerbuild(RobotType.GARDENER,dir);
					} else {
						rc.hireGardener(dir);
					}
					rc.broadcast(GARDENER_CHANNEL, numGard + 1);
				}

				if (robots.length == 0) {
					rc.broadcast(Archon_Distress, 0);
					wander();
				} else {
					rc.broadcast(OwnArchonPositionX, (int)(rc.getLocation().x));
					rc.broadcast(OwnArchonPositionY, (int)(rc.getLocation().y));
					dodge();
					System.out.println("Distress!!!!!!!!!!");
				}

//                wander();
//                if (rc.getRoundNum() < 500) {
//                    System.out.print(rc.getRoundNum());
//                    if (NeedMore_Gardener(3)) {
//                        Direction dir = randomDir();
//                        if (rc.canHireGardener(dir)) {
//                            rc.hireGardener(dir);
//                        } else {
//                            wander();
//                        }
//                    }
//                } else {
//                    System.out.print(rc.getRoundNum());
//                    if (NeedMore_Gardener(5)){
//
//                        Direction dir = randomDir();
//                        if (rc.canHireGardener(dir)) {
//                            rc.hireGardener(dir);
//                        } else {
//
//                            wander();
//                        }
//                    }
//                }

				canwin();
				end_game();
				Clock.yield();


			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



	public static void runGardener() throws GameActionException {
		int BattleMode = 0;


		while(BattleMode == 0){
			try {
				if (checkIfSurroundinghaveGardener(rc.getLocation())){
					wander();
				}else {
					BattleMode = 1;
				}
				Clock.yield();

			}catch(Exception  e ){
				e.printStackTrace();
			}

		}

		while(BattleMode == 1){
			try {
				MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());
				Direction plantDir = rc.getLocation().directionTo(ml[0]);
				if (rc.getRoundNum() < 10){
					Gardener_buildRobot(RobotType.SCOUT,plantDir);
				}else if (rc.getRoundNum()>= 10 && rc.getRoundNum() < 90 ){
					Gardener_buildRobot(RobotType.SOLDIER,plantDir);
				}

				else{

					BattleMode = 2;
				}
				Clock.yield();

			}catch(Exception  e ){
				e.printStackTrace();
			}
		}


		int counter = 0;
		while (BattleMode == 2){
			try {
				System.out.println("now in Mode 2");

				int treesPlanted = checkNumTreesNearby();
				MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());
				Direction plantDir = rc.getLocation().directionTo(ml[0]);
				int treesNeeded = (int)((CheckHowManySidesCanBuildtrees(plantDir))/2)-1;
				System.out.println(treesNeeded+" "+ treesPlanted);

				if (treesNeeded > 0 ) {
					if (ThereIsEnemyBotNearBy()) {
						Gardener_buildRobot(RobotType.SOLDIER, plantDir);
					}else {
						plantTrees(plantDir);
					}
				}else if(treesNeeded == 0 ){
					BattleMode = 3;
				}else if (treesNeeded  < 0 ){
					System.out.print("Error");
				}


				if (treesPlanted != 0){
					tryToWater();
					tryToShake();
				}

				Clock.yield();


			}catch (Exception e){
				e.printStackTrace();
			}
		}



		/// this is mode three


		while (BattleMode == 3) {
			try {
				System.out.println("now in Mode 3");

				int treesPlanted = checkNumTreesNearby();
				MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());
				Direction plantDir = rc.getLocation().directionTo(ml[0]);
				int treesNeeded = CheckHowManySidesCanBuildtrees(plantDir)-1;
				TreeInfo[] trees = rc.senseNearbyTrees(2, rc.getTeam());
				System.out.println(treesNeeded+" "+ treesPlanted);
				if (treesNeeded == 0) {
					if (ThereIsEnemyBotNearBy()) {
						Gardener_buildRobot(RobotType.SOLDIER, plantDir);
					}else {
						double rand = Math.random();
						if (rand <= 0.4) {
							Gardener_buildRobot(RobotType.LUMBERJACK,plantDir);
						} else if (rand > 0.4 && rand <= 0.9) {
							Gardener_buildRobot(RobotType.SOLDIER,plantDir);
						} else {
							Gardener_buildRobot(RobotType.SCOUT,plantDir);
						}
					}

				}else if (treesNeeded < 0){
					System.out.println("error");
				}
				else if (treesNeeded > 0){
					if (ThereIsEnemyBotNearBy()) {
						Gardener_buildRobot(RobotType.SOLDIER, plantDir);
					}else {
						plantTrees(plantDir);
					}
				}

				if (treesPlanted != 0){
					tryToWater();
					tryToShake();
				}

				Clock.yield();


			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static boolean ishittingtree (RobotInfo b) throws GameActionException {
		MapLocation currentloc = rc.getLocation();
		MapLocation robotloc = b.getLocation();
		Direction dirtorobot = currentloc.directionTo(robotloc);
		MapLocation bulletloc = currentloc.add(dirtorobot, GameConstants.BULLET_SPAWN_OFFSET);
		float disttobullet = currentloc.distanceTo(bulletloc);
		float disttorobot = currentloc.distanceTo(robotloc);
		TreeInfo[] nearbytree = rc.senseNearbyTrees(disttorobot, Team.NEUTRAL);

		if (nearbytree.length == 0) {
			return false;
		} else if (checktrees(dirtorobot,currentloc,nearbytree,disttobullet,disttorobot)){
			return true;
		} else {
			return false;
		}
		}


	public static boolean checktrees (Direction dirtorobot,MapLocation currentloc, TreeInfo[] nearbytree , float disttobullet , float disttorobot) throws GameActionException {
		float dist;

		for (TreeInfo t : nearbytree) {
			for (dist = disttobullet; dist < disttorobot; dist += 0.5) {
				if (rc.isLocationOccupiedByTree(currentloc.add(dirtorobot, dist))) {
					rc.setIndicatorDot(currentloc.add(dirtorobot,dist),225,225,225);
					return true;
				}
			}
		}
		return false;
	}

	public static void runSoldier() throws GameActionException {
		while (true) {
			try {
				dodge();
				MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());
				Direction enemyBase = rc.getLocation().directionTo(ml[0]);
				RobotInfo[] bots = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
				RobotInfo closestrobot = null;
				for (RobotInfo b: bots){
					float shortestdist = 2000000000;
					float currentdist = rc.getLocation().distanceTo(b.getLocation());
					if (currentdist <= shortestdist) {
						closestrobot = b;
					} else {}
				}
				if (rc.readBroadcast(Archon_Distress)!=0){
				    tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX,OwnArchonPositionY)));
                }
				if (ThereIsEnemyBotNearBy()) {
					for (RobotInfo b : bots) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						rc.setIndicatorLine(rc.getLocation(),b.getLocation(),225,225,225);
							if (!ishittingtree(b)) {
								ArchonBroadcast(b);
								GardenerBroadcast(b);
								SoliderBroadcast(b);
								if (b.getTeam() != rc.getTeam()) {
									soliderShoot(b, bots.length);
									enemyArchonDieCheck(b);
								} else {
									if (rc.readBroadcast(ArchonPositionX) != 0 && rc.readBroadcast(ArchonPositionY) != 0 && rc.readBroadcast(ARCHON_DIE) == 0 && rc.readBroadcast(Archon_Distress) == 0) {
										tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX, ArchonPositionY)));
									} else if (rc.readBroadcast(ARCHON_DIE) == 0 && rc.readBroadcast(Archon_Distress) == 0 && rc.readBroadcast(EnemySoliderPositionX) != 0 && rc.readBroadcast(Solider_Die) == 0) {
										tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(EnemySoliderPositionX, EnemySoliderPositionY)));
									} else if (rc.readBroadcast(ARCHON_DIE) == 0 && rc.readBroadcast(Archon_Distress) == 0 && rc.readBroadcast(EnemySoliderPositionX) == 0) {
										tryMove(enemyBase);
									} else if (rc.readBroadcast(Archon_Distress) == 0) {
										wander();
									} else {
										tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX, OwnArchonPositionY)));
									}
								}
							} else {

							}
						}
						}

				 else {
					if (rc.readBroadcast(ArchonPositionX) != 0 && rc.readBroadcast(ArchonPositionY) != 0&&rc.readBroadcast(ARCHON_DIE)==0&&rc.readBroadcast(Archon_Distress)==0) {
						tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX, ArchonPositionY)));
					} else if (rc.readBroadcast(ARCHON_DIE)==0 && rc.readBroadcast(Archon_Distress)==0 && rc.readBroadcast(EnemySoliderPositionX)!=0&&rc.readBroadcast(Solider_Die)==0){
						tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(EnemySoliderPositionX,EnemySoliderPositionY)));
					} else if (rc.readBroadcast(ARCHON_DIE)==0&&rc.readBroadcast(Archon_Distress)==0 && rc.readBroadcast(EnemySoliderPositionX)==0){
						tryMove(enemyBase);
					}else if (rc.readBroadcast(Archon_Distress)==0){
						wander();
					}else{
						tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX,OwnArchonPositionY)));
					}
				}

				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void runScout(int mode) throws GameActionException {
		while (true && mode == 1) {
			try {
				dodge();
				shakeNearbyTrees();
				MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());
				Direction enemyBase = rc.getLocation().directionTo(ml[0]);
				RobotInfo[] bots = rc.senseNearbyRobots();

				if (rc.readBroadcast(Archon_Distress)!=0){
                    tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX,OwnArchonPositionY)));
                }
				if (rc.readBroadcast(GardenerPositionX) == 0 && rc.readBroadcast(GardenerPositionY) == 0) {
                    if (rc.readBroadcast(ArchonPositionX) != 0 && rc.readBroadcast(ArchonPositionY) != 0&&rc.readBroadcast(ARCHON_DIE)==0&&rc.readBroadcast(Archon_Distress)==0) {
                        tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX, ArchonPositionY)));
                    } else if (rc.readBroadcast(ARCHON_DIE)==0&&rc.readBroadcast(Archon_Distress)==0){
                        tryMove(enemyBase);
                    }else if (rc.readBroadcast(Archon_Distress)==0){
                        wander();
                    }else{
                        tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX,OwnArchonPositionY)));
                    }

					for (RobotInfo b : bots) {
						ArchonBroadcast(b);
						GardenerBroadcast(b);
						SoliderBroadcast(b);
					}
				} else {
					if (ThereIsEnemyBotNearBy()) {
						for (RobotInfo b : bots) {
							ArchonBroadcast(b);
							GardenerBroadcast(b);
							SoliderBroadcast(b);
							Direction dir = rc.getLocation().directionTo(b.getLocation());
							if (b.getTeam() != rc.getTeam()) {
								if (rc.canFireSingleShot()) {
									rc.fireSingleShot(dir);
									enemyArchonDieCheck(b);
								}
									if (b.getType() == RobotType.LUMBERJACK || b.getType() == RobotType.SOLDIER) {
										tryMove(rc.getLocation().directionTo(b.getLocation()).opposite());
									}
									if (rc.canMove(dir) && dir == enemyBase && !rc.hasMoved()) {
										rc.move(dir);
									} else {
										if (!rc.hasMoved()) {
											rc.move(dir.rotateLeftDegrees(10));
										}

									}


							}
//                        }
						}
					} else {
						tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(GardenerPositionX, GardenerPositionY)));
					}
				}
				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}


		}
	}

	private static void runLumberjack() throws GameActionException {

		while (true) {
			try {
				dodge();
				MapLocation[] ml = rc.getInitialArchonLocations(rc.getTeam().opponent());
				Direction enemyBase = rc.getLocation().directionTo(ml[0]);
				RobotInfo[] bots = rc.senseNearbyRobots();
                if (rc.readBroadcast(Archon_Distress)!=0){
                    tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX,OwnArchonPositionY)));
                }
				if (ThereIsEnemyBotNearBy()) {
					for (RobotInfo b : bots) {
						ArchonBroadcast(b);
						GardenerBroadcast(b);
						SoliderBroadcast(b);
						float dist = rc.getLocation().distanceTo(b.getLocation());

						if (b.getTeam() != rc.getTeam()) {
							Direction dir = rc.getLocation().directionTo(b.getLocation());
							//strike it
							if (rc.canStrike() && dist <= GameConstants.LUMBERJACK_STRIKE_RADIUS + rc.getType().bodyRadius) {
								rc.strike();
								enemyArchonDieCheck(b);
							} else if (rc.canMove(dir) && !rc.hasMoved()) {
								rc.move(dir);
							}
							break;
						}
					}
				} else {
                    if (rc.readBroadcast(ArchonPositionX) != 0 && rc.readBroadcast(ArchonPositionY) != 0&&rc.readBroadcast(ARCHON_DIE)==0&&rc.readBroadcast(Archon_Distress)==0) {
                        tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(ArchonPositionX, ArchonPositionY)));
                    } else if (rc.readBroadcast(ARCHON_DIE)==0&&rc.readBroadcast(Archon_Distress)==0){
                        tryMove(enemyBase);
                    }else if (rc.readBroadcast(Archon_Distress)==0){
                        wander();
                    }else{
                        tryMove(rc.getLocation().directionTo(giveMapLocationOfArchon(OwnArchonPositionX,OwnArchonPositionY)));
                    }
					TreeInfo[] tree = rc.senseNearbyTrees();
					for (TreeInfo t : tree) {
						if (rc.canChop(t.getID()) && t.getTeam() != rc.getTeam()) {
							rc.chop(t.getID());
							rc.shake(t.getID());
							break;
						}
					}
				}
				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}