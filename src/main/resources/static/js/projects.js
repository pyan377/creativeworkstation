let currentProject = null;
let selectedNewAssets = [];
let selectedAssets = new Set();
let currentAssets = [];
let currentUser = null;
let selectedLibraryAssetIds = [];
let libraryAssets = [];

const LIBRARY_IMAGE_TYPES = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'];
const LIBRARY_VIDEO_TYPES = ['mp4', 'avi', 'mov', 'wmv', 'webm', 'mkv'];

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    loadProjects();
    initEventListeners();
    checkUrlForProject();
});

async function checkAuth() {
    try {
        const response = await API.checkAuth();
        if (!response.ok) {
            window.location.href = '/login.html';
            return;
        }
        const userData = await response.json();
        currentUser = userData;
        document.getElementById('userName').textContent = userData.username || '用户';
    } catch (err) {
        console.error('认证检查失败', err);
        window.location.href = '/login.html';
    }
}

async function handleLogout() {
    try {
        await API.logout();
        window.location.href = '/login.html';
    } catch (err) {
        console.error('退出失败', err);
        window.location.href = '/login.html';
    }
}

function checkUrlForProject() {
    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get('projectId') || urlParams.get('openProject');
    if (projectId) {
        setTimeout(() => {
            openProjectDetail(parseInt(projectId, 10));
            window.history.replaceState({}, document.title, window.location.pathname);
        }, 500);
    }
}

function initEventListeners() {
    document.getElementById('filterCategory').addEventListener('change', loadProjects);
    document.getElementById('filterStatus').addEventListener('change', loadProjects);
    document.getElementById('projectForm').addEventListener('submit', handleProjectSubmit);
    document.getElementById('addAssetForm').addEventListener('submit', handleAddAsset);
    document.getElementById('addTaskForm').addEventListener('submit', handleAddRelatedTask);
    initAssetDropzone();
}

async function loadProjects() {
    const category = document.getElementById('filterCategory').value;
    const status = document.getElementById('filterStatus').value;

    try {
        const projects = await API.getProjects(category || null, status || null);
        renderProjects(projects);
    } catch (err) {
        console.error('加载项目失败', err);
        renderProjects([]);
    }
}

function renderProjects(projects) {
    const container = document.getElementById('projectList');
    const emptyState = document.getElementById('emptyState');

    if (!projects || projects.length === 0) {
        container.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    container.innerHTML = projects.map(project => `
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition cursor-pointer overflow-hidden" onclick="openProjectDetail(${project.id})">
            <div class="h-40 bg-gradient-to-br from-indigo-50 to-purple-50 flex items-center justify-center">
                <i class="fas fa-image text-5xl text-indigo-300"></i>
            </div>
            <div class="p-5">
                <h3 class="font-bold text-lg mb-2">${project.title || '未命名'}</h3>
                <div class="flex items-center gap-2 text-sm text-gray-500 mb-3">
                    <span class="px-2 py-1 bg-gray-100 rounded-full">${project.category || '未分类'}</span>
                    <span class="px-2 py-1 rounded-full ${getStatusClass(project.status)}">${project.status || '草稿'}</span>
                </div>
                <p class="text-sm text-gray-500 line-clamp-2">${project.description || '暂无描述'}</p>
            </div>
        </div>
    `).join('');
}

function getStatusClass(status) {
    const classes = {
        '草稿': 'bg-gray-100 text-gray-600',
        '进行中': 'bg-blue-100 text-blue-600',
        '已完成': 'bg-green-100 text-green-600',
        '归档': 'bg-yellow-100 text-yellow-600'
    };
    return classes[status] || 'bg-gray-100 text-gray-600';
}

async function openProjectDetail(id) {
    try {
        const project = await API.getProject(id);
        const assets = await API.getProjectAssets(id);
        const prompts = await API.getProjectPrompts(id);
        currentProject = project;
        currentAssets = assets || [];
        selectedAssets.clear();
        updateSelectedCount();

        document.getElementById('detailTitle').textContent = project.title;
        document.getElementById('detailCategory').textContent = project.category || '未分类';
        document.getElementById('detailStatus').textContent = project.status || '草稿';
        document.getElementById('detailDesc').textContent = project.description || '暂无描述';

        renderAssets(currentAssets);
        renderPrompts(prompts || []);
        await loadTasksForProject(id);
        document.getElementById('detailModal').classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    } catch (err) {
        console.error('加载项目详情失败', err);
        alert('加载项目详情失败');
    }
}

function closeDetailModal() {
    document.getElementById('detailModal').classList.add('hidden');
    document.body.style.overflow = '';
    currentProject = null;
    currentAssets = [];
    selectedAssets.clear();
    document.getElementById('relatedTasks').innerHTML = '';
    document.getElementById('relatedTaskCount').textContent = '0 个';
}

const TASK_STATUS_LABELS = {
    TODO: { text: '待办', class: 'bg-gray-100 text-gray-600' },
    DOING: { text: '进行中', class: 'bg-indigo-100 text-indigo-600' },
    REVIEW: { text: '审核', class: 'bg-amber-100 text-amber-700' },
    DONE: { text: '完成', class: 'bg-green-100 text-green-600' }
};

async function loadTasksForProject(projectId) {
    const container = document.getElementById('relatedTasks');
    const countEl = document.getElementById('relatedTaskCount');

    try {
        const tasks = await API.getTasksByProject(projectId);
        countEl.textContent = `${tasks.length} 个`;
        renderRelatedTasks(tasks);
    } catch (err) {
        console.error('加载关联任务失败', err);
        countEl.textContent = '0 个';
        container.innerHTML = '<p class="text-red-400 text-sm text-center py-4">加载关联任务失败</p>';
    }
}

function renderRelatedTasks(tasks) {
    const container = document.getElementById('relatedTasks');

    if (!tasks || tasks.length === 0) {
        container.innerHTML = `
            <div class="text-center py-6 text-gray-400">
                <i class="fas fa-tasks text-3xl mb-2"></i>
                <p class="text-sm">暂无关联任务</p>
            </div>
        `;
        return;
    }

    container.innerHTML = tasks.map(task => {
        const status = TASK_STATUS_LABELS[task.status] || TASK_STATUS_LABELS.TODO;
        const deadlineText = task.deadline
            ? new Date(task.deadline).toLocaleString('zh-CN', {
                month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'
            })
            : '未设置截止';

        return `
            <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition">
                <div class="flex-1 min-w-0 mr-4">
                    <p class="font-medium text-gray-800 truncate">${escapeHtml(task.title)}</p>
                    <p class="text-xs text-gray-400 mt-1">
                        <i class="far fa-clock mr-1"></i>${deadlineText}
                    </p>
                </div>
                <span class="text-xs px-2.5 py-1 rounded-full flex-shrink-0 ${status.class}">${status.text}</span>
            </div>
        `;
    }).join('');
}

function openAddRelatedTaskModal() {
    if (!currentProject) return;

    document.getElementById('relatedTaskProjectId').value = currentProject.id;
    document.getElementById('relatedTaskProjectName').textContent = currentProject.title || '未命名';
    document.getElementById('addTaskForm').reset();
    document.getElementById('relatedTaskPriority').value = '2';
    document.getElementById('addTaskModal').classList.remove('hidden');
    document.getElementById('relatedTaskTitle').focus();
}

function closeAddRelatedTaskModal() {
    document.getElementById('addTaskModal').classList.add('hidden');
    document.getElementById('addTaskForm').reset();
}

async function handleAddRelatedTask(e) {
    e.preventDefault();

    if (!currentProject) {
        alert('请先打开作品详情');
        return;
    }

    const deadlineValue = document.getElementById('relatedTaskDeadline').value;
    const taskTypeValue = document.getElementById('relatedTaskType').value.trim();

    const taskData = {
        title: document.getElementById('relatedTaskTitle').value.trim(),
        description: document.getElementById('relatedTaskDescription').value.trim() || null,
        taskType: taskTypeValue || null,
        priority: parseInt(document.getElementById('relatedTaskPriority').value, 10),
        projectId: currentProject.id,
        status: 'TODO'
    };

    if (deadlineValue) {
        taskData.deadline = deadlineValue;
    }

    try {
        await API.createTask(taskData);
        alert('关联任务创建成功！');
        closeAddRelatedTaskModal();
        await loadTasksForProject(currentProject.id);
    } catch (err) {
        console.error('创建关联任务失败', err);
        alert('创建失败，请稍后重试');
    }
}

function renderAssets(assets) {
    const container = document.getElementById('detailAssets');
    if (!assets || assets.length === 0) {
        container.innerHTML = '<p class="text-gray-400 col-span-full text-center py-4">暂无素材</p>';
        return;
    }

    container.innerHTML = assets.map(asset => {
        const isSelected = selectedAssets.has(asset.id);
        const fileType = asset.fileType?.toLowerCase();
        const isImage = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(fileType);
        const isVideo = ['mp4', 'avi', 'mov', 'wmv'].includes(fileType);
        
        let previewContent = '';
        
        if (isImage) {
            previewContent = `<img src="/api/assets/${asset.id}/preview" class="h-48 w-full object-cover" onerror="this.style.display='none'; this.parentElement.innerHTML='<div class=\\'h-48 flex items-center justify-center\\'><i class=\\'fas fa-image text-3xl text-indigo-400\\'></i></div>'">`;
        } else if (isVideo) {
            previewContent = `<video src="/api/assets/${asset.id}/preview" class="h-48 w-full object-cover" controls></video>`;
        } else if (fileType === 'pdf') {
            previewContent = `<div class="h-48 flex items-center justify-center bg-red-50"><i class="fas fa-file-pdf text-5xl text-red-500"></i></div>`;
        } else {
            previewContent = `<div class="h-48 flex items-center justify-center bg-gray-50"><i class="fas fa-file text-3xl text-gray-400"></i></div>`;
        }

        return `
            <div class="border border-gray-200 rounded-lg overflow-hidden group relative bg-white ${isSelected ? 'ring-2 ring-indigo-500' : ''}" data-asset-id="${asset.id}">
                <input type="checkbox" 
                    class="absolute top-3 left-3 w-5 h-5 cursor-pointer z-10" 
                    ${isSelected ? 'checked' : ''} 
                    onchange="toggleAssetSelect(${asset.id})">
                <div class="cursor-pointer">
                    ${previewContent}
                </div>
                <div class="p-3">
                    <p class="text-sm font-medium truncate">${asset.fileName}</p>
                    <p class="text-xs text-gray-400">${formatFileSize(asset.fileSize)}</p>
                </div>
                <button onclick="event.stopPropagation(); deleteAsset(${asset.id})" class="absolute top-3 right-3 w-6 h-6 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition flex items-center justify-center z-10">
                    <i class="fas fa-times text-xs"></i>
                </button>
                <button onclick="event.stopPropagation(); downloadSingleAsset(${asset.id})" class="absolute bottom-3 right-3 w-8 h-8 bg-blue-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition flex items-center justify-center z-10">
                    <i class="fas fa-download text-xs"></i>
                </button>
            </div>
        `;
    }).join('');
}

function toggleAssetSelect(assetId) {
    if (selectedAssets.has(assetId)) {
        selectedAssets.delete(assetId);
    } else {
        selectedAssets.add(assetId);
    }
    renderAssets(currentAssets);
    updateSelectedCount();
}

function toggleSelectAll() {
    if (selectedAssets.size === currentAssets.length) {
        selectedAssets.clear();
    } else {
        currentAssets.forEach(asset => selectedAssets.add(asset.id));
    }
    renderAssets(currentAssets);
    updateSelectedCount();
}

function updateSelectedCount() {
    const count = document.getElementById('selectedCount');
    const exportBtn = document.getElementById('exportSelectedBtn');
    
    if (selectedAssets.size > 0) {
        count.textContent = `已选择 ${selectedAssets.size} 个`;
        count.classList.remove('hidden');
        exportBtn.classList.remove('hidden');
    } else {
        count.classList.add('hidden');
        exportBtn.classList.add('hidden');
    }
}

function downloadSingleAsset(assetId) {
    window.open(`/api/assets/${assetId}/preview`, '_blank');
}

async function exportSelectedAssets() {
    if (selectedAssets.size === 0) {
        alert('请先选择要导出的素材');
        return;
    }
    
    const assetIds = Array.from(selectedAssets);
    const url = `/api/assets/export?ids=${assetIds.join(',')}`;
    window.open(url, '_blank');
}

function formatFileSize(bytes) {
    if (!bytes) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function openCreateModal() {
    document.getElementById('modalTitle').textContent = '新建作品';
    document.getElementById('projectId').value = '';
    document.getElementById('projectName').value = '';
    document.getElementById('projectType').value = '平面设计';
    document.getElementById('projectDesc').value = '';
    document.getElementById('projectTags').value = '';
    document.querySelector('input[name="status"][value="草稿"]').checked = true;
    document.getElementById('createModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closeCreateModal() {
    document.getElementById('createModal').classList.add('hidden');
    document.body.style.overflow = '';
}

async function handleProjectSubmit(e) {
    e.preventDefault();

    const id = document.getElementById('projectId').value;
    const data = {
        title: document.getElementById('projectName').value,
        category: document.getElementById('projectType').value,
        status: document.querySelector('input[name="status"]:checked').value,
        description: document.getElementById('projectDesc').value,
        tags: document.getElementById('projectTags').value
    };

    try {
        if (id) {
            await API.updateProject(id, data);
            alert('项目更新成功！');
        } else {
            await API.createProject(data);
            alert('项目创建成功！');
        }
        closeCreateModal();
        loadProjects();
    } catch (err) {
        console.error('保存失败', err);
        alert('保存失败: ' + err.message);
    }
}

function editProject() {
    if (!currentProject) return;

    document.getElementById('modalTitle').textContent = '编辑作品';
    document.getElementById('projectId').value = currentProject.id;
    document.getElementById('projectName').value = currentProject.title || '';
    document.getElementById('projectType').value = currentProject.category || '平面设计';
    document.getElementById('projectDesc').value = currentProject.description || '';
    document.getElementById('projectTags').value = currentProject.tags || '';
    const statusInput = document.querySelector(`input[name="status"][value="${currentProject.status || '草稿'}"]`);
    if (statusInput) statusInput.checked = true;

    closeDetailModal();
    document.getElementById('createModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

async function deleteProject() {
    if (!currentProject || !confirm('确定要删除这个项目吗？')) return;

    try {
        await API.deleteProject(currentProject.id);
        alert('项目删除成功！');
        closeDetailModal();
        loadProjects();
    } catch (err) {
        console.error('删除失败', err);
        alert('删除失败');
    }
}

function openAddAssetModal() {
    selectedNewAssets = [];
    document.getElementById('newAssetFileList').innerHTML = '';
    document.getElementById('addAssetModal').classList.remove('hidden');
}

async function openPickFromLibraryModal() {
    if (!currentProject) {
        alert('请先打开作品详情');
        return;
    }

    selectedLibraryAssetIds = [];
    updateLibraryPickCount();
    document.getElementById('pickLibraryModal').classList.remove('hidden');

    try {
        libraryAssets = await API.getAssets(null, false);
        renderLibraryAssetGrid();
    } catch (err) {
        console.error('加载素材库失败', err);
        libraryAssets = [];
        renderLibraryAssetGrid();
    }
}

function closePickFromLibraryModal() {
    document.getElementById('pickLibraryModal').classList.add('hidden');
    selectedLibraryAssetIds = [];
    libraryAssets = [];
}

function renderLibraryAssetGrid() {
    const grid = document.getElementById('libraryAssetGrid');
    const emptyState = document.getElementById('libraryEmptyState');

    if (!libraryAssets || libraryAssets.length === 0) {
        grid.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    grid.innerHTML = libraryAssets.map(asset => {
        const isSelected = selectedLibraryAssetIds.includes(asset.id);
        const preview = getLibraryAssetPreview(asset);
        return `
            <div class="relative bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 ${isSelected ? 'ring-2 ring-purple-500 ring-offset-1' : ''}">
                <input type="checkbox"
                       class="absolute top-2 left-2 w-5 h-5 cursor-pointer z-10 accent-purple-600"
                       ${isSelected ? 'checked' : ''}
                       onchange="toggleLibraryAssetSelection(${asset.id}, this.checked)">
                <div class="aspect-square bg-gray-50 flex items-center justify-center overflow-hidden">
                    ${preview}
                </div>
                <div class="p-2 border-t border-gray-100">
                    <p class="text-xs font-medium text-gray-800 truncate" title="${escapeHtml(asset.fileName)}">${escapeHtml(asset.fileName || '未命名')}</p>
                </div>
            </div>
        `;
    }).join('');
}

function getLibraryAssetPreview(asset) {
    const fileType = (asset.fileType || '').toLowerCase();
    if (LIBRARY_IMAGE_TYPES.includes(fileType)) {
        const src = asset.fileUrl || `/api/assets/${asset.id}/preview`;
        return `<img src="${src}" alt="${escapeHtml(asset.fileName)}" class="w-full h-full object-cover" onerror="this.onerror=null;this.src='/api/assets/${asset.id}/preview'">`;
    }
    if (LIBRARY_VIDEO_TYPES.includes(fileType)) {
        return `<div class="flex flex-col items-center justify-center text-blue-500"><i class="fas fa-film text-3xl mb-1"></i><span class="text-[10px] text-gray-400">视频</span></div>`;
    }
    return `<div class="flex flex-col items-center justify-center text-gray-400"><i class="fas fa-file text-3xl mb-1"></i><span class="text-[10px]">文件</span></div>`;
}

function toggleLibraryAssetSelection(assetId, checked) {
    if (checked) {
        if (!selectedLibraryAssetIds.includes(assetId)) {
            selectedLibraryAssetIds.push(assetId);
        }
    } else {
        selectedLibraryAssetIds = selectedLibraryAssetIds.filter(id => id !== assetId);
    }
    updateLibraryPickCount();
    renderLibraryAssetGrid();
}

function updateLibraryPickCount() {
    document.getElementById('libraryPickCount').textContent = selectedLibraryAssetIds.length;
    document.getElementById('confirmLibraryPickBtn').disabled = selectedLibraryAssetIds.length === 0;
}

async function confirmImportFromLibrary() {
    if (!currentProject || selectedLibraryAssetIds.length === 0) return;

    const btn = document.getElementById('confirmLibraryPickBtn');
    btn.disabled = true;

    try {
        await API.batchAssignAssets(selectedLibraryAssetIds, currentProject.id);
        alert(`已成功导入 ${selectedLibraryAssetIds.length} 个素材！`);
        closePickFromLibraryModal();

        const assets = await API.getProjectAssets(currentProject.id);
        currentAssets = assets || [];
        renderAssets(currentAssets);
    } catch (err) {
        console.error('从素材库导入失败', err);
        alert('导入失败，请稍后重试');
    } finally {
        btn.disabled = selectedLibraryAssetIds.length === 0;
    }
}

function closeAddAssetModal() {
    document.getElementById('addAssetModal').classList.add('hidden');
}

function initAssetDropzone() {
    const dropzone = document.getElementById('assetDropzone');
    const fileInput = document.getElementById('newAssetFile');

    if (!dropzone || !fileInput) return;

    dropzone.addEventListener('click', () => fileInput.click());

    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('border-indigo-400');
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('border-indigo-400');
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('border-indigo-400');
        handleAssetFiles(e.dataTransfer.files);
    });

    fileInput.addEventListener('change', (e) => {
        handleAssetFiles(e.target.files);
    });
}

function handleAssetFiles(files) {
    Array.from(files).forEach(file => {
        if (!selectedNewAssets.find(f => f.name === file.name && f.size === file.size)) {
            selectedNewAssets.push(file);
        }
    });
    renderNewAssetList();
}

function renderNewAssetList() {
    const container = document.getElementById('newAssetFileList');
    container.innerHTML = selectedNewAssets.map((file, index) => `
        <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div class="flex items-center gap-3">
                <i class="fas fa-file text-gray-400"></i>
                <div>
                    <p class="text-sm font-medium">${file.name}</p>
                    <p class="text-xs text-gray-400">${formatFileSize(file.size)}</p>
                </div>
            </div>
            <button type="button" onclick="removeNewAsset(${index})" class="text-red-500 hover:text-red-700">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `).join('');
}

function removeNewAsset(index) {
    selectedNewAssets.splice(index, 1);
    renderNewAssetList();
}

async function handleAddAsset(e) {
    e.preventDefault();

    if (selectedNewAssets.length === 0) {
        alert('请先选择文件');
        return;
    }

    try {
        for (const file of selectedNewAssets) {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('projectId', currentProject.id);
            await API.uploadAsset(formData);
        }

        alert('上传成功！');
        closeAddAssetModal();

        const assets = await API.getProjectAssets(currentProject.id);
        currentAssets = assets || [];
        renderAssets(currentAssets);
    } catch (err) {
        console.error('上传失败', err);
        alert('上传失败');
    }
}

async function deleteAsset(id) {
    if (!confirm('确定要删除这个素材吗？')) return;

    try {
        await API.deleteAsset(id);
        selectedAssets.delete(id);
        const assets = await API.getProjectAssets(currentProject.id);
        currentAssets = assets || [];
        renderAssets(currentAssets);
        updateSelectedCount();
    } catch (err) {
        console.error('删除失败', err);
        alert('删除失败');
    }
}

// ========== 提示词功能 ==========
function renderPrompts(prompts) {
    const container = document.getElementById('promptList');
    const countElement = document.getElementById('promptCount');
    
    countElement.textContent = `${prompts.length} 条`;
    
    if (prompts.length === 0) {
        container.innerHTML = `
            <div class="text-center py-6 text-gray-400">
                <i class="fas fa-lightbulb text-3xl mb-2"></i>
                <p>暂无提示词</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = prompts.map(prompt => {
        const date = prompt.createdAt ? new Date(prompt.createdAt).toLocaleDateString('zh-CN') : '';
        const rating = prompt.rating || 3;
        return `
            <div class="bg-gray-50 rounded-lg p-4 relative group">
                <div class="flex justify-between items-start mb-2">
                    <div class="flex-1">
                        <div class="flex items-center gap-2 mb-2">
                            ${renderStars(rating, prompt.id)}
                        </div>
                        ${prompt.tags ? `
                            <div class="flex flex-wrap gap-1 mb-2">
                                ${prompt.tags.split(',').map(tag => 
                                    `<span class="text-xs px-2 py-0.5 bg-purple-100 text-purple-600 rounded-full">${tag.trim()}</span>`
                                ).join('')}
                            </div>
                        ` : ''}
                        <pre class="text-sm text-gray-700 whitespace-pre-wrap max-h-32 overflow-y-auto font-sans">${escapeHtml(prompt.content)}</pre>
                    </div>
                    <button onclick="deletePrompt(${prompt.id})" class="ml-2 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition flex-shrink-0">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
                ${date ? `<p class="text-xs text-gray-400 mt-2">${date}</p>` : ''}
            </div>
        `;
    }).join('');
}

function renderStars(rating, promptId) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        const isFilled = i <= rating;
        stars += `
            <button 
                onclick="updatePromptRating(${promptId}, ${i})" 
                class="text-lg transition hover:scale-110 ${isFilled ? 'text-yellow-400' : 'text-gray-300'}"
                title="${i}星"
            >
                <i class="fas fa-star"></i>
            </button>
        `;
    }
    return stars;
}

async function updatePromptRating(promptId, rating) {
    try {
        await API.updatePromptRating(promptId, rating);
        const prompts = await API.getProjectPrompts(currentProject.id);
        renderPrompts(prompts || []);
    } catch (err) {
        console.error('更新星级失败', err);
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

async function deletePrompt(id) {
    if (!confirm('确定要删除这条提示词吗？')) return;
    
    try {
        await API.deletePrompt(id);
        const prompts = await API.getProjectPrompts(currentProject.id);
        renderPrompts(prompts || []);
    } catch (err) {
        console.error('删除提示词失败', err);
        alert('删除失败');
    }
}
