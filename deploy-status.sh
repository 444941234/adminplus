#!/bin/bash

echo "=== AdminPlus 部署状态检查 ==="
echo "检查时间: $(date)"
echo ""

# 检查Docker容器状态
echo "1. Docker容器状态:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# 检查后端服务健康状态
echo "2. 后端服务健康检查:"
curl -s http://localhost:8081/api/actuator/health | jq . 2>/dev/null || echo "后端服务未响应"
echo ""

# 检查前端服务健康状态
echo "3. 前端服务健康检查:"
curl -s http://localhost/ | head -n 5 2>/dev/null || echo "前端服务未响应"
echo ""

echo "=== 部署状态检查完成 ==="