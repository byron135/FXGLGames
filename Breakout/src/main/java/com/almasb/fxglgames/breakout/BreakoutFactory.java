/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2017 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.fxglgames.breakout;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyDef;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.ui.FontType;
import com.almasb.fxglgames.breakout.components.BallComponent;
import com.almasb.fxglgames.breakout.components.BatComponent;
import com.almasb.fxglgames.breakout.components.BrickComponent;
import com.almasb.fxglgames.breakout.components.MoveDownComponent;
import javafx.geometry.Point2D;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class BreakoutFactory implements EntityFactory {

    @Spawns("brick_blue")
    public Entity newBrick(SpawnData data) {
        return entityBuilder()
                .from(data)
                .type(BreakoutType.BRICK)
                .bbox(new HitBox(BoundingShape.box(96, 32)))
                .collidable()
                .with(new PhysicsComponent())
                .with(new BrickComponent())
                .build();
    }

    @Spawns("bat")
    public Entity newBat(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);

        return entityBuilder()
                .from(data)
                .type(BreakoutType.BAT)
                .at(getAppWidth() / 2 - 50, getAppHeight() - 70)
                .viewWithBBox(getAssetLoader().loadTexture("bat.png", 464 / 3, 102 / 3))
                .collidable()
                .with(physics)
                .with(new BatComponent())
                .build();
    }

    @Spawns("ball")
    public Entity newBall(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().restitution(1f).density(0.03f));

        var bd = new BodyDef();
        bd.setType(BodyType.DYNAMIC);
        bd.setFixedRotation(true);

        physics.setBodyDef(bd);

        var emitter = ParticleEmitters.newFireEmitter();
        emitter.setSourceImage(texture("ball.png"));
        emitter.setBlendMode(BlendMode.SRC_OVER);
        emitter.setNumParticles(1);
        emitter.setEmissionRate(1);
        emitter.setSpawnPointFunction(i -> new Point2D(0, 0));
        emitter.setScaleFunction(i -> new Point2D(-0.1, -0.1));
        emitter.setExpireFunction(i -> Duration.millis(110));
        emitter.setControl(p -> {
            ImageView view = (ImageView) p.getView();
        });

        var e = entityBuilder()
                .from(data)
                .type(BreakoutType.BALL)
                .bbox(new HitBox(BoundingShape.circle(64)))
                .view("ball.png")
                .collidable()
                .with(physics)
                .with(new ParticleComponent(emitter))
                .with(new EffectComponent())
                .with(new BallComponent())
                .scale(0.1, 0.1)
                .build();

        e.getTransformComponent().setScaleOrigin(new Point2D(0, 0));

        System.out.println(e.getX() + " " + e.getY());

        emitter.minSizeProperty().bind(e.getTransformComponent().scaleXProperty().multiply(60));
        emitter.maxSizeProperty().bind(e.getTransformComponent().scaleXProperty().multiply(60));

        return e;
    }

    @Spawns("sparks")
    public Entity newSparks(SpawnData data) {
        var emitter = ParticleEmitters.newSparkEmitter();
        emitter.setMaxEmissions(1);
        emitter.setAllowParticleRotation(true);
        emitter.setStartColor(Color.LIGHTBLUE);
        emitter.setEndColor(Color.BLUE);

        return entityBuilder()
                .from(data)
                .with(new ParticleComponent(emitter))
                .with(new ExpireCleanComponent(Duration.seconds(1.5)))
                .build();
    }

    @Spawns("powerupGrow")
    public Entity newPowerupGrow(SpawnData data) {
        var view = getUIFactory().newText("GROW", Color.WHITE, 16);
        view.setFont(getUIFactory().newFont(FontType.GAME, 16));

        return entityBuilder()
                .from(data)
                .type(BreakoutType.POWERUP)
                .viewWithBBox(view)
                .collidable()
                .with(new MoveDownComponent(400))
                .build();
    }
}
