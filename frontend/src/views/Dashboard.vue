<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <WelcomeBanner
      :username="userStore.user?.nickname || 'Admin'"
      :greeting="greeting"
      action-text="查看详情"
      @action="handleBannerAction"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.key">
        <StatCard
          :type="stat.type"
          :icon="stat.icon"
          :value="stat.value"
          :label="stat.label"
          :trend="stat.trend"
          :trend-up="stat.trendUp"
          :loading="loading"
        />
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div ref="userGrowthChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>角色分布</span>
          </template>
          <div ref="roleDistributionChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <span>菜单类型分布</span>
          </template>
          <div ref="menuDistributionChartRef" class="chart-container chart-container-small"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作、系统信息 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="8">
        <ActionCard
          title="快捷操作"
          :actions="quickActions"
          @action="handleQuickAction"
        />
      </el-col>

      <el-col :span="16">
        <el-card class="system-info-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <div v-loading="systemInfoLoading" class="system-info">
            <el-row :gutter="20">
              <el-col :span="12" v-for="info in systemInfoList" :key="info.key">
                <div class="info-item">
                  <el-icon class="info-icon"><component :is="info.icon" /></el-icon>
                  <div class="info-content">
                    <div class="info-label">{{ info.label }}</div>
                    <div class="info-value">{{ info.value }}</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { WelcomeBanner, StatCard, ActionCard } from '@adminplus/ui-vue';
import type { ActionItem } from '@adminplus/ui-vue';
import * as echarts from 'echarts';

defineOptions({
  name: 'Dashboard'
});

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const systemInfoLoading = ref(false);

// 统计数据
const stats = ref([
  {
    key: 'users',
    type: 'primary' as const,
    icon: 'User',
    value: 1234,
    label: '用户总数',
    trend: '+12%',
    trendUp: true
  },
  {
    key: 'roles',
    type: 'success' as const,
    icon: 'UserFilled',
    value: 56,
    label: '角色数量',
    trend: '+5%',
    trendUp: true
  },
  {
    key: 'menus',
    type: 'warning' as const,
    icon: 'Menu',
    value: 23,
    label: '菜单数量',
    trend: '0%',
    trendUp: true
  },
  {
    key: 'logs',
    type: 'danger' as const,
    icon: 'Document',
    value: 856,
    label: '日志总数',
    trend: '-3%',
    trendUp: false
  }
]);

// 快捷操作
const quickActions: ActionItem[] = [
  { id: 'user', label: '添加用户', icon: 'User', type: 'primary' },
  { id: 'role', label: '添加角色', icon: 'UserFilled', type: 'success' },
  { id: 'menu', label: '添加菜单', icon: 'Menu', type: 'warning' },
  { id: 'system', label: '系统设置', icon: 'Setting', type: 'info' }
];

// 系统信息
const systemInfo = ref({
  systemName: 'AdminPlus',
  systemVersion: '1.0.0',
  osName: '',
  jdkVersion: '',
  uptime: 0
});

const systemInfoList = computed(() => [
  { key: 'name', label: '系统名称', value: systemInfo.value.systemName, icon: 'House' },
  { key: 'version', label: '系统版本', value: systemInfo.value.systemVersion, icon: 'Document' },
  { key: 'os', label: '操作系统', value: systemInfo.value.osName || '-', icon: 'Setting' },
  { key: 'jdk', label: 'JDK版本', value: systemInfo.value.jdkVersion || '-', icon: 'Document' },
  { key: 'uptime', label: '运行时间', value: formatUptime(systemInfo.value.uptime), icon: 'Clock' },
  { key: 'memory', label: '内存使用', value: '512 MB / 2048 MB', icon: 'Monitor' }
]);

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return '上午好，开始新的一天！';
  if (hour < 18) return '下午好，继续加油！';
  return '晚上好，注意休息！';
});

// 图表引用
const userGrowthChartRef = ref<HTMLElement>();
const roleDistributionChartRef = ref<HTMLElement>();
const menuDistributionChartRef = ref<HTMLElement>();
let userGrowthChart: echarts.ECharts | null = null;
let roleDistributionChart: echarts.ECharts | null = null;
let menuDistributionChart: echarts.ECharts | null = null;

// 初始化图表
const initCharts = () => {
  // 用户增长趋势图
  if (userGrowthChartRef.value) {
    userGrowthChart = echarts.init(userGrowthChartRef.value);
    userGrowthChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月']
      },
      yAxis: { type: 'value' },
      series: [{
        data: [120, 200, 150, 80, 70, 110],
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(74, 144, 226, 0.3)' },
            { offset: 1, color: 'rgba(74, 144, 226, 0)' }
          ])
        },
        lineStyle: {
          color: '#4a90e2'
        },
        itemStyle: {
          color: '#4a90e2'
        }
      }]
    });
  }

  // 角色分布图
  if (roleDistributionChartRef.value) {
    roleDistributionChart = echarts.init(roleDistributionChartRef.value);
    roleDistributionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 1048, name: '管理员' },
          { value: 735, name: '普通用户' },
          { value: 580, name: '访客' },
          { value: 484, name: '游客' }
        ]
      }]
    });
  }

  // 菜单类型分布图
  if (menuDistributionChartRef.value) {
    menuDistributionChart = echarts.init(menuDistributionChartRef.value);
    menuDistributionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 335, name: '目录' },
          { value: 310, name: '菜单' },
          { value: 234, name: '按钮' }
        ]
      }]
    });
  }
};

// 窗口大小变化时重绘图表
const handleResize = () => {
  userGrowthChart?.resize();
  roleDistributionChart?.resize();
  menuDistributionChart?.resize();
};

// 事件处理
const handleBannerAction = () => {
  console.log('Banner action clicked');
};

const handleQuickAction = (id: string) => {
  const routes: Record<string, string> = {
    user: '/system/user',
    role: '/system/role',
    menu: '/system/menu',
    system: '/system/config'
  };
  if (routes[id]) {
    router.push(routes[id]);
  }
};

const formatUptime = (seconds: number) => {
  if (!seconds) return '-';
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${days}天 ${hours}小时 ${minutes}分钟`;
};

const formatDate = () => {
  const now = new Date();
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  });
};

const fetchSystemInfo = async () => {
  systemInfoLoading.value = true;
  try {
    // TODO: 调用实际的 API
    // const response = await getSystemInfo();
    // systemInfo.value = response.data;
    systemInfo.value = {
      systemName: 'AdminPlus',
      systemVersion: '1.0.0',
      osName: 'Windows 11',
      jdkVersion: '21.0.1',
      uptime: 86400 * 3 + 3600 * 5
    };
  } finally {
    systemInfoLoading.value = false;
  }
};

onMounted(() => {
  initCharts();
  fetchSystemInfo();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  userGrowthChart?.dispose();
  roleDistributionChart?.dispose();
  menuDistributionChart?.dispose();
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped lang="scss">
.dashboard {
  padding: 0;
}

.stats-row,
.charts-row,
.bottom-row {
  margin-top: var(--space-lg);
}

.chart-card {
  @include card-style;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--border-color);
    padding: var(--space-md) var(--space-lg);
  }

  :deep(.el-card__body) {
    padding: var(--space-lg);
  }
}

.chart-container {
  height: 300px;
}

.chart-container-small {
  height: 250px;
}

.system-info-card {
  @include card-style;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--border-color);
  }
}

.system-info {
  .info-item {
    display: flex;
    align-items: center;
    gap: var(--space-md);
    padding: var(--space-md);
    margin-bottom: var(--space-sm);
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
  }

  .info-icon {
    font-size: 24px;
    color: var(--primary-color);
  }

  .info-label {
    font-size: 12px;
    color: var(--text-secondary);
  }

  .info-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
  }
}

@media (max-width: 767px) {
  .stats-row {
    :deep(.el-col) {
      margin-bottom: var(--space-md);
    }
  }

  .charts-row {
    :deep(.el-col) {
      margin-bottom: var(--space-md);
    }
  }
}
</style>
