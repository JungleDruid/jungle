package net.natruid.jungle.systems.render;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.Bag;
import com.badlogic.gdx.utils.Pool;
import net.natruid.jungle.components.render.*;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.Logger;
import net.natruid.jungle.utils.RendererHelper;
import net.natruid.jungle.views.DebugView;

import java.util.Comparator;

public class RenderBatchSystem extends BaseSystem {
    public final RendererHelper renderer = new RendererHelper();

    private final Comparator<RenderJob> comparator = new RenderJobComparator();
    private final Pool<RenderJob> jobPool = new Pool<RenderJob>() {
        @Override
        protected RenderJob newObject() {
            return new RenderJob(-1, null);
        }
    };

    private ComponentMapper<RenderComponent> mRender;
    private ComponentMapper<TextureComponent> mTexture;
    private ComponentMapper<ShaderComponent> mShader;
    private ComponentMapper<CropComponent> mCrop;
    private ComponentMapper<UIComponent> mUI;
    private Bag<RenderJob> sortedJobs = new Bag<>(RenderJob.class);
    private boolean needSorting = false;

    public RenderBatchSystem() {
        DebugView debugView = Sky.jungle.getDebugView();
        if (debugView != null) {
            debugView.renderer = renderer;
        }
    }

    @Override
    protected void processSystem() {
        if (needSorting) {
            try {
                sortedJobs.sort(comparator);
            } catch (IllegalArgumentException e) {
                Logger.error("RenderBatchSystem sorting error.", e);
            }
            needSorting = false;
        }

        RenderJob[] data = sortedJobs.getData();
        for (int i = 0; i < sortedJobs.size(); i++) {
            RenderJob job = data[i];
            RenderSystem agent = job.agent;

            agent.process(job.entityId);
        }

        renderer.end();
    }

    public void registerAgent(int entityId, RenderSystem agent) {
        if (!mRender.has(entityId)) throw new RuntimeException("Require RenderComponent!");
        RenderJob job = jobPool.obtain();
        job.entityId = entityId;
        job.agent = agent;
        sortedJobs.add(job);
        needSorting = true;
    }

    public void unregisterAgent(int entityId, RenderSystem agent) {
        RenderJob[] data = sortedJobs.getData();
        for (int i = sortedJobs.size() - 1; i >= 0; i--) {
            RenderJob job = data[i];
            if (job.entityId == entityId && job.agent == agent) {
                sortedJobs.remove(i);
                jobPool.free(job);
                needSorting = true;
                break;
            }
        }
    }

    @Override
    protected void dispose() {
        renderer.dispose();
        DebugView debugView = Sky.jungle.getDebugView();
        debugView.renderer = null;
    }

    public class RenderJob implements Pool.Poolable {
        public int entityId;
        public RenderSystem agent;

        public RenderJob(int entityId, RenderSystem agent) {
            this.entityId = entityId;
            this.agent = agent;
        }

        @Override
        public void reset() {
            entityId = -1;
            agent = null;
        }
    }

    private class RenderJobComparator implements Comparator<RenderJob> {
        private int compareNull(Object o0, Object o1) {
            return (o0 != null && o1 == null) ? 1
                : (o0 == null && o1 != null) ? -1
                : 0;
        }

        @Override
        public int compare(RenderJob j0, RenderJob j1) {
            int p0 = j0.entityId;
            int p1 = j1.entityId;

            int result;

            result = compareNull(mUI.get(p0), mUI.get(p1));
            if (result != 0) return result;

            result = Float.compare(
                Float.isNaN(j0.agent.zOverride) ? mRender.get(p0).z : j0.agent.zOverride,
                Float.isNaN(j1.agent.zOverride) ? mRender.get(p1).z : j0.agent.zOverride
            );
            if (result != 0) return result;

            result = Integer.compare(j0.agent.hashCode(), j1.agent.hashCode());
            if (result != 0) return result;

            int i0, i1;
            try {
                i0 = mTexture.get(p0).getRegion().getTexture().hashCode();
            } catch (NullPointerException ignore) {
                i0 = 0;
            }
            try {
                i1 = mTexture.get(p1).getRegion().getTexture().hashCode();
            } catch (NullPointerException ignore) {
                i1 = 0;
            }

            result = Integer.compare(i0, i1);
            if (result != 0) return result;

            try {
                i0 = mShader.get(p0).hashCode();
            } catch (NullPointerException ignore) {
                i0 = 0;
            }
            try {
                i1 = mShader.get(p1).hashCode();
            } catch (NullPointerException ignore) {
                i1 = 0;
            }

            result = Integer.compare(i0, i1);
            if (result != 0) return result;

            try {
                i0 = mCrop.get(p0).rect.hashCode();
            } catch (NullPointerException ignore) {
                i0 = 0;
            }
            try {
                i1 = mCrop.get(p1).rect.hashCode();
            } catch (NullPointerException ignore) {
                i1 = 0;
            }

            result = Integer.compare(i0, i1);

            return result;
        }
    }
}
