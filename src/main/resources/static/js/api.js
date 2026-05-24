// 封装所有的后端 API 请求

// 检查并处理认证失败
const handleResponse = (response) => {
    if (response.status === 401) {
        window.location.href = '/login.html';
        return Promise.reject('未登录');
    }
    return response;
};

const API = {
    // 认证相关
    checkAuth: () => fetch('/api/auth/check').then(handleResponse),
    logout: () => fetch('/api/auth/logout', { method: 'POST' }),
    
    // 获取统计数据
    getSummary: () => fetch('/api/dashboard/summary').then(handleResponse).then(res => res.json()),

    // 随机抽取项目
    getRandomTarget: () => fetch('/api/dashboard/random-target').then(handleResponse).then(res => res.json()),

    // 获取所有项目
    getProjects: (category, status) => {
        let url = '/api/projects';
        const params = new URLSearchParams();
        if (category) params.append('category', category);
        if (status) params.append('status', status);
        if (params.toString()) url += '?' + params.toString();
        console.log('请求项目列表:', url);
        return fetch(url).then(handleResponse).then(res => {
            console.log('响应状态:', res.status);
            return res.json();
        });
    },

    // 获取单个项目
    getProject: (id) => fetch('/api/projects/' + id).then(handleResponse).then(res => res.json()),

    // 获取项目的素材
    getProjectAssets: (id) => fetch('/api/projects/' + id + '/assets').then(handleResponse).then(res => res.json()),

    // 创建新项目
    createProject: (data) => fetch('/api/projects', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            title: data.title,
            category: data.type,
            status: data.status,
            description: data.description,
            tags: data.tags
        })
    }).then(handleResponse).then(res => res.json()),

    // 更新项目
    updateProject: (id, data) => fetch('/api/projects/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }).then(handleResponse).then(res => res.json()),

    // 删除项目
    deleteProject: (id) => fetch('/api/projects/' + id, { method: 'DELETE' }).then(handleResponse),

    // 上传素材
    uploadAsset: (formData) => fetch('/api/assets/upload', {
        method: 'POST',
        body: formData
    }).then(handleResponse).then(res => res.json()),

    // 删除素材
    deleteAsset: (id) => fetch('/api/assets/' + id, { method: 'DELETE' }).then(handleResponse),

    // 保存提示词
    savePrompt: (data) => fetch('/api/prompts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }).then(handleResponse).then(res => res.json()),
    
    // 获取项目的提示词
    getProjectPrompts: (projectId) => fetch(`/api/prompts/project/${projectId}`).then(handleResponse).then(res => res.json()),
    
    // 删除提示词
    deletePrompt: (id) => fetch(`/api/prompts/${id}`, { method: 'DELETE' }).then(handleResponse),
    
    // 更新提示词星级
    updatePromptRating: (id, rating) => fetch(`/api/prompts/${id}/rating`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating })
    }).then(handleResponse).then(res => res.json())
};
