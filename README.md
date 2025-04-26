# Fast-DAQ

快速数据采集(Fast-DAQ)是一个基于Go语言开发的单体应用程序。

## 项目结构

```
fast-daq/
├── api/           # API定义和文档
├── build/         # 构建和CI文件
├── cmd/           # 命令行应用程序
│   └── fast-daq/  # 主应用程序
├── configs/       # 配置文件
├── docs/          # 文档
├── internal/      # 私有应用代码
│   ├── handler/   # HTTP处理器
│   └── service/   # 业务逻辑
├── pkg/           # 公共包
│   └── models/    # 数据模型
├── test/          # 测试文件
└── web/           # Web相关资源
```

## 快速开始

1. 构建项目:
```
go build -o fast-daq ./cmd/fast-daq
```

2. 运行应用:
```
./fast-daq
```

默认情况下，应用程序将在 http://localhost:8080 上运行。
