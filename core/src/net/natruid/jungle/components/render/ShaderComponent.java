package net.natruid.jungle.components.render;

import com.artemis.Component;
import com.badlogic.gdx.graphics.GL20;
import net.natruid.jungle.utils.Shader;

public class ShaderComponent extends Component {
    public static final ShaderComponent DEFAULT = new ShaderComponent();

    public Shader shader = Shader.DEFAULT;
    public int blendSrcFunc = GL20.GL_SRC_ALPHA;
    public int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
}
