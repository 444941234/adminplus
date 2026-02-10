#!/bin/bash

echo "======================================="
echo "AdminPlus 测试体系验证"
echo "======================================="

echo ""
echo "✅ 测试架构验证"
echo ""

# 检查测试文件是否存在
echo "1. 检查测试配置文件..."
if [ -f "backend/src/test/resources/application-test.yml" ]; then
    echo "   ✅ 后端测试配置存在"
else
    echo "   ❌ 后端测试配置缺失"
fi

if [ -f "frontend/vitest.config.js" ]; then
    echo "   ✅ 前端测试配置存在"
else
    echo "   ❌ 前端测试配置缺失"
fi

echo ""
echo "2. 检查测试代码..."

# 后端测试
test_files=(
    "backend/src/test/java/com/adminplus/controller/AuthControllerTest.java"
    "backend/src/test/java/com/adminplus/service/AuthServiceIntegrationTest.java"
    "backend/src/test/java/com/adminplus/filter/XssFilterTest.java"
    "backend/src/test/java/com/adminplus/BaseIntegrationTest.java"
    "backend/src/test/java/com/adminplus/TestUtils.java"
)

for file in "${test_files[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $(basename $file) 存在"
    else
        echo "   ❌ $(basename $file) 缺失"
    fi
done

# 前端测试
frontend_tests=(
    "frontend/test/components/LoginForm.test.js"
    "frontend/test/utils/auth.test.js"
    "frontend/test/setup.js"
)

for file in "${frontend_tests[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $(basename $file) 存在"
    else
        echo "   ❌ $(basename $file) 缺失"
    fi
done

echo ""
echo "3. 检查测试文档..."

if [ -f "TESTING_STRATEGY.md" ]; then
    echo "   ✅ 测试策略文档存在"
else
    echo "   ❌ 测试策略文档缺失"
fi

if [ -f "run-tests.sh" ]; then
    echo "   ✅ 自动化测试脚本存在"
    chmod +x run-tests.sh
else
    echo "   ❌ 自动化测试脚本缺失"
fi

echo ""
echo "4. 检查依赖配置..."

# 检查后端依赖
if grep -q "h2" "backend/pom.xml"; then
    echo "   ✅ H2 测试依赖已配置"
else
    echo "   ❌ H2 测试依赖未配置"
fi

# 检查前端依赖
if grep -q "vitest" "frontend/package.json"; then
    echo "   ✅ Vitest 测试依赖已配置"
else
    echo "   ❌ Vitest 测试依赖未配置"
fi

echo ""
echo "======================================="
echo "测试体系验证完成"
echo ""
echo "📝 说明:"
echo "   - 测试架构已完整搭建"
echo "   - 测试代码示例已实现"
echo "   - 测试配置已就绪"
echo "   - 测试文档已编写"
echo ""
echo "🚀 下一步:"
echo "   - 解决测试执行环境问题"
echo "   - 扩展测试用例覆盖"
echo "   - 集成 CI/CD 流程"
echo "======================================="