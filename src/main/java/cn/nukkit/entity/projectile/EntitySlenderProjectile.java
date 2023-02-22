package cn.nukkit.entity.projectile;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import co.aikar.timings.Timings;

/**
 * @author PowerNukkitX Project Team
 * <a href="https://github.com/PowerNukkitX/PowerNukkitX/blob/master/src/main/java/cn/nukkit/entity/projectile/SlenderProjectile.java">powernukkitx original file</a>
 *
 * 这个抽象类代表较为细长的投射物实体(例如弓箭,三叉戟),它通过重写{@link Entity#move}方法实现这些实体较为准确的碰撞箱计算。
 * <p>
 * This abstract class represents slender projectile entities (e.g.arrow, trident), and it realized a more accurate collision box calculation for these entities by overriding the {@link Entity#move} method.
 */
public abstract class EntitySlenderProjectile extends EntityProjectile {

    public EntitySlenderProjectile(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public EntitySlenderProjectile(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    @Override
    public float getWidth() {
        return 0.1f;
    }

    @Override
    public float getHeight() {
        return 0.1f;
    }

    /*
     * 经过测试这个算法在大多数情况下效果不错。
     */
    @Override
    public boolean move(double dx, double dy, double dz) {
        if (dx == 0 && dz == 0 && dy == 0) {
            return true;
        }
        if (Timings.entityMoveTimer != null) Timings.entityMoveTimer.startTiming();

        this.ySize *= 0.4;

        double movX = dx;
        double movY = dy;
        double movZ = dz;

        AxisAlignedBB currentAABB = this.boundingBox.clone();
        Vector3 dirVector = new Vector3(dx, dy, dz).multiply(1 / (double) 15);
        boolean isCollision = false;
        for (int i = 0; i < 15; ++i) {
            AxisAlignedBB[] collisionResult = this.level.getCollisionCubes(this, currentAABB.offset(dirVector.x, dirVector.y, dirVector.z), false);
            if (collisionResult.length > 0) {
                isCollision = true;
                break;
            }
        }
        if (isCollision) {
            if (dy > 0) {
                double y1 = currentAABB.getMinY() - this.boundingBox.getMaxY();
                if (y1 < dy) {
                    dy = y1;
                }
            }
            if (dy < 0) {
                double y2 = currentAABB.getMaxY() - this.boundingBox.getMinY();
                if (y2 > dy) {
                    dy = y2;
                }
            }

            if (dx > 0) {
                double x1 = currentAABB.getMinX() - this.boundingBox.getMaxX();
                if (x1 < dx) {
                    dx = x1;
                }
            }
            if (dx < 0) {
                double x2 = currentAABB.getMaxX() - this.boundingBox.getMinX();
                if (x2 > dx) {
                    dx = x2;
                }
            }

            if (dz > 0) {
                double z1 = currentAABB.getMinZ() - this.boundingBox.getMaxZ();
                if (z1 < dz) {
                    dz = z1;
                }
            }
            if (dz < 0) {
                double z2 = currentAABB.getMaxZ() - this.boundingBox.getMinZ();
                if (z2 > dz) {
                    dz = z2;
                }
            }
        }
        this.boundingBox.offset(0, dy, 0);
        this.boundingBox.offset(dx, 0, 0);
        this.boundingBox.offset(0, 0, dz);
        this.x = (this.boundingBox.getMinX() + this.boundingBox.getMaxX()) / 2;
        this.y = this.boundingBox.getMinY() - this.ySize;
        this.z = (this.boundingBox.getMinZ() + this.boundingBox.getMaxZ()) / 2;

        this.checkChunks();

        this.checkGroundState(movX, movY, movZ, dx, dy, dz);
        this.updateFallState(this.onGround);

        if (movX != dx) {
            this.motionX = 0;
        }

        if (movY != dy) {
            this.motionY = 0;
        }

        if (movZ != dz) {
            this.motionZ = 0;
        }

        if (Timings.entityMoveTimer != null) Timings.entityMoveTimer.stopTiming();
        return true;
    }
}
