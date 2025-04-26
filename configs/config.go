package configs

import (
	"os"
	"strconv"
)

// Config 应用配置
type Config struct {
	Port int
	// 添加更多配置项...
}

// NewConfig 创建默认配置
func NewConfig() *Config {
	port := 8080
	if portStr := os.Getenv("APP_PORT"); portStr != "" {
		if p, err := strconv.Atoi(portStr); err == nil {
			port = p
		}
	}

	return &Config{
		Port: port,
	}
}
