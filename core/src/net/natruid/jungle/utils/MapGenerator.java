package net.natruid.jungle.utils;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.IntIntMap;
import net.natruid.jungle.components.ObstacleComponent;
import net.natruid.jungle.components.TileComponent;
import net.natruid.jungle.components.render.PosComponent;
import net.natruid.jungle.components.render.RenderComponent;
import net.natruid.jungle.components.render.TextureComponent;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.systems.PathfinderSystem;
import net.natruid.jungle.utils.types.ObstacleType;
import net.natruid.jungle.utils.types.TerrainType;

import java.util.ArrayDeque;

public class MapGenerator {
    public final RandomXS128 random;

    private final int columns;
    private final int rows;
    private final World world;
    private final long seed;
    private final Archetype tileArchetype;
    private final IntBag emptyTiles;

    private ComponentMapper<TileComponent> mTile;
    private ComponentMapper<ObstacleComponent> mObstacle;
    private PathfinderSystem pathfinderSystem;
    private int[][] map;
    private boolean initialized = false;
    private ArrayDeque<Integer> creationQueue = new ArrayDeque<>();
    private IntIntMap distanceMap = new IntIntMap();

    public MapGenerator(int columns, int rows, World world, long seed) {
        this.columns = columns;
        this.rows = rows;
        this.world = world;
        this.seed = seed;
        //noinspection unchecked
        tileArchetype = new ArchetypeBuilder().add(
            TileComponent.class,
            RenderComponent.class,
            PosComponent.class,
            TextureComponent.class
        ).build(world);

        random = new RandomXS128(seed);
        emptyTiles = new IntBag(this.columns * this.rows);
    }

    public MapGenerator(int columns, int rows, World world) {
        this(columns, rows, world, Sky.fate.nextLong());
    }

    public int[][] init() {
        Logger.debug("Map seed: " + seed);
        map = new int[columns][rows];
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                int entityId = world.create(tileArchetype);
                mTile.get(entityId).coord.set(x, y);
                emptyTiles.add(entityId);
                map[x][y] = entityId;
            }
        }
        initialized = true;
        return map;
    }

    private int getTile(int x, int y, boolean reversed) {
        if (x < 0 || y < 0) return -1;
        if (!reversed) {
            if (x >= columns || y >= rows) return -1;
        } else {
            if (x >= rows || y >= columns) return -1;
        }
        return !reversed ? map[x][y] : map[y][x];
    }

    private void createLine(TerrainType terrainType, int minWidth, int maxWidth, boolean vertical, boolean fork, long mutationFactor) {
        int ref = vertical ? columns : rows;
        int length = vertical ? rows : columns;
        int startRange = ref / 2;
        int wMid = random.nextInt(startRange) + (ref - startRange) / 2;
        int width = random.nextInt(maxWidth - minWidth) + minWidth;
        long mutateChance = 0;

        boolean reversed = random.nextBoolean();

        for (int i = 0; i < length; i++) {
            int l = reversed ? length - 1 - i : i;
            if (fork && mTile.get(getTile(l, wMid, vertical)).terrainType == terrainType) {
                break;
            }
            boolean noMutation = false;
            for (int w = 0; w < width; w++) {
                int tile = getTile(l, wMid + w - width / 2, vertical);
                if (tile < 0) continue;
                if (mTile.get(tile).terrainType == TerrainType.WATER) {
                    noMutation = true;
                }
                replaceTile(tile, terrainType);
            }
            if (noMutation) continue;
            if (mutateChance >= 100 || random.nextLong(100) >= 100 - mutateChance) {
                mutateChance = 0;
                if (random.nextBoolean()) {     // direction mutation
                    if (random.nextBoolean()) {
                        if (wMid < ref - 1) {
                            wMid += 1;
                            replaceTile(
                                getTile(l, Math.min(ref - 1, wMid + width / 2 - 1 + width % 2), vertical),
                                terrainType
                            );
                        }
                    } else {
                        if (wMid > 0) {
                            wMid -= 1;
                            replaceTile(
                                getTile(l, Math.max(0, wMid - width / 2), vertical),
                                terrainType
                            );
                        }
                    }
                } else {                        // width mutation
                    if (width != maxWidth && (width == minWidth || random.nextBoolean())) {
                        width += 1;
                    } else {
                        width -= 1;
                    }
                }
            } else {
                mutateChance += mutationFactor;
            }
        }
    }

    private void createLine(TerrainType terrainType, boolean vertical, boolean fork) {
        createLine(terrainType, 1, 3, vertical, fork, 30);
    }

    private void createArea(TerrainType terrainType, int minRadius, int maxRadius) {
        int start = map[random.nextInt(columns)][random.nextInt(rows)];
        creationQueue.addLast(start);
        distanceMap.put(start, 1);
        while (!creationQueue.isEmpty()) {
            Integer tile = creationQueue.removeFirst();
            int distance = distanceMap.get(tile, -1);
            assert distance >= 0;
            replaceTile(tile, terrainType);
            if (distance >= maxRadius) continue;
            Point coord = mTile.get(tile).coord;
            float chance = distance < minRadius ? 1
                : 1f - (distance + 1 - minRadius) / (float) (maxRadius - minRadius);
            if (chance <= 0f) continue;
            for (int diff = -1; diff <= 1; diff += 2) {
                for (int i = 0; i <= 1; i++) {
                    int x = coord.x;
                    int y = coord.y;
                    if (i == 0)
                        x += diff;
                    else
                        y += diff;
                    if (x < 0 || y < 0 || x >= columns || y >= rows) continue;
                    int next = map[x][y];
                    if (distanceMap.containsKey(next)) continue;
                    if (chance >= 1f || random.nextFloat() < chance) {
                        creationQueue.add(next);
                        distanceMap.put(next, distance + 1);
                    }
                }
            }
        }
        distanceMap.clear();
    }

    private void createArea(TerrainType terrainType, int minRadius) {
        createArea(terrainType, minRadius, Math.min(columns, rows) / 2);
    }

    private void createPath(boolean vertical) {
        int ref = vertical ? columns : rows;
        float centerFactor = 2;
        int start = -1;
        boolean reversed = random.nextBoolean();
        for (int i = 0; i <= 20; i++) {
            int startRange = (int) (ref / centerFactor);
            int x = reversed ? ref - 1 : 0;
            int y = random.nextInt(startRange) + (ref - startRange) / 2;
            start = getTile(x, y, vertical);
            if (start < 0) continue;
            boolean ideal = true;
            int x0 = vertical ? y : x;
            int y0 = vertical ? x : y;
            for (int diff = -1; diff <= 1; diff += 2) {
                for (int j = 0; j <= 1; j++) {
                    int x1 = x0;
                    int y1 = y0;
                    if (j == 0) x1 += diff;
                    else y1 += diff;
                    if (x1 < 0 || y1 < 0 || x1 >= columns || y1 >= rows) continue;
                    int tile = map[x1][y1];
                    TileComponent cTile = mTile.get(tile);
                    if (cTile.terrainType == TerrainType.WATER || cTile.obstacle >= 0) {
                        ideal = false;
                        break;
                    }
                }
            }
            if (!ideal) continue;
            TileComponent cTile = mTile.get(start);
            if (cTile.terrainType != TerrainType.WATER && cTile.obstacle < 0) break;
            reversed = !reversed;
            centerFactor -= 0.2f;
            centerFactor = Math.max(centerFactor, 1f);
        }

        float minCost = Float.MAX_VALUE;
        PathNode end = null;
        PathNode[] area = pathfinderSystem.area(start, Float.NaN, false, true);
        for (PathNode node : area) {
            Point coord = mTile.get(node.tile).coord;
            boolean endX = !vertical && coord.x == (reversed ? 0 : columns - 1);
            boolean endY = vertical && coord.y == (reversed ? 0 : rows - 1);
            if (node.cost < minCost && (endX || endY)) {
                minCost = node.cost;
                end = node;
            }
        }
        if (end == null) return;
        buildRoad(end.tile);
        while (end.prev != null) {
            Point coord = mTile.get(end.tile).coord;
            end = end.prev;
            TileComponent cTile = mTile.get(end.tile);
            boolean endX = reversed && coord.x == columns - 1 || !reversed && coord.x == 0;
            boolean endY = reversed && coord.y == rows - 1 || !reversed && coord.y == 0;
            if (!vertical && endX && cTile.coord.x == coord.x) break;
            if (vertical && endY && cTile.coord.y == coord.y) break;
            buildRoad(end.tile);
        }
    }

    private void replaceTile(int entityId, TerrainType terrainType) {
        TileComponent tileComponent = mTile.get(entityId);
        tileComponent.terrainType = terrainType;
    }

    private void buildRoad(int entityId) {
        mTile.get(entityId).hasRoad = true;
    }

    private int getEmptyTile() {
        while (emptyTiles.size() > 0) {
            int tile = emptyTiles.remove(random.nextInt(emptyTiles.size()));
            TileComponent cTile = mTile.get(tile);
            if (cTile.obstacle >= 0) continue;
            return tile;
        }
        return -1;
    }

    private void createObstacles(int amount) {
        int count = 0;
        while (count < amount) {
            int tile = getEmptyTile();
            if (tile < 0) break;
            TileComponent cTile = mTile.get(tile);
            ObstacleType obstacleType = ObstacleType.ROCK;
            boolean destroyable = false;
            if (cTile.terrainType == TerrainType.GRASS) {
                obstacleType = ObstacleType.TREE;
                destroyable = true;
            }
            int obstacle = world.create();
            {
                ObstacleComponent it = mObstacle.create(obstacle);
                it.type = obstacleType;
                it.destroyable = destroyable;
                it.maxHp = 100f;
                it.hp = it.maxHp;
            }
            count += 1;
            cTile.obstacle = obstacle;
        }
    }

    private void clean() {
        emptyTiles.clear();
    }

    public int[][] generate() {
        Logger.startWatch("Map Generation");
        if (!initialized) init();
        for (int i = 0; i < random.nextInt(5) + 5; i++) {
            createArea(TerrainType.fromValue(random.nextInt(2) + 1), Math.min(columns, rows) / 3);
        }
        for (int i = 0; i < random.nextInt(3); i++) {
            createArea(TerrainType.WATER, 2, 5);
        }
        boolean vertical = random.nextBoolean();
        int riverCount = 0;
        for (int i = 0; i < random.nextInt(4); i++) {
            createLine(TerrainType.WATER, vertical, true);
            vertical = !vertical;
            riverCount++;
        }
        for (int i = 0; i < random.nextInt(3); i++) {
            createArea(TerrainType.WATER, 2, 5);
        }
        createObstacles(random.nextInt(columns * rows / 20) + columns * rows / 40);
        for (int i = 0; i < Math.max(Math.min(random.nextInt(2) + riverCount, 2), 1); i++) {
            createPath(vertical);
            vertical = !vertical;
        }
        clean();
        Logger.stopWatch("Map Generation");
        return map;
    }
}
