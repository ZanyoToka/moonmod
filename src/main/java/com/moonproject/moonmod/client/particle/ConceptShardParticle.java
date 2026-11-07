package com.moonproject.moonmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 概念残片：抹去技能触发时四散的冷白碎片。
 *
 * <p>向四周摊开、几乎不受重力，寿命比另外两种都长一点，
 * 让「被抹掉的东西正在碎掉」这一层读得出来。</p>
 */
public class ConceptShardParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ConceptShardParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 22 + this.random.nextInt(14);
        this.quadSize = 0.13F + this.random.nextFloat() * 0.09F;
        this.friction = 0.9F;
        this.gravity = 0.005F;
        this.hasPhysics = false;
        this.rCol = 0.92F;
        this.gCol = 0.94F;
        this.bCol = 1.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = Math.max(0.0F, 1.0F - life * life * life);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new ConceptShardParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
