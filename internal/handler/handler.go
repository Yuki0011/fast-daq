package handler

import (
	"net/http"
)

// Handler 处理HTTP请求的接口
type Handler interface {
	ServeHTTP(w http.ResponseWriter, r *http.Request)
}

// New 创建一个新的处理器
func New() Handler {
	return &baseHandler{}
}

type baseHandler struct{}

func (h *baseHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	// 处理HTTP请求
}
