package com.moonproject.moonmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 记忆余烬：拔刀瞬间与记忆灼烧时从使用者身上飘起的火星。
 *
 * <p>纯客户端表现：向上飘、略微减速、边飘边缩小并淡出，不落地、不碰撞。</p>
 */
public class MemoryEmberParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected MemoryEmberParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 16 + this.random.nextInt(14);
        this.quadSize = 0.055F + this.random.nextFloat() * 0.045F;
        this.friction = 0.94F;
        this.gravity = -0.012F;      // 负重力 = 上浮
        this.hasPhysics = false;
        this.rCol = 1.0F;
        this.gCol = 0.78F;
        this.bCol = 0.52F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        // 越接近寿命终点越透明（alpha 由渲染层的颜色 alpha 控制）
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = Math.max(0.0F, 1.0F - life * life);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float life = ((float) this.age + partialTick) / (float) this.lifetime;
        return this.quadSize * Math.max(0.05F, 1.0F - life * 0.55F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** 供 {@code RegisterParticleProvidersEvent#registerSpriteSet} 用的工厂。 */
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new MemoryEmberParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
