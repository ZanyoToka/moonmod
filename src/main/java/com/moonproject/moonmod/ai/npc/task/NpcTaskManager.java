package com.moonproject.moonmod.ai.npc.task;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 模块化任务调度管理器 (Task Manager)
 * 统一管理 NPC 运行时的行为状态机，支持任务切换、被打断恢复与自主调度。
 */
public class NpcTaskManager {
    private static final Logger LOGGER = LogManager.getLogger(NpcTaskManager.class);
    private static final Map<String, INpcTask> REGISTERED_TASKS = new LinkedHashMap<>();

    static {
        register(new IdleTask());
        register(new GuardTask());
        register(new HarvestTask());
        register(new FollowTask());
    }

    private final AiPlayerNpcEntity npc;
    private INpcTask currentTask;

    public NpcTaskManager(AiPlayerNpcEntity npc) {
        this.npc = npc;
        this.currentTask = getTask("idle");
    }

    public static void register(INpcTask task) {
        if (task != null) {
            REGISTERED_TASKS.put(task.getTaskId().toLowerCase(), task);
        }
    }

    public static INpcTask getTask(String taskId) {
        return REGISTERED_TASKS.getOrDefault(taskId.toLowerCase(), REGISTERED_TASKS.get("idle"));
    }

    public static Collection<INpcTask> getAllTasks() {
        return Collections.unmodifiableCollection(REGISTERED_TASKS.values());
    }

    public INpcTask getCurrentTask() {
        return currentTask != null ? currentTask : getTask("idle");
    }

    public void switchTask(String taskId) {
        INpcTask next = getTask(taskId);
        if (next == null || next == currentTask) return;

        if (currentTask != null) {
            currentTask.onStop(npc);
        }
        currentTask = next;
        currentTask.onStart(npc);
        npc.setTaskMode(currentTask.getTaskId());
        LOGGER.info("[AI NPC Task] NPC [{}] 切换行为模式为: {} ({})", npc.getScoreboardName(), currentTask.getDisplayName(), currentTask.getTaskId());
    }

    public void tick() {
        if (npc == null || !npc.isAlive() || npc.isConversing()) {
            return;
        }

        if (currentTask == null) {
            currentTask = getTask("idle");
            currentTask.onStart(npc);
        }

        if (currentTask.canExecute(npc)) {
            currentTask.onTick(npc);
            if (currentTask.isFinished(npc)) {
                switchTask("idle");
            }
        } else {
            switchTask("idle");
        }
    }
}
