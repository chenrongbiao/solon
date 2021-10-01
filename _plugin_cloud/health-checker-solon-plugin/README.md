<h1 align="center">Solon Health Checks</h1>

<div align="center">
Author noear，iYarnFog
</div>

## ✨ 特性

- 🌈 一行代买添加检查点，侵入性极低。
- 📦 开箱即用的高质量组件。

## 📦 安装

```xml
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>health-checker-solon-plugin</artifactId>
</dependency>
```

## ⚙️ 配置

```yaml
# No configuration.
```

## 🔨 示例

```java
import org.noear.solon.cloud.extend.health.HealthChecker;

import org.noear.solon.core.handle.Result;

public class Test {
    public Test() {
        HealthChecker.addPoint("preflight", Result::succeed);
        HealthChecker.addPoint("test", Result::failure);
        HealthChecker.addPoint("boom", () -> { throw new IllegalStateException(); });
    }
}
```

```text
GET /healthz
Response Code:
200 : Everything is fine
503 : At least one procedure has reported a non-healthy state
500 : One procedure has thrown an error or has not reported a status in time
Response:
{"checks":[{"name":"test","status":1},{"name":"boom","status":2},{"name":"preflight","status":0}],"outcome":2}
```