package net.natruid.jungle.components;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.utils.Faction;
import net.natruid.jungle.utils.skill.Skill;

@PooledWeaver
public class UnitComponent extends Component {
    public final Array<Skill> skills = new Array<>();
    public final ObjectMap<String, Integer> proficiencies = new ObjectMap<>();
    @EntityId
    public int tile = -1;
    public Faction faction = Faction.NONE;
    public int level = 0;
    public int exp = 0;
    public int hp = 0;
    public int ap = 0;
    public float extraMovement = 0;

    protected void reset() {
        faction = Faction.NONE;
        skills.clear();
        proficiencies.clear();
    }
}
