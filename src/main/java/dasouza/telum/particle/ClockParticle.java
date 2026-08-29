package dasouza.telum.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class ClockParticle extends SingleQuadParticle {

    protected ClockParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet.get(level.getRandom()));
        this.friction = 0.98F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        if (ySpeed < 0.0) {
            // Falling clock rain particle: apply light downward gravity & extended lifetime
            this.gravity = 0.08F;
            this.lifetime = 60 + this.random.nextInt(40);
        } else {
            this.gravity = 0.0F;
            this.lifetime = 30 + this.random.nextInt(15);
        }

        this.quadSize *= 0.9F;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ClockParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
