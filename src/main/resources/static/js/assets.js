let selectedAssetIds = [];
let currentCategory = '';
let currentAssignFilter = '';
let allAssets = [];
let pendingUploadFiles = [];

const CATEGORY_LABELS = {
    DESIGN: '平面设计',
    VIDEO: '视频',
    PHOTO: '照片'
};

const IMAGE_TYPES = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'];
const VIDEO_TYPES = ['mp4', 'avi', 'mov', 'wmv', 'webm', 'mkv'];
const MAX_UPLOAD_FILES = 9;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    initEventListeners();
    switchCategory('');
    await loadAssets();
    checkUrlAction();
});

function checkUrlAction() {
    const params = new URLSearchParams(window.location.search);
    if (params.get('action') === 'upload') {
        openUploadModal();
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

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

function showDevelopmentAlert() {
    alert('🚧 此功能正在开发中，敬请期待！');
}

function initEventListeners() {
    document.getElementById('uploadForm').addEventListener('submit', handleBatchUpload);
    document.getElementById('assignForm').addEventListener('submit', handleBatchAssign);
    document.getElementById('filterStatus').addEventListener('change', onAssignFilterChange);
    initUploadDropzone();
}

function onAssignFilterChange() {
    currentAssignFilter = document.getElementById('filterStatus').value;
    loadAssets();
}

function initUploadDropzone() {
    const dropzone = document.getElementById('uploadDropzone');
    const fileInput = document.getElementById('uploadFiles');
    if (!dropzone || !fileInput) return;

    dropzone.addEventListener('click', () => fileInput.click());
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('border-blue-400', 'bg-blue-50/30');
    });
    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('border-blue-400', 'bg-blue-50/30');
    });
    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('border-blue-400', 'bg-blue-50/30');
        addUploadFiles(e.dataTransfer.files);
    });
    fileInput.addEventListener('change', (e) => addUploadFiles(e.target.files));
}

function addUploadFiles(files) {
    let rejected = false;
    Array.from(files).forEach(file => {
        if (pendingUploadFiles.length >= MAX_UPLOAD_FILES) {
            if (!rejected) {
                alert(`单次最多只能选择 ${MAX_UPLOAD_FILES} 个文件，请分批上传。`);
                rejected = true;
            }
            return;
        }
        const exists = pendingUploadFiles.some(f => f.name === file.name && f.size === file.size);
        if (!exists) {
            pendingUploadFiles.push(file);
        }
    });
    renderUploadFileList();
}

function renderUploadFileList() {
    const container = document.getElementById('uploadFileList');
    const countEl = document.getElementById('uploadFileCount');
    if (pendingUploadFiles.length === 0) {
        container.innerHTML = '';
        countEl.classList.add('hidden');
        return;
    }
    countEl.textContent = `已选择 ${pendingUploadFiles.length} / ${MAX_UPLOAD_FILES} 个文件`;
    countEl.classList.remove('hidden');
    container.innerHTML = pendingUploadFiles.map((file, index) => `
        <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div class="flex items-center gap-3 min-w-0">
                <i class="fas fa-file text-gray-400"></i>
                <div class="min-w-0">
                    <p class="text-sm font-medium truncate">${escapeHtml(file.name)}</p>
                    <p class="text-xs text-gray-400">${formatFileSize(file.size)}</p>
                </div>
            </div>
            <button type="button" onclick="removeUploadFile(${index})" class="text-red-500 hover:text-red-700 flex-shrink-0">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `).join('');
}

function removeUploadFile(index) {
    pendingUploadFiles.splice(index, 1);
    renderUploadFileList();
}

async function loadAssets() {
    try {
        const isAssigned = currentAssignFilter === '' ? null : currentAssignFilter;
        allAssets = await API.getAssets(currentCategory || null, isAssigned);
        selectedAssetIds = selectedAssetIds.filter(id => allAssets.some(a => a.id === id));
        renderAssetGrid();
        updateBatchActionButtons();
    } catch (err) {
        console.error('加载素材失败', err);
        allAssets = [];
        renderAssetGrid();
    }
}

function switchCategory(category) {
    currentCategory = category;
    document.querySelectorAll('.category-tab').forEach(tab => {
        const active = tab.dataset.category === category;
        tab.classList.toggle('active', active);
        tab.classList.toggle('bg-indigo-600', active);
        tab.classList.toggle('text-white', active);
        tab.classList.toggle('shadow-md', active);
        tab.classList.toggle('text-gray-600', !active);
        tab.classList.toggle('bg-white', !active);
        tab.classList.toggle('border', !active);
        tab.classList.toggle('border-gray-200', !active);
    });
    loadAssets();
}

function renderAssetGrid() {
    const grid = document.getElementById('assetGrid');
    const emptyState = document.getElementById('emptyState');

    if (!allAssets || allAssets.length === 0) {
        grid.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    grid.innerHTML = allAssets.map(asset => renderAssetCard(asset)).join('');
}

function renderAssetCard(asset) {
    const isSelected = selectedAssetIds.includes(asset.id);
    const preview = getAssetPreview(asset);
    const categoryLabel = CATEGORY_LABELS[asset.assetCategory] || '未分类';

    return `
        <div class="asset-card group relative bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-200 ${isSelected ? 'ring-2 ring-purple-500 ring-offset-1' : ''}">
            <input type="checkbox"
                   class="absolute top-3 left-3 w-5 h-5 cursor-pointer z-10 accent-purple-600"
                   ${isSelected ? 'checked' : ''}
                   onchange="toggleAssetSelection(${asset.id}, this.checked)">
            <div class="aspect-square bg-gray-50 flex items-center justify-center overflow-hidden">
                ${preview}
            </div>
            <div class="p-3 border-t border-gray-100">
                <p class="text-xs font-medium text-gray-800 truncate" title="${escapeHtml(asset.fileName)}">${escapeHtml(asset.fileName || '未命名')}</p>
                <div class="flex items-center justify-between mt-1 gap-1">
                    <span class="text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-500">${categoryLabel}</span>
                    ${asset.projectId ? '<span class="text-[10px] px-1.5 py-0.5 rounded bg-green-100 text-green-600">已关联</span>' : '<span class="text-[10px] px-1.5 py-0.5 rounded bg-amber-50 text-amber-600">未分配</span>'}
                </div>
            </div>
        </div>
    `;
}

function getAssetPreview(asset) {
    const fileType = (asset.fileType || '').toLowerCase();

    if (IMAGE_TYPES.includes(fileType)) {
        const src = asset.fileUrl || `/api/assets/${asset.id}/preview`;
        return `<img src="${src}" alt="${escapeHtml(asset.fileName)}" class="w-full h-full object-cover" onerror="this.onerror=null;this.src='/api/assets/${asset.id}/preview'">`;
    }
    if (VIDEO_TYPES.includes(fileType)) {
        return `<div class="flex flex-col items-center justify-center text-blue-500"><i class="fas fa-film text-4xl mb-2"></i><span class="text-xs text-gray-400">视频</span></div>`;
    }
    return getFallbackIcon(fileType, asset.assetCategory || '');
}

function getFallbackIcon(fileType, category) {
    if (category === 'DESIGN' || ['psd', 'ai', 'sketch', 'fig'].includes(fileType)) {
        return `<div class="flex flex-col items-center justify-center text-purple-500"><i class="fas fa-palette text-4xl mb-2"></i><span class="text-xs text-gray-400">设计文件</span></div>`;
    }
    if (category === 'PHOTO') {
        return `<div class="flex flex-col items-center justify-center text-emerald-500"><i class="fas fa-camera text-4xl mb-2"></i><span class="text-xs text-gray-400">照片</span></div>`;
    }
    return `<div class="flex flex-col items-center justify-center text-gray-400"><i class="fas fa-file text-4xl mb-2"></i><span class="text-xs">文件</span></div>`;
}

function toggleAssetSelection(assetId, checked) {
    if (checked) {
        if (!selectedAssetIds.includes(assetId)) {
            selectedAssetIds.push(assetId);
        }
    } else {
        selectedAssetIds = selectedAssetIds.filter(id => id !== assetId);
    }
    updateBatchActionButtons();
    renderAssetGrid();
}

function updateBatchActionButtons() {
    const hasSelection = selectedAssetIds.length > 0;
    document.getElementById('batchAssignBtn').disabled = !hasSelection;
    document.getElementById('batchDeleteBtn').disabled = !hasSelection;
}

async function handleBatchDelete() {
    if (selectedAssetIds.length === 0) return;

    const count = selectedAssetIds.length;
    const confirmed = confirm(
        `确定要删除选中的 ${count} 个素材吗？如果该素材已关联作品，将同时在作品中消失。此操作不可恢复！`
    );
    if (!confirmed) return;

    try {
        await API.batchDeleteAssets(selectedAssetIds);
        alert(`已成功删除 ${count} 个素材`);
        selectedAssetIds = [];
        updateBatchActionButtons();
        await loadAssets();
    } catch (err) {
        console.error('批量删除失败', err);
        alert('删除失败，请稍后重试');
    }
}

function updateBatchAssignButton() {
    updateBatchActionButtons();
}

function openUploadModal() {
    pendingUploadFiles = [];
    renderUploadFileList();
    document.getElementById('uploadForm').reset();
    document.getElementById('uploadLoading').classList.add('hidden');
    document.getElementById('uploadSubmitBtn').disabled = false;
    document.getElementById('uploadModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closeUploadModal() {
    document.getElementById('uploadModal').classList.add('hidden');
    document.body.style.overflow = '';
    pendingUploadFiles = [];
    renderUploadFileList();
}

async function handleBatchUpload(e) {
    e.preventDefault();

    if (pendingUploadFiles.length === 0) {
        alert('请先选择要上传的文件');
        return;
    }

    if (pendingUploadFiles.length > MAX_UPLOAD_FILES) {
        alert(`单次最多只能上传 ${MAX_UPLOAD_FILES} 个文件，请减少选择后重试。`);
        return;
    }

    const submitBtn = document.getElementById('uploadSubmitBtn');
    const loadingEl = document.getElementById('uploadLoading');
    submitBtn.disabled = true;
    loadingEl.classList.remove('hidden');

    const formData = new FormData();
    pendingUploadFiles.forEach(file => formData.append('files', file));
    formData.append('assetCategory', document.getElementById('uploadCategory').value);

    try {
        const result = await API.uploadAssetsBatch(formData);
        const count = Array.isArray(result) ? result.length : 1;
        alert(`成功上传 ${count} 个素材！`);
        closeUploadModal();
        await loadAssets();
    } catch (err) {
        console.error('批量上传失败', err);
        alert('上传失败，请稍后重试');
    } finally {
        submitBtn.disabled = false;
        loadingEl.classList.add('hidden');
    }
}

async function openAssignModal() {
    if (selectedAssetIds.length === 0) return;

    document.getElementById('assignCount').textContent = selectedAssetIds.length;
    document.getElementById('assignLoading').classList.add('hidden');
    document.getElementById('assignSubmitBtn').disabled = false;
    document.getElementById('assignModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';

    try {
        const projects = await API.getProjects();
        const select = document.getElementById('assignProjectId');
        if (!projects || projects.length === 0) {
            select.innerHTML = '<option value="">暂无作品，请先创建</option>';
        } else {
            select.innerHTML = '<option value="">请选择目标作品</option>' +
                projects.map(p => `<option value="${p.id}">${escapeHtml(p.title || '未命名')}</option>`).join('');
        }
    } catch (err) {
        console.error('加载作品列表失败', err);
        document.getElementById('assignProjectId').innerHTML = '<option value="">加载失败</option>';
    }
}

function closeAssignModal() {
    document.getElementById('assignModal').classList.add('hidden');
    document.body.style.overflow = '';
}

async function handleBatchAssign(e) {
    e.preventDefault();

    const projectId = document.getElementById('assignProjectId').value;
    if (!projectId) {
        alert('请选择目标作品');
        return;
    }

    const submitBtn = document.getElementById('assignSubmitBtn');
    const loadingEl = document.getElementById('assignLoading');
    submitBtn.disabled = true;
    loadingEl.classList.remove('hidden');

    try {
        await API.batchAssignAssets(selectedAssetIds, parseInt(projectId, 10));
        alert('素材已成功加入作品！');
        selectedAssetIds = [];
        closeAssignModal();
        updateBatchActionButtons();
        await loadAssets();
    } catch (err) {
        console.error('批量关联失败', err);
        alert('关联失败，请稍后重试');
    } finally {
        submitBtn.disabled = false;
        loadingEl.classList.add('hidden');
    }
}

function formatFileSize(bytes) {
    if (!bytes) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}
