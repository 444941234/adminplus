#!/bin/bash

echo "======================================="
echo "AdminPlus 后端项目快速检查"
echo "======================================="

echo ""
echo "1. 检查项目结构..."
ls -la src/main/java/com/adminplus/
echo ""

echo "2. 检查关键依赖..."
grep -E "spring-boot-starter|spring-boot-starter-test|junit|lombok" pom.xml
echo ""

echo "3. 检查测试结构..."
ls -la src/test/java/com/adminplus/
echo ""

echo "4. 检查配置文件..."
ls -la src/main/resources/ src/test/resources/
echo ""

echo "5. 检查编译状态..."
mvn dependency:resolve -q
echo ""

echo "======================================="
echo "快速检查完成"
echo "======================================="