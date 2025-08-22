package org.dreeam.leaf.util.cache;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class TargetableEntityCache {

    private final Map<Class<?>, Set<LivingEntity>> entitiesByClass = new Object2ObjectOpenHashMap<>();
    private final Set<Class<?>> targetableClasses = new ObjectOpenHashSet<>();
    private final Map<Class<?>, SpatialIndex> spatialIndices = new Object2ObjectOpenHashMap<>();

    public void registerTargetableClass(Class<?> clazz) {
        if (targetableClasses.add(clazz)) {
            entitiesByClass.put(clazz, new ObjectOpenHashSet<>());
            spatialIndices.put(clazz, new SpatialIndex());
        }
    }

    public void addEntity(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) return;

        // Add to all applicable class caches
        for (Class<?> targetClass : targetableClasses) {
            if (targetClass.isInstance(entity)) {
                entitiesByClass.get(targetClass).add(entity);
                spatialIndices.get(targetClass).add(entity);
            }
        }
    }

    public void removeEntity(LivingEntity entity) {
        if (entity == null) return;

        // Remove from all applicable class caches
        for (Class<?> targetClass : targetableClasses) {
            if (targetClass.isInstance(entity)) {
                Set<LivingEntity> entities = entitiesByClass.get(targetClass);
                if (entities != null) {
                    entities.remove(entity);
                }
                SpatialIndex index = spatialIndices.get(targetClass);
                if (index != null) {
                    index.remove(entity);
                }
            }
        }
    }

    public <T extends LivingEntity> List<T> getEntitiesInRange(Class<T> targetClass, AABB searchBox) {
        SpatialIndex index = spatialIndices.get(targetClass);
        if (index == null) {
            return Collections.emptyList();
        }
        return (List<T>) index.getInRange(searchBox);
    }

    // Simple spatial index for range queries
    private static class SpatialIndex {
        private final Set<LivingEntity> entities = new ObjectOpenHashSet<>();

        public void add(LivingEntity entity) {
            entities.add(entity);
        }

        public void remove(LivingEntity entity) {
            entities.remove(entity);
        }

        public List<LivingEntity> getInRange(AABB box) {
            List<LivingEntity> result = new ObjectArrayList<>();
            for (LivingEntity entity : entities) {
                if (!entity.isRemoved() && entity.getBoundingBox().intersects(box)) {
                    result.add(entity);
                }
            }
            return result;
        }
    }
}
