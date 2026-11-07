package com.moonproject.moonmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 焦黑斩痕：命中处崩落的深色碎屑。
 *
 * <p>表现上「先快后慢」——初速大、摩擦大、受重力下落，比余烬更快消失，
 * 用来把刀锋落点那一下「钉」在视野里。</p>
 */
public class BlackSlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected BlackSlashParticle(ClientLevel level, double x, double y, double z,
                                 double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 12 + this.random.nextInt(10);
        this.quadSize = 0.20F + this.random.nextFloat() * 0.12F;
        this.friction = 0.82F;
        this.gravity = 0.06F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = Math.max(0.0F, 1.0F - life);
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
            return new BlackSlashParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
