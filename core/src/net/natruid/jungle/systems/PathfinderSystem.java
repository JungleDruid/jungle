package net.natruid.jungle.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntDeque;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import net.natruid.jungle.components.TileComponent;
import net.natruid.jungle.utils.PathNode;
import net.natruid.jungle.utils.Point;
import net.natruid.jungle.utils.types.ExtractPathType;
import net.natruid.jungle.utils.types.TerrainType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class PathfinderSystem extends BaseSystem {
    private ComponentMapper<TileComponent> mTile;
    private TileSystem tileSystem;
    private final Pool<Pathfinder> pool = new Pool<Pathfinder>(8) {
        @Override
        protected Pathfinder newObject() {
            return new Pathfinder(PathfinderSystem.this, mTile, tileSystem);
        }
    };

    private Pathfinder obtain() {
        synchronized (pool) {
            return pool.obtain();
        }
    }

    private void free(Pathfinder pathfinder) {
        synchronized (pool) {
            pool.free(pathfinder);
        }
    }

    public PathNode[] area(int from, float maxCost, boolean diagonal) {
        return area(from, maxCost, diagonal, false);
    }

    public PathNode[] area(int from, boolean diagonal) {
        return area(from, Float.NaN, diagonal, false);
    }

    public PathNode[] area(int from, float maxCost, boolean diagonal, boolean buildingRoad) {
        Pathfinder pathfinder = obtain();
        PathNode[] area = pathfinder.area(from, maxCost, diagonal, buildingRoad);
        free(pathfinder);
        return area;
    }

    public Deque<PathNode> path(int from, int goal, boolean diagonal) {
        return path(from, goal, diagonal, -1, ExtractPathType.EXACT, Float.NaN);
    }

    public Deque<PathNode> path(int from, int goal, boolean diagonal, int unit, ExtractPathType type, float maxCost) {
        Pathfinder pathfinder = obtain();
        Deque<PathNode> path = pathfinder.path(from, goal, diagonal, unit, type, maxCost);
        free(pathfinder);
        return path;
    }

    public Deque<PathNode> extractPath(PathNode[] area, int goal) {
        return extractPath(area, goal, -1, ExtractPathType.EXACT, Float.NaN, Float.NaN);
    }

    public Deque<PathNode> extractPath(PathNode[] area, int goal, int unit, ExtractPathType type, float maxCost, float inRange) {
        return extractPath(Arrays.asList(area), goal, unit, type, maxCost, inRange);
    }

    private Deque<PathNode> extractPath(Iterable<PathNode> pathNodes, int goal, int unit,
                                        ExtractPathType type, float maxCost, float inRange) {
        if (type == ExtractPathType.EXACT || type == ExtractPathType.CLOSEST) {
            if (unit < 0 || mTile.get(goal).unit < 0) {
                for (PathNode node : pathNodes) {
                    if (goal == node.tile) {
                        return buildPath(node, unit, maxCost);
                    }
                }
            }
            if (type == ExtractPathType.EXACT) return null;
        }

        PathNode bestNode = null;
        float minDist = Float.MAX_VALUE;
        for (PathNode node : pathNodes) {
            int unit1 = mTile.get(node.tile).unit;
            if (unit > 0 && unit1 >= 0 && unit != unit1) continue;
            float dist = tileSystem.getDistance(node.tile, goal);
            if (!Float.isNaN(inRange) && dist > inRange) continue;
            boolean better;
            if (bestNode == null) {
                better = true;
            } else {
                switch (type) {
                    case CLOSEST:
                        better = dist < minDist || dist == minDist && node.cost < bestNode.cost;
                        break;
                    case LOWEST_COST:
                        better = node.cost < bestNode.cost;
                        break;
                    default:
                        throw new RuntimeException("IMPOSSIBLE! " + type);
                }
            }
            if (better) {
                bestNode = node;
                minDist = dist;
            }
        }

        if (bestNode != null) return buildPath(bestNode, unit, maxCost);

        return null;
    }

    private Deque<PathNode> buildPath(PathNode node, int unit, float maxCost) {
        LinkedList<PathNode> path = new LinkedList<PathNode>();
        PathNode current = node;
        while (current != null) {
            int unit1 = mTile.get(current.tile).unit;
            if ((Float.isNaN(maxCost) || current.cost <= maxCost) && (unit < 0 || unit1 < 0 || unit == unit1)) {
                path.addFirst(current);
            }
            current = current.prev;
        }
        return path;
    }

    @Override
    protected void processSystem() {
    }

    private class Pathfinder {
        private final PathfinderSystem pathfinderSystem;
        private final ComponentMapper<TileComponent> mTile;
        private final TileSystem tileSystem;

        private final BinaryHeap<PathNode> frontier = new BinaryHeap<>();
        private final IntMap<PathNode> visited = new IntMap<>();
        private final BooleanArray walkables = new BooleanArray(4);
        private final ArrayDeque<Boolean> walkableDiagonals = new ArrayDeque<>(4);
        private final IntDeque searchQueue = new IntDeque();
        private final Point searchDirection = new Point();

        private Pathfinder(PathfinderSystem pathfinderSystem, ComponentMapper<TileComponent> mTile, TileSystem tileSystem) {
            this.pathfinderSystem = pathfinderSystem;
            this.mTile = mTile;
            this.tileSystem = tileSystem;
        }

        private void init(int from) {
            PathNode node = new PathNode(from, 0f);
            frontier.clear();
            visited.clear();
            frontier.add(node);
            visited.put(from, node);
        }

        private boolean searchNeighbors(@NotNull PathNode current, boolean diagonal, float maxCost, int goal, boolean buildingRoad) {
            if (!diagonal) {
                walkables.clear();
                walkableDiagonals.clear();
            }
            if (diagonal && walkableDiagonals.size() == 0) return false;

            float costMultiplier = 1;

            searchQueue.clear();
            {
                TileComponent it = mTile.get(current.tile);
                if (buildingRoad && it.terrainType == TerrainType.WATER && current.prev != null) {
                    searchDirection.set(it.coord);
                    searchDirection.times(2);
                    searchDirection.minus(mTile.get(current.prev.tile).coord);
                    int next = tileSystem.get(searchDirection);
                    if (next >= 0) searchQueue.add(next);
                } else {
                    int roadCount = 0;
                    for (int i = 0; i < 4; i++) {
                        int next = tileSystem.neighbor(it.coord.x, it.coord.y, i, diagonal);
                        searchQueue.add(next);
                        if (buildingRoad) {
                            if (next >= 0) {
                                if (mTile.get(next).terrainType == TerrainType.WATER && it.terrainType != TerrainType.WATER)
                                    costMultiplier += 1f;
                                if (mTile.get(next).hasRoad) roadCount += 1;
                            } else {
                                costMultiplier += 0.75f;
                            }
                        }
                    }
                    if (buildingRoad && !it.hasRoad && roadCount > 1) return false;
                }
            }

            while (!searchQueue.isEmpty()) {
                int next = searchQueue.popFirst();
                TileComponent nextTileComponent = (next >= 0) ? mTile.get(next) : null;
                boolean nextWalkable = nextTileComponent != null && nextTileComponent.obstacle < 0;
                if (!diagonal) {
                    int size = walkables.size;
                    if (size > 0) {
                        walkableDiagonals.addLast(nextWalkable || walkables.get(size - 1));
                        if (size == 3) {
                            walkableDiagonals.addLast(nextWalkable || walkables.get(0));
                        }
                    }
                    walkables.add(nextWalkable);
                }
                float cost = (!diagonal) ? 1f : 1.5f;
                if (nextTileComponent != null) {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (nextTileComponent.terrainType) {
                        case WATER:
                            if (!nextTileComponent.hasRoad) {
                                cost *= buildingRoad ? 10f : 3f;
                            }
                            break;
                        default:
                            if (nextTileComponent.hasRoad) {
                                cost *= buildingRoad ? 0.1f : 0.5f;
                            }
                    }
                }
                float nextCost = current.cost + cost * costMultiplier;
                if (diagonal && !walkableDiagonals.removeFirst() || !nextWalkable || (!Float.isNaN(maxCost) && (nextCost > maxCost)))
                    continue;
                PathNode nextNode = visited.get(next);
                if (nextNode == null || nextNode.cost > nextCost) {
                    PathNode node = nextNode;
                    if (node == null) node = new PathNode(next, nextCost, current);
                    if (nextNode == null) {
                        visited.put(next, node);
                        if (node.tile == goal) return true;
                    } else {
                        nextNode.cost = nextCost;
                        nextNode.prev = current;
                    }
                    float priority = nextCost;
                    if (goal >= 0) {
                        priority += heuristic(goal, next, nextTileComponent.hasRoad ? -0.5f : 0f);
                    }
                    frontier.add(node, priority);
                }
            }
            return false;
        }

        private float heuristic(int a, int b, float f) {
            return tileSystem.getDistance(a, b) + f;
        }

        public PathNode[] area(int from, float maxCost, boolean diagonal, boolean buildingRoad) {
            init(from);
            while (!frontier.isEmpty()) {
                PathNode current = frontier.pop();
                searchNeighbors(current, false, maxCost, -1, buildingRoad);
                if (diagonal) searchNeighbors(current, true, maxCost, -1, buildingRoad);
            }
            IntMap.Values<PathNode> values = visited.values();
            PathNode[] ret = new PathNode[visited.size];
            {
                int i = 0;
                while (values.hasNext()) ret[i++] = values.next();
            }
            return ret;
        }

        public Deque<PathNode> path(int from, int goal, boolean diagonal, int unit, ExtractPathType type, float maxCost) {
            init(from);
            while (!frontier.isEmpty()) {
                PathNode current = frontier.pop();
                for (int i = 0; i < (diagonal ? 2 : 1); i++) {
                    if (searchNeighbors(current, i > 0, Float.NaN, goal, false))
                        return pathfinderSystem.extractPath(visited.values(), goal, unit, type, maxCost, Float.NaN);
                }
            }
            return null;
        }
    }
}
