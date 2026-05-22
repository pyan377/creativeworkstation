// 全局变量
let selectedAssets = [];
let selectedPromptFile = null;
let promptInputType = 'text';
let currentUser = null;

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', async () => {
    console.log("Portfolio Studio 前端已就绪");
    await checkAuth();
    loadDashboardData();
    initEventListeners();
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

// 初始化所有事件监听器
function initEventListeners() {
    // 新建作品表单
    document.getElementById('createProjectForm').addEventListener('submit', handleCreateProject);
    
    // 素材表单
    document.getElementById('assetForm').addEventListener('submit', handleUploadAssets);
    initAssetDropzone();
    
    // 提示词表单
    document.getElementById('promptForm').addEventListener('submit', handleSavePrompt);
    initPromptDropzone();
    initPromptTypeButtons();
    
    // 搜索功能
    initSearchFunction();
}

// 切换工作模式
function switchMode(mode) {
    document.querySelectorAll('.mode-item').forEach(el => el.classList.remove('active'));
    const modeElements = document.querySelectorAll('.mode-item');
    if (mode === 'SILENT_ORGANIZE') {
        modeElements[0].classList.add('active');
    } else if (mode === 'FOCUS_EDIT') {
        modeElements[1].classList.add('active');
    }

    const bannerHint = document.getElementById('banner-hint');
    const modeLabel = document.getElementById('current-mode-label');

    if (mode === 'SILENT_ORGANIZE') {
        modeLabel.innerText = "安静整理";
        bannerHint.innerText = "今天适合把散落的文件、说明和过程图慢慢归位。";
    } else if (mode === 'FOCUS_EDIT') {
        modeLabel.innerText = "专注修改";
        bannerHint.innerText = "集中精力处理待办任务，完善作品源文件。";
    }
}

// 加载统计数据
async function loadDashboardData() {
    try {
        const data = await API.getSummary();
        if (data) {
            document.getElementById('stat-recent').innerText = data.recentCount || 0;
            document.getElementById('stat-tasks').innerText = data.taskCount || 0;
            document.getElementById('stat-candidates').innerText = data.candidateCount || 0;
            document.getElementById('target-files').innerText = data.missingFiles || 0;
            document.getElementById('target-desc').innerText = data.missingDesc || 0;
        }
    } catch (err) {
        console.error("加载数据失败，使用模拟数据", err);
        const data = {
            recentCount: 1,
            taskCount: 0,
            candidateCount: 2,
            missingFiles: 0,
            missingDesc: 1
        };
        document.getElementById('stat-recent').innerText = data.recentCount;
        document.getElementById('stat-tasks').innerText = data.taskCount;
        document.getElementById('stat-candidates').innerText = data.candidateCount;
        document.getElementById('target-files').innerText = data.missingFiles;
        document.getElementById('target-desc').innerText = data.missingDesc;
    }
}

// 加载项目列表到下拉框
async function loadProjectsToSelects() {
    try {
        const projects = await API.getProjects();
        const selects = ['assetProject', 'promptProject'];
        selects.forEach(selectId => {
            const select = document.getElementById(selectId);
            select.innerHTML = '<option value="">-- 不关联作品 --</option>';
            projects.forEach(project => {
                const option = document.createElement('option');
                option.value = project.id;
                option.textContent = project.title;
                select.appendChild(option);
            });
        });
    } catch (err) {
        console.error("加载项目列表失败", err);
    }
}

// 夜间模式切换
const darkModeBtn = document.getElementById('dark-mode-toggle');
const toggleDot = document.getElementById('toggle-dot');
let isDark = false;

darkModeBtn.addEventListener('click', () => {
    isDark = !isDark;
    if (isDark) {
        toggleDot.style.transform = 'translateX(24px)';
        darkModeBtn.classList.replace('bg-gray-300', 'bg-indigo-500');
    } else {
        toggleDot.style.transform = 'translateX(0)';
        darkModeBtn.classList.replace('bg-indigo-500', 'bg-gray-300');
    }
});

// 显示开发中提示
function showDevelopmentAlert() {
    alert("🚧 此功能正在开发中，敬请期待！");
}

// 随机抽取目标项目
async function drawRandomTarget() {
    try {
        const project = await API.getRandomTarget();
        if (project) {
            alert("抽到了项目：" + project.title);
        } else {
            alert("目前没有待完善的项目！");
        }
    } catch (err) {
        console.error("抽取失败", err);
        alert("抽取失败，请稍后重试");
    }
}

// ========== 新建作品弹窗 ==========
function openCreateModal() {
    const modal = document.getElementById('createModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    document.getElementById('projectName').focus();
}

function closeCreateModal() {
    const modal = document.getElementById('createModal');
    modal.classList.add('hidden');
    document.body.style.overflow = '';
    document.getElementById('createProjectForm').reset();
}

async function handleCreateProject(e) {
    e.preventDefault();
    
    const projectData = {
        title: document.getElementById('projectName').value,
        type: document.getElementById('projectType').value,
        status: document.querySelector('input[name="status"]:checked').value,
        description: document.getElementById('projectDesc').value,
        tags: document.getElementById('projectTags').value
    };
    
    try {
        await API.createProject(projectData);
        alert("作品创建成功！");
        closeCreateModal();
        loadDashboardData();
    } catch (err) {
        console.error("创建失败", err);
        alert("创建失败，请稍后重试");
    }
}

// ========== 新建作品弹窗 ==========
function openCreateModal() {
    const modal = document.getElementById('createModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    document.getElementById('projectName').value = '';
    document.getElementById('projectType').value = '平面设计';
    document.getElementById('projectDesc').value = '';
    document.getElementById('projectTags').value = '';
    document.querySelector('input[name="status"][value="草稿"]').checked = true;
    document.getElementById('projectName').focus();
}

function closeCreateModal() {
    const modal = document.getElementById('createModal');
    modal.classList.add('hidden');
    document.body.style.overflow = '';
    document.getElementById('createProjectForm').reset();
}

// ========== 添加素材弹窗 ==========
function openAssetModal() {
    const modal = document.getElementById('assetModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    selectedAssets = [];
    renderAssetFileList();
    loadProjectsToSelects();
}

function closeAssetModal() {
    const modal = document.getElementById('assetModal');
    modal.classList.add('hidden');
    document.body.style.overflow = '';
    document.getElementById('assetForm').reset();
    selectedAssets = [];
    renderAssetFileList();
}

function initAssetDropzone() {
    const dropzone = document.getElementById('assetDropzone');
    const fileInput = document.getElementById('assetFile');
    
    dropzone.addEventListener('click', () => fileInput.click());
    
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dropzone-active');
    });
    
    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dropzone-active');
    });
    
    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dropzone-active');
        handleAssetFiles(e.dataTransfer.files);
    });
    
    fileInput.addEventListener('change', (e) => {
        handleAssetFiles(e.target.files);
    });
}

function handleAssetFiles(files) {
    Array.from(files).forEach(file => {
        if (!selectedAssets.find(f => f.name === file.name && f.size === file.size)) {
            selectedAssets.push(file);
        }
    });
    renderAssetFileList();
}

function removeAsset(index) {
    selectedAssets.splice(index, 1);
    renderAssetFileList();
}

function renderAssetFileList() {
    const container = document.getElementById('assetFileList');
    container.innerHTML = '';
    
    selectedAssets.forEach((file, index) => {
        const div = document.createElement('div');
        div.className = 'file-item';
        div.innerHTML = `
            <div class="icon"><i class="fas fa-file"></i></div>
            <div class="info">
                <div class="name">${file.name}</div>
                <div class="size">${formatFileSize(file.size)}</div>
            </div>
            <div class="remove" onclick="removeAsset(${index})"><i class="fas fa-times"></i></div>
        `;
        container.appendChild(div);
    });
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

async function handleUploadAssets(e) {
    e.preventDefault();
    
    if (selectedAssets.length === 0) {
        alert("请先选择要上传的文件");
        return;
    }
    
    const projectId = document.getElementById('assetProject').value;
    
    try {
        for (const file of selectedAssets) {
            const formData = new FormData();
            formData.append('file', file);
            if (projectId) {
                formData.append('projectId', projectId);
            }
            await API.uploadAsset(formData);
        }
        alert(`成功上传 ${selectedAssets.length} 个文件！`);
        closeAssetModal();
    } catch (err) {
        console.error("上传失败", err);
        alert("上传失败，请稍后重试");
    }
}

// ========== 保存提示词弹窗 ==========
function openPromptModal() {
    const modal = document.getElementById('promptModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    selectedPromptFile = null;
    promptInputType = 'text';
    updatePromptTypeUI();
    loadProjectsToSelects();
}

function closePromptModal() {
    const modal = document.getElementById('promptModal');
    modal.classList.add('hidden');
    document.body.style.overflow = '';
    document.getElementById('promptForm').reset();
    selectedPromptFile = null;
    promptInputType = 'text';
    updatePromptTypeUI();
}

function initPromptTypeButtons() {
    document.querySelectorAll('.prompt-input-type-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            promptInputType = btn.dataset.type;
            updatePromptTypeUI();
        });
    });
}

function updatePromptTypeUI() {
    document.querySelectorAll('.prompt-input-type-btn').forEach(btn => {
        if (btn.dataset.type === promptInputType) {
            btn.classList.add('border-purple-500', 'bg-purple-50', 'text-purple-700', 'font-medium');
            btn.classList.remove('border-gray-300', 'text-gray-600');
        } else {
            btn.classList.remove('border-purple-500', 'bg-purple-50', 'text-purple-700', 'font-medium');
            btn.classList.add('border-gray-300', 'text-gray-600', 'hover:border-gray-400');
        }
    });
    
    document.getElementById('promptTextArea').classList.toggle('hidden', promptInputType !== 'text');
    document.getElementById('promptFileArea').classList.toggle('hidden', promptInputType !== 'file');
}

function initPromptDropzone() {
    const dropzone = document.getElementById('promptDropzone');
    const fileInput = document.getElementById('promptFile');
    
    dropzone.addEventListener('click', () => fileInput.click());
    
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dropzone-active');
    });
    
    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dropzone-active');
    });
    
    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dropzone-active');
        if (e.dataTransfer.files.length > 0) {
            selectedPromptFile = e.dataTransfer.files[0];
            document.getElementById('promptFileName').textContent = selectedPromptFile.name;
        }
    });
    
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            selectedPromptFile = e.target.files[0];
            document.getElementById('promptFileName').textContent = selectedPromptFile.name;
        }
    });
}

async function readFileAsText(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.onerror = (e) => reject(e);
        reader.readAsText(file);
    });
}

async function handleSavePrompt(e) {
    e.preventDefault();
    
    let content = '';
    
    if (promptInputType === 'text') {
        content = document.getElementById('promptContent').value;
        if (!content.trim()) {
            alert("请输入提示词内容");
            return;
        }
    } else {
        if (!selectedPromptFile) {
            alert("请选择要上传的文件");
            return;
        }
        try {
            content = await readFileAsText(selectedPromptFile);
        } catch (err) {
            alert("读取文件失败");
            return;
        }
    }
    
    const promptData = {
        content: content,
        tags: document.getElementById('promptTags').value,
        projectId: document.getElementById('promptProject').value || null
    };
    
    try {
        await API.savePrompt(promptData);
        alert("提示词保存成功！");
        closePromptModal();
    } catch (err) {
        console.error("保存失败", err);
        alert("保存失败，请稍后重试");
    }
}

// ========== 搜索功能 ==========
function initSearchFunction() {
    const searchInput = document.getElementById('searchInput');
    const searchResults = document.getElementById('searchResults');
    
    let searchTimeout;
    
    // 输入时搜索
    searchInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        const query = e.target.value.trim();
        
        if (query.length === 0) {
            searchResults.classList.add('hidden');
            return;
        }
        
        searchTimeout = setTimeout(() => {
            performSearch(query);
        }, 300);
    });
    
    // 点击外部关闭搜索结果
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.relative')) {
            searchResults.classList.add('hidden');
        }
    });
    
    // 防止点击搜索结果区域时关闭
    searchResults.addEventListener('click', (e) => {
        e.stopPropagation();
    });
}

async function performSearch(query) {
    const searchResults = document.getElementById('searchResults');
    
    try {
        const projects = await API.getProjects();
        
        // 过滤匹配的项目
        const filteredProjects = projects.filter(project => {
            const title = (project.title || '').toLowerCase();
            const description = (project.description || '').toLowerCase();
            const tags = (project.tags || '').toLowerCase();
            const category = (project.category || '').toLowerCase();
            const searchQuery = query.toLowerCase();
            
            return title.includes(searchQuery) || 
                   description.includes(searchQuery) || 
                   tags.includes(searchQuery) ||
                   category.includes(searchQuery);
        });
        
        renderSearchResults(filteredProjects, query);
    } catch (err) {
        console.error("搜索失败", err);
        searchResults.innerHTML = `
            <div class="p-4 text-center text-gray-500">
                搜索出错，请稍后重试
            </div>
        `;
        searchResults.classList.remove('hidden');
    }
}

function renderSearchResults(projects, query) {
    const searchResults = document.getElementById('searchResults');
    
    if (projects.length === 0) {
        searchResults.innerHTML = `
            <div class="p-4 text-center text-gray-500">
                没有找到匹配的作品
            </div>
        `;
        searchResults.classList.remove('hidden');
        return;
    }
    
    searchResults.innerHTML = projects.map(project => {
        // 查找匹配的内容用于显示
        const matchText = getMatchingText(project, query);
        
        return `
            <div class="p-4 hover:bg-gray-50 cursor-pointer transition border-b border-gray-100 last:border-b-0" 
                 onclick="openProjectDetailFromSearch(${project.id})">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        <i class="fas fa-folder text-indigo-500"></i>
                    </div>
                    <div class="flex-1 min-w-0">
                        <h4 class="font-medium text-gray-800 truncate">${highlightText(project.title || '未命名', query)}</h4>
                        <p class="text-xs text-gray-500 truncate">${matchText}</p>
                    </div>
                    <div class="text-xs px-2 py-1 rounded-full ${getStatusClassForSearch(project.status)} flex-shrink-0">
                        ${project.status || '草稿'}
                    </div>
                </div>
            </div>
        `;
    }).join('');
    
    searchResults.classList.remove('hidden');
}

function getMatchingText(project, query) {
    const searchQuery = query.toLowerCase();
    const description = project.description || '';
    const tags = project.tags || '';
    const category = project.category || '';
    
    // 优先显示标签匹配
    if (tags.toLowerCase().includes(searchQuery)) {
        return `标签: ${highlightText(tags, query)}`;
    }
    // 其次显示分类匹配
    if (category.toLowerCase().includes(searchQuery)) {
        return `分类: ${highlightText(category, query)}`;
    }
    // 最后显示描述
    if (description) {
        return highlightText(description.substring(0, 60), query) + (description.length > 60 ? '...' : '');
    }
    
    return category || '未分类';
}

function highlightText(text, query) {
    if (!text || !query) return text;
    const regex = new RegExp(`(${query})`, 'gi');
    return text.replace(regex, '<mark class="bg-yellow-200 px-1 rounded">$1</mark>');
}

function getStatusClassForSearch(status) {
    const classes = {
        '草稿': 'bg-gray-100 text-gray-600',
        '进行中': 'bg-blue-100 text-blue-600',
        '已完成': 'bg-green-100 text-green-600',
        '归档': 'bg-yellow-100 text-yellow-600'
    };
    return classes[status] || 'bg-gray-100 text-gray-600';
}

async function openProjectDetailFromSearch(projectId) {
    // 关闭搜索结果
    document.getElementById('searchResults').classList.add('hidden');
    document.getElementById('searchInput').value = '';
    
    // 跳转到项目页面并打开详情
    window.location.href = `projects.html?openProject=${projectId}`;
}
