# TestInjector - 轻量级测试依赖注入框架

**版本**: 1.0.0-SNAPSHOT
**作者**: zhourui(V33215020)
**日期**: 2025-10-26

---

## 简介

TestInjector 是一个轻量级的测试依赖注入框架，专为简化 Java 单元测试中的对象创建和 Mock 配置而设计。

### 核心价值

- **🚀 速度提升 1000 倍**: 毫秒级启动 vs Spring Test 的秒级启动
- **✂️ 代码减少 80%**: 自动依赖注入 vs 手动创建
- **🎯 零学习成本**: 熟悉 Mockito 即可使用
- **🔧 极致灵活**: 支持注解、Builder、混合三种方式

### 适用场景

- ✅ Manager 层单元测试
- ✅ Service 层单元测试
- ✅ 复杂依赖链测试
- ✅ 需要混合 Mock 和真实对象的场景

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.oppo.usercenter</groupId>
    <artifactId>test-injector-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 2. 编写测试

```java
@ExtendWith(TestInjectorExtension.class)
class QuickStartTest {

    @Mock
    private ExternalService mockExternal;

    @InjectMocks
    private BusinessManager manager;

    @Test
    void test() {
        when(mockExternal.getData()).thenReturn("test data");

        manager.process();

        verify(mockExternal).getData();
    }
}
```

---

## 三种使用方式

### 方式 1: Mockito 注解（推荐）

最简洁的方式，适合大多数场景。

```java
@ExtendWith(TestInjectorExtension.class)
class AnnotationStyleTest {

    @Mock
    private UserGroupGatewayService mockUserGroupService;

    @Spy
    private PlanConfigManager spyPlanConfigManager;

    @InjectMocks
    private AudienceFilterManager manager;

    @Test
    void test() {
        when(mockUserGroupService.batchMatch(...)).thenReturn(...);

        UsergroupMatchResult result = manager.matchOnline(...);

        assertThat(result.isMatched()).isTrue();
        verify(spyPlanConfigManager).getPlanAudienceListFromCache(1);
    }
}
```

**优点**:
- ✅ 代码最简洁
- ✅ 熟悉 Mockito 即可使用
- ✅ 支持测试基类复用

### 方式 2: Builder API

适合需要动态配置或无法使用注解的场景。

```java
@Test
void builderStyleTest() {
    AudienceFilterManager manager = TestInjector.builder()
        .mock(UserGroupGatewayService.class)
        .mockWith(PlanConfigManager.class, mock -> {
            when(mock.getPlanAudienceListFromCache(anyInt()))
                .thenReturn(Collections.emptyList());
        })
        .autoCreate(AudienceFilterManager.class)
        .build();

    // 使用 manager 进行测试
}
```

**优点**:
- ✅ 流畅的 API
- ✅ 无需注解
- ✅ 灵活控制

### 方式 3: 混合使用

结合注解和 Builder 的优点。

```java
@ExtendWith(TestInjectorExtension.class)
class MixedStyleTest {

    @Mock private UserGroupGatewayService mockUserGroupService;
    @InjectMocks private AudienceFilterManager manager;

    @Test
    void test() {
        // 使用注解创建的 Mock
        when(mockUserGroupService.batchMatch(...)).thenReturn(...);

        // 通过 Builder 补充配置
        TestInjector.builder()
            .mockWith(BlacklistManager.class, mock -> {
                when(mock.queryBlacklist(any())).thenReturn(null);
            });
    }
}
```

**优点**:
- ✅ 最灵活
- ✅ 结合两种方式的优点

---

## 核心功能

### 1. 自动依赖注入

TestInjector 会自动分析构造器参数并递归创建依赖。

```java
// 自动创建 SimpleManager 及其依赖 SimpleService
SimpleManager manager = TestInjector.createInstance(SimpleManager.class);
```

**决策流程**:
1. 接口/抽象类 → 自动创建 Mock
2. 具体类 → 创建真实对象（递归创建依赖）
3. 用户注册的实例 → 使用用户实例

### 2. 智能 Mock (SmartAnswer)

自动返回合理的默认值，减少手动 `when()` 配置。

```java
TestInjector injector = TestInjector.builder()
    .enableSmartMock()
    .buildInjector();

SimpleService service = injector.getInstance(SimpleService.class);

// 自动返回空字符串而不是 null
assertThat(service.getData()).isEqualTo("");
```

**支持的类型**:
- `boolean` / `Boolean` → `false`
- `int` / `Integer` / `long` / `Long` → `0` / `0L`
- `String` → `""`
- `List` / `Set` / `Map` → 空集合
- `Optional` → `Optional.empty()`

### 3. 循环依赖检测

自动检测并报告循环依赖，提供清晰的错误信息。

```
CircularDependencyException: 检测到循环依赖

依赖路径:
  com.oppo.usercenter.A
  → com.oppo.usercenter.B
  → com.oppo.usercenter.C
  → com.oppo.usercenter.A  ← 循环开始

建议解决方案:
  1. 重新设计依赖关系，打破循环
  2. 使用 @Spy 替代部分 @InjectMocks
  3. 使用 Setter 注入（未来版本支持）
```

### 4. 构造器选择策略

选择参数最多的构造器（模拟 Spring 行为）。

**优先级**:
1. 优先选择 `public` 构造器中参数最多的
2. 如果没有 `public` 构造器，选择所有构造器中参数最多的
3. 使用反射 `setAccessible(true)` 访问私有构造器

---

## 注解详解

### @Mock vs @Spy vs 无注解

| 注解 | 对象类型 | 默认行为 | 可部分 Mock | 可 verify | 使用场景 |
|------|---------|---------|------------|----------|---------|
| `@Mock` | Mock 对象 | 返回默认值 | ✅ 全部 Mock | ✅ 可以 | 外部服务、DAO |
| `@Spy` | Spy 对象 | 调用真实方法 | ✅ 可部分 Mock | ✅ 可以 | 需要监控的内部服务 |
| 无注解 | 真实对象 | 调用真实方法 | ❌ 不可 Mock | ❌ 不可以 | 完全信任的内部逻辑 |

### 决策树

```
依赖类型判断：

1. 是外部系统？（Dubbo/HTTP/MQ/DAO）
   YES → 使用 @Mock
   NO  → 继续

2. 需要验证调用或部分 Mock？
   YES → 使用 @Spy
   NO  → 继续

3. 完全信任真实逻辑
   → 无注解（TestInjector 自动创建真实对象）
```

---

## Builder API 详解

### 基础方法

```java
TestInjector.Builder builder = TestInjector.builder();

// 强制 Mock 指定类型
builder.mock(UserGroupGatewayService.class, PlanConfigManager.class);

// 强制创建真实对象
builder.real(BlacklistManager.class);

// 注册用户实例
SimpleService customService = mock(SimpleService.class);
builder.instance(SimpleService.class, customService);

// 启用智能 Mock
builder.enableSmartMock();

// 启用调试日志
builder.enableDebug();
```

### mockWith 方法

创建 Mock 对象并立即配置。

```java
TestInjector.builder()
    .mockWith(UserGroupGatewayService.class, mock -> {
        when(mock.batchMatch(any(), any(), any(), any(), anyBoolean()))
            .thenAnswer(invocation -> {
                // 自定义逻辑
            });
    })
    .autoCreate(AudienceFilterManager.class)
    .build();
```

### TypedBuilder

`autoCreate()` 返回 TypedBuilder，提供类型安全的链式调用。

```java
SimpleManager manager = TestInjector.builder()
    .autoCreate(SimpleManager.class)  // 返回 TypedBuilder<SimpleManager>
    .mock(SimpleService.class)
    .enableSmartMock()
    .build();  // 返回 SimpleManager
```

---

## 最佳实践

### ✅ 推荐

```java
// ✅ 外部服务用 @Mock
@Mock private ExternalService mockExternal;

// ✅ 需要 verify 或部分 Mock 用 @Spy
@Spy private InternalService spyInternal;

// ✅ 完全真实逻辑无注解（TestInjector 自动创建）
// private ConfigManager configManager;

// ✅ 推荐的测试结构
@ExtendWith(TestInjectorExtension.class)
class RecommendedTestStructure {

    // ========== 外部依赖（Mock） ==========
    @Mock private UserGroupGatewayService mockUserGroupService;
    @Mock private ReachMqGatewayService mockReachMqService;

    // ========== 关键内部组件（Spy） ==========
    @Spy private PlanConfigManager spyPlanConfigManager;

    // ========== 被测对象 ==========
    @InjectMocks
    private ResourcePullDispatcher dispatcher;

    @Test
    void should_XXX_when_YYY() {
        // Given
        // When
        // Then
    }
}
```

### ❌ 避免

```java
// ❌ 不要过度使用 @Spy
@Spy private Service1 spy1;
@Spy private Service2 spy2;
@Spy private Service3 spy3;  // 如果不需要 verify，用真实对象即可

// ❌ 不要手动创建所有依赖
ExternalService mock = mock(ExternalService.class);
MyManager manager = new MyManager(mock, ...);  // 失去自动化优势

// ❌ 不要在 @BeforeEach 中创建对象
@BeforeEach
void setUp() {
    manager = TestInjector.createInstance(MyManager.class);  // 用注解更简洁
}
```

---

## FAQ

### Q1: TestInjector 与 MockitoExtension 有什么区别？

**A**: 主要区别在依赖注入能力

| 维度 | MockitoExtension | TestInjectorExtension |
|------|-----------------|----------------------|
| Mock 创建 | ✅ 自动 | ✅ 自动（+ 智能默认值） |
| 依赖注入 | ⚠️ 仅支持简单场景 | ✅ 智能递归创建 |
| 复杂依赖链 | ❌ 需手动创建 | ✅ 自动递归 |
| 配置优化 | ❌ 无 | ✅ 预定义配置 + Builder |

### Q2: 如何处理循环依赖？

**A**: 三种方案
1. **重新设计**（推荐）：打破循环依赖
2. **使用 @Spy**：手动创建其中一个
3. **重构代码**：使用 Setter 注入（未来版本支持）

### Q3: 支持 MyBatis-Plus Lambda 查询吗？

**A**: ✅ 完全支持

Mock Mapper 接口，使用真实实体类，Lambda 查询正常工作。

```java
@Mock
private UserMapper mockUserMapper;  // Mock Mapper

@InjectMocks
private UserService userService;     // 真实 Service

@Test
void test() {
    User user = new User();
    user.setName("Alice");

    when(mockUserMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(Collections.singletonList(user));

    // Service 内部的 Lambda 查询正常工作 ✅
    List<User> result = userService.queryByName("Alice");

    assertThat(result).hasSize(1);
}
```

### Q4: 性能如何？

**A**:
- 单次创建：~1-5ms（vs `new` ~0.01ms）
- 启动时间：0（vs Spring Test 3-10s）
- **比 Spring Test 快 1000 倍**

### Q5: 如何迁移现有测试？

**A**: 渐进式迁移
1. 新测试：直接使用 TestInjector
2. 旧测试：逐步替换 `@ExtendWith(MockitoExtension.class)` 为 `@ExtendWith(TestInjectorExtension.class)`
3. 不强制迁移：两种方式可以共存

---

## 技术架构

```
┌─────────────────────────────────────────────────┐
│                 使用层                           │
│  - JUnit 5 测试类                                │
│  - @ExtendWith / Builder API / 静态方法          │
└─────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│              TestInjector 核心层                 │
│  - 注解处理器（@Mock/@Spy/@InjectMocks）         │
│  - Builder API（流畅接口）                       │
│  - 依赖解析引擎                                  │
└─────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│               对象创建层                         │
│  - Mock 创建（Mockito）                          │
│  - Spy 创建                                      │
│  - 真实对象创建（反射）                          │
│  - 智能 Answer（SmartAnswer）                    │
└─────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│               基础设施层                         │
│  - 反射工具（ReflectionUtils）                   │
│  - 单例缓存（ConcurrentHashMap）                 │
│  - 循环依赖检测（ThreadLocal）                   │
└─────────────────────────────────────────────────┘
```

---

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

内部项目，仅限公司内部使用。

---

**维护者**: zhourui(V33215020)
**更新日期**: 2025-10-26
