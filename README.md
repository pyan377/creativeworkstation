# Portfolio Studio - 个人创作工作台

一个现代化的创作项目管理系统，帮助你管理作品项目、素材、提示词等内容。主要面对自媒体工作者。

## 功能特性

### 🏠 首页功能
- **仪表板概览**：快速查看项目统计和工作状态
- **搜索功能**：按作品名、描述、标签搜索项目
- **快捷操作**：新建作品、添加素材、保存提示词
- **今日整理目标**：随机抽取待完善项目进行整理

### 📁 作品项目管理
- **项目列表**：查看和管理所有作品项目
- **分类筛选**：按作品分类或状态筛选
- **项目详情**：查看项目信息、相关素材和提示词
- **编辑功能**：创建、编辑、删除项目
- **素材管理**：上传、预览、下载、批量导出素材

### 🎨 提示词系统
- **星级评分**：给提示词评分（1-5星）
- **智能排序**：高星提示词优先显示
- **标签管理**：为提示词添加标签便于分类
- **多种输入方式**：支持文本输入或文件上传

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
- **PostgreSQL** 数据库
- **Lombok** 简化代码

### 前端
- **HTML5** + **Tailwind CSS**
- **原生 JavaScript**
- **Font Awesome** 图标库

## 快速开始

### 1. 环境要求
- JDK 17 或更高版本
- Maven 3.6+
- PostgreSQL 12+ 数据库

### 2. 数据库配置

#### 创建数据库
在 PostgreSQL 中创建名为 `portfolio_studio` 的数据库：

```sql
CREATE DATABASE portfolio_studio;
```

#### 配置数据库连接
修改 `src/main/resources/application.properties`：

```properties
# 数据库连接配置
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio_studio
spring.datasource.username=postgres
spring.datasource.password=你的密码

# 文件存储路径（确保文件夹存在）
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

打开浏览器访问：http://localhost:8080/login.html

首次使用请先注册账号。

## 数据库表结构

项目使用 Spring Data JPA 自动创建表结构，主要实体包括：

- **tb_user** - 用户表
- **tb_work_project** - 作品项目表
- **tb_creative_asset** - 创作素材表
- **tb_prompt_word** - 提示词表
- **tb_assignment_task** - 作业任务表
- **tb_system_config** - 系统配置表

## 项目结构

```
creativeworkstation/
├── src/
│   ├── main/
│   │   ├── java/com/example/creativeworkstation/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据仓库
│   │   │   ├── service/         # 业务逻辑
│   │   │   └── util/            # 工具类
│   │   └── resources/
│   │       ├── static/          # 前端资源
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── *.html
│   │       └── application.properties
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

### 管理素材
1. 打开项目详情
2. 点击"添加素材"上传文件
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
3. 在 `controller/` 下创建控制器
4. 在 `src/main/resources/static/` 下添加前端页面

### 修改数据库
实体类修改后，设置 `spring.jpa.hibernate.ddl-auto=update` 会自动更新表结构

## 常见问题

### 上传失败
- 检查 `upload.dir` 路径是否存在且有写入权限
- 检查文件大小是否超过100MB限制

### 数据库连接失败
- 确认 PostgreSQL 服务正在运行
- 检查用户名密码是否正确
- 确认数据库已创建

### 会话超时
- 默认30分钟无操作会自动退出
- 可在 `SessionUtil.java` 中修改超时时间

## 许可证

本项目仅供学习和个人使用。
