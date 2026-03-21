# Lesson 01：先看懂后端现有登录链路

## 这节课学什么

这节课不让你上来就写订单和拼团。
先做一件更重要的事：看懂这个 Spring Boot 项目里，**一条已经跑通的业务链路**到底是怎么走的。

我们选的样本是：`用户登录 / 获取用户信息`。

因为它已经具备了后端开发最核心的几层：

- `Controller`
- `Service`
- `Mapper`
- `Entity`
- `DTO`
- `Result`
- `Redis`
- `JWT`

你把这条链路看懂，后面补订单、拼团、后台管理，思路都会顺很多。

## 你现在的后端项目是什么结构

项目根包在：

`src/main/java/com/langkeyo`

目前主要目录有：

- `common`：放通用返回结构，比如 `Result`、`ResultCode`
- `config`：放配置，比如 CORS、MyBatis-Plus、Redis
- `controller`：接口入口，负责接收请求
- `dto`：数据传输对象，负责给前端返回更合适的数据结构
- `entity`：数据库实体，对应表结构
- `mapper`：数据库访问层
- `service`：业务层接口
- `service/impl`：业务层实现
- `utils`：工具类，比如 `JwtUtil`、`WechatUtil`

这就是一个典型的 Spring Boot 分层项目。

## 先记一句最重要的话

后端里也有和前端很像的“分层思想”：

- 前端：`page -> service -> api`
- 后端：`controller -> service -> mapper -> database`

你可以把它理解成：

- 前端页面不该直接写接口细节
- 后端控制器也不该直接写复杂业务和 SQL

本质是一回事：**入口层只负责接住请求，业务细节下沉。**

## 登录链路怎么走

### 第 1 层：Controller 接请求

文件：`src/main/java/com/langkeyo/controller/UserController.java`

你先重点看这两个接口：

- `POST /api/user/login`
- `GET /api/user/info`

### 你要重点观察什么

#### 1. `login(@RequestParam String code)`

这里的意思是：
前端把小程序 `wx.login()` 拿到的 `code` 传给后端。

Controller 做了两件事：

1. 判断 `code` 是否为空
2. 调 `userService.login(code)`

也就是说，Controller 自己**没有写登录业务细节**。
它只是：

- 收参数
- 做最基础校验
- 调业务层
- 返回统一结果

这和你前端学过的一样：

**入口层负责接住，不负责把所有事都干完。**

#### 2. `getUserInfo(@RequestHeader("Authorization") String token)`

这里说明：
后端会从请求头里拿 `Authorization`。

然后它做了这几步：

1. 去掉 `Bearer ` 前缀
2. 调 `userService.getUserByToken(token)`
3. 如果查不到用户，就返回 `USER_NOT_FOUND`
4. 查到了就返回成功结果

这里你要开始记住一个后端常见流程：

`token -> userId -> 查用户 -> 返回 DTO`

## 第 2 层：Service 是真正的业务层

文件：`src/main/java/com/langkeyo/service/IUserService.java`

这里定义了两个核心方法：

- `LoginResponseDTO login(String code)`
- `UserDTO getUserByToken(String token)`

### 为什么要先写接口再写实现

因为接口的意义是：

**先把“能力边界”定义出来。**

也就是说，先想清楚：

- 用户服务应该提供什么能力
- 输入是什么
- 输出是什么

而不是一上来就把逻辑直接堆在实现类里。

这个习惯和你前端先抽 `service/order.ts` 的思路是一致的。

## 第 3 层：ServiceImpl 里才有真正业务细节

文件：`src/main/java/com/langkeyo/service/impl/UserServiceImpl.java`

这是这节课最值得你反复看的文件。

### `login(String code)` 的业务顺序

你可以按下面这条线去理解：

1. 调微信接口换 `openid`
2. 根据 `openid` 查数据库里有没有这个用户
3. 没有就自动注册一个用户
4. 生成 JWT token
5. 把 token 放到 Redis
6. 把 `User` 转成 `UserDTO`
7. 返回 `LoginResponseDTO`

这条链路特别像你前端刚学会的“成功主链路”。

只是名字变了：

- 前端：校验 -> 提交 -> 成功收尾
- 后端：查微信 -> 查用户 -> 注册 -> 发 token -> 缓存 -> 返回 DTO

你应该开始感受到：

**不管前端还是后端，本质都是在组织业务步骤。**

### 这里你要重点理解 4 个点

#### 1. 为什么先查 `openid`

因为在微信小程序登录体系里，`openid` 是用户在当前小程序下的唯一标识。

后端不是靠前端传用户名密码认人，而是靠微信返回的身份标识认人。

#### 2. 为什么要“查不到就注册”

这是小程序很常见的自动注册思路。

用户第一次登录时，数据库没有这条用户记录。
那就创建一条默认用户数据。

所以这里的“登录”其实包含了两件事：

- 老用户登录
- 新用户自动注册后登录

#### 3. 为什么返回 DTO，不直接返回 Entity

`User` 是数据库实体。
`UserDTO` 是给前端返回的数据对象。

这样做的原因是：

- 数据库字段不一定都适合直接给前端
- 可以控制返回字段
- 避免把一些不该暴露的字段直接抛出去

你以后会经常看到：

- `Entity`：对数据库
- `DTO`：对数据传输
- `VO`：对页面展示

#### 4. 为什么 token 还要放 Redis

因为单纯生成 JWT 还不够。
把 token 放到 Redis，后面可以做：

- 登录态控制
- 过期控制
- 踢下线
- 多端状态管理

这也和你前端“本地缓存 + 全局状态”的思维可以对照着理解。

## 第 4 层：Mapper 是怎么工作的

文件：`src/main/java/com/langkeyo/mapper/UserMapper.java`

这里你会发现，`UserMapper` 很短，几乎没写 SQL。

原因是它继承了：

`BaseMapper<User>`

这意味着 MyBatis-Plus 已经帮你准备好了常见 CRUD。

所以你现在看到的：

- `this.getById(...)`
- `this.getOne(...)`
- `this.save(...)`

这些很多都不是手写 SQL，而是 MyBatis-Plus 提供出来的能力。

这对你现在学习特别友好，因为你不用一上来就陷进 XML SQL 里。

## 第 5 层：Entity 是数据库映射

文件：`src/main/java/com/langkeyo/entity/User.java`

你现在重点看这几个字段：

- `id`
- `openid`
- `nickname`
- `avatar`
- `mobile`
- `isLeader`
- `createTime`
- `updateTime`
- `deleted`

### 这里你要特别记住两个点

#### 1. `mobile` 用的是 `String`

这和你前端刚刚问的手机号为什么用字符串是完全一致的。

手机号虽然长得像数字，但它本质上是标识符，不是拿来算术运算的。
所以前后端都更适合用字符串处理。

#### 2. `@TableLogic`

这个表示逻辑删除。

意思不是把数据真删掉，而是打一个删除标记。

这在真实项目里很常见，因为很多业务数据不能真的物理删除。

## 这节课你先不要做什么

先不要急着：

- 新建订单 Controller
- 新建拼团 Service
- 写一堆接口
- 直接上 Redis 分布式锁

你现在最重要的是：

**先把现有这条用户链路看懂。**

## 你的观察作业

你现在只做这 3 件事：

1. 自己顺着代码走一遍 `POST /api/user/login` 的调用链
2. 用自己的话说清楚：`Controller`、`Service`、`Mapper` 分别干什么
3. 回答一个问题：为什么 `UserController` 里不直接写数据库查询逻辑

## 你回答时可以直接按这个模板

```md
### 我理解的登录调用链
1. 前端把 code 传给 `/api/user/login`
2. Controller 接住参数并做基础校验
3. Service 调微信接口获取 openid
4. Service 根据 openid 查用户
5. 不存在则自动注册
6. 生成 token 并写入 Redis
7. 返回 token 和用户信息给前端

### 我理解的三层职责
- Controller：
- Service：
- Mapper：

### 为什么 Controller 不直接查数据库
- 
```

## 下一课预告

下一课我们会继续两种可能路径里的一个：

1. 继续深挖用户登录链路，把 `DTO / Entity / Result` 的边界讲透
2. 开始设计第一个和前端直接对接的新业务模块，比如订单或拼团

你做完这节的观察作业后，我再带你定下一步。
