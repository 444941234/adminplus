import { vi } from 'vitest'

// Mock 字典数据
const MOCK_DICT_DATA: Record<string, Array<{ id: string; label: string; value: string | number; sortOrder: number; status: number }>> = {
  workflow_status: [
    { id: '1', label: '草稿', value: '0', sortOrder: 1, status: 1 },
    { id: '2', label: '运行中', value: '1', sortOrder: 2, status: 1 },
    { id: '3', label: '已批准', value: '2', sortOrder: 3, status: 1 },
    { id: '4', label: '已拒绝', value: '3', sortOrder: 4, status: 1 },
    { id: '5', label: '已取消', value: '4', sortOrder: 5, status: 1 }
  ],
  common_status: [
    { id: '1', label: '启用', value: 1, sortOrder: 1, status: 1 },
    { id: '2', label: '禁用', value: 0, sortOrder: 2, status: 1 }
  ],
  log_type: [
    { id: '1', label: '登录日志', value: '1', sortOrder: 1, status: 1 },
    { id: '2', label: '操作日志', value: '2', sortOrder: 2, status: 1 }
  ],
  operation_type: [
    { id: '1', label: '查询', value: '1', sortOrder: 1, status: 1 },
    { id: '2', label: '新增', value: '2', sortOrder: 2, status: 1 },
    { id: '3', label: '修改', value: '3', sortOrder: 3, status: 1 },
    { id: '4', label: '删除', value: '4', sortOrder: 4, status: 1 }
  ],
  log_status: [
    { id: '1', label: '成功', value: '1', sortOrder: 1, status: 1 },
    { id: '2', label: '失败', value: '0', sortOrder: 2, status: 1 }
  ],
  user_status: [
    { id: '1', label: '正常', value: 1, sortOrder: 1, status: 1 },
    { id: '2', label: '锁定', value: 0, sortOrder: 2, status: 1 }
  ]
}

// 全局 mock getDictItems，避免每个测试文件都需要手动添加
vi.mock('@/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api')>()
  return {
    ...actual,
    getDictItems: vi.fn().mockImplementation((dictType: string) => {
      return Promise.resolve({
        data: MOCK_DICT_DATA[dictType] || []
      })
    })
  }
})