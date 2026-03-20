# Profile Page Redesign Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the Profile page with a Clean & Modern style featuring inline editing, activity dashboard, and quick settings.

**Architecture:** Single-page Vue 3 component with Composition API, using shadcn/ui components. Data flows from API calls through composables to components. Inline editing uses optimistic updates with rollback on error.

**Tech Stack:** Vue 3.5 (script setup), TypeScript, Pinia stores, Tailwind CSS, shadcn/ui components, vue-sonner for toasts

---

## File Structure

```
frontend/src/
├── api/
│   └── profile.ts                    # MODIFY: Add activity and settings endpoints
├── types/
│   └── index.ts                      # MODIFY: Add ActivityStats, UserSettings types
├── composables/
│   └── useInlineEdit.ts              # CREATE: Reusable inline edit logic
├── components/
│   └── profile/
│       ├── ProfileHero.vue           # CREATE: Hero section with avatar and actions
│       ├── ProfileInfo.vue           # CREATE: Inline editable profile fields
│       ├── ProfileSecurity.vue       # CREATE: Security card (password, 2FA)
│       ├── ActivityDashboard.vue     # CREATE: Activity stats and timeline
│       ├── QuickSettings.vue         # CREATE: Settings toggles
│       └── CompletionBadge.vue       # CREATE: Profile completion indicator
└── views/
    └── Profile.vue                   # MODIFY: Complete redesign using new components
```

---

## Chunk 1: Type Definitions and API Extensions

### Task 1: Extend Type Definitions

**Files:**
- Modify: `frontend/src/types/index.ts`

- [ ] **Step 1: Add ActivityStats interface**

```typescript
// Add after Profile interface (line 209)

// Activity statistics for profile dashboard
export interface ActivityStats {
  daysActive: number
  totalActions: number
  lastLogin: string
  lastLoginIp: string
  recentActivity: ActivityItem[]
}

// Single activity item
export interface ActivityItem {
  id: string
  action: string
  timestamp: string
  type: 'update' | 'create' | 'delete' | 'login'
}
```

- [ ] **Step 2: Add UserSettings interface**

```typescript
// Add after ActivityStats interface

// User preferences and settings
export interface UserSettings {
  notifications: boolean
  darkMode: boolean
  emailUpdates: boolean
  language: string
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/types/index.ts
git commit -m "feat(types): add ActivityStats and UserSettings interfaces"
```

### Task 2: Extend Profile API

**Files:**
- Modify: `frontend/src/api/profile.ts`

- [ ] **Step 1: Write test for new activity endpoint**

Create test file `frontend/src/api/__tests__/profile.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getActivityStats, getSettings, updateSettings } from '../profile'

// Mock the request module
vi.mock('@/api/request', () => ({
  get: vi.fn(),
  put: vi.fn()
}))

import { get, put } from '@/api/request'

describe('Profile API - Activity & Settings', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getActivityStats', () => {
    it('should fetch activity stats successfully', async () => {
      const mockStats = {
        daysActive: 127,
        totalActions: 2341,
        lastLogin: '2025-03-20T09:42:00',
        lastLoginIp: '192.168.1.100',
        recentActivity: [
          { id: '1', action: 'Updated profile', timestamp: '2025-03-20T10:23:00', type: 'update' }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockStats })

      const result = await getActivityStats()

      expect(get).toHaveBeenCalledWith('/profile/activity')
      expect(result.data).toEqual(mockStats)
    })
  })

  describe('getSettings', () => {
    it('should fetch user settings', async () => {
      const mockSettings = { notifications: true, darkMode: false, emailUpdates: true, language: 'zh-CN' }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockSettings })

      const result = await getSettings()

      expect(get).toHaveBeenCalledWith('/profile/settings')
      expect(result.data).toEqual(mockSettings)
    })
  })

  describe('updateSettings', () => {
    it('should update user settings', async () => {
      const updates = { darkMode: true }
      const mockSettings = { notifications: true, darkMode: true, emailUpdates: true, language: 'zh-CN' }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockSettings })

      const result = await updateSettings(updates)

      expect(put).toHaveBeenCalledWith('/profile/settings', updates)
      expect(result.data.darkMode).toBe(true)
    })
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd frontend && npm test -- api/profile.test.ts
```

Expected: FAIL - functions not defined

- [ ] **Step 3: Implement activity and settings API functions**

Add to `frontend/src/api/profile.ts` after existing exports:

```typescript
// Get activity statistics
export function getActivityStats() {
  return get<ActivityStats>('/profile/activity')
}

// Get user settings
export function getUserSettings() {
  return get<UserSettings>('/profile/settings')
}

// Update user settings
export function updateUserSettings(data: Partial<UserSettings>) {
  return put<UserSettings>('/profile/settings', data)
}
```

Add imports at top of file:

```typescript
import type { Profile, ActivityStats, UserSettings } from '@/types'
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd frontend && npm test -- api/profile.test.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add frontend/src/api/profile.ts frontend/src/api/__tests__/profile.test.ts
git commit -m "feat(api): add activity stats and user settings endpoints"
```

---

## Chunk 2: Inline Edit Composable

### Task 3: Create useInlineEdit Composable

**Files:**
- Create: `frontend/src/composables/useInlineEdit.ts`
- Test: `frontend/src/composables/__tests__/useInlineEdit.test.ts`

- [ ] **Step 1: Write the failing test**

```typescript
import { describe, it, expect, vi } from 'vitest'
import { useInlineEdit } from '../useInlineEdit'

describe('useInlineEdit', () => {
  it('should start in non-editing mode', () => {
    const { isEditing, startEditing, value } = useInlineEdit('initial', vi.fn())

    expect(isEditing.value).toBe(false)
    expect(value.value).toBe('initial')
  })

  it('should enter edit mode when startEditing is called', () => {
    const { isEditing, startEditing } = useInlineEdit('initial', vi.fn())

    startEditing()

    expect(isEditing.value).toBe(true)
  })

  it('should cancel edit and restore original value', () => {
    const onSave = vi.fn()
    const { isEditing, value, startEditing, cancelEdit } = useInlineEdit('initial', onSave)

    startEditing()
    value.value = 'modified'
    cancelEdit()

    expect(isEditing.value).toBe(false)
    expect(value.value).toBe('initial')
    expect(onSave).not.toHaveBeenCalled()
  })

  it('should save and call onSave with new value', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    const { isEditing, value, startEditing, save } = useInlineEdit('initial', onSave)

    startEditing()
    value.value = 'modified'
    await save()

    expect(isEditing.value).toBe(false)
    expect(onSave).toHaveBeenCalledWith('modified')
  })

  it('should handle save error and restore value', async () => {
    const onSave = vi.fn().mockRejectedValue(new Error('Save failed'))
    const { isEditing, value, startEditing, save, error } = useInlineEdit('initial', onSave)

    startEditing()
    value.value = 'modified'
    await save()

    expect(isEditing.value).toBe(false)
    expect(value.value).toBe('initial')
    expect(error.value).toBeInstanceOf(Error)
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd frontend && npm test -- composables/useInlineEdit.test.ts
```

Expected: FAIL - composable doesn't exist

- [ ] **Step 3: Implement useInlineEdit composable**

```typescript
import { ref, watch } from 'vue'

export interface UseInlineEditOptions {
  onSave?: (value: string) => Promise<void> | void
  onError?: (error: Error) => void
}

export function useInlineEdit(
  initialValue: string,
  options: UseInlineEditOptions = {}
) {
  const { onSave, onError } = options

  const isEditing = ref(false)
  const value = ref(initialValue)
  const originalValue = ref(initialValue)
  const error = ref<Error | null>(null)
  const isSaving = ref(false)

  // Update original value when initial value changes from outside
  watch(() => initialValue, (newValue) => {
    if (!isEditing.value) {
      value.value = newValue
      originalValue.value = newValue
    }
  })

  function startEditing() {
    error.value = null
    isEditing.value = true
  }

  function cancelEdit() {
    value.value = originalValue.value
    isEditing.value = false
    error.value = null
  }

  async function save() {
    if (!onSave) {
      isEditing.value = false
      return
    }

    isSaving.value = true
    error.value = null

    try {
      await onSave(value.value)
      originalValue.value = value.value
      isEditing.value = false
    } catch (e) {
      const err = e instanceof Error ? e : new Error('Save failed')
      error.value = err
      value.value = originalValue.value
      onError?.(err)
    } finally {
      isSaving.value = false
    }
  }

  return {
    isEditing,
    value,
    error,
    isSaving,
    startEditing,
    cancelEdit,
    save
  }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
cd frontend && npm test -- composables/useInlineEdit.test.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add frontend/src/composables/useInlineEdit.ts frontend/src/composables/__tests__/useInlineEdit.test.ts
git commit -m "feat(composable): add useInlineEdit composable"
```

---

## Chunk 3: Profile Components - Hero & Info

### Task 4: Create ProfileHero Component

**Files:**
- Create: `frontend/src/components/profile/ProfileHero.vue`
- Test: `frontend/src/components/profile/__tests__/ProfileHero.test.ts`

- [ ] **Step 1: Write the component**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import type { Profile } from '@/types'

interface Props {
  profile: Profile
  isOnline?: boolean
}

interface Emits {
  (e: 'edit'): void
  (e: 'changeAvatar'): void
}

const props = withDefaults(defineProps<Props>(), {
  isOnline: false
})

const emit = defineEmits<Emits>()

const initials = computed(() => {
  const name = props.profile.nickname || props.profile.username || '?'
  return name.charAt(0).toUpperCase()
})

const roles = computed(() => props.profile.roles?.filter(Boolean) ?? [])
</script>

<template>
  <div class="profile-hero">
    <div class="profile-hero__background">
      <div class="profile-hero__circle profile-hero__circle--1"></div>
      <div class="profile-hero__circle profile-hero__circle--2"></div>
    </div>

    <div class="profile-hero__content">
      <div class="profile-hero__avatar">
        <Avatar class="profile-hero__avatar-img">
          <AvatarFallback>{{ initials }}</AvatarFallback>
        </Avatar>
        <div v-if="isOnline" class="profile-hero__status"></div>
      </div>

      <div class="profile-hero__info">
        <div class="profile-hero__name-row">
          <h1 class="profile-hero__name">{{ profile.nickname || profile.username }}</h1>
          <Badge variant="secondary" class="profile-hero__username">@{{ profile.username }}</Badge>
        </div>

        <p class="profile-hero__subtitle">
          {{ roles.join(' · ') || 'No roles assigned' }}
        </p>

        <div class="profile-hero__contact">
          <Badge v-if="profile.email" variant="outline">{{ profile.email }}</Badge>
          <Badge v-if="profile.phone" variant="outline">{{ profile.phone }}</Badge>
        </div>
      </div>

      <div class="profile-hero__actions">
        <Button @click="emit('edit')">Edit Profile</Button>
        <Button variant="ghost" @click="emit('changeAvatar')">Change Avatar</Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-hero {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  padding: 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
}

.profile-hero__background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.profile-hero__circle {
  position: absolute;
  border-radius: 50%;
}

.profile-hero__circle--1 {
  top: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  transform: translate(30%, -30%);
}

.profile-hero__circle--2 {
  bottom: 0;
  left: 0;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.05);
  transform: translate(-30%, 30%);
}

.profile-hero__content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.profile-hero__avatar {
  position: relative;
}

.profile-hero__avatar-img {
  width: 96px;
  height: 96px;
  background: white;
  border-radius: 16px;
  font-size: 36px;
  font-weight: 700;
  color: #3b82f6;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.profile-hero__status {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 28px;
  height: 28px;
  background: #10b981;
  border: 3px solid white;
  border-radius: 50%;
}

.profile-hero__info {
  flex: 1;
  min-width: 200px;
}

.profile-hero__name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.profile-hero__name {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.profile-hero__username {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
}

.profile-hero__subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin: 0 0 12px;
}

.profile-hero__contact {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.profile-hero__contact :deep(.badge) {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 6px;
}

.profile-hero__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 640px) {
  .profile-hero__content {
    flex-direction: column;
    text-align: center;
  }

  .profile-hero__name-row {
    justify-content: center;
    flex-wrap: wrap;
  }

  .profile-hero__contact {
    justify-content: center;
  }
}
</style>
```

- [ ] **Step 2: Write component test**

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProfileHero from '../ProfileHero.vue'

describe('ProfileHero', () => {
  const mockProfile = {
    id: '1',
    username: 'admin',
    nickname: 'Admin User',
    email: 'admin@example.com',
    phone: '+86 138****8888',
    avatar: '',
    deptName: 'IT',
    roles: ['Super Admin', 'User Manager']
  }

  it('should render profile information', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    expect(wrapper.text()).toContain('Admin User')
    expect(wrapper.text()).toContain('@admin')
    expect(wrapper.text()).toContain('Super Admin · User Manager')
  })

  it('should show online status when isOnline is true', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile, isOnline: true }
    })

    expect(wrapper.find('.profile-hero__status').exists()).toBe(true)
  })

  it('should emit edit event when Edit Profile button is clicked', async () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('edit')).toBeTruthy()
  })

  it('should emit changeAvatar event when Change Avatar button is clicked', async () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    const buttons = wrapper.findAll('button')
    await buttons[1].trigger('click')

    expect(wrapper.emitted('changeAvatar')).toBeTruthy()
  })

  it('should show initials from nickname', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    expect(wrapper.find('.profile-hero__avatar-img').text()).toBe('A')
  })
})
```

- [ ] **Step 3: Run test to verify it passes**

```bash
cd frontend && npm test -- components/profile/__tests__/ProfileHero.test.ts
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/profile/ProfileHero.vue frontend/src/components/profile/__tests__/ProfileHero.test.ts
git commit -m "feat(profile): add ProfileHero component"
```

### Task 5: Create ProfileInfo Component with Inline Editing

**Files:**
- Create: `frontend/src/components/profile/ProfileInfo.vue`

- [ ] **Step 1: Create InlineEditField sub-component**

```vue
<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useInlineEdit } from '@/composables/useInlineEdit'

interface Props {
  modelValue: string
  label: string
  placeholder?: string
  type?: 'text' | 'email'
  readonly?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'save', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  readonly: false
})

const emit = defineEmits<Emits>()

const { isEditing, value, startEditing, cancelEdit, save, error, isSaving } = useInlineEdit(
  props.modelValue,
  {
    onSave: async (newValue) => {
      emit('save', newValue)
      emit('update:modelValue', newValue)
    }
  }
)

// Update local value when modelValue changes from parent
watch(() => props.modelValue, (newValue) => {
  if (!isEditing.value) {
    value.value = newValue
  }
})

const inputRef = ref<HTMLInputElement>()

async function handleStartEditing() {
  if (props.readonly) return
  startEditing()
  await nextTick()
  inputRef.value?.focus()
}
</script>

<template>
  <div class="inline-edit-field">
    <label class="inline-edit-field__label">{{ label }}</label>

    <div
      v-if="!isEditing"
      class="inline-edit-field__display"
      :class="{ 'inline-edit-field__display--editable': !readonly }"
      @click="handleStartEditing"
    >
      <span class="inline-edit-field__value">{{ modelValue || '—' }}</span>
      <span v-if="!readonly" class="inline-edit-field__edit-icon">✏️</span>
    </div>

    <div v-else class="inline-edit-field__editing">
      <input
        ref="inputRef"
        v-model="value"
        :type="type"
        :placeholder="placeholder"
        class="inline-edit-field__input"
        @keyup.enter="save"
        @keyup.esc="cancelEdit"
        @blur="save"
      />
      <span v-if="error" class="inline-edit-field__error">{{ error.message }}</span>
    </div>
  </div>
</template>

<style scoped>
.inline-edit-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.inline-edit-field__label {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

.inline-edit-field__display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #111827;
  padding: 10px 14px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.inline-edit-field__display--editable {
  cursor: pointer;
  transition: all 0.2s;
}

.inline-edit-field__display--editable:hover {
  border-color: #3b82f6;
}

.inline-edit-field__display:hover .inline-edit-field__edit-icon {
  opacity: 1;
}

.inline-edit-field__edit-icon {
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.2s;
}

.inline-edit-field__value {
  flex: 1;
}

.inline-edit-field__editing {
  position: relative;
}

.inline-edit-field__input {
  width: 100%;
  font-size: 14px;
  color: #111827;
  padding: 10px 14px;
  background: white;
  border-radius: 8px;
  border: 2px solid #3b82f6;
  outline: none;
}

.inline-edit-field__error {
  position: absolute;
  bottom: -20px;
  left: 0;
  font-size: 11px;
  color: #ef4444;
}
</style>
```

- [ ] **Step 2: Create ProfileInfo component**

```vue
<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Card } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import InlineEditField from './InlineEditField.vue'
import { toast } from 'vue-sonner'
import { updateProfile } from '@/api'
import type { Profile } from '@/types'

interface Props {
  profile: Profile
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'updated'): void
}>()

const localProfile = reactive({ ...props.profile })
const isSaving = ref(false)

const roles = computed(() => props.profile.roles?.filter(Boolean) ?? [])

async function handleFieldSave(field: keyof Profile, value: string) {
  isSaving.value = true

  try {
    await updateProfile({ [field]: value })
    localProfile[field] = value
    toast.success(`${field} updated successfully`)
    emit('updated')
  } catch (error) {
    console.error('Update failed:', error)
    toast.error('Failed to update. Please try again.')
    throw error // Let InlineEditField handle the revert
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <Card class="profile-info">
    <div class="profile-info__header">
      <h3 class="profile-info__title">Profile Information</h3>
      <p class="profile-info__subtitle">Click any field to edit inline. Changes save automatically.</p>
    </div>

    <div class="profile-info__fields">
      <InlineEditField
        v-model="localProfile.nickname"
        label="Display Name"
        placeholder="Enter display name"
        @save="handleFieldSave('nickname', $event)"
      />

      <div class="profile-info__row">
        <InlineEditField
          v-model="localProfile.email"
          label="Email"
          type="email"
          placeholder="Enter email"
          @save="handleFieldSave('email', $event)"
        />

        <InlineEditField
          v-model="localProfile.phone"
          label="Phone"
          placeholder="Enter phone number"
          @save="handleFieldSave('phone', $event)"
        />
      </div>

      <InlineEditField
        :model-value="localProfile.deptName || ''"
        label="Department"
        placeholder="Department"
        readonly
      />

      <div class="profile-info__roles">
        <span class="inline-edit-field__label">Roles</span>
        <div class="profile-info__role-badges">
          <Badge v-for="role in roles" :key="role" variant="secondary">
            {{ role }}
          </Badge>
          <Badge v-if="roles.length === 0" variant="outline">No roles assigned</Badge>
        </div>
      </div>
    </div>
  </Card>
</template>

<style scoped>
.profile-info {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
}

.profile-info__header {
  margin-bottom: 20px;
}

.profile-info__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px;
}

.profile-info__subtitle {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.profile-info__fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-info__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.profile-info__roles {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-info__role-badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

@media (max-width: 640px) {
  .profile-info__row {
    grid-template-columns: 1fr;
  }
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/profile/InlineEditField.vue frontend/src/components/profile/ProfileInfo.vue
git commit -m "feat(profile): add InlineEditField and ProfileInfo components"
```

---

## Chunk 4: Activity Dashboard & Settings Components

### Task 6: Create ActivityDashboard Component

**Files:**
- Create: `frontend/src/components/profile/ActivityDashboard.vue`

- [ ] **Step 1: Create the component**

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Card } from '@/components/ui/card'
import { getActivityStats } from '@/api'
import type { ActivityStats } from '@/types'

const stats = ref<ActivityStats | null>(null)
const loading = ref(true)

async function fetchStats() {
  loading.value = true
  try {
    const res = await getActivityStats()
    stats.value = res.data
  } catch (error) {
    console.error('Failed to fetch activity stats:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)

function formatTime(timestamp: string): string {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) return 'Just now'
    return `Today, ${date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}`
  }
  if (days === 1) return 'Yesterday'
  if (days < 7) return `${days} days ago`
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

function getActivityColor(type: ActivityStats['recentActivity'][0]['type']): string {
  const colors = {
    update: '#3b82f6',
    create: '#10b981',
    delete: '#ef4444',
    login: '#f59e0b'
  }
  return colors[type] || '#6b7280'
}
</script>

<template>
  <Card class="activity-dashboard">
    <div v-if="loading" class="activity-dashboard__loading">Loading...</div>

    <div v-else-if="stats">
      <h3 class="activity-dashboard__title">Your Activity</h3>

      <div class="activity-dashboard__stats">
        <div class="activity-stat activity-stat--blue">
          <div class="activity-stat__value">{{ stats.daysActive }}</div>
          <div class="activity-stat__label">Days Active</div>
        </div>
        <div class="activity-stat activity-stat--green">
          <div class="activity-stat__value">{{ stats.totalActions.toLocaleString() }}</div>
          <div class="activity-stat__label">Actions</div>
        </div>
      </div>

      <h4 class="activity-dashboard__subtitle">Recent Activity</h4>

      <div class="activity-dashboard__timeline">
        <div
          v-for="item in stats.recentActivity.slice(0, 5)"
          :key="item.id"
          class="activity-item"
        >
          <div
            class="activity-item__dot"
            :style="{ backgroundColor: getActivityColor(item.type) }"
          ></div>
          <div class="activity-item__content">
            <div class="activity-item__action">{{ item.action }}</div>
            <div class="activity-item__time">{{ formatTime(item.timestamp) }}</div>
          </div>
        </div>
      </div>

      <div class="activity-dashboard__footer">
        <div class="activity-dashboard__last-login">
          Last login: {{ formatTime(stats.lastLogin) }}
        </div>
        <div class="activity-dashboard__ip">{{ stats.lastLoginIp }}</div>
      </div>
    </div>
  </Card>
</template>

<style scoped>
.activity-dashboard {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
}

.activity-dashboard__loading {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.activity-dashboard__title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px;
}

.activity-dashboard__stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.activity-stat {
  padding: 16px;
  border-radius: 10px;
  text-align: center;
}

.activity-stat--blue {
  background: #eff6ff;
}

.activity-stat--green {
  background: #f0fdf4;
}

.activity-stat__value {
  font-size: 24px;
  font-weight: 700;
  color: #1d4ed8;
}

.activity-stat--green .activity-stat__value {
  color: #059669;
}

.activity-stat__label {
  font-size: 11px;
  color: #6b7280;
}

.activity-dashboard__subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px;
}

.activity-dashboard__timeline {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-item {
  display: flex;
  gap: 10px;
  align-items: start;
}

.activity-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
}

.activity-item__action {
  font-size: 12px;
  color: #374151;
}

.activity-item__time {
  font-size: 10px;
  color: #9ca3af;
}

.activity-dashboard__footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.activity-dashboard__last-login {
  font-size: 11px;
  color: #6b7280;
}

.activity-dashboard__ip {
  font-size: 11px;
  color: #9ca3af;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/profile/ActivityDashboard.vue
git commit -m "feat(profile): add ActivityDashboard component"
```

### Task 7: Create QuickSettings Component

**Files:**
- Create: `frontend/src/components/profile/QuickSettings.vue`

- [ ] **Step 1: Create the component**

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Card } from '@/components/ui/card'
import { getUserSettings, updateUserSettings } from '@/api'
import { toast } from 'vue-sonner'
import type { UserSettings } from '@/types'

const settings = ref<UserSettings>({
  notifications: true,
  darkMode: false,
  emailUpdates: true,
  language: 'zh-CN'
})

const loading = ref(false)
const updating = ref<string | null>(null)

async function fetchSettings() {
  loading.value = true
  try {
    const res = await getUserSettings()
    settings.value = res.data
  } catch (error) {
    console.error('Failed to fetch settings:', error)
  } finally {
    loading.value = false
  }
}

async function updateSetting(key: keyof UserSettings, value: boolean | string) {
  updating.value = key

  try {
    const res = await updateUserSettings({ [key]: value })
    settings.value = res.data
    toast.success('Setting updated')
  } catch (error) {
    console.error('Failed to update setting:', error)
    toast.error('Failed to update setting')
    // Revert on error
    settings.value[key] = settings.value[key] === value ? !value as any : value
  } finally {
    updating.value = null
  }
}

onMounted(fetchSettings)

interface SettingOption {
  key: keyof UserSettings
  icon: string
  label: string
  type: 'toggle' | 'select'
  options?: { value: string; label: string }[]
}

const settingOptions: SettingOption[] = [
  { key: 'notifications', icon: '🔔', label: 'Notifications', type: 'toggle' },
  { key: 'darkMode', icon: '🌙', label: 'Dark Mode', type: 'toggle' },
  { key: 'emailUpdates', icon: '📧', label: 'Email Updates', type: 'toggle' },
  {
    key: 'language',
    icon: '🌐',
    label: 'Language',
    type: 'select',
    options: [
      { value: 'zh-CN', label: '简体中文' },
      { value: 'en-US', label: 'English' }
    ]
  }
]
</script>

<template>
  <Card class="quick-settings">
    <div v-if="loading" class="quick-settings__loading">Loading...</div>

    <div v-else>
      <h3 class="quick-settings__title">Quick Settings</h3>

      <div class="quick-settings__list">
        <div
          v-for="option in settingOptions"
          :key="option.key"
          class="quick-settings__item"
        >
          <div class="quick-settings__label">
            <span class="quick-settings__icon">{{ option.icon }}</span>
            <span class="quick-settings__text">{{ option.label }}</span>
          </div>

          <!-- Toggle -->
          <button
            v-if="option.type === 'toggle'"
            class="quick-toggle"
            :class="{ 'quick-toggle--active': settings[option.key] }"
            :disabled="updating === option.key"
            @click="updateSetting(option.key, !settings[option.key])"
          >
            <div class="quick-toggle__thumb"></div>
          </button>

          <!-- Select -->
          <select
            v-else
            :value="settings[option.key]"
            class="quick-select"
            :disabled="updating === option.key"
            @change="updateSetting(option.key, ($event.target as HTMLSelectElement).value)"
          >
            <option v-for="opt in option.options" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </div>

      <div class="quick-settings__footer">
        <button class="quick-settings__view-all">View All Settings</button>
      </div>
    </div>
  </Card>
</template>

<style scoped>
.quick-settings {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
}

.quick-settings__loading {
  text-align: center;
  padding: 20px;
  color: #6b7280;
}

.quick-settings__title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px;
}

.quick-settings__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-settings__item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-settings__label {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-settings__icon {
  font-size: 16px;
}

.quick-settings__text {
  font-size: 13px;
  color: #374151;
}

/* Toggle Switch */
.quick-toggle {
  width: 40px;
  height: 22px;
  background: #e5e7eb;
  border-radius: 11px;
  position: relative;
  cursor: pointer;
  border: none;
  padding: 0;
  transition: background 0.2s;
}

.quick-toggle--active {
  background: #3b82f6;
}

.quick-toggle:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-toggle__thumb {
  width: 18px;
  height: 18px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: transform 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.quick-toggle--active .quick-toggle__thumb {
  transform: translateX(18px);
}

/* Select */
.quick-select {
  font-size: 12px;
  color: #6b7280;
  padding: 4px 8px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  cursor: pointer;
}

.quick-settings__footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.quick-settings__view-all {
  width: 100%;
  background: #f3f4f6;
  color: #374151;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.quick-settings__view-all:hover {
  background: #e5e7eb;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/profile/QuickSettings.vue
git commit -m "feat(profile): add QuickSettings component"
```

### Task 8: Create CompletionBadge Component

**Files:**
- Create: `frontend/src/components/profile/CompletionBadge.vue`

- [ ] **Step 1: Create the component**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import type { Profile } from '@/types'

interface Props {
  profile: Profile
}

const props = defineProps<Props>()

const completionPercentage = computed(() => {
  const fields = [
    props.profile.nickname,
    props.profile.email,
    props.profile.phone,
    props.profile.avatar
  ]
  const filled = fields.filter(Boolean).length
  return Math.round((filled / fields.length) * 100)
})

const completionMessage = computed(() => {
  const pct = completionPercentage.value
  if (pct === 100) return 'Your profile is complete!'
  if (pct >= 75) return 'Almost there! Just a bit more.'
  if (pct >= 50) return 'Halfway to a complete profile.'
  return 'Complete your profile to get the most out of the platform.'
})
</script>

<template>
  <div class="completion-badge">
    <div class="completion-badge__header">
      <span class="completion-badge__icon">🏆</span>
      <span class="completion-badge__title">
        {{ completionPercentage === 100 ? 'Profile Complete!' : 'Profile Progress' }}
      </span>
    </div>
    <p class="completion-badge__message">{{ completionMessage }}</p>
    <div class="completion-badge__bar">
      <div
        class="completion-badge__fill"
        :style="{ width: `${completionPercentage}%` }"
      ></div>
    </div>
    <div class="completion-badge__percentage">{{ completionPercentage }}%</div>
  </div>
</template>

<style scoped>
.completion-badge {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  padding: 16px;
}

.completion-badge__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.completion-badge__icon {
  font-size: 20px;
}

.completion-badge__title {
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
}

.completion-badge__message {
  font-size: 11px;
  color: #b45309;
  margin: 0 0 12px;
}

.completion-badge__bar {
  height: 6px;
  background: rgba(146, 64, 238, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.completion-badge__fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b, #d97706);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.completion-badge__percentage {
  font-size: 12px;
  font-weight: 600;
  color: #92400e;
  text-align: right;
  margin-top: 4px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/profile/CompletionBadge.vue
git commit -m "feat(profile): add CompletionBadge component"
```

---

## Chunk 5: Security Component & Main Profile Page

### Task 9: Create ProfileSecurity Component

**Files:**
- Create: `frontend/src/components/profile/ProfileSecurity.vue`

- [ ] **Step 1: Create the component**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { toast } from 'vue-sonner'
import { changePassword } from '@/api'

const isDialogOpen = ref(false)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const loading = ref(false)

async function handleChangePassword() {
  if (!form.value.oldPassword || !form.value.newPassword) {
    toast.warning('Please fill in all fields')
    return
  }

  if (form.value.newPassword !== form.value.confirmPassword) {
    toast.warning('Passwords do not match')
    return
  }

  if (form.value.newPassword.length < 6) {
    toast.warning('Password must be at least 6 characters')
    return
  }

  loading.value = true

  try {
    await changePassword(form.value.oldPassword, form.value.newPassword)
    toast.success('Password changed successfully')
    isDialogOpen.value = false
    form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    console.error('Password change failed:', error)
    toast.error('Failed to change password')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <Card class="profile-security">
    <h3 class="profile-security__title">Security</h3>

    <div class="profile-security__item">
      <div class="profile-security__info">
        <div class="profile-security__label">Password</div>
        <div class="profile-security__sub">Last changed 30 days ago</div>
      </div>
      <Button size="sm" @click="isDialogOpen = true">Change</Button>
    </div>

    <div class="profile-security__item">
      <div class="profile-security__info">
        <div class="profile-security__label">Two-Factor Auth</div>
        <div class="profile-security__sub profile-security__sub--success">Enabled · SMS verification</div>
      </div>
      <Button size="sm" variant="outline">Manage</Button>
    </div>

    <!-- Password Change Dialog -->
    <Dialog v-model:open="isDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Change Password</DialogTitle>
        </DialogHeader>

        <div class="password-form">
          <div class="password-form__field">
            <label>Current Password</label>
            <Input
              v-model="form.oldPassword"
              type="password"
              placeholder="Enter current password"
            />
          </div>

          <div class="password-form__field">
            <label>New Password</label>
            <Input
              v-model="form.newPassword"
              type="password"
              placeholder="Enter new password (min 6 characters)"
            />
          </div>

          <div class="password-form__field">
            <label>Confirm Password</label>
            <Input
              v-model="form.confirmPassword"
              type="password"
              placeholder="Confirm new password"
            />
          </div>

          <div class="password-form__actions">
            <Button variant="outline" @click="isDialogOpen = false">Cancel</Button>
            <Button :disabled="loading" @click="handleChangePassword">
              {{ loading ? 'Changing...' : 'Change Password' }}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  </Card>
</template>

<style scoped>
.profile-security {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  margin-top: 16px;
}

.profile-security__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px;
}

.profile-security__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
}

.profile-security__item:last-child {
  margin-bottom: 0;
}

.profile-security__info {
  flex: 1;
}

.profile-security__label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.profile-security__sub {
  font-size: 11px;
  color: #6b7280;
}

.profile-security__sub--success {
  color: #10b981;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.password-form__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.password-form__field label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.password-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/profile/ProfileSecurity.vue
git commit -m "feat(profile): add ProfileSecurity component"
```

### Task 10: Redesign Main Profile Page

**Files:**
- Modify: `frontend/src/views/Profile.vue`

- [ ] **Step 1: Backup current implementation and create new Profile page**

```vue
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { toast } from 'vue-sonner'
import ProfileHero from '@/components/profile/ProfileHero.vue'
import ProfileInfo from '@/components/profile/ProfileInfo.vue'
import ProfileSecurity from '@/components/profile/ProfileSecurity.vue'
import ActivityDashboard from '@/components/profile/ActivityDashboard.vue'
import QuickSettings from '@/components/profile/QuickSettings.vue'
import CompletionBadge from '@/components/profile/CompletionBadge.vue'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { getProfile, uploadAvatar } from '@/api'
import type { Profile } from '@/types'

const profile = ref<Profile | null>(null)
const loading = ref(true)
const avatarDialogOpen = ref(false)
const isOnline = ref(true)

const initials = computed(() => {
  if (!profile.value) return '?'
  const name = profile.value.nickname || profile.value.username
  return name.charAt(0).toUpperCase()
})

async function fetchProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data
  } catch (error) {
    console.error('Failed to fetch profile:', error)
    toast.error('Failed to load profile')
  } finally {
    loading.value = false
  }
}

async function handleAvatarUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file || !profile.value) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    toast.warning('Please select an image file')
    return
  }

  // Validate file size (max 2MB)
  if (file.size > 2 * 1024 * 1024) {
    toast.warning('Image must be smaller than 2MB')
    return
  }

  loading.value = true

  try {
    await uploadAvatar(file)
    toast.success('Avatar updated successfully')
    await fetchProfile()
    avatarDialogOpen.value = false
  } catch (error) {
    console.error('Avatar upload failed:', error)
    toast.error('Failed to upload avatar')
  } finally {
    loading.value = false
  }
}

function handleProfileUpdated() {
  fetchProfile()
}

onMounted(fetchProfile)
</script>

<template>
  <div class="profile-page">
    <div v-if="loading || !profile" class="profile-page__loading">
      <div class="profile-page__spinner"></div>
      <p>Loading profile...</p>
    </div>

    <div v-else class="profile-page__content">
      <!-- Hero Section -->
      <ProfileHero
        :profile="profile"
        :is-online="isOnline"
        @edit="() => {}"
        @change-avatar="avatarDialogOpen = true"
      />

      <!-- Three Column Layout -->
      <div class="profile-page__grid">
        <!-- Left Column: Profile Info & Security -->
        <div class="profile-page__main">
          <ProfileInfo :profile="profile" @updated="handleProfileUpdated" />
          <ProfileSecurity />
        </div>

        <!-- Middle Column: Activity Dashboard -->
        <div class="profile-page__activity">
          <ActivityDashboard />
        </div>

        <!-- Right Column: Quick Settings & Completion Badge -->
        <div class="profile-page__settings">
          <QuickSettings />
          <CompletionBadge :profile="profile" />
        </div>
      </div>
    </div>

    <!-- Avatar Upload Dialog -->
    <Dialog v-model:open="avatarDialogOpen">
      <DialogContent class="avatar-dialog">
        <DialogHeader>
          <DialogTitle>Change Avatar</DialogTitle>
        </DialogHeader>

        <div class="avatar-upload">
          <div class="avatar-upload__preview">
            <div class="avatar-upload__circle">
              {{ initials }}
            </div>
          </div>

          <div class="avatar-upload__info">
            <p>Upload a new avatar image</p>
            <p class="avatar-upload__hint">Supports JPG, PNG · Max 2MB</p>
          </div>

          <input
            id="avatar-input"
            type="file"
            accept="image/*"
            class="avatar-upload__input"
            @change="handleAvatarUpload"
          />

          <label for="avatar-input" class="avatar-upload__button">
            {{ loading ? 'Uploading...' : 'Choose File' }}
          </label>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.profile-page__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
  color: #6b7280;
}

.profile-page__spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.profile-page__content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-page__grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 24px;
}

.profile-page__main {
  display: flex;
  flex-direction: column;
}

/* Avatar Dialog */
.avatar-dialog :deep(.dialog-content) {
  max-width: 400px;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
}

.avatar-upload__preview {
  display: flex;
  justify-content: center;
}

.avatar-upload__circle {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  color: white;
}

.avatar-upload__info {
  text-align: center;
}

.avatar-upload__info p {
  margin: 0;
  font-size: 14px;
  color: #374151;
}

.avatar-upload__hint {
  font-size: 12px;
  color: #6b7280;
}

.avatar-upload__input {
  display: none;
}

.avatar-upload__button {
  display: inline-block;
  background: #3b82f6;
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.avatar-upload__button:hover {
  background: #2563eb;
}

/* Responsive */
@media (max-width: 1200px) {
  .profile-page__grid {
    grid-template-columns: 1fr 1fr;
  }

  .profile-page__activity {
    grid-column: 2;
  }

  .profile-page__settings {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 16px;
  }

  .profile-page__grid {
    grid-template-columns: 1fr;
  }

  .profile-page__activity,
  .profile-page__settings {
    grid-column: 1;
  }

  .profile-page__settings {
    display: flex;
    flex-direction: column;
  }
}
</style>
```

- [ ] **Step 2: Test the page in development**

```bash
cd frontend && npm run dev
```

Visit http://localhost:5173/profile and verify:
- Hero section displays with avatar and user info
- Inline editing works on profile fields
- Activity dashboard loads stats
- Settings toggles work
- Avatar upload dialog opens

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/Profile.vue
git commit -m "feat(profile): redesign profile page with new components"
```

---

## Chunk 6: Backend API Implementation

### Task 11: Implement Activity Stats API Endpoint

**Files:**
- Modify: `backend/src/main/java/com/adminplus/controller/ProfileController.java`
- Modify: `backend/src/main/java/com/adminplus/service/ProfileService.java`

- [ ] **Step 1: Add ActivityStats DTO**

Create `backend/src/main/java/com/adminplus/pojo/dto/ActivityStats.java`:

```java
package com.adminplus.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityStats {
    private Long daysActive;
    private Long totalActions;
    private String lastLogin;
    private String lastLoginIp;
    private List<ActivityItem> recentActivity;

    @Data
    public static class ActivityItem {
        private String id;
        private String action;
        private String timestamp;
        private String type; // update, create, delete, login
    }
}
```

- [ ] **Step 2: Add endpoint in ProfileController**

```java
@GetMapping("/activity")
@PreAuthorize("isAuthenticated()")
public ApiResponse<ActivityStats> getActivityStats() {
    ActivityStats stats = profileService.getActivityStats(getCurrentUserId());
    return ApiResponse.success(stats);
}
```

- [ ] **Step 3: Implement service method**

```java
public ActivityStats getActivityStats(String userId) {
    // Calculate days active
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BizException("User not found"));
    long daysActive = ChronoUnit.DAYS.between(
        user.getCreateTime().toLocalDate(),
        LocalDate.now()
    ) + 1;

    // Get total actions from logs
    long totalActions = logRepository.countByUsername(user.getUsername());

    // Get last login from logs
    Log lastLogin = logRepository.findFirstByUsernameOrderByCreateTimeDesc(user.getUsername())
        .orElse(null);
    String lastLoginTime = lastLogin != null ? lastLogin.getCreateTime() : null;
    String lastLoginIp = lastLogin != null ? lastLogin.getIp() : "Unknown";

    // Get recent activity
    List<Log> recentLogs = logRepository.findTop5ByUsernameOrderByCreateTimeDesc(user.getUsername());
    List<ActivityStats.ActivityItem> recentActivity = recentLogs.stream()
        .map(log -> {
            ActivityStats.ActivityItem item = new ActivityStats.ActivityItem();
            item.setId(log.getId());
            item.setAction(log.getDescription());
            item.setTimestamp(log.getCreateTime());
            item.setType(mapLogTypeToActivityType(log.getOperationType()));
            return item;
        })
        .collect(Collectors.toList());

    ActivityStats stats = new ActivityStats();
    stats.setDaysActive(daysActive);
    stats.setTotalActions(totalActions);
    stats.setLastLogin(lastLoginTime);
    stats.setLastLoginIp(lastLoginIp);
    stats.setRecentActivity(recentActivity);

    return stats;
}

private String mapLogTypeToActivityType(Integer operationType) {
    if (operationType == null) return "update";
    return switch (operationType) {
        case 1 -> "login";
        case 2, 3 -> "create";
        case 4, 5 -> "update";
        case 6 -> "delete";
        default -> "update";
    };
}
```

- [ ] **Step 4: Add repository method if needed**

In `LogRepository.java`:

```java
Log findFirstByUsernameOrderByCreateTimeDesc(String username);

List<Log> findTop5ByUsernameOrderByCreateTimeDesc(String username);
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/adminplus/pojo/dto/ActivityStats.java \
        backend/src/main/java/com/adminplus/controller/ProfileController.java \
        backend/src/main/java/com/adminplus/service/ProfileService.java
git commit -m "feat(api): add activity stats endpoint"
```

### Task 12: Implement User Settings API Endpoint

**Files:**
- Modify: `backend/src/main/java/com/adminplus/controller/ProfileController.java`
- Create: `backend/src/main/java/com/adminplus/pojo/entity/UserSettings.java`
- Create: `backend/src/main/java/com/adminplus/repository/UserSettingsRepository.java`

- [ ] **Step 1: Create UserSettings entity**

```java
package com.adminplus.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_settings")
public class UserSettings {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("user_id")
    private String userId;

    private Boolean notifications;

    private Boolean darkMode;

    private Boolean emailUpdates;

    private String language;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: Create repository**

```java
package com.adminplus.repository;

import com.adminplus.pojo.entity.UserSettings;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface UserSettingsRepository extends BaseMapper<UserSettings> {
    default UserSettings findByUserId(String userId) {
        return selectOne(new LambdaQueryWrapper<UserSettings>()
            .eq(UserSettings::getUserId, userId));
    }
}
```

- [ ] **Step 3: Create settings DTO**

```java
package com.adminplus.pojo.dto;

import lombok.Data;

@Data
public class UserSettingsDto {
    private Boolean notifications;
    private Boolean darkMode;
    private Boolean emailUpdates;
    private String language;
}
```

- [ ] **Step 4: Add controller endpoints**

```java
@GetMapping("/settings")
@PreAuthorize("isAuthenticated()")
public ApiResponse<UserSettingsDto> getSettings() {
    UserSettingsDto settings = profileService.getUserSettings(getCurrentUserId());
    return ApiResponse.success(settings);
}

@PutMapping("/settings")
@PreAuthorize("isAuthenticated()")
public ApiResponse<UserSettingsDto> updateSettings(@RequestBody UserSettingsDto dto) {
    UserSettingsDto settings = profileService.updateUserSettings(getCurrentUserId(), dto);
    return ApiResponse.success(settings);
}
```

- [ ] **Step 5: Implement service methods**

```java
@Autowired
private UserSettingsRepository userSettingsRepository;

public UserSettingsDto getUserSettings(String userId) {
    UserSettings settings = userSettingsRepository.findByUserId(userId);
    if (settings == null) {
        // Return default settings
        settings = new UserSettings();
        settings.setNotifications(true);
        settings.setDarkMode(false);
        settings.setEmailUpdates(true);
        settings.setLanguage("zh-CN");
    }

    UserSettingsDto dto = new UserSettingsDto();
    dto.setNotifications(settings.getNotifications());
    dto.setDarkMode(settings.getDarkMode());
    dto.setEmailUpdates(settings.getEmailUpdates());
    dto.setLanguage(settings.getLanguage());
    return dto;
}

@Transactional
public UserSettingsDto updateUserSettings(String userId, UserSettingsDto dto) {
    UserSettings settings = userSettingsRepository.findByUserId(userId);

    if (settings == null) {
        settings = new UserSettings();
        settings.setUserId(userId);
    }

    settings.setNotifications(dto.getNotifications());
    settings.setDarkMode(dto.getDarkMode());
    settings.setEmailUpdates(dto.getEmailUpdates());
    settings.setLanguage(dto.getLanguage());

    if (settings.getId() == null) {
        userSettingsRepository.insert(settings);
    } else {
        userSettingsRepository.updateById(settings);
    }

    return dto;
}
```

- [ ] **Step 6: Create database migration**

```sql
CREATE TABLE IF NOT EXISTS user_settings (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL UNIQUE,
    notifications BOOLEAN DEFAULT TRUE,
    dark_mode BOOLEAN DEFAULT FALSE,
    email_updates BOOLEAN DEFAULT TRUE,
    language VARCHAR(10) DEFAULT 'zh-CN',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/adminplus/pojo/entity/UserSettings.java \
        backend/src/main/java/com/adminplus/repository/UserSettingsRepository.java \
        backend/src/main/java/com/adminplus/pojo/dto/UserSettingsDto.java \
        backend/src/main/java/com/adminplus/controller/ProfileController.java \
        backend/src/main/java/com/adminplus/service/ProfileService.java
git commit -m "feat(api): add user settings endpoints"
```

---

## Chunk 7: Integration Testing & Documentation

### Task 13: Write Integration Tests

**Files:**
- Create: `frontend/src/views/__tests__/Profile.test.ts`

- [ ] **Step 1: Write integration test**

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Profile from '../Profile.vue'

// Mock API
vi.mock('@/api', () => ({
  getProfile: vi.fn(() => Promise.resolve({
    data: {
      id: '1',
      username: 'admin',
      nickname: 'Admin User',
      email: 'admin@example.com',
      phone: '+86 138****8888',
      avatar: '',
      deptName: 'IT',
      roles: ['Super Admin']
    }
  })),
  uploadAvatar: vi.fn(() => Promise.resolve({ data: { avatarUrl: '' } }))
}))

// Mock components
vi.mock('@/components/profile/ProfileHero.vue', () => ({
  default: {
    name: 'ProfileHero',
    template: '<div class="mock-hero">{{ profile.nickname }}</div>',
    props: ['profile', 'isOnline']
  }
}))

vi.mock('@/components/profile/ProfileInfo.vue', () => ({
  default: {
    name: 'ProfileInfo',
    template: '<div class="mock-info">{{ profile.email }}</div>',
    props: ['profile'],
    emits: ['updated']
  }
}))

vi.mock('@/components/profile/ProfileSecurity.vue', () => ({
  default: { name: 'ProfileSecurity', template: '<div class="mock-security"></div>' }
}))

vi.mock('@/components/profile/ActivityDashboard.vue', () => ({
  default: { name: 'ActivityDashboard', template: '<div class="mock-activity"></div>' }
}))

vi.mock('@/components/profile/QuickSettings.vue', () => ({
  default: { name: 'QuickSettings', template: '<div class="mock-settings"></div>' }
}))

vi.mock('@/components/profile/CompletionBadge.vue', () => ({
  default: {
    name: 'CompletionBadge',
    template: '<div class="mock-badge">{{ completionPercentage }}%</div>',
    props: ['profile']
  }
}))

describe('Profile Page Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should render loading state initially', () => {
    const wrapper = mount(Profile, {
      global: {
        stubs: {
          Dialog: true,
          DialogContent: true,
          DialogHeader: true,
          DialogTitle: true
        }
      }
    })

    // Initially shows loading
    expect(wrapper.find('.profile-page__loading').exists()).toBe(true)
  })

  it('should render all profile components after data loads', async () => {
    const wrapper = mount(Profile, {
      global: {
        stubs: {
          Dialog: true,
          DialogContent: true,
          DialogHeader: true,
          DialogTitle: true
        }
      }
    })

    // Wait for async data loading
    await new Promise(resolve => setTimeout(resolve, 100))
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.mock-hero').exists()).toBe(true)
    expect(wrapper.find('.mock-info').exists()).toBe(true)
    expect(wrapper.find('.mock-security').exists()).toBe(true)
    expect(wrapper.find('.mock-activity').exists()).toBe(true)
    expect(wrapper.find('.mock-settings').exists()).toBe(true)
    expect(wrapper.find('.mock-badge').exists()).toBe(true)
  })

  it('should open avatar dialog when change-avatar event is emitted', async () => {
    const wrapper = mount(Profile, {
      global: {
        stubs: {
          Dialog: true,
          DialogContent: true,
          DialogHeader: true,
          DialogTitle: true
        }
      }
    })

    await new Promise(resolve => setTimeout(resolve, 100))
    await wrapper.vm.$nextTick()

    // Trigger change-avatar
    await wrapper.vm.$refs.profileHero?.$emit('change-avatar')

    // Dialog should open
    expect(wrapper.vm.avatarDialogOpen).toBe(true)
  })
})
```

- [ ] **Step 2: Run tests**

```bash
cd frontend && npm test
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/__tests__/Profile.test.ts
git commit -m "test(profile): add integration tests for Profile page"
```

### Task 14: Update Documentation

**Files:**
- Modify: `D:\IdeaProjects\adminplus\docs\superpowers\specs\2025-03-20-profile-redesign-design.md`

- [ ] **Step 1: Add implementation notes to design doc**

Append to the design document:

```markdown
## Implementation Notes

### Components Created
- `ProfileHero.vue` - Hero section with gradient background
- `ProfileInfo.vue` - Inline editable profile fields
- `ProfileSecurity.vue` - Security settings card
- `ActivityDashboard.vue` - Activity stats and timeline
- `QuickSettings.vue` - Settings toggle switches
- `CompletionBadge.vue` - Profile completion indicator
- `InlineEditField.vue` - Reusable inline edit component

### Composables Created
- `useInlineEdit.ts` - Manages inline edit state and operations

### API Endpoints Added
- `GET /api/profile/activity` - Get user activity statistics
- `GET /api/profile/settings` - Get user preferences
- `PUT /api/profile/settings` - Update user preferences

### Database Changes
- New table: `user_settings` for storing user preferences

### Known Limitations
1. Activity stats depend on existing log data
2. Settings are not synchronized across devices
3. Avatar upload uses same endpoint as before
4. 2FA management is UI only (backend integration needed)
```

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/specs/2025-03-20-profile-redesign-design.md
git commit -m "docs(profile): update design doc with implementation notes"
```

### Task 15: Final Verification

**Files:**
- All modified files

- [ ] **Step 1: Run full test suite**

```bash
cd frontend && npm test
cd ../backend && mvn test
```

- [ ] **Step 2: Build frontend**

```bash
cd frontend && npm run build
```

- [ ] **Step 3: Verify page loads without errors**

Open browser DevTools and check:
- No console errors
- All components render
- API calls succeed
- Responsive layout works on different screen sizes

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "feat(profile): complete profile page redesign implementation"
```

---

## Summary

This plan implements a complete redesign of the Profile page with:

1. **Clean & Modern visual style** - Blue gradient hero, card-based layout, subtle shadows
2. **Inline editing** - Click any field to edit, auto-save on blur
3. **Activity dashboard** - Stats, timeline, login history
4. **Quick settings** - Toggle switches for common preferences
5. **Completion badge** - Gamification element showing profile completeness

**Total estimated implementation time:** 4-6 hours

**Key technical decisions:**
- Reusable `useInlineEdit` composable for DRY
- Optimistic updates with rollback on error
- Responsive three-column grid layout
- shadcn/ui components for consistency
