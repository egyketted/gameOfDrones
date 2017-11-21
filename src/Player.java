import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/
class Player {

    private static int PLAYER_ID = 0;
    private static int PLAYER_NUMBER = 0;
    private static int DRONE_NUMBER = 0;
    private static int ZONE_NUMBER = 0;

    private static final int MAX_X = 4000;
    private static final int MAX_Y = 1800;
    private static final int MAX_DRONE_TRAVEL_DISTANCE = 100;

    private static Integer ROUNDS_TO_CALC_WITH = 0;

    private static long MAX_CALC_TIME = 0;

    private static final int OCCUPATION_RADIUS = 100;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        PLAYER_NUMBER = in.nextInt(); // number of players in the game (2 to 4
                                      // players)
        PLAYER_ID = in.nextInt(); // ID of your player (0, 1, 2, or 3)
        DRONE_NUMBER = in.nextInt(); // number of drones in each team (3 to 11)
        ZONE_NUMBER = in.nextInt(); // number of zones on the map (4 to 8)
        Zones zones = new Zones();
        Drones drones = new Drones(zones);
        Ai ai = new OffensiveAi();
        double worldDiagonal = new Coordinate(0, 0).distance(new Coordinate(MAX_X, MAX_Y));
        double maxRoundsToCross = worldDiagonal / MAX_DRONE_TRAVEL_DISTANCE;
        ROUNDS_TO_CALC_WITH = (int) maxRoundsToCross;
        if ((double) ROUNDS_TO_CALC_WITH < maxRoundsToCross) {
            ROUNDS_TO_CALC_WITH++;
        }

        for (int i = 0; i < ZONE_NUMBER; i++) {
            zones.addZone(new Zone(new Coordinate(in.nextInt(), in.nextInt())).setId(i)); // corresponds
                                                                                          // to
                                                                                          // the
                                                                                          // position
                                                                                          // of
                                                                                          // the
                                                                                          // center
                                                                                          // of
                                                                                          // a
                                                                                          // zone.
                                                                                          // A
                                                                                          // zone
                                                                                          // is
                                                                                          // a
                                                                                          // circle
                                                                                          // with
                                                                                          // a
                                                                                          // radius
                                                                                          // of
                                                                                          // 100
                                                                                          // units.
        }

        for (int i = 0; i < PLAYER_NUMBER; i++) {
            for (int j = 0; j < DRONE_NUMBER; j++) {
                drones.addDrone(i, new Drone(i).setId(j));
                System.err.println("set drone id to: " + j);
            }
        }

        // game loop
        while (true) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < ZONE_NUMBER; i++) {
                zones.getZone(i).setOwner(in.nextInt()); // ID of the team
                                                         // controlling the zone
                                                         // (0, 1, 2, or 3) or
                                                         // -1 if it is not
                                                         // controlled. The
                                                         // zones are given in
                                                         // the same order as in
                                                         // the initialization.
            }
            for (int i = 0; i < PLAYER_NUMBER; i++) {
                for (int j = 0; j < DRONE_NUMBER; j++) {
                    drones.getDrone(i, j).setCoordinates(in.nextInt(), in.nextInt()); // The
                                                                                      // first
                                                                                      // D
                                                                                      // lines
                                                                                      // contain
                                                                                      // the
                                                                                      // coordinates
                                                                                      // of
                                                                                      // drones
                                                                                      // of
                                                                                      // a
                                                                                      // player
                                                                                      // with
                                                                                      // the
                                                                                      // ID
                                                                                      // 0,
                                                                                      // the
                                                                                      // following
                                                                                      // D
                                                                                      // lines
                                                                                      // those
                                                                                      // of
                                                                                      // the
                                                                                      // drones
                                                                                      // of
                                                                                      // player
                                                                                      // 1,
                                                                                      // and
                                                                                      // thus
                                                                                      // it
                                                                                      // continues
                                                                                      // until
                                                                                      // the
                                                                                      // last
                                                                                      // player.
                }
            }

            drones.evaluate();
            zones.evaluate(drones);
            ai = ai.evaluate(zones, drones);

            long duration = System.currentTimeMillis() - start;
            if (duration > MAX_CALC_TIME) {
                MAX_CALC_TIME = duration;
            }
            System.err.println(
                    String.format("Calculations took %d ms to compleate highest is %d", duration, MAX_CALC_TIME));
            for (Drone drone : drones.getDrones(PLAYER_ID)) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                System.out.println(drone.getTarget()); // output a destination
                                                       // point to be reached by
                                                       // one of your drones.
                                                       // The first line
                                                       // corresponds to the
                                                       // first of your drones
                                                       // that you were provided
                                                       // as input, the next to
                                                       // the second, etc.
            }
        }
    }

    public static int getPlayerId() {
        return PLAYER_ID;
    }

    public static int getPlayerNumber() {
        return PLAYER_NUMBER;
    }

    public static int getDroneNum() {
        return DRONE_NUMBER;
    }

    public static int getZoneNum() {
        return ZONE_NUMBER;
    }

    public static int getOccupationRadius() {
        return OCCUPATION_RADIUS;
    }

    public static Integer getRoundsToCalcWith() {
        return ROUNDS_TO_CALC_WITH;
    }

    public static int getMaxDroneTravelDistance() {
        return MAX_DRONE_TRAVEL_DISTANCE;
    }

    private static interface Ai {

        public Ai evaluate(Zones zones, Drones drones);

    }

    private static class AiMetadata {

        private Drone drone;
        private SortingTree<ZoneRequest> sortingTree;
        private int sumNeeded;

        public AiMetadata(Drone drone) {
            this.drone = drone;
            sortingTree = new SortingTree<>();
        }

        public void addRequest(Zone zone, OccupationType type, int sumNeeded) {
            sortingTree.addValue(new ZoneRequest(zone, type, sumNeeded, drone.getZoneDistance(zone)));
        }

        public Zone getTargetZone() {
            ZoneRequest first = sortingTree.popFirst();
            if (first == null) {
                sumNeeded = 0;
                return null;
            }
            sumNeeded = first.getSumNeeded();
            return first.getZone();
        }

        public Zone getTargetZoneForSorting() {
            return sortingTree.getFirst().getValue().getZone();
        }

        public int getDronesNeeded() {
            return sumNeeded;
        }

        public void recalculate() {
            for (ZoneRequest request : sortingTree) {
                if (!request.getZone().needsMoreDrones()) {
                    sortingTree.remove(request);
                }
            }
            sortingTree.deepRecalc();
        }

        @Override
        public String toString() {
            return sortingTree.toString();
        }
    }

    public enum OccupationType {
        CONQUER, DEFEND
    }

    private static class OffensiveAi implements Ai {

        @Override
        public Ai evaluate(Zones zones, Drones drones) {
            for (Zone zone : zones) {
                OccupationType occupationType;
                if (zone.getOwner() == Player.getPlayerId()) {
                    occupationType = OccupationType.DEFEND;
                } else {
                    occupationType = OccupationType.CONQUER;
                }
                for (Drone drone : zones.getZoneDroneMap().get(zone)) {
                    int dronesNeeded = zones.getEffectiveEnemyNumberInZone(zone, drone);
                    if (zone.getOwner() == -1 && drone.getOwner() == Player.getPlayerId()
                            && willConquerItBeforeEnemys(drone, zones, zone)) {
                        dronesNeeded = 1;
                        occupationType = OccupationType.DEFEND;
                    }
                    if (occupationType == OccupationType.CONQUER) {
                        dronesNeeded++;
                    }
                    // if (closestMoveingEnemyIsCloser(zone, drones, drone) &&
                    // dronesNeeded > 0) {
                    // dronesNeeded++;
                    // }
                    System.err.println(String.format("\tZone%d needs %d drones.", zone.getId(), dronesNeeded));
                    System.err.println(String.format("\tThere is %d enemys in Zone%d occupation type is %s",
                            zones.getEffectiveEnemyNumberInZone(zone, drone), zone.getId(), occupationType.name()));
                    if (dronesNeeded == 0) {
                        continue;
                    }
                    AiMetadata metaData = drone.getMetaData();
                    if (metaData == null) {
                        metaData = new AiMetadata(drone);
                    }
                    metaData.addRequest(zone, occupationType, dronesNeeded);
                    drone.setMetaData(metaData);
                }
            }
            drones.rearrangeOwnDrones();
            for (Drone drone : drones) {
                System.err.println(drone);
            }
            SortingTree<Drone> copyOfDrones = drones.copyOwn();
            for (Drone drone = copyOfDrones.popFirst(); drone != null; drone = copyOfDrones.popFirst()) {
                AiMetadata droneMetadata = drone.getMetaData();
                if (droneMetadata == null) {
                    continue;
                }
                Zone targetZone = droneMetadata.getTargetZone();
                while (targetZone != null && !targetZone.needsMoreDrones()) {
                    System.err.println(String.format("\tGetting new zone for Drone%d", drone.getId()));
                    targetZone = droneMetadata.getTargetZone();
                    if (targetZone != null) {
                        System.err.println(String.format("\tGot Zone%d, needs more:%s", targetZone.getId(),
                                String.valueOf(targetZone.needsMoreDrones())));
                    }
                }
                if (targetZone == null) {
                    continue;
                }
                System.err.println(String.format("Drone%d will go to Zone%d", drone.getId(), targetZone.getId()));
                Coordinate target = new Coordinate(UtilityMethods.calculateBestPosition(targetZone, drone));
                drone.setTarget(target);
                if (drone.isFree()) {
                    drone.setBusyFlag();
                }
                targetZone.sentDrone(drone);
                copyOfDrones.deepRecalc();
                for (Drone droneOut : copyOfDrones) {
                    System.err.println(droneOut);
                }
            }
            TreeSet<Zone> zonesNeedingMore = new TreeSet<>(new Comparator<Zone>() {

                @Override
                public int compare(Zone z1, Zone z2) {
                    int compare = Integer.compare(z1.getDronesNeededForSorting(), z2.getDronesNeededForSorting());
                    if (compare == 0) {
                        return Integer.compare(z1.getId(), z2.getId());
                    }
                    return compare;
                }
            });
            int dronesInTheRightPlaceNum = 0;
            int droneNum = Player.getDroneNum();
            for (Zone zone : zones) {
                if (zone.needsMoreDrones() && zone.getDronesOnRoute().size() > 0) {
                    zonesNeedingMore.add(zone);
                } else if (!zone.needsMoreDrones()) {
                    dronesInTheRightPlaceNum += zone.getDronesOnRoute().size();
                }
            }
            for (Zone zone : zonesNeedingMore) {
                for (Zone lessImportantZone : zonesNeedingMore.descendingSet()) {
                    if (zone == lessImportantZone || dronesInTheRightPlaceNum >= droneNum) {
                        break;
                    }
                    List<Drone> dronesRemoved = new ArrayList<>();
                    for (Drone drone : lessImportantZone.getDronesOnRoute()) {
                        drone.setTarget(UtilityMethods.calculateBestPosition(zone, drone));
                        dronesRemoved.add(drone);
                        dronesInTheRightPlaceNum++;
                        System.err.println(String.format("\tSent Drone%d from Zone%d to Zone%d", drone.getId(),
                                lessImportantZone.getId(), zone.getId()));
                        if (!zone.needsMoreDrones() || dronesInTheRightPlaceNum >= droneNum) {
                            break;
                        }
                    }
                    lessImportantZone.getDronesOnRoute().removeAll(dronesRemoved);
                    if (!zone.needsMoreDrones() || dronesInTheRightPlaceNum >= droneNum) {
                        break;
                    }
                }
                if (dronesInTheRightPlaceNum >= droneNum) {
                    break;
                }
            }
            for (Drone drone : drones) {
                if (drone.isFree()) {
                    if (drones.getClosestEnemyDrone(drone) != null) {
                        Drone closestEnemy = drones.getClosestEnemyDrone(drone);
                        drone.setTarget(closestEnemy.getCoordinates());
                        System.err.println(String.format("Drone%d will foloww Player%d's Drone%d", drone.getId(),
                                closestEnemy.getOwner(), closestEnemy.getId()));
                    } else {
                        System.err.println(String.format("Drone%d remains unoccupied", drone.getId()));
                    }
                }
            }
            return this;
            // TODO change methodology, just count which zone needs drones and
            // how
            // much, then dispach the drones that has the zone as their closest,
            // if there is not enough, do nothing free drones after the process
            // will
            // be sent to farther targets

            // TODO new idea, a new data structure should be implemented in
            // which
            // zones are stored in a treeset based on the combined travel
            // distance
            // of the closest drones needed by the zone. The treeset should be
            // rearranged and everything recalculated after a zone is pulled,
            // thus
            // removed from the structure
            // should have a hasNext() method since we have to iterate through
            // all
            // the zones. Also target for drones should be recalculated every
            // round
        }

        private boolean willConquerItBeforeEnemys(Drone drone, Zones zones, Zone zone) {
            return zones.getEnemyNumThatGetsThereFaster(drone.getRoundsToArrive(zone), zone) == 0;
        }

        private boolean closestMoveingEnemyIsCloser(Zone zone, Drones drones, Drone drone) {
            Drone closestEnemy = drones.getClosestEnemyMap().get(drone);
            return (closestEnemy != null && closestEnemy.getCoordinates()
                    .distance(UtilityMethods.calculateBestPosition(zone, closestEnemy)) < drone.getCoordinates()
                            .distance(UtilityMethods.calculateBestPosition(zone, drone)));
        }

    }

    private static class Coordinate {

        private int x;
        private int y;

        public Coordinate() {

        }

        public Coordinate(Coordinate coordinate) {
            this.x = coordinate.x;
            this.y = coordinate.y;
        }

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("%d %d", x, y);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Coordinate)) {
                return false;
            }
            Coordinate other = (Coordinate) obj;
            return other.x == x && other.y == y;
        }

        public double distance(Coordinate coordinate) {
            return Math.sqrt(Math.pow(coordinate.x - x, 2) + Math.pow(coordinate.y - y, 2));
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int hashCode() {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            short sx = (short) x;
            short sy = (short) y;
            buffer.putShort(sx);
            buffer.putShort(sy);
            return buffer.getInt(0);
        }

    }

    private static interface IRecalculateable<T> extends Comparable<T> {

        public void recalculate();
    }

    private static class Line {

        int nx = 0;
        int ny = 0;
        int constPart = 0;

        public Line(int nx, int ny, int cx, int cy) {
            this.nx = nx;
            this.ny = ny;
            this.constPart = nx * cx + ny * cy;
        }

        public boolean includes(Coordinate coordinate) {
            if (nx == 0 && ny == 0 && constPart == 0) {
                return false;
            }
            return nx * coordinate.getX() + ny * coordinate.getY() == constPart;
            // isInsideArea(nx * coordinate.getX() + ny * coordinate.getY(),
            // constPart, nx * 0.5 + ny * 0.5);
        }

        public boolean isInsideArea(int leftSide, int rightSide, double areaWidth) {
            return leftSide >= rightSide - areaWidth && leftSide < rightSide + areaWidth;
        }
    }

    private static class SortingTree<T extends IRecalculateable<T>> implements Iterable<T> {

        private SortingTreeNode<T> root;

        private List<T> sortedValues;

        public void addValue(T value) {
            SortingTreeNode<T> node = new SortingTreeNode<>(value);
            if (root == null) {
                root = node;
                // System.err.println(String.format("Zone%d will be added as
                // root with value of %.2f",
                // node.getZone().getId(), node.getValue()));
                return;
            }
            insertNode(root, node);
        }

        public boolean hasNext() {
            return root != null;
        }

        public T popFirst() {
            // System.err.println("\tThe tree before popping:");
            // System.err.println("\t\t" + String.valueOf(root));
            SortingTreeNode<T> first = getFirst(root);
            if (first == null) {
                return null;
            }
            // System.err.println(String.format("Zone%d got as first",
            // first.getZoneRequest().getZone().getId()));
            if (!first.isRoot()) {
                // System.err.println("\tFirst is not root");
                if (first.hasRight()) {
                    // System.err.println(String.format("\t\tFirst has right
                    // Zone%d's left set to Zone%d",
                    // first.getParent().getZoneRequest().getZone().getId(),
                    // first.getRight().getZoneRequest().getZone().getId()));
                    first.getParent().setLeft(first.getRight());
                    first.getRight().setParent(first.getParent());
                } else {
                    // System.err.println(String.format("\t\tFirst has no right
                    // Zone%d's left set to null",
                    // first.getParent().getZoneRequest().getZone().getId()));
                    first.getParent().setLeft(null);
                }
            } else {
                // System.err.println("\tFirst is root");
                if (root.hasRight()) {
                    // System.err.println(String.format("\t\tSetting Zone%d as
                    // root because Zone%d is poped",
                    // root.getRight().getZoneRequest().getZone().getId(),
                    // root.getZoneRequest().getZone().getId()));
                    root = root.getRight();
                    root.setParent(null);
                } else {
                    // System.err.println("\t\tSetting root to null");
                    root = null;
                }
            }
            return first.getValue();
        }

        public SortingTreeNode<T> getFirst() {
            return getFirst(root);
        }

        private SortingTreeNode<T> getFirst(SortingTreeNode<T> node) {
            if (node == null) {
                return null;
            }
            if (node.hasLeft()) {
                return getFirst(node.getLeft());
            }
            return node;
        }

        public void addAll(Iterable<T> values) {
            for (T value : values) {
                addValue(value);
            }
        }

        private void insertNode(SortingTreeNode<T> parent, SortingTreeNode<T> child) {
            if (parent.compareTo(child) >= 0) {
                if (parent.hasLeft()) {
                    insertNode(parent.getLeft(), child);
                } else {
                    parent.setLeft(child);
                    child.setParent(parent);
                    // System.err.println(String.format("Zone%d will be added
                    // with value of %.2f %nleft of Zone%d value %.2f",
                    // child.getZone().getId(), child.getValue(),
                    // parent.getZone().getId(), parent.getValue()));
                }
            } else {
                if (parent.hasRight()) {
                    insertNode(parent.getRight(), child);
                } else {
                    parent.setRight(child);
                    child.setParent(parent);
                    // System.err.println(String.format("Zone%d will be added
                    // with value of %.2f %nright of Zone%d value %.2f",
                    // child.getZone().getId(), child.getValue(),
                    // parent.getZone().getId(), parent.getValue()));
                }
            }
        }

        @Override
        public Iterator<T> iterator() {
            sortedValues = new ArrayList<>();
            // System.err.println(String.format("Calling addZonesSorted with
            // root Zone%d",
            // root.getZone().getId()));
            addValuesSorted(sortedValues, root);
            // System.err.println(String.format("Returning iterator for %d long
            // list of zones",
            // sortedZones.size()));
            return sortedValues.iterator();
        }

        private void addValuesSorted(List<T> sortedList, SortingTreeNode<T> node) {
            if (node == null) {
                return;
            }
            if (node.hasLeft()) {
                // System.err.println(String.format("\tZone%d has left
                // continuing with Zone%d",
                // node.getZone().getId(), node.getLeft().getZone().getId()));
                addValuesSorted(sortedList, node.getLeft());
            }
            // System.err.println(String.format("\t\tAdding Zone%d to the list",
            // node.getZone().getId()));
            sortedList.add(node.getValue());
            if (node.hasRight()) {
                // System.err.println(String.format("\tZone%d has right
                // continuing with Zone%d",
                // node.getZone().getId(), node.getRight().getZone().getId()));
                addValuesSorted(sortedList, node.getRight());
            }
        }

        public void deepRecalc() {
            sortedValues = new ArrayList<>();
            addValuesSorted(sortedValues, root);
            destroyTree(root);
            root = null;
            for (T value : sortedValues) {
                value.recalculate();
            }
            addAll(sortedValues);
        }

        private void destroyTree(SortingTreeNode<T> node) {
            if (node == null) {
                return;
            }
            if (node.hasLeft()) {
                destroyTree(node.getLeft());
                node.setLeft(null);
            }
            if (node.hasRight()) {
                destroyTree(node.getRight());
                node.setRight(null);
            }
            node.setParent(null);
        }

        public void remove(T value) {
            remove(root, value);
        }

        private void remove(SortingTreeNode<T> node, T value) {
            int compare = node.getValue().compareTo(value);
            if (compare < 0 && node.hasLeft()) {
                remove(node.getLeft(), value);
            } else if (compare > 0 && node.hasRight()) {
                remove(node.getRight(), value);
            } else if (compare == 0) {
                List<T> list = new ArrayList<>();
                if (node.hasLeft()) {
                    addValuesSorted(list, node.getLeft());
                    node.getLeft().setParent(null);
                    node.setLeft(null);
                }
                if (node.hasRight()) {
                    addValuesSorted(list, node.getRight());
                    node.getRight().setParent(null);
                    node.setRight(null);
                }
                if (!node.isRoot()) {
                    if (node.getParent().getLeft() == node) {
                        node.getParent().setLeft(null);
                    } else {
                        node.getParent().setRight(null);
                    }
                    node.setParent(null);
                }
                addAll(list);
            }

        }

        @Override
        public String toString() {
            return String.valueOf(root);
        }
    }

    private static class SortingTreeNode<T extends Comparable<T>> implements Comparable<SortingTreeNode<T>> {

        private SortingTreeNode<T> left;
        private SortingTreeNode<T> right;

        private SortingTreeNode<T> parent;

        private T value;

        public SortingTreeNode(T value) {
            this.value = value;
        }

        public boolean hasLeft() {
            return left != null;
        }

        public boolean hasRight() {
            return right != null;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public SortingTreeNode<T> getLeft() {
            return left;
        }

        public void setLeft(SortingTreeNode<T> left) {
            this.left = left;
        }

        public SortingTreeNode<T> getRight() {
            return right;
        }

        public void setRight(SortingTreeNode<T> right) {
            this.right = right;
        }

        public SortingTreeNode<T> getParent() {
            return parent;
        }

        public void setParent(SortingTreeNode<T> parent) {
            this.parent = parent;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public int compareTo(SortingTreeNode<T> node) {
            return value.compareTo(node.value);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(value)).append(":[").append(String.valueOf(left)).append(",")
                    .append(String.valueOf(right)).append("]");
            return sb.toString();
        }
    }

    private static class ZoneRequest implements IRecalculateable<ZoneRequest> {

        private Zone zone;
        private OccupationType type;
        private int sumNeeded;
        private double distance;

        public ZoneRequest(Zone zone, OccupationType type, int sumNeeded, double distance) {
            this.zone = zone;
            this.type = type;
            this.sumNeeded = sumNeeded;
            this.distance = distance;
        }

        public Zone getZone() {
            return zone;
        }

        public void setZone(Zone zone) {
            this.zone = zone;
        }

        public OccupationType getType() {
            return type;
        }

        public void setType(OccupationType type) {
            this.type = type;
        }

        public int getSumNeeded() {
            return sumNeeded;
        }

        public void setSumNeeded(int sumNeeded) {
            this.sumNeeded = sumNeeded;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        @Override
        public int compareTo(ZoneRequest request) {
            if (!zone.needsMoreDrones() && !request.getZone().needsMoreDrones()) {
                return Integer.compare(zone.getId(), request.getZone().getId());
            } else if (zone.needsMoreDrones() && !request.getZone().needsMoreDrones()) {
                return -1;
            } else if (!zone.needsMoreDrones() && request.getZone().needsMoreDrones()) {
                return 1;
            }
            int compare = Integer.compare(sumNeeded, request.sumNeeded);
            if (compare == 0) {
                return Double.compare(getDistance(), request.getDistance());
            }
            return compare;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Zone").append(zone.getId());
            return sb.toString();
        }

        @Override
        public void recalculate() {
            // ZoneRequest does not have any parameters that needs to be
            // recalculated yet.
        }

    }

    private static class Drone implements IRecalculateable<Drone> {

        private Coordinate prevCoordinates;
        private Coordinate coordinates;
        private Coordinate target;
        private Coordinate directionVector;;
        private Zone possibleTarget;
        private int owner;
        private int id;
        private boolean isBusy = false;

        private Map<Zone, Double> zoneDistanceMap;

        private AiMetadata metaData;

        public Drone(int owner) {
            this.owner = owner;
        }

        public boolean isMoveing() {
            if (prevCoordinates == null) {
                return false;
            }
            return !coordinates.equals(prevCoordinates);
        }

        public Coordinate getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinate coordinates) {
            setCoordinates(coordinates.getX(), coordinates.getY());
        }

        public void setCoordinates(int x, int y) {
            if (this.coordinates != null) {
                this.prevCoordinates = new Coordinate(coordinates.getX(), coordinates.getY());
            }
            this.coordinates = new Coordinate(x, y);
            if (prevCoordinates == null) {
                this.directionVector = new Coordinate(0, 0);
            } else {
                this.directionVector = new Coordinate(coordinates.getX() - prevCoordinates.getX(),
                        coordinates.getY() - prevCoordinates.getY());
            }
            setTarget(x, y);
            isBusy = false;
            zoneDistanceMap = new HashMap<>();
        }

        public Coordinate getTarget() {
            return target;
        }

        public void setTarget(Coordinate target) {
            this.target = target;
            isBusy = false;
        }

        public void setTarget(int x, int y) {
            this.target = new Coordinate(x, y);
            isBusy = false;
        }

        public boolean isFree() {
            return !isBusy && coordinates.equals(target);
        }

        public int getOwner() {
            return owner;
        }

        public void setOwner(int owner) {
            this.owner = owner;
        }

        public void setBusyFlag() {
            isBusy = true;
        }

        public int getId() {
            return id;
        }

        public Drone setId(int id) {
            this.id = id;
            return this;
        }

        public Zone getPossibleTarget() {
            return possibleTarget;
        }

        public void setPossibleTarget(Zone possibleTarget) {
            this.possibleTarget = possibleTarget;
        }

        public Coordinate getPrevCoordinates() {
            return prevCoordinates;
        }

        public void setPrevCoordinates(Coordinate prevCoordinates) {
            this.prevCoordinates = prevCoordinates;
        }

        public double getZoneDistance(Zone zone) {
            if (zoneDistanceMap.get(zone) == null) {
                Coordinate zoneEntryPosition = UtilityMethods.calculateBestPosition(zone, this);
                double distance = coordinates.distance(zoneEntryPosition);
                if (zone.isInsideArea(coordinates)) {
                    distance = 0;
                }
                zoneDistanceMap.put(zone, distance);
            }
            return zoneDistanceMap.get(zone);
        }

        public AiMetadata getMetaData() {
            return metaData;
        }

        public void setMetaData(AiMetadata metaData) {
            this.metaData = metaData;
        }

        @Override
        public int compareTo(Drone drone) {
            if (metaData == null && drone.getMetaData() == null) {
                // System.err.println(String.format("\t\tboth metadatas are null
                // comparing Drone%d and Drone%d based on id", getId(),
                // drone.getId()));
                return Integer.compare(getId(), drone.getId());
            } else if (metaData != null && drone.getMetaData() == null) {
                // System.err.println(String.format("\t\tDrone%d's metadata is
                // not null Drone%d's metadat is null returning -1", getId(),
                // drone.getId()));
                return -1;
            } else if (metaData == null && drone.getMetaData() != null) {
                // System.err.println(String.format("\t\tDrone%d's metadata is
                // null Drone%d's metadat is not null returning 1", getId(),
                // drone.getId()));
                return 1;
            }
            int compare = Double.compare(getZoneDistance(metaData.getTargetZoneForSorting()),
                    drone.getZoneDistance(drone.getMetaData().getTargetZoneForSorting()));
            // System.err.println(String.format("\t\tcomparing Drone%d and
            // Drone%d based on their distance from target resulted in %d",
            // getId(), drone.getId(), compare));
            if (compare == 0) {
                return Integer.compare(getId(), drone.getId());
            }
            return compare;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Drone").append(id).append(":[").append("prefTarget:")
                    .append(String.valueOf(metaData == null ? null : metaData.getTargetZoneForSorting()))
                    .append(", distance:")
                    .append(metaData == null ? null
                            : String.format("%.2f", zoneDistanceMap.get(metaData.getTargetZoneForSorting())))
                    .append("\n\tZoneTree: ").append(metaData).append("]");
            return sb.toString();
        }

        @Override
        public void recalculate() {
            if (metaData != null) {
                metaData.recalculate();
            }
        }

        public Coordinate getEstimatedPositionForRound(int round) {
            return new Coordinate(coordinates.getX() + (round * directionVector.getX()),
                    coordinates.getY() + (round * directionVector.getY()));
        }

        public int getRoundsToArrive(Zone zone) {
            double distance = coordinates.distance(UtilityMethods.calculateBestPosition(zone, this));
            double roundsToArrive = distance / Player.getMaxDroneTravelDistance();
            int roundedRoundsToArrive = (int) roundsToArrive;
            return roundedRoundsToArrive < roundsToArrive ? roundedRoundsToArrive + 1 : roundedRoundsToArrive;
        }

    }

    private static class Drones implements Iterable<Drone> {

        private Map<Integer, List<Drone>> drones;
        private Map<Drone, Drone> closestEnemyMap;
        private Zones zones;
        private SortingTree<Drone> ownDrones;

        public Drones(Zones zones) {
            drones = new HashMap<>();
            ownDrones = new SortingTree<>();
            this.zones = zones;
            closestEnemyMap = new HashMap<>();
        }

        @Override
        public Iterator<Drone> iterator() {
            return ownDrones.iterator();
        }

        public void addDrone(Drone drone) {
            addDrone(Player.getPlayerId(), drone);
        }

        public void addDrone(Integer playerId, Drone drone) {
            if (!drones.containsKey(playerId)) {
                drones.put(playerId, new ArrayList<>());
            }
            drones.get(playerId).add(drone);
            if (playerId == Player.getPlayerId()) {
                ownDrones.addValue(drone);
            }
        }

        public void rearrangeOwnDrones() {
            // System.err.println("Rearranging own drones!");
            SortingTree<Drone> newOwnDrones = new SortingTree<>();
            newOwnDrones.addAll(ownDrones);
            ownDrones = newOwnDrones;
        }

        public Drone getDrone(int playerId, int droneNum) {
            return drones.get(playerId).get(droneNum);
        }

        public int getFreeDroneNum() {
            int count = 0;
            for (Drone drone : this) {
                if (drone.isFree()) {
                    count++;
                }
            }
            return count;
        }

        public List<Drone> getDrones(int playerId) {
            return drones.get(playerId);
        }

        public void evaluate() {
            for (int i = 0; i < Player.getPlayerNumber(); i++) {
                for (Drone drone : drones.get(i)) {
                    drone.setPossibleTarget(null);
                    drone.setMetaData(null);
                }
            }
            for (Drone drone : this) {
                double minDistance = Double.MAX_VALUE;
                Drone closest = null;
                for (int i = 0; i < Player.getPlayerNumber(); i++) {
                    if (i == Player.getPlayerId()) {
                        continue;
                    }
                    for (Drone enemyDrone : drones.get(i)) {
                        double distance = drone.getCoordinates().distance(enemyDrone.getCoordinates());
                        if (distance < minDistance && enemyDrone.isMoveing()) {
                            minDistance = distance;
                            closest = enemyDrone;
                        }

                        // drone possible target calculation
                        if (enemyDrone.getPrevCoordinates() != null) {
                            int nX = -1 * (enemyDrone.getCoordinates().getY() - enemyDrone.getPrevCoordinates().getY());
                            int nY = enemyDrone.getCoordinates().getX() - enemyDrone.getPrevCoordinates().getX();
                            Line possibleRoute = new Line(nX, nY, enemyDrone.getCoordinates().getX(),
                                    enemyDrone.getCoordinates().getY());
                            Zone possibleTarget = null;
                            outer: for (Zone zone : zones) {
                                for (Coordinate coordinate : zone.getBorder()) {
                                    if (possibleRoute.includes(coordinate)) {
                                        possibleTarget = zone;
                                        break outer;
                                    }
                                }
                            }
                            enemyDrone.setPossibleTarget(possibleTarget);
                        }
                    }
                }
                closestEnemyMap.put(drone, closest);
            }
        }

        public Drone getClosestEnemyDrone(Drone own) {
            return closestEnemyMap.get(own);
        }

        public Map<Drone, Drone> getClosestEnemyMap() {
            return closestEnemyMap;
        }

        public SortingTree<Drone> copyOwn() {
            SortingTree<Drone> copy = new SortingTree<>();
            copy.addAll(ownDrones);
            return copy;
        }
    }

    private static class UtilityMethods {

        public static Coordinate calculateBestPosition(Zone zone, Drone drone) {
            // System.err.println(String.format("\tCalculating best approach for
            // Drone%d and Zone%d", drone.getId(), zone.getId()));
            double minDistance = Double.MAX_VALUE;
            Coordinate minCoordinate = null;
            for (Coordinate coordinate : zone.getBorder()) {
                double distance = coordinate.distance(drone.getCoordinates());
                if (distance < minDistance) {
                    minDistance = distance;
                    minCoordinate = coordinate;
                }
            }
            return minCoordinate;
        }

    }

    private static class Zone {

        private Coordinate coordinates;
        private int owner;
        private int id;
        private int dronesNeeded;
        private List<Drone> dronesOnRoute;

        private HashSet<Coordinate> area;
        private HashSet<Coordinate> border;

        public Zone(Coordinate coordinates) {
            this.setCoordinates(coordinates);
            area = new HashSet<>();
            border = new HashSet<>();
            for (int x = coordinates.getX() - Player.getOccupationRadius(); x < coordinates.getX()
                    + Player.getOccupationRadius(); x++) {
                for (int y = coordinates.getY() - Player.getOccupationRadius(); y < coordinates.getY()
                        + Player.getOccupationRadius(); y++) {
                    Coordinate coordinate = new Coordinate(x, y);
                    double distance = coordinate.distance(coordinates);
                    if (distance <= Player.getOccupationRadius()) {
                        area.add(coordinate);
                    }
                    if (distance == Player.getOccupationRadius()) {
                        border.add(coordinate);
                    }
                }
            }
            area.add(coordinates);
        }

        public boolean isInsideExtendedArea(Coordinate coordinate) {
            return coordinate.distance(coordinates) <= Player.getOccupationRadius() * 2;
        }

        public HashSet<Coordinate> getBorder() {
            return border;
        }

        public boolean isInsideArea(Coordinate coordinate) {
            return area.contains(coordinate);
        }

        public int getOwner() {
            return owner;
        }

        public void setOwner(int owner) {
            this.owner = owner;
        }

        public Coordinate getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinate coordinates) {
            this.coordinates = coordinates;
        }

        public int getId() {
            return id;
        }

        public Zone setId(int id) {
            this.id = id;
            return this;
        }

        public int getDronesNeededForSorting() {
            if (dronesOnRoute.size() == 0) {
                return Integer.MAX_VALUE;
            }
            return dronesOnRoute.get(dronesOnRoute.size() - 1).getMetaData().getDronesNeeded();
        }

        public void setDronesNeeded(int dronesNeeded) {
            this.dronesNeeded = dronesNeeded;
            dronesOnRoute = new ArrayList<Drone>();
        }

        public void sentDrone(Drone drone) {
            dronesOnRoute.add(drone);
        }

        public boolean needsMoreDrones() {
            if (dronesOnRoute.size() == 0) {
                return true;
            }
            // System.err.println(String.format("\t\tZone%d needs %d drones
            // according to Drone%d's meatadata", id,
            // dronesOnRoute.get(dronesOnRoute.size() - 1)
            // .getMetaData().getDronesNeeded(),
            // dronesOnRoute.get(dronesOnRoute.size() - 1).getId()));
            return dronesOnRoute.get(dronesOnRoute.size() - 1).getMetaData().getDronesNeeded() > dronesOnRoute.size();
        }

        public List<Drone> getDronesOnRoute() {
            return dronesOnRoute;
        }

        @Override
        public String toString() {
            return "Zone" + id;
        }

}

    private static class Zones implements Iterable<Zone> {

        private List<Zone> zones;

        private Map<Zone, TreeSet<Drone>> zoneDroneMap;
        private Map<Integer, Map<Integer, Map<Integer, HashSet<Drone>>>> dronesInsideZone;

        public Zones() {
            zones = new ArrayList<>();
            zoneDroneMap = new HashMap<>();
            dronesInsideZone = new HashMap<>();
        }

        public void addZone(Zone zone) {
            zones.add(zone);
            for (Integer roundToArrive = 0; roundToArrive < Player.getRoundsToCalcWith(); roundToArrive++) {
                Map<Integer, HashSet<Drone>> map = new HashMap<>();
                for (int i = 0; i < Player.getPlayerNumber(); i++) {
                    map.put(i, new HashSet<>());
                }
                if (dronesInsideZone.get(zone.getId()) == null) {
                    dronesInsideZone.put(zone.getId(), new HashMap<>());
                }
                if (dronesInsideZone.get(zone.getId()).get(roundToArrive) == null) {
                    dronesInsideZone.get(zone.getId()).put(roundToArrive, new HashMap<>());
                }
                dronesInsideZone.get(zone.getId()).put(roundToArrive, map);
            }
        }

        @Override
        public Iterator<Zone> iterator() {
            return zones.iterator();
        }

        public Zone getZone(int zoneNum) {
            return zones.get(zoneNum);
        }

        public int getOwnDronesInZone(Zone zone) {
            return dronesInsideZone.get(zone.getId()).get(Player.getPlayerId()).size();
        }

        public int getEffectiveEnemyNumberInZone(Zone zone, Drone drone) {
            int max = 0;
            for (int i = 0; i < Player.getPlayerNumber(); i++) {
                if (i == Player.getPlayerId()) {
                    continue;
                }
                int round = drone.getRoundsToArrive(zone);
                int size = getEnemyThatGetsThereInRound(round, i, dronesInsideZone.get(zone.getId()));
                System.err.println(String.format("There will be %d drones of Player%d arriveing by %d turns to Zone%d",
                        size, i, round, zone.getId()));
                // System.err.println("\t" + dronesInsideZone);
                if (max < size) {
                    max = size;
                }
            }
            return max;
        }

        public int getEnemyThatGetsThereInRound(int round, int player, Map<Integer, Map<Integer, HashSet<Drone>>> map) {
            int count = 0;
            for (int i = 0; i <= round; i++) {
                count += map.get(i).get(player).size();
                // System.err.println(String.format("\tNumber of drones that
                // gets there in %d turn: %d", i,
                // map.get(i).get(player).size()));
            }
            return count;
        }

        public int getTrueEnemyNumberInZone(Zone zone) {
            int sum = 0;
            for (int i = 0; i < Player.getPlayerNumber(); i++) {
                if (i == Player.getPlayerId()) {
                    continue;
                }
                sum += dronesInsideZone.get(zone.getId()).get(i).size();
            }
            return sum;
        }

        public void evaluate(Drones drones) {
            dronesInsideZone = new HashMap<>();
            for (Zone zone : zones) {
                zone.setDronesNeeded(100); // just to reset dronesSent list
                dronesInsideZone.put(zone.getId(), new HashMap<>());
                for (int round = 0; round < Player.getRoundsToCalcWith(); round++) {
                    dronesInsideZone.get(zone.getId()).put(round, new HashMap<>());
                    for (int i = 0; i < Player.getPlayerNumber(); i++) {
                        dronesInsideZone.get(zone.getId()).get(round).put(i, new HashSet<>());
                    }
                }
                TreeSet<Drone> dronesToZone = new TreeSet<Drone>(new Comparator<Drone>() {

                    @Override
                    public int compare(Drone d1, Drone d2) {
                        int comp = Double.compare(d1.getCoordinates().distance(zone.getCoordinates()),
                                d2.getCoordinates().distance(zone.getCoordinates()));
                        if (comp != 0) {
                            return comp;
                        }
                        return Integer.compare(d1.getId(), d2.getId());
                    }
                });

                for (Drone drone : drones) {
                    dronesToZone.add(drone);
                    if (zone.isInsideArea(drone.getCoordinates())) {
                        dronesInsideZone.get(zone.getId()).get(0).get(drone.getOwner()).add(drone);
                    }
                }

                for (int i = 0; i < Player.getPlayerNumber(); i++) {
                    for (Drone drone : drones.getDrones(i)) {
                        if (zone.isInsideArea(drone.getCoordinates())) {
                            dronesInsideZone.get(zone.getId()).get(0).get(drone.getOwner()).add(drone);
                        } else if (zone.isInsideExtendedArea(drone.getCoordinates())
                                && drone.getOwner() != Player.getPlayerId()) {
                            dronesInsideZone.get(zone.getId()).get(0).get(drone.getOwner()).add(drone);
                        } else {
                            for (int round = 1; round < Player.getRoundsToCalcWith(); round++) {
                                if (zone.isInsideArea(drone.getEstimatedPositionForRound(round))) {
                                    dronesInsideZone.get(zone.getId()).get(round).get(drone.getOwner()).add(drone);
                                }
                            }
                        }
                    }
                }

                zoneDroneMap.put(zone, dronesToZone);
            }
            System.err.println(mapAsString(dronesInsideZone));

        }

        private StringBuilder mapAsString(Map<Integer, Map<Integer, Map<Integer, HashSet<Drone>>>> map) {
            StringBuilder sb = new StringBuilder();
            for (Integer zoneId : map.keySet()) {
                sb.append("Zone").append(zoneId).append(":\n");
                for (int round : map.get(zoneId).keySet()) {
                    if (round > 10) {
                        continue;
                    }
                    sb.append("\tRound").append(round).append(":\n");
                    for (int player : map.get(zoneId).get(round).keySet()) {
                        sb.append("\t\tPlayer").append(player).append(": ");
                        for (Drone drone : map.get(zoneId).get(round).get(player)) {
                            sb.append("Drone").append(drone.getId()).append(", ");
                        }
                        sb.append("\n");
                    }
                }
                sb.append("\n");
            }
            return sb;
        }

        public Zone getZoneAt(Coordinate coordinates) {
            for (Zone zone : zones) {
                if (zone.isInsideArea(coordinates)) {
                    return zone;
                }
            }
            return null;
        }

        public int getOnRoutOwnDroneNum(Zone zone) {
            int count = 0;
            for (Drone drone : zoneDroneMap.get(zone)) {
                if (zone.equals(getZoneAt(drone.getTarget()))) {
                    count++;
                }
            }
            return count;
        }

        public List<Zone> getZones() {
            return zones;
        }

        public Map<Zone, TreeSet<Drone>> getZoneDroneMap() {
            return zoneDroneMap;
        }

        public Map<Integer, Map<Integer, HashSet<Drone>>> getDronesInsideZone(Zone zone) {
            return dronesInsideZone.get(zone.getId());
        }

        public int getEnemyNumThatGetsThereFaster(int roundsToArrive, Zone zone) {
            int max = 0;
            for (int i = 0; i < Player.getPlayerNumber(); i++) {
                if (i == Player.getPlayerId()) {
                    continue;
                }
                int sum = getEnemyThatGetsThereInRound(roundsToArrive - 1, i, dronesInsideZone.get(zone.getId()));
                if (max < sum) {
                    max = sum;
                }
            }
            return max;
        }

    }
}