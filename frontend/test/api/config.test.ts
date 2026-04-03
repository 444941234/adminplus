import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as configApi from '@/api/config'

// Mock the request module
const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDel = vi.fn()

vi.mock('@/utils/request', () => ({
  get: (url: string, params?: any) => mockGet(url, params),
  post: (url: string, data?: any) => mockPost(url, data),
  put: (url: string, data?: any) => mockPut(url, data),
  del: (url: string, data?: any) => mockDel(url, data)
}))

describe('config API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Set up default successful responses
    mockGet.mockResolvedValue({ data: {} })
    mockPost.mockResolvedValue({ data: {} })
    mockPut.mockResolvedValue({ data: {} })
    mockDel.mockResolvedValue({ data: {} })
  })

  // =========================================================================
  // 1. Config Group APIs
  // =========================================================================
  describe('Config Group APIs', () => {
    describe('getConfigGroupList', () => {
      it('fetches config group list with pagination', async () => {
        await configApi.getConfigGroupList({ page: 1, size: 10, keyword: 'test' })

        expect(mockGet).toHaveBeenCalledWith('/sys/config-groups', {
          page: 1,
          size: 10,
          keyword: 'test'
        })
      })

      it('fetches config group list without filters', async () => {
        await configApi.getConfigGroupList({})

        expect(mockGet).toHaveBeenCalledWith('/sys/config-groups', {})
      })
    })

    describe('getAllConfigGroups', () => {
      it('fetches all config groups', async () => {
        await configApi.getAllConfigGroups()

        expect(mockGet).toHaveBeenCalledWith('/sys/config-groups/all', undefined)
      })
    })

    describe('getConfigGroupById', () => {
      it('fetches config group by id', async () => {
        await configApi.getConfigGroupById('group-001')

        expect(mockGet).toHaveBeenCalledWith('/sys/config-groups/group-001', undefined)
      })
    })

    describe('getConfigGroupByCode', () => {
      it('fetches config group by code', async () => {
        await configApi.getConfigGroupByCode('SYSTEM')

        expect(mockGet).toHaveBeenCalledWith('/sys/config-groups/code/SYSTEM', undefined)
      })
    })

    describe('createConfigGroup', () => {
      it('creates new config group', async () => {
        const data = {
          name: 'System Config',
          code: 'SYSTEM',
          icon: 'settings',
          sortOrder: 1,
          description: 'System settings'
        }
        await configApi.createConfigGroup(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/config-groups', data)
      })

      it('creates config group with minimal data', async () => {
        const data = { name: 'Test', code: 'TEST' }
        await configApi.createConfigGroup(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/config-groups', data)
      })
    })

    describe('updateConfigGroup', () => {
      it('updates config group', async () => {
        const data = { name: 'Updated Name', sortOrder: 2 }
        await configApi.updateConfigGroup('group-001', data)

        expect(mockPut).toHaveBeenCalledWith('/sys/config-groups/group-001', data)
      })
    })

    describe('deleteConfigGroup', () => {
      it('deletes config group', async () => {
        await configApi.deleteConfigGroup('group-001')

        expect(mockDel).toHaveBeenCalledWith('/sys/config-groups/group-001', undefined)
      })
    })

    describe('updateConfigGroupStatus', () => {
      it('updates config group status', async () => {
        await configApi.updateConfigGroupStatus('group-001', 1)

        expect(mockPut).toHaveBeenCalledWith('/sys/config-groups/group-001/status?status=1', undefined)
      })
    })
  })

  // =========================================================================
  // 2. Config APIs
  // =========================================================================
  describe('Config APIs', () => {
    describe('getConfigList', () => {
      it('fetches config list with pagination', async () => {
        await configApi.getConfigList({ page: 1, size: 10, groupId: 'group-001', keyword: 'test' })

        expect(mockGet).toHaveBeenCalledWith('/sys/configs', {
          page: 1,
          size: 10,
          groupId: 'group-001',
          keyword: 'test'
        })
      })

      it('fetches config list without filters', async () => {
        await configApi.getConfigList({})

        expect(mockGet).toHaveBeenCalledWith('/sys/configs', {})
      })
    })

    describe('getConfigsByGroupId', () => {
      it('fetches configs by group id', async () => {
        await configApi.getConfigsByGroupId('group-001')

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/group/group-001', undefined)
      })
    })

    describe('getConfigsByGroupCode', () => {
      it('fetches configs by group code', async () => {
        await configApi.getConfigsByGroupCode('SYSTEM')

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/group-code/SYSTEM', undefined)
      })
    })

    describe('getConfigById', () => {
      it('fetches config by id', async () => {
        await configApi.getConfigById('config-001')

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/config-001', undefined)
      })
    })

    describe('getConfigByKey', () => {
      it('fetches config by key', async () => {
        await configApi.getConfigByKey('app.title')

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/key/app.title', undefined)
      })
    })

    describe('createConfig', () => {
      it('creates new config', async () => {
        const data = {
          groupId: 'group-001',
          name: 'App Title',
          key: 'app.title',
          value: 'My App',
          valueType: 'STRING',
          effectType: 'IMMEDIATE',
          defaultValue: 'AdminPlus',
          description: 'Application title',
          isRequired: true,
          validationRule: '.{1,100}',
          sortOrder: 1
        }
        await configApi.createConfig(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/configs', data)
      })

      it('creates config with minimal required fields', async () => {
        const data = {
          groupId: 'group-001',
          name: 'Test Config',
          key: 'test.config',
          valueType: 'STRING'
        }
        await configApi.createConfig(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/configs', data)
      })
    })

    describe('updateConfig', () => {
      it('updates config', async () => {
        const data = { value: 'new value', status: 1 }
        await configApi.updateConfig('config-001', data)

        expect(mockPut).toHaveBeenCalledWith('/sys/configs/config-001', data)
      })
    })

    describe('batchUpdateConfigs', () => {
      it('batch updates configs', async () => {
        const items = [
          { id: 'config-001', value: 'value1' },
          { id: 'config-002', value: 'value2' }
        ]
        await configApi.batchUpdateConfigs(items)

        expect(mockPut).toHaveBeenCalledWith('/sys/configs/batch', { items })
      })
    })

    describe('deleteConfig', () => {
      it('deletes config', async () => {
        await configApi.deleteConfig('config-001')

        expect(mockDel).toHaveBeenCalledWith('/sys/configs/config-001', undefined)
      })
    })

    describe('updateConfigStatus', () => {
      it('updates config status', async () => {
        await configApi.updateConfigStatus('config-001', 1)

        expect(mockPut).toHaveBeenCalledWith('/sys/configs/config-001/status?status=1', undefined)
      })
    })

    describe('getConfigHistory', () => {
      it('fetches config history', async () => {
        await configApi.getConfigHistory('config-001')

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/config-001/history', undefined)
      })
    })

    describe('rollbackConfig', () => {
      it('rolls back config to history version', async () => {
        const data = { historyId: 'history-001', remark: 'Rollback to previous version' }
        await configApi.rollbackConfig('config-001', data)

        expect(mockPost).toHaveBeenCalledWith('/sys/configs/config-001/rollback', data)
      })
    })

    describe('exportConfigs', () => {
      it('exports configs with filters', async () => {
        await configApi.exportConfigs({ groupId: 'group-001', format: 'JSON' })

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/export', {
          groupId: 'group-001',
          format: 'JSON'
        })
      })

      it('exports all configs without filters', async () => {
        await configApi.exportConfigs({})

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/export', {})
      })
    })

    describe('importConfigs', () => {
      it('imports configs in JSON format', async () => {
        const data = {
          content: '{"configs": []}',
          format: 'JSON' as const,
          mode: 'OVERWRITE' as const
        }
        await configApi.importConfigs(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/configs/import', data)
      })

      it('imports configs in YAML format with MERGE mode', async () => {
        const data = {
          content: 'configs: []',
          format: 'YAML' as const,
          mode: 'MERGE' as const
        }
        await configApi.importConfigs(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/configs/import', data)
      })

      it('imports configs for validation only', async () => {
        const data = {
          content: 'test',
          format: 'JSON' as const,
          mode: 'VALIDATE' as const
        }
        await configApi.importConfigs(data)

        expect(mockPost).toHaveBeenCalledWith('/sys/configs/import', data)
      })
    })

    describe('refreshConfigCache', () => {
      it('refreshes config cache', async () => {
        await configApi.refreshConfigCache()

        expect(mockPost).toHaveBeenCalledWith('/sys/configs/refresh-cache', {})
      })
    })

    describe('getEffectInfo', () => {
      it('fetches config effect info', async () => {
        await configApi.getEffectInfo()

        expect(mockGet).toHaveBeenCalledWith('/sys/configs/effect-info', undefined)
      })
    })
  })
})
