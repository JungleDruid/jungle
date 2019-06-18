package net.natruid.jungle.systems;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.managers.TagManager;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.components.ObstacleComponent;
import net.natruid.jungle.components.TileComponent;
import net.natruid.jungle.components.render.*;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.ImmutablePoint;
import net.natruid.jungle.utils.MapGenerator;
import net.natruid.jungle.utils.Point;
import net.natruid.jungle.utils.Shader;
import net.natruid.jungle.utils.callbacks.RenderCallback;
import net.natruid.jungle.utils.types.RendererType;
import net.natruid.jungle.utils.types.TerrainType;
import net.natruid.jungle.views.DebugView;

import static net.natruid.jungle.utils.Constants.*;

public class TileSystem extends BaseSystem implements InputProcessor {
    public static final int TILE_SIZE = 64;
    private static final String TAG_GRID_RENDERER = "GridRenderer";
    private static final String TAG_MOUSE_ON_TILE = "MouseOnTile";
    private static final int HALF_TILE_SIZE = TILE_SIZE / 2;
    private static final Color GRID_COLOR = new Color(.5f, .7f, .3f, .8f);
    private static final Color MOUSE_ON_TILE_COLOR = new Color(1f, 1f, 0f, 0.4f);
    private static final ShaderComponent TILE_SHADER_COMPONENT = new ShaderComponent();
    private static final ShaderComponent WATER_TILE_SHADER_COMPONENT = new ShaderComponent();
    private static final ShaderComponent STONE_IN_WATER_SHADER_COMPONENT = new ShaderComponent();

    static {
        {
            TILE_SHADER_COMPONENT.shader = new Shader("tile");
            ShaderProgram instance = TILE_SHADER_COMPONENT.shader.getInstance();
            instance.begin();
            instance.setUniformf("bound", 1f - 64f / 96f);
            instance.end();
            TILE_SHADER_COMPONENT.blendSrcFunc = GL20.GL_SRC_ALPHA;
            TILE_SHADER_COMPONENT.blendDstFunc = GL20.GL_ONE;
        }
        {
            WATER_TILE_SHADER_COMPONENT.shader = new Shader("tile");
            ShaderProgram instance = WATER_TILE_SHADER_COMPONENT.shader.getInstance();
            instance.begin();
            instance.setUniformf("bound", 1f - 64f / 96f);
            instance.end();
            WATER_TILE_SHADER_COMPONENT.blendSrcFunc = GL20.GL_SRC_ALPHA;
            WATER_TILE_SHADER_COMPONENT.blendDstFunc = GL20.GL_ONE;
        }
        {
            STONE_IN_WATER_SHADER_COMPONENT.shader = new Shader("tile");
            ShaderProgram instance = STONE_IN_WATER_SHADER_COMPONENT.shader.getInstance();
            instance.begin();
            instance.setUniformf("bound", 0.45f);
            instance.end();

        }
    }

    private int columns = 0;
    private int rows = 0;
    private Point mouseCoord = null;
    private long seed = 0L;

    private final CropComponent tileCropComponent = new CropComponent();

    private CameraSystem cameraSystem;
    private TagManager tagManager;
    private ComponentMapper<TileComponent> mTile;
    private ComponentMapper<RenderComponent> mRender;
    private ComponentMapper<PosComponent> mPos;
    private ComponentMapper<AngleComponent> mAngle;
    private ComponentMapper<ScaleComponent> mScale;
    private ComponentMapper<InvisibleComponent> mInvisible;
    private ComponentMapper<TextureComponent> mTexture;
    private ComponentMapper<RectComponent> mRect;
    private ComponentMapper<CustomRenderComponent> mCustomRender;
    private ComponentMapper<ObstacleComponent> mObstacle;
    private ComponentMapper<ShaderComponent> mShader;

    private int[][] tileEntities;

    private final RenderCallback gridRenderCallback = renderer -> {
        OrthographicCamera camera = cameraSystem.camera;
        ShapeRenderer shapeRenderer = renderer.shapeRenderer;

        renderer.begin(camera, RendererType.SHAPE_LINE);
        float origin = -HALF_TILE_SIZE;
        float right = -HALF_TILE_SIZE + columns * TILE_SIZE;
        float top = -HALF_TILE_SIZE + rows * TILE_SIZE;

        float shadowOffset = 1 * camera.zoom;
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= columns; i++) {
            shapeRenderer.line(
                origin + i * TILE_SIZE + shadowOffset,
                origin - shadowOffset,
                origin + i * TILE_SIZE + shadowOffset,
                top - shadowOffset
            );
        }
        for (int i = 0; i <= rows; i++) {
            shapeRenderer.line(
                origin + shadowOffset,
                origin + i * TILE_SIZE - shadowOffset,
                right + shadowOffset,
                origin + i * TILE_SIZE - shadowOffset
            );
        }
        shapeRenderer.setColor(GRID_COLOR);
        for (int i = 0; i <= columns; i++) {
            shapeRenderer.line(
                origin + i * TILE_SIZE,
                origin,
                origin + i * TILE_SIZE,
                top
            );
        }
        for (int i = 0; i <= rows; i++) {
            shapeRenderer.line(
                origin,
                origin + i * TILE_SIZE,
                right,
                origin + i * TILE_SIZE
            );
        }
    };

    public int get(Point coord) {
        if (!isCoordValid(coord)) return -1;
        return tileEntities[coord.x][coord.y];
    }

    public int get(int x, int y) {
        if (!isCoordValid(x, y)) return -1;
        return tileEntities[x][y];
    }

    public boolean isCoordValid(Point coord) {
        if (coord == null) return false;
        return isCoordValid(coord.x, coord.y);
    }

    private boolean isCoordValid(int x, int y) {
        return x >= 0 && y >= 0 && x < columns && y < rows;
    }

    public int neighbor(int x, int y, int index, boolean diagonal) {
        ImmutablePoint p = diagonal ? Point.diagonals[index] : Point.adjacent[index];
        return get(x + p.getX(), y + p.getY());
    }

    public int right(Point coord) {
        return get(coord.x + 1, coord.y);
    }

    public int left(Point coord) {
        return get(coord.x - 1, coord.y);
    }

    public int up(Point coord) {
        return get(coord.x, coord.y + 1);
    }

    public int down(Point coord) {
        return get(coord.x, coord.y - 1);
    }

    private TextureRegion dirtTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/dirt.png")));
    private TextureRegion grassTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/grass.png")));
    private TextureRegion roadTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/road.png")));
    private TextureRegion roadTextureUpDown = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/road-ud.png")));
    private TextureRegion roadTextureRightUp = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/road-ru.png")));
    private TextureRegion roadTextureRightUpLeft = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/road-rul.png")));
    private TextureRegion roadTextureRightUpLeftDown = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/road-ruld.png")));
    private TextureRegion waterTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/water.png")));
    private TextureRegion bridgeTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/bridge.png")));
    private TextureRegion bridgeMultiDirectionTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/bridge-multi-direction.png")));
    private TextureRegion treeTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/tree.png")));
    private TextureRegion rockTexture = new TextureRegion(new Texture(Sky.scout.locate("assets/img/tiles/rock.png")));

    public void create(int columns, int rows) {
        create(columns, rows, Sky.fate.nextLong());
    }

    private int getMouseOnTile() {
        return tagManager.getEntityId(TAG_MOUSE_ON_TILE);
    }

    private void setMouseOnTile(int value) {
        if (value >= 0) tagManager.register(TAG_MOUSE_ON_TILE, value);
        else tagManager.unregister(TAG_MOUSE_ON_TILE);
    }

    public void create(int columns, int rows, long seed) {
        reset();
        this.columns = columns;
        this.rows = rows;
        this.seed = seed;
        MapGenerator generator = new MapGenerator(columns, rows, world, seed);
        world.inject(generator);
        tileEntities = generator.init();
        generator.generate();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                int tile = tileEntities[x][y];
                TileComponent cTile = mTile.get(tile);
                mPos.get(tile).xy.set(x * TILE_SIZE, y * TILE_SIZE);
                {
                    TextureComponent it = mTexture.get(tile);
                    TextureRegion region;
                    switch (cTile.terrainType) {
                        case NONE:
                        case DIRT:
                            region = dirtTexture;
                            break;
                        case GRASS:
                            region = grassTexture;
                            break;
                        case WATER:
                            region = waterTexture;
                            break;
                        default:
                            throw new RuntimeException("Unknown terrain type.");
                    }

                    Color color;
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (cTile.terrainType) {
                        case WATER:
                            color = new Color(1f, 1f, 1f, .7f);
                            break;
                        default:
                            float gb = .8f + generator.random.nextFloat() * .2f;
                            color = new Color(
                                Math.min(gb + .2f, 1f),
                                gb,
                                gb,
                                1f
                            );
                            break;
                    }

                    it.setRegion(region);
                    it.color.set(color);
                }

                world.edit(tile)
                    .add(cTile.terrainType == TerrainType.WATER ? WATER_TILE_SHADER_COMPONENT : TILE_SHADER_COMPONENT)
                    .add(tileCropComponent);

                if (cTile.hasRoad) {
                    // create road or bridge
                    {
                        int road = world.create();
                        mPos.create(road).set(mPos.get(tile).xy);
                        Point coord = cTile.coord;
                        int roadCount = 0;
                        boolean[] dirs = new boolean[4];
                        for (int i = 0; i < dirs.length; i++) {
                            int t;
                            switch (i) {
                                case 0:
                                    t = right(coord);
                                    break;
                                case 1:
                                    t = up(coord);
                                    break;
                                case 2:
                                    t = left(coord);
                                    break;
                                default:
                                    t = down(coord);
                            }
                            if (t >= 0 && mTile.get(t).hasRoad) {
                                dirs[i] = true;
                                roadCount += 1;
                            } else {
                                dirs[i] = false;
                            }
                        }
                        if (cTile.terrainType == TerrainType.WATER) {
                            // create bridge
                            mRender.create(road).z = mRender.get(tile).z + 0.2f;
                            boolean rotate = false;
                            boolean multiDirection = false;
                            switch (roadCount) {
                                case 1:
                                    if (dirs[1] || dirs[3])
                                        rotate = true;
                                    break;
                                case 2:
                                    if (dirs[1] && dirs[3]) {
                                        rotate = true;
                                    } else if (!(dirs[0] && dirs[2])) {
                                        multiDirection = true;
                                    }
                                    break;
                                default:
                                    multiDirection = true;
                            }
                            if (rotate) mAngle.create(road).rotation = 90f;
                            mTexture.create(road).setRegion(multiDirection ? bridgeMultiDirectionTexture : bridgeTexture);
                        } else {
                            // create road
                            mRender.create(road).z = mRender.get(tile).z + 0.1f;
                            int directions = 0;
                            for (int i = 0; i < dirs.length; i++) {
                                boolean dir = dirs[i];
                                if (dir) directions = directions | (1 << i);
                            }
                            TextureRegion region;
                            switch (directions) {
                                case UP | DOWN:
                                case UP:
                                case DOWN:
                                    region = roadTextureUpDown;
                                    break;
                                case LEFT | RIGHT:
                                case LEFT:
                                case RIGHT:
                                    mAngle.create(road).rotation = 90f;
                                    region = roadTextureUpDown;
                                    break;
                                case RIGHT | UP:
                                    region = roadTextureRightUp;
                                    break;
                                case UP | LEFT:
                                    mAngle.create(road).rotation = 90f;
                                    region = roadTextureRightUp;
                                    break;
                                case LEFT | DOWN:
                                    mAngle.create(road).rotation = 180f;
                                    region = roadTextureRightUp;
                                    break;
                                case DOWN | RIGHT:
                                    mAngle.create(road).rotation = 270f;
                                    region = roadTextureRightUp;
                                    break;
                                case RIGHT | UP | LEFT:
                                    region = roadTextureRightUpLeft;
                                    break;
                                case UP | LEFT | DOWN:
                                    mAngle.create(road).rotation = 90f;
                                    region = roadTextureRightUpLeft;
                                    break;
                                case LEFT | DOWN | RIGHT:
                                    mAngle.create(road).rotation = 180f;
                                    region = roadTextureRightUpLeft;
                                    break;
                                case DOWN | RIGHT | UP:
                                    mAngle.create(road).rotation = 270f;
                                    region = roadTextureRightUpLeft;
                                    break;
                                case RIGHT | UP | LEFT | DOWN:
                                    region = roadTextureRightUpLeftDown;
                                    break;
                                default:
                                    region = roadTexture;
                            }
                            mTexture.create(road).setRegion(region);
                        }
                    }
                }

                if (cTile.obstacle >= 0) {
                    int obstacle = cTile.obstacle;
                    ObstacleComponent cObstacle = mObstacle.get(obstacle);
                    mRender.create(obstacle).z = Z_OBSTACLE;
                    mPos.create(obstacle).set(mPos.get(tile));
                    mScale.create(obstacle).set(
                        generator.random.nextFloat() * 0.2f + 0.9f,
                        generator.random.nextFloat() * 0.2f + 0.9f
                    );
                    {
                        TextureComponent it = mTexture.create(obstacle);
                        TextureRegion region;

                        //noinspection SwitchStatementWithTooFewBranches
                        switch (cObstacle.type) {
                            case ROCK:
                                if (cTile.terrainType == TerrainType.WATER) {
                                    world.edit(obstacle).add(STONE_IN_WATER_SHADER_COMPONENT);
                                }
                                region = rockTexture;
                                break;
                            default:
                                region = treeTexture;
                        }
                        it.setRegion(region);
                        it.flipX = generator.random.nextBoolean();
                    }
                }
            }
        }
        {
            int entityId = world.create();
            tagManager.register("GridRenderer", entityId);
            mRender.create(entityId).z = Z_GRID;
            mInvisible.create(entityId);
            mCustomRender.create(entityId).renderCallback = gridRenderCallback;
        }
        {
            int entityId = world.create();
            setMouseOnTile(entityId);
            mRender.create(entityId).z = Z_MOUSE_ON_TILE;
            mPos.create(entityId);
            mInvisible.create(entityId);
            {
                RectComponent it = mRect.create(entityId);
                it.width = TILE_SIZE;
                it.height = TILE_SIZE;
                it.color.set(MOUSE_ON_TILE_COLOR);
            }
        }
        {
            Rectangle it = tileCropComponent.rect;
            it.set(-HALF_TILE_SIZE, -HALF_TILE_SIZE, (columns * TILE_SIZE), (rows * TILE_SIZE));
            cameraSystem.cropRect = it;
        }
    }

    public float getDistance(int tile1, int tile2) {
        Point coord1 = mTile.get(tile1).coord;
        Point coord2 = mTile.get(tile2).coord;
        int x = Math.abs(coord1.x - coord2.x);
        int y = Math.abs(coord1.y - coord2.y);
        return Math.min(x, y) * 1.5f + Math.abs(x - y);
    }

    private final Vector3 projection = new Vector3();

    private Point screenToCoord(Point screenCoord) {
        if (columns == 0) return null;

        int screenX = screenCoord.x;
        int screenY = screenCoord.y;
        OrthographicCamera camera = cameraSystem.camera;
        projection.set(screenX, screenY, 0f);
        camera.unproject(projection);
        int x = Math.round(projection.x) + HALF_TILE_SIZE;
        int y = Math.round(projection.y) + HALF_TILE_SIZE;
        if (x < 0 || x >= columns * TILE_SIZE || y < 0 || y >= rows * TILE_SIZE) return null;

        return screenCoord.set(x / TILE_SIZE, y / TILE_SIZE);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.APOSTROPHE) {
            int gridRenderer = tagManager.getEntityId("GridRenderer");
            if (gridRenderer > 0) {
                if (mInvisible.has(gridRenderer)) {
                    mInvisible.remove(gridRenderer);
                } else {
                    mInvisible.create(gridRenderer);
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    private final Point[] tempCoords = new Point[]{new Point(), new Point()};
    private int tempCoordsIndex = 0;

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (!this.checkProcessing()) return false;

        Point currentCoord = screenToCoord(tempCoords[tempCoordsIndex].set(screenX, screenY));

        if (currentCoord == mouseCoord) return false;
        int mouseOnTile = getMouseOnTile();
        PosComponent pos = mPos.get(mouseOnTile);
        if (pos == null) return false;

        mouseCoord = currentCoord;
        tempCoordsIndex = 1 - tempCoordsIndex;

        DebugView debugView = Sky.jungle.getDebugView();
        if (currentCoord == null) {
            mInvisible.create(mouseOnTile);
            if (debugView != null) {
                debugView.tileLabel.setText("Tile: -1");
            }
            return false;
        }

        mInvisible.remove(mouseOnTile);
        pos.set(mPos.get(get(currentCoord)));

        if (debugView != null) {
            debugView.tileLabel.setText(String.format("Tile: %d %s", get(currentCoord), currentCoord));
        }

        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    private void reset() {
        columns = 0;
        rows = 0;
        setMouseOnTile(-1);
        tagManager.unregister(TAG_GRID_RENDERER);
        cameraSystem.cropRect = null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            mInvisible.remove(getMouseOnTile());
        }
    }

    @Override
    protected void dispose() {
        reset();
        super.dispose();
    }

    public long getSeed() {
        return seed;
    }

    public Point getMouseCoord() {
        return mouseCoord;
    }

    @Override
    protected void processSystem() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(ShaderComponent.class)).getEntities();
            for (int i = 0; i < entities.size(); i++) {
                mShader.get(entities.get(i)).shader.reset();
            }
        }

        try {
            ShaderProgram instance = WATER_TILE_SHADER_COMPONENT.shader.getInstance();
            instance.begin();
            instance.setUniformf("time", Sky.jungle.getTime());
            instance.end();
        } catch (Exception ignored) {
        }
    }
}
