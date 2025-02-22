package cn.elytra.mod.bandit.common.mining.executor

import cn.elytra.mod.bandit.common.mining.Context
import cn.elytra.mod.bandit.common.mining.VeinMiningHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.minecraft.util.math.BlockPos
import java.util.*

class PlainVeinMiningExecutor(override val context: Context) : SimpleVeinMiningExecutor() {

	override val positions: Flow<BlockPos> = flow {
		val centerPos = context.centerPos
		val blockState = context.blockState

		val visited = HashSet<BlockPos>()
		val queue =
			PriorityQueue<BlockPos> { bp1, bp2 -> bp1.distanceSq(centerPos) compareTo bp2.distanceSq(centerPos) }
		queue.add(centerPos)

		while(true) {
			val p = queue.poll() ?: break

			if(p in visited) {
				continue
			}

			visited += p

			emit(p)

			queue += p.getNeighborsInBox()
				.filterNot { it in visited }
				.filterNot { context.world.isAirBlock(it) }
				.filter {
					val thatBlockState = context.world.getBlockState(it)
					thatBlockState == blockState ||
							VeinMiningHandler.isInAdditionalBlockStates(blockState, thatBlockState)
				}
		}
	}

	private fun BlockPos.getNeighborsInBox(): Iterable<BlockPos> {
		return BlockPos.getAllInBox(this.x - 1, this.y - 1, this.z - 1, this.x + 1, this.y + 1, this.z + 1)
	}
}