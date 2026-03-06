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
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.key" class="stats-grid-item">
        <StatCard
          :type="stat.type"
          :icon="stat.icon"
          :value="stat.value"
          :label="stat.label"
          :trend="stat.trend"
          :trend-up="stat.trendUp"
          :loading="loading"
        />
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="charts-grid-item charts-grid-item-main">
        <BmCard title="用户增长趋势" shadow="small" class="chart-card">
          <div ref="userGrowthChartRef" class="chart-container"></div>
        </BmCard>
      </div>
      <div class="charts-grid-item charts-grid-item-side">
        <BmCard title="角色分布" shadow="small" class="chart-card">
          <div ref="roleDistributionChartRef" class="chart-container"></div>
        </BmCard>
      </div>
    </div>

    <div class="charts-grid charts-grid-full">
      <BmCard title="菜单类型分布" shadow="small" class="chart-card">
        <div ref="menuDistributionChartRef" class="chart-container chart-container-small"></div>
      </BmCard>
    </div>

    <!-- 快捷操作、系统信息 -->
    <div class="bottom-grid">
      <div class="bottom-grid-item bottom-grid-item-actions">
        <ActionCard
          title="快捷操作"
          :actions="quickActions"
          @action="handleQuickAction"
        />
      </div>

      <div class="bottom-grid-item bottom-grid-item-info">
        <BmCard title="系统信息" shadow="small" class="system-info-card">
          <div v-if="systemInfoLoading" class="system-info-loading">加载中...</div>
          <div v-else class="system-info">
            <div v-for="info in systemInfoList" :key="info.key" class="info-item">
              <BmIcon :icon="getIconSymbol(info.icon)" class="info-icon" />
              <div class="info-content">
                <div class="info-label">{{ info.label }}</div>
                <div class="info-value">{{ info.value }}</div>
              </div>
            </div>
          </div>
        </BmCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { WelcomeBanner, StatCard, ActionCard, BmCard, BmIcon } from '@adminplus/ui-vue';
import type { ActionItem } from '@adminplus/ui-vue';
import * as echarts from 'echarts';

defineOptions({
  name: 'Dashboard'
});

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const systemInfoLoading = ref(false);

// 图标符号映射
const getIconSymbol = (iconName: string): string => {
  const iconMap: Record<string, string> = {
    'House': '🏠',
    'Document': '📄',
    'Setting': '⚙️',
    'Clock': '🕐',
    'Monitor': '🖥️',
    'User': '👤',
    'UserFilled': '👥',
    'Menu': '📋',
    'Calendar': '📅',
    'Location': '📍',
    'Phone': '📞',
    'Email': '📧',
    'Lock': '🔒',
    'Unlock': '🔓',
    'View': '👁️',
    'Hide': '🙈',
    'Edit': '✏️',
    'Delete': '🗑️',
    'Add': '➕',
    'Remove': '➖',
    'Check': '✅',
    'Close': '❌',
    'Search': '🔍',
    'Filter': '🔽',
    'Sort': '🔼',
    'Refresh': '🔄',
    'Download': '⬇️',
    'Upload': '⬆️',
    'Share': '🔗',
    'Star': '⭐',
    'Heart': '❤️',
    'Bell': '🔔',
    'Info': 'ℹ️',
    'Warning': '⚠️',
    'Error': '❌',
    'Success': '✅',
    'Question': '❓',
    'Help': '❓'
  };
  return iconMap[iconName] || '•';
};

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
  // 用户增长趋势图 - 智谱AI风格
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
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0)' }
          ])
        },
        lineStyle: {
          color: '#1890ff',
          width: 3
        },
        itemStyle: {
          color: '#1890ff'
        }
      }]
    });
  }

  // 角色分布图 - 智谱AI风格
  if (roleDistributionChartRef.value) {
    roleDistributionChart = echarts.init(roleDistributionChartRef.value);
    roleDistributionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 1048, name: '管理员', itemStyle: { color: '#1890ff' } },
          { value: 735, name: '普通用户', itemStyle: { color: '#6366F1' } },
          { value: 580, name: '访客', itemStyle: { color: '#52c41a' } },
          { value: 484, name: '游客', itemStyle: { color: '#fa8c16' } }
        ]
      }]
    });
  }

  // 菜单类型分布图 - 智谱AI风格
  if (menuDistributionChartRef.value) {
    menuDistributionChart = echarts.init(menuDistributionChartRef.value);
    menuDistributionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 335, name: '目录', itemStyle: { color: '#1890ff' } },
          { value: 310, name: '菜单', itemStyle: { color: '#6366F1' } },
          { value: 234, name: '按钮', itemStyle: { color: '#52c41a' } }
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
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

// 统计卡片网格
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.stats-grid-item {
  min-width: 0;
}

// 图表网格
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-lg);

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.charts-grid-full {
  grid-template-columns: 1fr;
}

.charts-grid-item {
  min-width: 0;
}

.chart-card {
  height: 100%;

  :deep(.bm-card__body) {
    padding: var(--space-lg);
  }
}

.chart-container {
  height: 300px;

  @media (max-width: 768px) {
    height: 250px;
  }
}

.chart-container-small {
  height: 250px;

  @media (max-width: 768px) {
    height: 200px;
  }
}

// 底部网格
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--space-lg);

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.bottom-grid-item {
  min-width: 0;
}

.system-info-card {
  height: 100%;

  :deep(.bm-card__body) {
    padding: var(--space-lg);
  }
}

.system-info-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  color: var(--text-secondary);
}

.system-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md);

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);

  &:hover {
    background: var(--bg-tertiary);
    transform: translateY(-2px);
  }
}

.info-icon {
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--primary-gradient);
  color: var(--text-inverse);
  border-radius: var(--radius-md);
}

.info-content {
  flex: 1;
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

// 响应式调整
@media (max-width: 768px) {
  .dashboard {
    gap: var(--space-md);
  }

  .stats-grid,
  .charts-grid,
  .bottom-grid {
    gap: var(--space-md);
  }

  .info-item {
    padding: var(--space-sm);
  }

  .info-icon {
    width: 32px;
    height: 32px;
    font-size: 18px;
  }
}
</style>
