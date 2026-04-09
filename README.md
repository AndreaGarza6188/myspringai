# myspringai — Spring AI Agent 示例工程

一个使用 **Spring AI** 构建的智能代理（Agent）示例工程。

- ✅ **Web 聊天界面** — 浏览器中直接与 AI 对话
- ✅ **网络搜索工具** — 通过 DuckDuckGo 搜索互联网；或抓取任意 URL 内容
- ✅ **本地文件工具** — 列出、读取、搜索本地数据目录中的文件
- ✅ **内置示例工具** — 获取当前时间、日期计算、基础数学运算
- ✅ **可扩展工具系统** — 只需实现一个接口即可添加自定义工具，AI 自动发现并调用

---

## 快速开始

### 1. 前提条件

| 工具 | 版本 |
|------|------|
| JDK  | 17+  |
| Maven | 3.8+ |
| API Key | OpenAI 或任意兼容模型的 Key |

### 2. 配置 API Key 和模型

```bash
export OPENAI_API_KEY=sk-...                        # 必填
export OPENAI_BASE_URL=https://api.openai.com       # 可选，默认 OpenAI；替换为第三方地址可接入兼容模型
export OPENAI_MODEL=gpt-4o-mini                     # 可选，默认 gpt-4o-mini
```

或者在 `src/main/resources/application.yml`（**不要将真实 Key 提交到代码仓库**）：

```yaml
spring:
  ai:
    openai:
      api-key: sk-...
      base-url: https://api.deepseek.com   # 第三方 Base URL
      chat:
        options:
          model: deepseek-chat
```

### 3. 运行

```bash
./mvnw spring-boot:run
# 或者
mvn spring-boot:run
```

### 4. 访问

打开浏览器访问 [http://localhost:8080](http://localhost:8080)

---

## 功能演示

启动后，可以向 AI 发送如下问题：

| 问题示例 | 触发的工具 |
|---------|-----------|
| 列出本地有哪些文件？ | `listFiles` |
| 读取 products.txt 的内容 | `readFile` |
| 搜索包含"退换货"的文件 | `searchFiles` |
| 现在几点了？ | `getCurrentDateTime` |
| 从 2024-01-01 到今天有多少天？ | `daysBetween` |
| 1234 × 56 等于多少？ | `calculate` |
| 搜索 Spring AI 的最新资讯 | `searchWeb` |
| 抓取 https://spring.io 的内容 | `fetchWebPage` |

---

## 项目结构

```
src/
├── main/
│   ├── java/com/example/myspringai/
│   │   ├── MySpringAiApplication.java       # 主入口
│   │   ├── controller/
│   │   │   └── AgentController.java         # REST API
│   │   ├── service/
│   │   │   └── AgentService.java            # 核心 Agent 逻辑（工具注册 + 对话记忆）
│   │   ├── tools/
│   │   │   ├── ToolProvider.java            # ★ 自定义工具接口（实现此接口即可扩展）
│   │   │   ├── WebSearchTool.java           # 内置：网络搜索 / URL 抓取
│   │   │   ├── LocalFileTool.java           # 内置：本地文件读取/搜索
│   │   │   └── DateTimeTool.java            # 内置示例：日期时间 / 计算器
│   │   └── config/
│   │       ├── ChatConfig.java              # ChatMemory Bean 配置
│   │       └── DataInitializer.java         # 启动时复制示例数据文件
│   └── resources/
│       ├── application.yml                  # 应用配置
│       ├── static/index.html                # Web 聊天界面
│       └── data/                            # 示例数据文件（首次启动复制到 ./data/）
└── test/
    └── java/com/example/myspringai/
        ├── MySpringAiApplicationTests.java  # Spring 上下文冒烟测试
        └── tools/LocalFileToolTest.java     # 本地文件工具单元测试
```

---

## ★ 如何添加自定义工具

只需 **4 步**，无需修改任何现有代码：

### 步骤一：创建工具类

在 `src/main/java/com/example/myspringai/tools/` 目录下新建一个类：

```java
package com.example.myspringai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component  // ← Spring 管理此 Bean
public class MyCustomTool implements ToolProvider {  // ← 实现 ToolProvider 接口

    @Tool(description = "查询天气。输入城市名称，返回当前天气状况。")
    public String getWeather(String city) {
        // TODO: 调用真实天气 API
        return "城市 " + city + " 当前天气：晴，气温 22°C";
    }

    @Tool(description = "将文字翻译为英文。")
    public String translateToEnglish(String text) {
        // TODO: 接入翻译 API
        return "Translated: " + text;
    }
}
```

### 步骤二：重启应用

```bash
mvn spring-boot:run
```

### 步骤三：验证注册

查看启动日志，确认工具数量增加：

```
Registered 10 tool callback(s) from 4 ToolProvider bean(s)
```

### 步骤四：测试

直接在聊天界面提问，AI 会自动识别何时调用你的工具。

---

### 工具编写建议

| 要点 | 说明 |
|------|------|
| **描述要详细** | `@Tool(description = "...")` 中的描述是 AI 判断何时调用此工具的唯一依据 |
| **参数名要清晰** | 参数名会传递给 AI，命名要见名知意（已配置 `-parameters` 编译选项） |
| **返回值为 String** | 返回结果以文字形式供 AI 阅读和处理 |
| **优雅处理错误** | 捕获异常并返回友好的错误信息，不要让工具抛出异常 |
| **保持单一职责** | 每个方法只做一件事，AI 的工具选择更精准 |

---

## 配置说明

`src/main/resources/application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:YOUR_KEY}        # 推荐通过环境变量设置
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}  # 第三方兼容模型的 Base URL
      chat:
        options:
          model: ${OPENAI_MODEL:gpt-4o-mini}     # 模型名称
          temperature: 0.7

app:
  data:
    directory: ${DATA_DIR:./data}                # 本地文件工具的根目录
```

**环境变量：**

```bash
export OPENAI_API_KEY=sk-...          # 必填：API Key
export OPENAI_BASE_URL=https://...    # 可选：第三方 Base URL（默认 https://api.openai.com）
export OPENAI_MODEL=gpt-4o-mini       # 可选：模型名称
export DATA_DIR=/path/to/your/data    # 可选：自定义数据目录
```

---

## 接入第三方 OpenAI 兼容模型

只需设置 `OPENAI_BASE_URL` 和 `OPENAI_MODEL` 环境变量，无需修改任何代码，即可接入任何 OpenAI 兼容的大模型。

### DeepSeek

```bash
export OPENAI_API_KEY=sk-...                      # DeepSeek API Key
export OPENAI_BASE_URL=https://api.deepseek.com
export OPENAI_MODEL=deepseek-chat
```

### 硅基流动 (SiliconFlow)

```bash
export OPENAI_API_KEY=sk-...                           # SiliconFlow API Key
export OPENAI_BASE_URL=https://api.siliconflow.cn/v1
export OPENAI_MODEL=Qwen/Qwen2.5-72B-Instruct
```

### 阿里云百炼 / 通义千问

```bash
export OPENAI_API_KEY=sk-...                                    # 阿里云 API Key
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
export OPENAI_MODEL=qwen-plus
```

### Ollama（本地部署）

```bash
export OPENAI_API_KEY=ollama                  # 任意非空字符串即可
export OPENAI_BASE_URL=http://localhost:11434/v1
export OPENAI_MODEL=llama3
```

---

## 使用外部配置文件

打包成 JAR 后，可以将 `application.yml` 放到 JAR 外部，方便在不同环境中单独维护配置，无需重新打包。

Spring Boot 按以下顺序自动加载配置（优先级从高到低）：

1. JAR 同目录的 `config/application.yml`
2. JAR 同目录的 `application.yml`
3. JAR 内部的 `application.yml`（默认）

### 方式一：`config/` 子目录（推荐）

```bash
myapp/
├── myspringai-0.0.1.jar
└── config/
    └── application.yml     ← 此处的配置自动覆盖 JAR 内部配置
```

```bash
cd myapp
java -jar myspringai-0.0.1.jar
```

### 方式二：指定任意外部路径

```bash
java -jar myspringai-0.0.1.jar \
  --spring.config.additional-location=file:/etc/myspringai/application.yml
```

或通过环境变量：

```bash
export SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/myspringai/application.yml
java -jar myspringai-0.0.1.jar
```

> **提示：** 使用 `additional-location` 时，外部配置与 JAR 内部配置**合并**（外部优先）；  
> 使用 `--spring.config.location` 则**完全替换**内部配置。

---

## REST API

### 创建会话

```
POST /api/session
```

响应：

```json
{ "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" }
```

### 发送消息

```
POST /api/chat
Content-Type: application/json

{
  "sessionId": "xxxxxxxx-...",   // 可选，不传时自动生成新会话
  "message": "你好，请帮我查询一下产品目录"
}
```

响应：

```json
{
  "sessionId": "xxxxxxxx-...",
  "response": "好的，以下是产品目录中的商品信息：..."
}
```

---

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.0 | 基础框架 |
| Spring AI | 1.0.0 | AI 模型集成、工具调用、对话记忆 |
| OpenAI GPT | gpt-4o-mini | 默认语言模型 |
| Java HttpClient | JDK 17 内置 | 网络请求（WebSearchTool） |
| Thymeleaf / 原生 HTML | — | Web 聊天界面 |

---

## 运行测试

```bash
# 单元测试（不需要 API Key）
mvn test -Dtest=LocalFileToolTest

# 集成测试（Spring 上下文，不需要真实 API Key）
mvn test -Dtest=MySpringAiApplicationTests

# 全部测试
mvn test
```
