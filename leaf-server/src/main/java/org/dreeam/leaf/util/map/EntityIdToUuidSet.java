/*
 *Licensed under: LGPL-3.0 (https://www.gnu.org/licenses/lgpl-3.0.html)
 */

package org.dreeam.leaf.util.map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

public class EntityIdToUuidSet extends AbstractSet<UUID> {
    private final it.unimi.dsi.fastutil.ints.IntOpenHashSet backing;
    private final Level level;

    public EntityIdToUuidSet(it.unimi.dsi.fastutil.ints.IntOpenHashSet backing, Level level) {
        this.backing = backing;
        this.level = level;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof UUID uuid)) return false;
        Entity entity = ((ServerLevel) level).getEntity(uuid);
        return entity != null && backing.contains(entity.getId());
    }

    @Override
    public boolean add(UUID uuid) {
        Entity entity = ((ServerLevel) level).getEntity(uuid);
        if (entity == null) return false;
        return backing.add(entity.getId());
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof UUID uuid)) return false;
        Entity entity = ((ServerLevel) level).getEntity(uuid);
        return entity != null && backing.remove(entity.getId());
    }

    @Override
    public Iterator<UUID> iterator() {
        return new Iterator<UUID>() {
            private final it.unimi.dsi.fastutil.ints.IntIterator intIterator = backing.iterator();
            private UUID nextUuid = null;
            private boolean hasPrecomputed = false;

            @Override
            public boolean hasNext() {
                if (!hasPrecomputed) {
                    computeNext();
                }
                return nextUuid != null;
            }

            @Override
            public UUID next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                UUID result = nextUuid;
                hasPrecomputed = false;
                nextUuid = null;
                return result;
            }

            @Override
            public void remove() {
                intIterator.remove();
            }

            private void computeNext() {
                while (intIterator.hasNext()) {
                    int entityId = intIterator.nextInt();
                    Entity entity = level.getEntity(entityId);
                    if (entity != null) {
                        nextUuid = entity.getUUID();
                        hasPrecomputed = true;
                        return;
                    }
                }
                nextUuid = null;
                hasPrecomputed = true;
            }
        };
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public void clear() {
        backing.clear();
    }
}
