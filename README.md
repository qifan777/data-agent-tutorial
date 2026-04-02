# 🚀 Data-Agent: 从 0 到 1 构建企业级 Text2SQL 智能体

> 本项目是一个**基于开源项目的学习型教程**：在参考 `spring-ai-alibaba/DataAgent` 的基础上，结合个人理解进行拆解、复现与讲解。  
> 目标是帮助开发者从 0 到 1 系统掌握 **StateGraph 图编排**、**双重 RAG 检索**、**自我纠错**、**HITL 人机协同** 等 Text2SQL Agent 关键能力。

<p align="left">
  <a href="https://github.com/qifan777/data-agent-tutorial">项目仓库</a> ·
  <a href="https://github.com/spring-ai-alibaba/DataAgent">参考项目</a>
</p>

## 📌 快速导航

- [项目介绍图](#-项目介绍图)
- [核心亮点](#-核心亮点你将学到什么)
- [技术栈](#️-现代化技术栈)
- [系统架构](#-宏观系统架构图-system-architecture)
- [执行链路](#️-端到端执行链路速览)
- [效果预览](#️-效果预览)
- [快速启动](#-快速启动-5-分钟极速体验)
- [教程导航](#-教程导航自顶向下进阶)

---

## 🖼️ 项目介绍图

[点击查看原图](./assets/readme/video-cover.svg)

<img src="./assets/readme/video-cover.svg" alt="Data-Agent 项目介绍图" width="100%" />

---

## ✨ 核心亮点：你将学到什么？

这个教程不是“只看概念”的介绍，而是围绕一个可运行项目，带你把 Text2SQL Agent 的关键能力拆开学透：

- 🧩 **从流程到代码的完整映射**：用 StateGraph 把问题理解、知识召回、规划、执行、纠错、报告串成清晰链路，知道每一步该放什么能力。
- 📚 **结构化 + 非结构化的双通道检索**：同时利用关系图谱信息与业务知识库，理解 Text2SQL 在真实业务里如何减少歧义和幻觉。
- 🛠️ **可落地的执行与纠错机制**：掌握 SQL 生成与执行、错误回溯修复、Python Docker 沙盒分析的协作方式，而不是停留在“生成 SQL”这一步。
- 🤝 **面向生产的交互设计**：通过 HITL 人工确认、A2A 协议、SSE 流式反馈，学习高风险场景下可控、可观测的人机协同模式。
- 📖 **可复现的学习路径**：基于开源项目进行拆解与复现，提供从骨架搭建到核心编排的章节化路线，适合边读边跑、逐步进阶。

## 🛠️ 现代化技术栈

- **后端**：`Kotlin` + `Spring Boot 3.x` + `Jimmer`
- **AI 与编排**：`Spring AI Alibaba Graph` + `Spring AI`
- **向量与存储**：`PostgreSQL` + `pgvector`
- **前端**：`Vue 3` + `TypeScript` + `Vite` + `A2UI`

---

## 🧭 宏观系统架构图 (System Architecture)

[点击查看原图](./assets/readme/A2A-client-server.png)

<img src="./assets/readme/A2A-client-server.png" alt="Data-Agent 宏观系统架构图" width="100%" />

---

## 🗺️ 端到端执行链路速览

```text
[用户自然语言提问]
   └── A2A 协议流式请求
        └── 路由意图识别
             ├── 知识召回（向量化业务词汇 + QA）
             ├── 关系图谱召回
             ├── 可行性评估与任务拆解（Planner）
             ├── 人工确认拦截（HITL）
             ├── SQL 生成与执行 + 自动纠错循环
             ├── Python Docker 沙盒执行与分析
             └── 报告整理（Report Generation）
                  └── 前端流式打字机效果呈现（A2UI）
```

---

## 🖼️ 效果预览

[点击查看原图（完整长图）](./assets/readme/all.png)

<img src="./assets/readme/report.png" alt="Data-Agent 系统最终效果图" width="100%" />

---

## ⚡ 快速启动 (5 分钟极速体验)

### 1. 环境准备

- 基础环境：`Java 21+`、`Node.js 20+`、`pnpm`
- 数据库：`PostgreSQL`（默认 `localhost:5432/data_agent_tutorial`）
- 必装扩展：`pgvector`

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. 启动后端

首次初始化数据库（在项目根目录执行）：

```bash
psql -U postgres data_agent_tutorial -f data-agent-backend/src/main/resources/database.sql
```

然后启动后端：

```bash
cd data-agent-backend
./gradlew bootRun
```

出现 `Tomcat started on port(s): 9933` 即启动成功。

### 3. 启动前端

```bash
cd data-agent-frontend
pnpm install
pnpm dev
```

默认地址：`http://localhost:3500`（自动代理 `/api` 到后端）。

### 4. 验证 A2A 链路

打开浏览器输入自然语言问题，若看到前端卡片出现流式节点打字机效果，即最小 Agent 闭环已跑通。

---

## 📖 教程导航（自顶向下进阶）

> 本教程按章节组织，**强烈建议切换到对应章节的 Git 分支**对照阅读源码，效果翻倍！

- 🏗️ **[00 项目骨架搭建](https://www.jarcheng.top/project/data-agent/00-project-scaffold/)**  
  后端 Kotlin + Jimmer 初始化，前端 Vue3 接入 API 自动生成。
- 🔌 **[01 A2A 协议实战](https://www.jarcheng.top/project/data-agent/01-a2a-workflow/)**  
  跑通 Agent 服务发现与 JSON-RPC 流式事件。
- 🕸️ **[02 Graph 编程基础](https://www.jarcheng.top/project/data-agent/02-graph-programming/)**  
  从单节点走向多分支路由，实现 `暂停 -> 人工确认 -> 续跑` 的 HITL 工作流。
- 🧠 **[03 Bird SQL 知识库基建](https://www.jarcheng.top/project/data-agent/03-bird-sql-knowledge/)**  
  完成结构化表关联入库与 PGVector 向量化。
- 🔥 **[04 SQL Agent 核心编排](https://www.jarcheng.top/project/data-agent/04-sql-agent-orchestration/)**（系列高潮）  
  逐个击破：知识召回、关系图谱、任务拆解、SQL 自纠错、Python 高阶计算、商业报告生成。


--- 

## 联系方式

付费远程运行/安装/定制开发联系微信：ljc666max

其他关于程序运行安装报错请加QQ群：

- 416765656（满）
- 632067985

