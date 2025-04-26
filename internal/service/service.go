package service

// Service 服务层接口
type Service interface {
	// 添加方法...
}

// New 创建一个新的服务实例
func New() Service {
	return &service{}
}

type service struct{}

// 在这里实现 Service 接口的方法...
