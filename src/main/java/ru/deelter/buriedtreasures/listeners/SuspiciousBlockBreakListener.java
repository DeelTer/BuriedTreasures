package ru.deelter.suspiciousgraves.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.data.Brushable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ru.deelter.suspiciousgraves.SuspiciousGraves;
import ru.deelter.suspiciousgraves.utils.RandomUtils;

import java.util.Objects;

public class SuspiciousBlockBreakListener implements Listener {

	private static final NamespacedKey BLOCK_ITEM_KEY = Objects.requireNonNull(NamespacedKey.fromString("suspicious:loot"));
	private final double dropBaseChance;
	private final int dropMaxTicksFalling;

	public SuspiciousBlockBreakListener(@NotNull SuspiciousGraves instance) {
		FileConfiguration config = instance.getConfig();
		dropBaseChance = config.getDouble("drops.suspicious-chance");
		dropMaxTicksFalling = config.getInt("drops.max-ticks-falling");
	}

	private double getDropChance(float ticksLived) {
		return Math.max(0, dropBaseChance * (dropMaxTicksFalling - (ticksLived - 7)) / dropMaxTicksFalling);
	}

	@EventHandler
	public void onStartFalling(@NotNull EntityChangeBlockEvent event) {
		if (!(event.getBlock().getState() instanceof BrushableBlock brushableBlock))
			return;

		ItemStack item = brushableBlock.getItem();
		if (item == null)
			return;
		if (item.getType().isAir())
			return;

		if (!(event.getEntity() instanceof FallingBlock fallingBlock))
			return;

		PersistentDataContainer container = fallingBlock.getPersistentDataContainer();
		container.set(BLOCK_ITEM_KEY, PersistentDataType.BYTE_ARRAY, item.serializeAsBytes());
	}

	@EventHandler
	public void onFall(@NotNull EntityRemoveFromWorldEvent event) {
		if (!(event.getEntity() instanceof FallingBlock fallingBlock))
			return;
		if (!(fallingBlock.getBlockData() instanceof Brushable))
			return;
		if (RandomUtils.RANDOM.nextDouble() > getDropChance(fallingBlock.getTicksLived()))
			return;

		PersistentDataContainer container = fallingBlock.getPersistentDataContainer();
		if (!container.has(BLOCK_ITEM_KEY))
			return;

		byte[] itemBytes = container.get(BLOCK_ITEM_KEY, PersistentDataType.BYTE_ARRAY);
		ItemStack item = ItemStack.deserializeBytes(itemBytes);

		fallingBlock.getWorld().dropItemNaturally(
				fallingBlock.getLocation().add(0.5, 0.5, 0.5),
				item
		);
	}
}
