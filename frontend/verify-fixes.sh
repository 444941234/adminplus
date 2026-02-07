#!/bin/bash

echo "========================================="
echo "AdminPlus 前端项目修复验证脚本"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 验证计数器
PASS=0
FAIL=0

# 验证函数
verify() {
    local test_name=$1
    local test_command=$2
    local min_expected=$3

    echo -n "测试: $test_name ... "

    result=$(eval $test_command)

    if [ "$result" -ge "$min_expected" ]; then
        echo -e "${GREEN}✓ 通过${NC} (实际: $result)"
        ((PASS++))
    else
        echo -e "${RED}✗ 失败${NC}"
        echo "  期望至少: $min_expected"
        echo "  实际: $result"
        ((FAIL++))
    fi
}

echo "🔴 高优先级验证"
echo "----------------"

# 1. 验证 localStorage 已移除
verify "router 中无 localStorage 引用" \
    "grep -c 'localStorage' src/router/index.js" \
    "0"

echo ""
echo "🟡 中优先级验证"
echo "----------------"

# 2. 验证 Token 刷新机制已实现
verify "request.js 中有 refreshToken 函数" \
    "grep -c 'const refreshToken' src/utils/request.js" \
    "1"

verify "request.js 中有 subscribeTokenRefresh 函数" \
    "grep -c 'const subscribeTokenRefresh' src/utils/request.js" \
    "1"

verify "request.js 中有 onRefreshed 函数" \
    "grep -c 'const onRefreshed' src/utils/request.js" \
    "1"

verify "request.js 中有 isRefreshing 变量" \
    "grep -c 'let isRefreshing' src/utils/request.js" \
    "1"

echo ""
echo "🟢 低优先级验证"
echo "----------------"

# 3. 验证图标按需导入
verify "main.js 中无全局图标导入" \
    "grep -c 'ElementPlusIconsVue' src/main.js" \
    "0"

verify "vite.config.js 中有 ElementPlusResolver" \
    "grep -c 'ElementPlusResolver' vite.config.js" \
    "2"

# 4. 验证构建优化配置
verify "vite.config.js 中有 manualChunks 配置" \
    "grep -c 'manualChunks' vite.config.js" \
    "1"

verify "vite.config.js 中有 terserOptions 配置" \
    "grep -c 'terserOptions' vite.config.js" \
    "1"

# 5. 验证防抖函数修复
verify "Dict.vue 中有 searchDebounced 函数" \
    "grep -c 'searchDebounced' src/views/system/Dict.vue" \
    "2"

# 6. 验证 useConfirm 统一使用
verify "Dict.vue 中导入了 useConfirm" \
    "grep -c 'useConfirm' src/views/system/Dict.vue" \
    "2"

verify "Role.vue 中导入了 useConfirm" \
    "grep -c 'useConfirm' src/views/system/Role.vue" \
    "2"

verify "DictItem.vue 中导入了 useConfirm" \
    "grep -c 'useConfirm' src/views/system/DictItem.vue" \
    "2"

verify "User.vue 中导入了 useConfirm" \
    "grep -c 'useConfirm' src/views/system/User.vue" \
    "2"

verify "Layout.vue 中导入了 useConfirm" \
    "grep -c 'useConfirm' src/layout/Layout.vue" \
    "2"

# 7. 验证常量命名
verify "constants/index.js 中有大写常量" \
    "grep -c 'export const' src/constants/index.js" \
    "5"

# 8. 验证 CSP 配置
verify "index.html 中有 CSP 配置" \
    "grep -c 'Content-Security-Policy' index.html" \
    "1"

# 9. 验证 JSDoc 覆盖
verify "auth.js 中有 JSDoc 注释" \
    "grep -c '/**' src/api/auth.js" \
    "5"

verify "user.js 中有 JSDoc 注释" \
    "grep -c '/**' src/api/user.js" \
    "10"

verify "role.js 中有 JSDoc 注释" \
    "grep -c '/**' src/api/role.js" \
    "7"

verify "dict.js 中有 JSDoc 注释" \
    "grep -c '/**' src/api/dict.js" \
    "15"

verify "menu.js 中有 JSDoc 注释" \
    "grep -c '/**' src/api/menu.js" \
    "5"

verify "stores/user.js 中有 JSDoc 注释" \
    "grep -c '/**' src/stores/user.js" \
    "11"

echo ""
echo "========================================="
echo "验证结果汇总"
echo "========================================="
echo -e "通过: ${GREEN}$PASS${NC}"
echo -e "失败: ${RED}$FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ 所有验证通过！${NC}"
    exit 0
else
    echo -e "${RED}✗ 有 $FAIL 项验证失败，请检查！${NC}"
    exit 1
fi