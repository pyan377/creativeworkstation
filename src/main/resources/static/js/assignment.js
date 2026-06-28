const LEGACY_TASK_TYPE_LABELS = {
    COMMERCIAL: '商单',
    DAILY: '日常',
    EVENT: '活动'
};

const PRIORITY_LABELS = {
    1: { text: '高', class: 'bg-red-100 text-red-600' },
    2: { text: '中', class: 'bg-yellow-100 text-yellow-700' },
    3: { text: '低', class: 'bg-gray-100 text-gray-500' }
};

const STATUS_COLUMNS = {
    TODO: 'column-todo',
    DOING: 'column-doing',
    REVIEW: 'column-doing',
    DONE: 'column-done'
};

const NEXT_STATUS = {
    TODO: 'DOING',
    DOING: 'REVIEW',
    REVIEW: 'DONE',
    DONE: 'TODO'
};

const ACTION_LABELS = {
    TODO: '开始处理',
    DOING: '提交审核',
    REVIEW: '标记完成',
    DONE: '重新打开'
};

let allTasks = [];
let highlightId = null;
let editingTaskId = null;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    highlightId = new URLSearchParams(window.location.search).get('highlight');
    document.getElementById('taskForm').addEventListener('submit', handleSaveTask);
    await loadProjectOptions();
    await loadTasks();
});

async function checkAuth() {
    try {
        const response = await API.checkAuth();
        if (!response.ok) {
            window.location.href = '/login.html';
            return;
        }
        const userData = await response.json();
        document.getElementById('userName').textContent = userData.username || '用户';
    } catch (err) {
        window.location.href = '/login.html';
    }
}

async function handleLogout() {
    try {
        await API.logout();
    } finally {
        window.location.href = '/login.html';
    }
}

async function loadTasks() {
    try {
        allTasks = await API.getTasks();
        renderKanban();
    } catch (err) {
        console.error('加载任务失败', err);
    }
}

function renderKanban() {
    const columns = {
        'column-todo': [],
        'column-doing': [],
        'column-done': []
    };

    allTasks.forEach(task => {
        const colId = STATUS_COLUMNS[task.status] || 'column-todo';
        columns[colId].push(task);
    });

    Object.entries(columns).forEach(([colId, tasks]) => {
        const container = document.getElementById(colId);
        container.innerHTML = tasks.length === 0
            ? '<p class="text-center text-gray-400 text-sm py-8">暂无任务</p>'
            : tasks.map(task => renderTaskCard(task)).join('');
    });

    document.getElementById('count-todo').textContent = columns['column-todo'].length;
    document.getElementById('count-doing').textContent = columns['column-doing'].length;
    document.getElementById('count-done').textContent = columns['column-done'].length;

    if (highlightId) {
        const card = document.querySelector(`[data-task-id="${highlightId}"]`);
        if (card) {
            card.classList.add('ring-2', 'ring-indigo-500', 'ring-offset-2');
            card.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }
}

function getTaskTypeLabel(taskType) {
    if (!taskType) return '任务';
    return LEGACY_TASK_TYPE_LABELS[taskType] || taskType;
}

function renderTaskCard(task) {
    const overdue = isTaskOverdue(task.deadline, task.status);
    const deadlineClass = getDeadlineTextClass(task.status, overdue);
    const priority = PRIORITY_LABELS[task.priority] || PRIORITY_LABELS[2];
    const typeLabel = getTaskTypeLabel(task.taskType);
    const deadlineText = task.deadline
        ? formatDeadline(task.deadline)
        : '未设置截止';

    return `
        <div class="task-card bg-white p-4 rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition cursor-pointer"
             data-task-id="${task.id}"
             onclick="openEditTaskModal(${task.id})">
            <div class="flex items-start justify-between gap-2 mb-2">
                <h4 class="font-bold text-sm text-gray-800 leading-snug">${escapeHtml(task.title)}</h4>
                <span class="text-xs px-2 py-0.5 rounded-full flex-shrink-0 ${priority.class}">${priority.text}</span>
            </div>
            <div class="flex flex-wrap gap-2 mb-3">
                ${task.projectId && task.projectName ? `<span class="text-xs px-2 py-0.5 rounded-full bg-violet-100 text-violet-700 flex items-center gap-1 cursor-pointer hover:bg-purple-200 transition-colors" onclick="goToProject(event, ${task.projectId})"><i class="fas fa-folder-open text-[10px]"></i>${escapeHtml(task.projectName)}</span>` : ''}
                ${task.platform ? `<span class="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600">${escapeHtml(task.platform)}</span>` : ''}
                <span class="text-xs px-2 py-0.5 rounded-full bg-purple-50 text-purple-600">${escapeHtml(typeLabel)}</span>
            </div>
            <p class="text-xs mb-3 ${deadlineClass}">
                <i class="far fa-clock mr-1"></i>${overdue ? '已超期 · ' : ''}${deadlineText}
            </p>
            <button onclick="event.stopPropagation(); advanceTask(${task.id}, '${task.status}')"
                    class="w-full text-xs py-1.5 rounded-lg border border-indigo-200 text-indigo-600 hover:bg-indigo-50 transition font-medium">
                ${ACTION_LABELS[task.status] || '切换状态'}
            </button>
        </div>
    `;
}

async function advanceTask(id, currentStatus) {
    const nextStatus = NEXT_STATUS[currentStatus] || 'DOING';
    try {
        await API.updateTask(id, { status: nextStatus });
        await loadTasks();
    } catch (err) {
        console.error('更新状态失败', err);
        alert('操作失败，请稍后重试');
    }
}

function isTaskOverdue(deadline, status) {
    if (!deadline) return false;
    if (status === 'DONE') return false;
    if (status !== 'TODO' && status !== 'DOING') return false;
    return new Date(deadline) < new Date();
}

function getDeadlineTextClass(status, overdue) {
    if (status === 'DONE') return 'text-gray-400';
    if (overdue) return 'text-red-500 font-medium';
    return 'text-gray-400';
}

function formatDeadline(deadline) {
    const date = new Date(deadline);
    return date.toLocaleString('zh-CN', {
        month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}

function toDatetimeLocalValue(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

function setTaskModalMode(mode) {
    const isEdit = mode === 'edit';
    document.getElementById('taskModalTitle').textContent = isEdit ? '编辑任务' : '添加作业任务';
    document.getElementById('taskSubmitBtn').textContent = isEdit ? '保存修改' : '创建任务';
}

function openTaskModal() {
    editingTaskId = null;
    setTaskModalMode('create');
    const modal = document.getElementById('taskModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    document.getElementById('taskForm').reset();
    document.getElementById('editingTaskId').value = '';
    document.getElementById('taskPriority').value = '2';
    loadProjectOptions().then(() => {
        document.getElementById('taskTitle').focus();
    });
}

function openEditTaskModal(taskId) {
    const task = allTasks.find(t => t.id === taskId);
    if (!task) return;

    editingTaskId = task.id;
    setTaskModalMode('edit');

    document.getElementById('editingTaskId').value = task.id;
    document.getElementById('taskTitle').value = task.title || '';
    document.getElementById('taskDescription').value = task.description || '';
    document.getElementById('taskDeadline').value = toDatetimeLocalValue(task.deadline);
    document.getElementById('taskType').value = getTaskTypeLabel(task.taskType);
    document.getElementById('taskPriority').value = String(task.priority || 2);

    loadProjectOptions().then(() => {
        document.getElementById('taskProjectId').value = task.projectId ? String(task.projectId) : '';
    });

    const modal = document.getElementById('taskModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    document.getElementById('taskTitle').focus();
}

function closeTaskModal() {
    const modal = document.getElementById('taskModal');
    modal.classList.add('hidden');
    document.body.style.overflow = '';
    document.getElementById('taskForm').reset();
    document.getElementById('editingTaskId').value = '';
    editingTaskId = null;
    setTaskModalMode('create');
}

async function loadProjectOptions() {
    const select = document.getElementById('taskProjectId');
    if (!select) return;

    const currentValue = select.value;

    try {
        const projects = await API.getProjects();
        select.innerHTML = '<option value="">不关联作品</option>' +
            projects.map(p => `<option value="${p.id}">${escapeHtml(p.title || '未命名')}</option>`).join('');
        if (currentValue) {
            select.value = currentValue;
        }
    } catch (err) {
        console.error('加载作品列表失败', err);
    }
}

function buildTaskPayload() {
    const deadlineValue = document.getElementById('taskDeadline').value;
    const taskTypeValue = document.getElementById('taskType').value.trim();

    const taskData = {
        title: document.getElementById('taskTitle').value.trim(),
        description: document.getElementById('taskDescription').value.trim() || null,
        taskType: taskTypeValue || null,
        priority: parseInt(document.getElementById('taskPriority').value, 10)
    };

    if (deadlineValue) {
        taskData.deadline = deadlineValue;
    }

    const projectIdValue = document.getElementById('taskProjectId').value;
    if (projectIdValue) {
        taskData.projectId = parseInt(projectIdValue, 10);
    }

    return taskData;
}

async function handleSaveTask(e) {
    e.preventDefault();

    const taskData = buildTaskPayload();
    const taskId = editingTaskId || document.getElementById('editingTaskId').value;

    try {
        if (taskId) {
            await API.updateTask(taskId, taskData);
            alert('任务更新成功！');
        } else {
            taskData.status = 'TODO';
            await API.createTask(taskData);
            alert('任务创建成功！');
        }
        closeTaskModal();
        await loadTasks();
    } catch (err) {
        console.error('保存任务失败', err);
        alert('保存失败，请稍后重试');
    }
}

window.goToProject = function(event, projectId) {
    event.stopPropagation();
    if (!projectId) return;
    window.location.href = `projects.html?projectId=${projectId}`;
};
