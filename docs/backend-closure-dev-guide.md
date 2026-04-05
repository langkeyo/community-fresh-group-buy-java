# 后台收尾开发文档（毕业答辩版）

更新时间：2026-04-05  
适用项目：`community-fresh-group-buy`（Spring Boot 后端）

## 1. 目标与边界

### 1.1 当前目标
- 在不大改架构的前提下完成答辩可演示的后台闭环。
- 保证主流程稳定：下单 -> 订单流转 -> AI推荐 -> 审核 -> 入词典库。
- 所有新增功能优先“最小可运行”，再迭代完善。

### 1.2 本阶段不做
- 不引入复杂微服务拆分。
- 不做本地大模型部署（先走官方 API）。
- 不做高复杂度权限系统（保留最小管理员能力即可）。

## 2. 当前完成情况

### 2.1 订单主链
- 已有订单创建、订单列表、订单详情、状态更新接口。
- 已接入 Redis 分布式锁防重复下单。
- 已接入 RabbitMQ 订单创建事件发送与消费（日志验证通过）。

### 2.2 AI模块
- `/api/ai/recommend` 已可返回结构化结果。
- DeepSeek 官方 API 已打通（非流式 JSON 方案）。
- 已有审核池表：`ai_recommend_review`。
- 已有审核通过入库表：`ai_recipe_library`。

### 2.3 管理端联动
- AI审核页已具备：列表、详情抽屉、通过/驳回、状态筛选。

## 3. 收尾任务清单（按优先级）

### P0（必须完成）
- [x] `AiController`：审核通过时做“幂等防重入库”（同 query+title 短期内避免重复写入）。
- [x] `AiController`：驳回接口补 `reviewRemark` 非空校验（最小长度 >= 2）。
- [x] `AiController`：`/api/ai/test-llm` 仅开发环境可用，生产禁用。
- [x] `AiLlmService`：统一异常消息（超时/401/5xx）返回业务友好文案。

### P1（建议完成）
- [x] AI审核列表分页接口（避免后期数据多时一次性拉取）。
- [x] 审核通过后将 `query_text` 做标准化（trim、小写、去多空格）用于命中优化。
- [x] 订单创建消息消费者加业务日志前缀（便于答辩演示检索）。

### P2（可选加分）
- [x] 增加“重新生成AI推荐”接口（针对驳回项）。
- [x] 为 `ai_recipe_library` 增加简单命中统计字段（hit_count）。

## 4. 接口约定（当前版本）

### 4.1 AI推荐
- `POST /api/ai/recommend?query=xxx`
- 响应：
  - `source`: `DB | AI`
  - `disclaimer`: 提示文案
  - `recipe`: `{ title, desc, tags, image, steps[] }`

### 4.2 AI审核
- `GET /api/ai/review/list?status=PENDING|APPROVED|REJECTED`
- `PUT /api/ai/review/approve/{id}?reviewer=admin`
- `PUT /api/ai/review/reject/{id}?reviewer=admin&remark=...`

### 4.3 测试接口（开发态）
- `POST /api/ai/test-llm?query=...`

## 5. 数据表说明（关键字段）

### 5.1 `ai_recommend_review`
- `query_text`：用户问题原文。
- `recipe_json`：AI结果（JSON字符串）。
- `status`：`PENDING/APPROVED/REJECTED`。
- `reviewer/review_remark/reviewed_at`：审核信息。

### 5.2 `ai_recipe_library`
- `query_text`：可用于后续命中策略。
- `title`：菜谱标题。
- `tags_json`：标签数组。
- `recipe_json`：完整结构化内容。
- `hit_count`：命中统计（默认0）。

## 6. 联调与验收标准

### 6.1 单条闭环验收
1. 提问一个未命中 query（如“黑松露炖藜麦”）。
2. `/api/ai/recommend` 返回 `source=AI`。
3. `ai_recommend_review` 产生 `PENDING` 记录。
4. 管理端点击“通过”。
5. `ai_recipe_library` 出现入库记录。

### 6.2 回归验收
- 订单创建、状态流转、订单详情不受 AI 改动影响。
- Redis 与 RabbitMQ 服务异常时，系统有明确报错，不应 silent fail。

## 7. 开发规范（本阶段）

- 每次只改一个功能点，改完即本地验证。
- 提交前至少跑：
  - `mvn -DskipTests compile`
- commit 前缀建议：
  - `feat(ai): ...`
  - `fix(ai): ...`
  - `chore(doc): ...`

## 8. 建议提交拆分

1. `feat(ai): guard test endpoint and normalize llm error handling`
2. `feat(ai-review): add validation and idempotent approve flow`
3. `feat(ai-review): add paged list api`
4. `chore(doc): update backend closure checklist`

---

如需进入“答辩材料模式”，下一份文档建议新增：
- 《后台模块答辩讲解稿（5分钟版）》
- 《问题-方案-效果证据清单（截图位）》
