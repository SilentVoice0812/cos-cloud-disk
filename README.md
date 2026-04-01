# 云盘系统 (COS Cloud Disk)

基于腾讯云COS的对象存储云盘系统

## 项目结构

```
├── cos-cloud-disk/          # 后端 SpringBoot 项目
│   ├── src/main/java/com/baoge/
│   │   ├── CosCloudDiskApplication.java    # 启动类
│   │   ├── config/CorsConfig.java          # CORS配置
│   │   ├── controller/FileController.java  # 文件接口
│   │   ├── entity/CosFile.java             # 文件实体
│   │   ├── mapper/CosFileMapper.java       # 数据库Mapper
│   │   ├── service/                        # 服务层
│   │   │   ├── CosFileService.java
│   │   │   └── impl/CosFileServiceImpl.java
│   │   └── utils/CosUtils.java             # COS工具类
│   ├── src/main/resources/
│   │   └── application.yml                 # 配置文件
│   ├── sql/init.sql                        # 数据库初始化脚本
│   └── pom.xml
│
└── cos-cloud-disk-web/     # 前端 Vue3 项目
    ├── src/
    │   ├── api/files.js                   # API接口
    │   ├── views/Home.vue                 # 主页面
    │   ├── router/index.js               # 路由配置
    │   └── main.js                       # 入口文件
    ├── package.json
    ├── vite.config.js
    └── index.html
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- 腾讯云COS bucket

### 2. 后端部署

```bash
# 1. 创建数据库
mysql -u root -p < sql/init.sql

# 2. 修改配置
# 编辑 src/main/resources/application.yml
# 填写腾讯云COS的 secret-id、secret-key、region、bucket-name、bucket-domain

# 3. 编译运行
cd cos-cloud-disk
mvn clean package -DskipTests
java -jar target/cos-cloud-disk-1.0.0.jar
```

### 3. 前端部署

```bash
# 1. 安装依赖
cd cos-cloud-disk-web
npm install

# 2. 开发模式运行
npm run dev

# 3. 生产构建
npm run build
```

## 功能特性

- ✅ 文件上传/下载
- ✅ 创建文件夹
- ✅ 删除文件/文件夹
- ✅ 重命名
- ✅ 文件搜索
- ✅ 目录浏览
- ✅ 面包屑导航
- ✅ 拖拽上传（TODO）
- ✅ 右键菜单（TODO）
- ✅ 文件移动（TODO）

## API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/files/list?parentId=0 | 获取文件列表 |
| POST | /api/files/upload | 上传文件 |
| POST | /api/files/createFolder | 创建文件夹 |
| DELETE | /api/files/{id} | 删除文件 |
| PUT | /api/files/{id}/rename | 重命名 |
| PUT | /api/files/{id}/move | 移动文件 |
| GET | /api/files/search | 搜索文件 |
| GET | /api/files/{id}/download | 获取下载链接 |

## 截图预览

界面采用简洁的文件管理器风格：
- 左侧工具栏：上传、新建文件夹
- 中间搜索栏
- 右侧面包屑导航
- 主区域：文件列表（图标+名称+大小+日期+操作）
