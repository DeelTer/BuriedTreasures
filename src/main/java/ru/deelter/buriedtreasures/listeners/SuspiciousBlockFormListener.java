package ru.deelter.suspiciousgraves.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrushableBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.deelter.suspiciousgraves.SuspiciousGraves;
import ru.deelter.suspiciousgraves.utils.RandomUtils;

import java.util.*;

public class SuspiciousBlockFormListener implements Listener {

	private final Map<Material, Material> blockReplacements = new HashMap<>();
	private final List<BlockFace> blockFaces = new ArrayList<>();
	private final List<Material> materials = new ArrayList<>();
	private boolean filterEnabled = false;
	private boolean whitelistMode = false;
	private final int maxY;

	public SuspiciousBlockFormListener(@NotNull SuspiciousGraves instance) {
		FileConfiguration config = instance.getConfig();
		config.getStringList("replacement.block-faces").forEach(face -> blockFaces.add(BlockFace.valueOf(face)));
		maxY = config.getInt("replacement.max-y");

		ConfigurationSection replacementSection = config.getConfigurationSection("replacement.blocks");
		if (replacementSection == null)
			return;

		replacementSection.getKeys(false).forEach(key -> blockReplacements.put(
				Material.getMaterial(key),
				Material.getMaterial(Objects.requireNonNull(replacementSection.getString(key)))
		));

		filterEnabled = config.getBoolean("items.filter-enabled");
		if (filterEnabled) {
			whitelistMode = config.getString("items.mode", "BLACKLIST").equalsIgnoreCase("WHITELIST");
			for (String materialId : config.getStringList("items.materials")) {
				try {
					materials.add(Material.valueOf(materialId.toUpperCase()));
				} catch (Exception e) {
					instance.getLogger().warning("No material with id " + materialId);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onDespawnItem(@NotNull ItemDespawnEvent event) {
		Item entity = event.getEntity();
		if (entity.isVisualFire())
			return;

		ItemStack item = entity.getItemStack();
		Material material = item.getType();

		if (filterEnabled) {
			if (whitelistMode && !materials.contains(material)) {
				return;
			} else if (materials.contains(material)) {
				return;
			}
		}

		Location floorLocation = entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();

		List<Block> blocks = new ArrayList<>();

		for (int i = 0; i < maxY; i++) {
			if (!blocks.isEmpty())
				break;
			for (BlockFace blockFace : blockFaces) {
				Block block = floorLocation.getBlock().getRelative(blockFace);
				if (!isBlockValid(block))
					continue;
				blocks.add(block);
			}
			floorLocation.subtract(0, 1, 0);
		}
		if (blocks.isEmpty())
			return;

		Block block = blocks.get(RandomUtils.RANDOM.nextInt(blocks.size()));
		block.setType(blockReplacements.get(block.getType()));

		BrushableBlock brushableBlock = (BrushableBlock) block.getState();
		brushableBlock.setItem(item);
		brushableBlock.update();
	}

	private boolean isBlockValid(@NotNull Block block) {
		return blockReplacements.containsKey(block.getType()) && block.getRelative(BlockFace.DOWN).getType().isSolid();
	}

}
