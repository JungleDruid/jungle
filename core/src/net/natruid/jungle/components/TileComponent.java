package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import net.natruid.jungle.utils.Point;
import net.natruid.jungle.utils.types.TerrainType;

public class TileComponent extends Component {
    public final Point coord = new Point();
    public TerrainType terrainType = TerrainType.NONE;
    public boolean hasRoad = false;
    @EntityId
    public int unit = -1;
    @EntityId
    public int obstacle = -1;

    protected void reset() {
        coord.set(0, 0);
        terrainType = TerrainType.NONE;
    }
}
