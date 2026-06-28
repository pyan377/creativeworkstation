# Portfolio Studio - 个人创作工作台

一个现代化的创作项目管理系统，帮助你管理作品项目、素材、提示词和作业任务。主要面向自媒体工作者。

## 功能特性

### 🏠 首页功能
- **仪表板概览**：查看最近作品、即将截止任务、候选作品等统计
- **搜索功能**：按作品名、描述、标签搜索项目
- **快捷操作**：Icon 卡片式入口（新建作品、添加素材、保存提示词、添加作业任务、作品集候选）
- **今日整理目标**：随机抽取待完善项目进行整理
- **紧急任务推荐**：右侧工作台展示最紧急的待办任务，支持一键跳转

### 📁 作品项目管理
- **项目列表**：查看和管理所有作品项目
- **分类筛选**：按作品分类或状态筛选
- **项目详情**：查看项目信息、关联任务、相关素材和提示词
- **关联任务**：详情弹窗展示该作品下的所有任务，支持「添加关联任务」快速创建
- **编辑功能**：创建、编辑、删除项目
- **素材管理**：
    - **本地上传**：直接上传文件至当前项目。
    - **库内选取**：支持**「从素材库选取」**，直接从未分配的中央资产中勾选并导入当前项目，实现素材复用。
    - **操作**：支持预览、下载、批量导出素材。

### 💎 创作资产（中央素材库）
- **独立页面**：`assets.html` 统一管理全部创作素材
- **分类 Tabs**：全部 / 平面设计（DESIGN）/ 视频（VIDEO）/ 照片（PHOTO）
- **批量上传**：多选本地文件，指定分类后一次性上传至素材库（`projectId` 为空表示未分配）
- **批量关联**：勾选多个素材，一键「批量加入作品」，关联到指定 `WorkProject`。
- **批量删除**：支持批量删除素材，**系统将自动清理数据库记录及本地物理文件**，确保存储空间不被浪费。
- **卡片交互**：复选框多选、缩略图预览、Hover 动效、已关联/未分配状态标识
- **静态访问**：上传文件可通过 `/uploads/**` 直接访问

### 🎨 提示词系统
- **星级评分**：给提示词评分（1-5星）
- **智能排序**：高星提示词优先显示
- **标签管理**：为提示词添加标签便于分类
- **多种输入方式**：支持文本输入或文件上传

### 📋 作业任务管理
- **看板视图**：待办、进行中、已完成三列看板（`assignment.html`）
- **快速创建 / 编辑**：点击卡片打开编辑弹窗；首页或任务页均可创建任务
- **任务字段**：标题、描述、截止时间、任务类型、优先级（高/中/低）、关联作品
- **自定义类型**：任务类型支持 datalist 预设（商单/日常/活动）或自由输入
- **状态流转**：待办 → 进行中 → 审核 → 完成，支持重新打开（按钮点击不触发编辑弹窗）
- **截止提醒**：首页统计 3 天内到期或已超期的未完成任务，数量大于 0 时标红
- **关联作品**：看板卡片显示紫色文件夹 badge；**点击 badge 跳转作品详情**
- **双向联动**：作品详情页可查看、添加该作品下的关联任务

### 👤 用户认证
- **用户注册**：创建个人账号，用户名不重复
- **用户登录**：安全的会话管理
- **自动超时**：30分钟无操作自动退出
- **退出登录**：安全的登出机制

## 技术栈

### 后端
- **Java 17**
- **Spring Boot 4.0.6**
- **Spring Data JPA**
- **PostgreSQL** / **H2**（开发环境默认 H2 内存库）
- **Lombok** 简化代码

### 前端
- **HTML5** + **Tailwind CSS**
- **原生 JavaScript**（Fetch API）
- **Font Awesome** 图标库

## 快速开始

### 1. 环境要求
- JDK 17 或更高版本
- Maven 3.6+
- PostgreSQL 12+（生产环境；本地开发可跳过，默认使用 H2）

### 2. 数据库配置

#### 本地开发（默认）
项目默认激活 `dev` 配置，使用 H2 内存数据库，**无需安装 PostgreSQL**，直接运行即可。

配置文件：`src/main/resources/application-dev.properties`

#### 生产环境（PostgreSQL）
在 PostgreSQL 中创建名为 `portfolio_studio` 的数据库：

```sql
CREATE DATABASE portfolio_studio;
```

修改 `src/main/resources/application-prod.properties` 或通过环境变量配置连接信息：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio_studio
spring.datasource.username=postgres
spring.datasource.password=你的密码
upload.dir=D:/PortfolioData
```

### 3. 创建文件存储目录
在你配置的 `upload.dir` 路径下创建文件夹：

**Windows:**
```cmd
mkdir D:\PortfolioData
```

**Mac/Linux:**
```bash
mkdir -p /Users/你的用户名/PortfolioData
```

### 4. 运行项目

#### 使用 Maven 命令：
```bash
mvn spring-boot:run
```

#### 或使用 IDE：
直接运行 `CreativeworkstationApplication.java` 主类

### 5. 访问应用

| 页面 | 地址 |
|------|------|
| 登录 | http://localhost:8080/login.html |
| 首页 | http://localhost:8080/index.html |
| 作品项目 | http://localhost:8080/projects.html |
| 创作资产 | http://localhost:8080/assets.html |
| 作业任务 | http://localhost:8080/assignment.html |

首次使用请先注册账号。

## 数据库表结构

项目使用 Spring Data JPA 自动创建表结构，主要实体包括：

- **tb_user** - 用户表
- **tb_work_project** - 作品项目表
- **tb_creative_asset** - 创作素材表
- **tb_prompt_word** - 提示词表
- **tb_assignment_task** - 作业任务表
- **tb_system_config** - 系统配置表

### 作业任务表字段（tb_assignment_task）

| 字段 | 说明 |
|------|------|
| title | 任务标题 |
| description | 任务详情 |
| platform | 发布平台 |
| taskType | 任务类型（自定义字符串，如：商单、日常、活动、外包等） |
| status | 状态：TODO / DOING / REVIEW / DONE |
| priority | 优先级：1（高）/ 2（中）/ 3（低） |
| deadline | 截止时间 |
| expectedRevenue | 预期收益 |
| projectId | 关联作品 ID（外键逻辑关联 `tb_work_project.id`） |
| userId | 所属用户 ID |
| createTime / updateTime | 创建与更新时间 |

> 响应 JSON 中还会包含非持久化字段 `projectName`（由后端根据 `projectId` 查询作品标题后填充，便于前端展示）。

### 创作素材表字段（tb_creative_asset）

| 字段 | 说明 |
|------|------|
| fileName | 原始文件名 |
| fileUrl | 前端访问路径，如 `/uploads/xxx.png` |
| filePath | 本地磁盘绝对路径（预览/导出用） |
| assetCategory | 分类：DESIGN / VIDEO / PHOTO |
| projectId | 关联作品 ID，为空表示未分配至作品 |
| fileSize / fileType | 文件大小与扩展名 |
| userId | 所属用户 ID |
| uploadTime / createdAt | 上传与创建时间 |

## API 接口

### 创作素材 `/api/assets`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/assets` | 获取当前用户素材（支持 `?category=`、`?projectId=`、`?unassigned=true`） |
| POST | `/api/assets/upload` | 上传素材（单文件 `file` 或批量 `files[]` + `assetCategory`） |
| PUT | `/api/assets/batch-assign` | 批量关联作品（Body: `{ assetIds, projectId }`） |
| GET | `/api/assets/{id}/preview` | 预览/下载单个素材 |
| GET | `/api/assets/export?ids=` | 批量导出 ZIP |
| DELETE | `/api/assets/{id}` | 删除素材 |

### 作业任务 `/api/tasks`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tasks` | 获取当前用户全部任务（含 `projectName`） |
| GET | `/api/tasks/project/{projectId}` | 获取指定作品下的关联任务列表 |
| GET | `/api/tasks/{id}` | 获取单个任务（含 `projectName`） |
| POST | `/api/tasks` | 创建任务（可传 `projectId`） |
| PUT | `/api/tasks/{id}` | 更新任务（可传 `projectId`） |
| DELETE | `/api/tasks/{id}` | 删除任务 |

### 首页聚合 `/api/dashboard`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/dashboard/summary` | 首页概览（含 `upcomingTasks`、`urgentTask`） |
| GET | `/api/dashboard/random-target` | 随机抽取待完善项目 |

## 项目结构

```
creativeworkstation/
├── src/
│   ├── main/
│   │   ├── java/com/example/creativeworkstation/
│   │   │   ├── config/          # WebConfig（/uploads/** 静态映射）
│   │   │   ├── controller/      # 控制器（Asset、Project、Task 等）
│   │   │   ├── dto/             # 请求 DTO（如 BatchAssignRequest）
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据仓库
│   │   │   ├── service/         # 业务逻辑（AssetService、AssignmentTaskService 等）
│   │   │   └── util/            # 工具类
│   │   └── resources/
│   │       ├── static/          # 前端资源
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   ├── index.html       # 首页
│   │       │   ├── projects.html    # 作品项目
│   │       │   ├── assets.html      # 创作资产（中央素材库）
│   │       │   └── assignment.html  # 作业任务看板
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
├── pom.xml
└── README.md
```

## 使用说明

### 登录系统
1. 访问登录页面
2. 点击注册创建新账号
3. 登录进入工作台

### 创建作品项目
1. 点击首页"新建作品"或项目页的新建按钮
2. 填写作品信息（名称、分类、状态、描述、标签）
3. 保存项目

### 管理作业任务
1. **添加任务**：在首页点击「添加作业任务」，或在作业任务页点击右上角「添加任务」
2. **编辑任务**：点击看板卡片打开编辑弹窗（状态按钮不会触发编辑）
3. **关联作品**：创建/编辑时选择「关联作品」；看板紫色 badge 可点击跳转作品详情
4. **查看看板**：侧边栏进入「作业任务」，在三列看板中查看任务
5. **流转状态**：点击卡片底部按钮（开始处理 → 提交审核 → 标记完成）
6. **自定义类型**：任务类型可从下拉选择或手动输入（如「外包」「学习」）

### 作品与任务联动
1. **任务 → 作品**：创建任务时关联作品；看板点击紫色文件夹标签跳转 `projects.html?projectId=xxx`
2. **作品 → 任务**：作品详情「关联任务」区块查看列表，或点击「添加关联任务」直接创建
3. 任务状态徽章：待办（灰）、进行中（靛蓝）、审核（琥珀）、完成（绿）

### 管理创作资产（中央素材库）
1. 侧边栏进入「创作资产」或访问 `assets.html`
2. 点击「批量上传素材」，选择分类（平面设计/视频/照片）并多选文件上传
3. 在网格中勾选素材，点击「批量加入作品」，选择目标作品完成关联
4. 已关联素材会显示「已关联」标识；未分配素材显示「未分配」
5. **快速入口**：首页点击「添加素材」图标，将自动跳转至资产页并开启上传弹窗。
2. **分类与筛选**：通过顶部 Tabs 切换类型，通过状态下拉框筛选「未分配」素材。
3. **清理资产**：勾选不再需要的素材，点击「批量删除」。注意：这会同步删除磁盘上的原始文件。

### 管理项目内素材
1. 打开作品详情
2. **灵活导入**：
    - 点击「添加素材」从本地上传。
    - 点击**「从素材库选取」**，在弹窗中勾选之前已上传到库但未归档的素材。
3. 支持图片、视频、PDF、设计文件等
4. 可预览、下载、批量导出素材

### 提示词评分
1. 在项目详情页查看提示词
2. 点击星星进行评分（1-5星）
3. 高星提示词会自动排在前面

### 搜索功能
在首页搜索框输入关键词，快速找到相关项目

## 开发说明

### 添加新功能
1. 在 `entity/` 下创建实体类
2. 在 `repository/` 下创建仓库接口
3. 在 `service/` 下编写业务逻辑（复杂查询、跨表填充等）
4. 在 `controller/` 下创建控制器
5. 在 `src/main/resources/static/` 下添加前端页面

### 修改数据库
实体类修改后，设置 `spring.jpa.hibernate.ddl-auto=update` 会自动更新表结构

### 文件存储与静态访问
- 上传目录由 `upload.dir` 配置（开发环境默认 `D:/PortfolioData`）
- `WebConfig` 将 `/uploads/**` 映射到本地 `upload.dir`，素材 `fileUrl` 可直接访问
- 兼容旧路径 `/files/**` 与 API 预览接口 `/api/assets/{id}/preview`

## 常见问题

### 页面修改后没有变化
- 重启 Spring Boot 应用
- 浏览器强制刷新（Ctrl + F5）

### 上传失败
- 检查 `upload.dir` 路径是否存在且有写入权限
- 检查文件大小是否超过100MB限制

### 数据库连接失败
- 本地开发确认使用的是 `dev` 配置（H2，无需 PostgreSQL）
- 生产环境确认 PostgreSQL 服务正在运行，用户名密码正确，数据库已创建

### 会话超时
- 默认30分钟无操作会自动退出
- 可在 `SessionUtil.java` 中修改超时时间

## 许可证

本项目仅供学习和个人使用。
