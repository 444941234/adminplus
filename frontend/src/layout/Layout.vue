<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { Button, Avatar, AvatarFallback, DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger, Separator } from '@/components/ui'
import { toast } from 'vue-sonner'
import {
  LayoutDashboard,
  Users,
  Shield,
  Bell,
  Menu as MenuIcon,
  Building2,
  BookOpen,
  FileText,
  FolderOpen,
  Settings,
  LogOut,
  User,
  ChevronLeft,
  ChevronRight,
  BarChart3,
  FileBarChart,
  Workflow,
  Clock3,
  GitBranch,
  ChevronDown
} from '@lucide/vue'
import { useUserStore } from '@/stores/user'
import { resetDynamicRoutes } from '@/router'
import { useWorkflowNoticeCounts } from '@/composables/workflow/useWorkflowNoticeCounts'
import { getWorkflowPermissionState } from '@/lib/page-permissions'
import {
  buildOpenGroupState,
  getSidebarMenuItems,
  getSidebarTree,
  isRouteActive as checkRouteActive,
  type SidebarNode
} from '@/layout/sidebar'

const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)
const { pendingCount, ccUnreadCount, urgeUnreadCount, fetchCounts } = useWorkflowNoticeCounts()
const workflowPermissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))

const iconMap: Record<string, typeof LayoutDashboard> = {
  LayoutDashboard,
  Users,
  Shield,
  Bell,
  Menu: MenuIcon,
  HomeFilled: LayoutDashboard,
  User: Users,
  UserFilled: Shield,
  Building2,
  OfficeBuilding: Building2,
  BookOpen,
  Document: BookOpen,
  FileText,
  FolderOpen,
  DocumentCopy: FileText,
  Settings,
  Tools: Settings,
  DataLine: BarChart3,
  TrendCharts: BarChart3,
  DataAnalysis: FileBarChart,
  Workflow,
  Clock3,
  GitBranch
}

interface SidebarMenuItem {
  path: string
  icon: typeof LayoutDashboard
  label: string
}
const getMenuIcon = (icon?: string) => iconMap[icon || ''] || LayoutDashboard

const openGroups = ref<Record<string, boolean>>({})

type SidebarDisplayNode =
  | {
      kind: 'item'
      id: string
      path: string
      label: string
      icon: typeof LayoutDashboard
    }
  | {
      kind: 'group'
      id: string
      label: string
      icon: typeof LayoutDashboard
      children: SidebarDisplayNode[]
    }

const mapSidebarNode = (node: SidebarNode): SidebarDisplayNode => {
  if (node.kind === 'item') {
    return {
      ...node,
      icon: getMenuIcon(node.icon)
    }
  }

  return {
    ...node,
    icon: getMenuIcon(node.icon),
    children: node.children.map(mapSidebarNode)
  }
}

const sidebarDataTree = computed(() => getSidebarTree(userStore.menus))

const sidebarTree = computed<SidebarDisplayNode[]>(() => sidebarDataTree.value.map(mapSidebarNode))

const menuItems = computed<SidebarMenuItem[]>(() => {
  return getSidebarMenuItems(userStore.menus).map((menu) => ({
    path: menu.path,
    icon: getMenuIcon(menu.icon),
    label: menu.label
  }))
})

const isRouteActive = (path: string) => {
  return checkRouteActive(route.path, path)
}

const ensureActiveGroupsExpanded = () => {
  openGroups.value = buildOpenGroupState(sidebarDataTree.value, route.path, openGroups.value)
}

const toggleGroup = (group: { id: string }) => {
  openGroups.value[group.id] = !openGroups.value[group.id]
}

const isDisplayItem = (
  node: SidebarDisplayNode
): node is Extract<SidebarDisplayNode, { kind: 'item' }> => node.kind === 'item'

const isDisplayGroup = (
  node: SidebarDisplayNode
): node is Extract<SidebarDisplayNode, { kind: 'group' }> => node.kind === 'group'

const getSidebarItems = (nodes: SidebarDisplayNode[]) => nodes.filter(isDisplayItem)
const getSidebarGroups = (nodes: SidebarDisplayNode[]) => nodes.filter(isDisplayGroup)

watch(sidebarTree, ensureActiveGroupsExpanded, { immediate: true, deep: true })
watch(() => route.path, ensureActiveGroupsExpanded)
watch(() => route.path, fetchCounts)

const handleLogout = async () => {
  await userStore.logout()
  resetDynamicRoutes()
  toast.success('已退出登录')
  window.location.href = '/login'
}

onMounted(() => {
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
  fetchCounts()
})
</script>

<template>
  <div class="flex h-screen bg-background">
    <!-- 侧边栏 -->
    <aside
      :class="[
        'bg-card border-r transition-all duration-300 flex flex-col',
        collapsed ? 'w-16' : 'w-64'
      ]"
    >
      <!-- Logo -->
      <div :class="[
        'h-14 flex items-center justify-center border-b',
        collapsed ? 'px-2' : 'px-4'
      ]">
        <RouterLink
          to="/dashboard"
          class="flex items-center gap-2 font-bold text-lg text-primary"
        >
          <svg
            viewBox="0 0 32 32"
            class="w-7 h-7 flex-shrink-0"
          >
            <rect
              width="32"
              height="32"
              rx="6"
              fill="currentColor"
            />
            <path
              d="M8 12L16 8L24 12V20L16 24L8 20V12Z"
              fill="white"
            />
          </svg>
          <span v-if="!collapsed">AdminPlus</span>
        </RouterLink>
      </div>

      <!-- 菜单 -->
      <nav :class="[
        'flex-1 space-y-1 overflow-y-auto scrollbar-hide',
        collapsed ? 'p-2' : 'p-3'
      ]">
        <template v-if="collapsed">
          <div class="flex flex-col space-y-1">
            <RouterLink
              v-for="item in menuItems"
              :key="item.path"
              :to="item.path"
              :class="[
                'flex items-center justify-center py-2.5 rounded-md text-sm font-medium transition-colors',
                route.path === item.path
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
              ]"
            >
              <component
                :is="item.icon"
                class="w-5 h-5 flex-shrink-0"
              />
            </RouterLink>
          </div>
        </template>
        <template v-else>
          <template
            v-for="node in sidebarTree"
            :key="node.id"
          >
            <RouterLink
              v-if="node.kind === 'item'"
              :to="node.path"
              :class="[
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                isRouteActive(node.path)
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
              ]"
            >
              <component
                :is="node.icon"
                class="h-5 w-5 flex-shrink-0"
              />
              <span>{{ node.label }}</span>
            </RouterLink>

            <div
              v-else
              class="space-y-1"
            >
              <button
                type="button"
                class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm font-medium text-foreground transition-colors hover:bg-accent"
                :aria-expanded="openGroups[node.id]"
                :aria-label="`${node.label} ${openGroups[node.id] ? '收起' : '展开'}`"
                @click="toggleGroup(node)"
              >
                <component
                  :is="node.icon"
                  class="h-5 w-5 flex-shrink-0 text-muted-foreground"
                />
                <span class="flex-1">{{ node.label }}</span>
                <ChevronDown
                  :class="[
                    'h-4 w-4 text-muted-foreground transition-transform',
                    openGroups[node.id] ? 'rotate-180' : ''
                  ]"
                />
              </button>

              <div
                v-show="openGroups[node.id]"
                class="space-y-1 pl-4"
              >
                <template
                  v-for="child in node.children"
                  :key="child.id"
                >
                  <RouterLink
                    v-if="child.kind === 'item'"
                    :to="child.path"
                    :class="[
                      'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                      isRouteActive(child.path)
                        ? 'bg-primary/10 text-primary'
                        : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                    ]"
                  >
                    <component
                      :is="child.icon"
                      class="h-4 w-4 flex-shrink-0"
                    />
                    <span>{{ child.label }}</span>
                  </RouterLink>
                  <div
                    v-else
                    class="space-y-1"
                  >
                    <button
                      type="button"
                      class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
                      :aria-expanded="openGroups[child.id]"
                      :aria-label="`${child.label} ${openGroups[child.id] ? '收起' : '展开'}`"
                      @click="toggleGroup(child)"
                    >
                      <component
                        :is="child.icon"
                        class="h-4 w-4 flex-shrink-0"
                      />
                      <span class="flex-1">{{ child.label }}</span>
                      <ChevronDown
                        :class="[
                          'h-4 w-4 transition-transform',
                          openGroups[child.id] ? 'rotate-180' : ''
                        ]"
                      />
                    </button>
                    <div
                      v-show="openGroups[child.id]"
                      class="space-y-1 pl-4"
                    >
                      <RouterLink
                        v-for="grandChild in getSidebarItems(child.children)"
                        :key="grandChild.id"
                        :to="grandChild.path"
                        :class="[
                          'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                          isRouteActive(grandChild.path)
                            ? 'bg-primary/10 text-primary'
                            : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                        ]"
                      >
                        <component
                          :is="grandChild.icon"
                          class="h-4 w-4 flex-shrink-0"
                        />
                        <span>{{ grandChild.label }}</span>
                      </RouterLink>
                      <div
                        v-for="deepGroup in getSidebarGroups(child.children)"
                        :key="deepGroup.id"
                        class="space-y-1"
                      >
                        <button
                          type="button"
                          class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
                          :aria-expanded="openGroups[deepGroup.id]"
                          :aria-label="`${deepGroup.label} ${openGroups[deepGroup.id] ? '收起' : '展开'}`"
                          @click="toggleGroup(deepGroup)"
                        >
                          <component
                            :is="deepGroup.icon"
                            class="h-4 w-4 flex-shrink-0"
                          />
                          <span class="flex-1">{{ deepGroup.label }}</span>
                          <ChevronDown
                            :class="[
                              'h-4 w-4 transition-transform',
                              openGroups[deepGroup.id] ? 'rotate-180' : ''
                            ]"
                          />
                        </button>
                        <div
                          v-show="openGroups[deepGroup.id]"
                          class="space-y-1 pl-4"
                        >
                          <RouterLink
                            v-for="grandChild in getSidebarItems(deepGroup.children)"
                            :key="grandChild.id"
                            :to="grandChild.path"
                            :class="[
                              'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                              isRouteActive(grandChild.path)
                                ? 'bg-primary/10 text-primary'
                                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                            ]"
                          >
                            <component
                              :is="grandChild.icon"
                              class="h-4 w-4 flex-shrink-0"
                            />
                            <span>{{ grandChild.label }}</span>
                          </RouterLink>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </template>
        </template>
      </nav>

      <!-- 折叠按钮 -->
      <div class="p-3 border-t">
        <Button
          variant="ghost"
          size="sm"
          class="w-full justify-center"
          @click="collapsed = !collapsed"
        >
          <ChevronLeft
            v-if="!collapsed"
            class="w-4 h-4"
          />
          <ChevronRight
            v-else
            class="w-4 h-4"
          />
        </Button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 头部 -->
      <header class="h-14 bg-card border-b flex items-center justify-between px-4">
        <div class="flex items-center gap-2">
          <Separator
            orientation="vertical"
            class="h-6"
          />
          <h1 class="text-lg font-semibold">
            {{ route.meta.title || 'AdminPlus' }}
          </h1>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="hidden md:flex items-center gap-2">
            <RouterLink
              v-if="workflowPermissionState.canApprovePendingActions"
              to="/workflow/pending"
            >
              <Button
                variant="outline"
                size="sm"
                class="gap-2"
              >
                <Clock3 class="w-4 h-4" />
                待审批
                <span
                  v-if="pendingCount > 0"
                  class="rounded-full bg-primary px-1.5 py-0.5 text-xs text-primary-foreground"
                >
                  {{ pendingCount }}
                </span>
              </Button>
            </RouterLink>
            <RouterLink
              v-if="workflowPermissionState.canViewCc"
              to="/workflow/cc"
            >
              <Button
                variant="outline"
                size="sm"
                class="gap-2"
              >
                <Users class="w-4 h-4" />
                抄送
                <span
                  v-if="ccUnreadCount > 0"
                  class="rounded-full bg-primary px-1.5 py-0.5 text-xs text-primary-foreground"
                >
                  {{ ccUnreadCount }}
                </span>
              </Button>
            </RouterLink>
            <RouterLink
              v-if="workflowPermissionState.canViewUrge"
              to="/workflow/urge"
            >
              <Button
                variant="outline"
                size="sm"
                class="gap-2"
              >
                <Bell class="w-4 h-4" />
                催办
                <span
                  v-if="urgeUnreadCount > 0"
                  class="rounded-full bg-primary px-1.5 py-0.5 text-xs text-primary-foreground"
                >
                  {{ urgeUnreadCount }}
                </span>
              </Button>
            </RouterLink>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <Button
                variant="ghost"
                class="flex items-center gap-2"
              >
                <Avatar class="w-7 h-7">
                  <AvatarFallback class="text-xs">
                    {{ userStore.nickname?.charAt(0) || userStore.username?.charAt(0) || 'U' }}
                  </AvatarFallback>
                </Avatar>
                <span class="hidden sm:inline">{{ userStore.nickname || userStore.username }}</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent
              align="end"
              class="w-48"
            >
              <DropdownMenuLabel>我的账户</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem as-child>
                <RouterLink
                  to="/profile"
                  class="flex items-center cursor-pointer"
                >
                  <User class="w-4 h-4 mr-2" />
                  个人资料
                </RouterLink>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                class="text-destructive cursor-pointer"
                @click="handleLogout"
              >
                <LogOut class="w-4 h-4 mr-2" />
                退出登录
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>

      <!-- 内容 -->
      <main class="flex-1 p-6 overflow-auto">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
