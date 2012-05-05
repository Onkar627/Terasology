package org.terasology.componentSystem.items;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.item.UseItemEvent;
import org.terasology.events.item.UseItemInDirectionEvent;
import org.terasology.events.item.UseItemOnBlockEvent;
import org.terasology.events.item.UseItemOnEntityEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;

import javax.vecmath.Vector3f;

/**
 * TODO: Refactor use methods into events? Usage should become a separate component
 * @author Immortius <immortius@gmail.com>
 */
public class ItemSystem implements EventHandlerSystem {
    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @ReceiveEvent(components=BlockItemComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        entity.getComponent(BlockItemComponent.class).placedEntity.destroy();
    }

    @ReceiveEvent(components=ItemComponent.class)
    public void useItemOnBlock(UseItemOnBlockEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null || itemComp.usage != ItemComponent.UsageType.OnBlock) return;

        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            if (placeBlock(blockItem.blockFamily, event.getTargetBlock(), event.getSurfaceDirection(), event.getSecondaryDirection(), blockItem)) {
                checkConsumeItem(item, itemComp);
            }
        } else {
            EntityRef targetEntity = blockEntityRegistry.getOrCreateEntityAt(event.getTargetBlock());
            item.send(new ActivateEvent(targetEntity, event.getInstigator()));
            checkConsumeItem(item, itemComp);
        }
    }

    @ReceiveEvent(components=ItemComponent.class)
    public void useItem(UseItemEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null || itemComp.usage != ItemComponent.UsageType.OnUser) return;

        item.send(new ActivateEvent(event.getInstigator(), event.getInstigator()));
        checkConsumeItem(item, itemComp);
    }

    @ReceiveEvent(components = ItemComponent.class)
    public void useItemOnEntity(UseItemOnEntityEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(event.getTarget(), event.getInstigator()));
        checkConsumeItem(item, itemComp);
    }

    @ReceiveEvent(components = ItemComponent.class)
    public void useItemInDirection(UseItemInDirectionEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(event.getLocation(), event.getDirection(), event.getInstigator()));
        checkConsumeItem(item, itemComp);
    }

    private void checkConsumeItem(EntityRef item, ItemComponent itemComp) {
        if (itemComp.consumedOnUse) {
            itemComp.stackCount--;
            if (itemComp.stackCount == 0) {
                item.destroy();
            }
            else {
                item.saveComponent(itemComp);
            }
        }
    }

    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     * @return True if a block was placed
     */
    private boolean placeBlock(BlockFamily type, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection, BlockItemComponent blockItem) {
        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceDirection.getVector3i());

        Block block = type.getBlockFor(surfaceDirection, secondaryDirection);
        if (block == null)
            return false;

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            worldProvider.setBlock(placementPos.x, placementPos.y, placementPos.z, block.getId(), true, true);
            AudioManager.play(new AssetUri(AssetType.SOUND, "engine:PlaceBlock"), 0.5f);
            if (blockItem.placedEntity.exists()) {
                // Establish a block entity
                blockItem.placedEntity.addComponent(new BlockComponent(placementPos, false));
                // TODO: Get regen and wait from block config?
                blockItem.placedEntity.addComponent(new HealthComponent(type.getArchetypeBlock().getHardness(), 2.0f,1.0f));
                blockItem.placedEntity = EntityRef.NULL;
            }
            return true;
        }
        return false;
    }
    
    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPos) {
        Block centerBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z));

        if (!centerBlock.isAllowBlockAttachment()) {
            return false;
        }

        Block adjBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z));
        if (adjBlock != null && !adjBlock.isInvisible() && !adjBlock.isSelectionRayThrough()) {
            return false;
        }

        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            for (EntityRef player : entityManager.iteratorEntities(PlayerComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
                LocationComponent location = player.getComponent(LocationComponent.class);
                AABBCollisionComponent collision = player.getComponent(AABBCollisionComponent.class);
                Vector3f worldPos = location.getWorldPosition();
                for (AABB blockAABB : block.getColliders(blockPos.x, blockPos.y, blockPos.z)) {
                    if (blockAABB.overlaps(new AABB(worldPos, collision.getExtents()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
