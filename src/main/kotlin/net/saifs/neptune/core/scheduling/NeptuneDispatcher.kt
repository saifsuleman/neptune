package net.saifs.neptune.core.scheduling

import kotlinx.coroutines.*
import kotlinx.coroutines.Runnable
import net.saifs.neptune.Neptune
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import kotlin.coroutines.CoroutineContext

private val bukkitScheduler = Bukkit.getScheduler()

@OptIn(InternalCoroutinesApi::class)
class NeptuneDispatcher(private val async: Boolean = false): CoroutineDispatcher(), Delay {
    private fun runTaskLater(delay: Long, runnable: () -> Unit): BukkitTask {
        return if (async) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(Neptune.instance, Runnable { runnable() }, delay)
        } else {
            Bukkit.getScheduler().runTaskLater(Neptune.instance, Runnable { runnable() }, delay)
        }
    }

    private fun runTask(runnable: () -> Unit): BukkitTask {
        return if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(Neptune.instance, Runnable(runnable))
        } else {
            Bukkit.getScheduler().runTask(Neptune.instance, Runnable(runnable))
        }
    }

    @ExperimentalCoroutinesApi
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val task = runTaskLater(timeMillis / 50) {
            continuation.apply { resumeUndispatched(Unit) }
        }
        continuation.invokeOnCancellation { task.cancel() }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive) {
            return
        }

        if (!async && Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            runTask { block.run() }
        }
    }
}