package ca.ckgames.ckprison.mines;

import ca.ckgames.ckprison.ranks.Rank;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.List;

public class Mine {
    public String name;
    public final BoundingBox bounds;
    public final List<Material> materials;
    public final List<Double> weights;
    public Rank requiredRank;
    private double totalWeight;
    public Block[] blocks;


    public Mine(String name, World world, BoundingBox bounds, List<Material> materials, List<Double> weights, Rank requiredRank) {
        this.name = name;
        this.bounds = bounds;
        this.materials = materials;
        this.weights = weights;
        this.requiredRank = requiredRank;

        totalWeight = 0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        int width = (int) (bounds.getMaxX() - bounds.getMinX() + 1);
        int height = (int) (bounds.getMaxY() - bounds.getMinY() + 1);
        int depth = (int) (bounds.getMaxZ() - bounds.getMinZ() + 1);
        blocks = new Block[width * height * depth];
        for (double x = bounds.getMinX(); x <= bounds.getMaxX(); x += 1) {
            for (double y = bounds.getMinY(); y <= bounds.getMaxY(); y += 1) {
                for (double z = bounds.getMinZ(); z <= bounds.getMaxZ(); z += 1) {
                    Block block = world.getBlockAt((int) x, (int) y, (int) z);
                    int index = (int) ((z - bounds.getMinZ()) * width * height
                            + (y - bounds.getMinY()) * width
                            + (x - bounds.getMinX()));
                    blocks[index] = block;
                }
            }
        }
    }

    public void resetMine() {
        for (Block block : blocks) {
            block.setType(getRandomMaterial());
        }
    }

    public Material getRandomMaterial() {
        int i = 0;
        for (double rand = Math.random() * totalWeight; i < weights.size() - 1; i += 1) {
            rand -= weights.get(i);
            if (rand <= 0.0) break;
        }
        return materials.get(i);
    }

    public boolean containsBlock(Block block) {
        return bounds.getMinX() <= block.getX()
                && bounds.getMaxX() >= block.getX()
                && bounds.getMinY() <= block.getY()
                && bounds.getMaxY() >= block.getY()
                && bounds.getMinZ() <= block.getZ()
                && bounds.getMaxZ() >= block.getZ();
    }
}
