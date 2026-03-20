<script setup lang="ts">
/**
 * ProfileHero Component
 *
 * Hero section displaying user profile information with a gradient background.
 * Shows avatar, name, username, roles, and contact information.
 * Provides edit and avatar change actions.
 *
 * @author AdminPlus
 * @since 2026-03-20
 *
 * @example
 * <ProfileHero
 *   :profile="userProfile"
 *   :is-online="true"
 *   @edit="handleEdit"
 *   @change-avatar="handleChangeAvatar"
 * />
 */
import { computed } from 'vue'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import type { Profile } from '@/types'

/**
 * Component props
 */
interface Props {
  /** User profile data */
  profile: Profile
  /** Shows online status indicator when true */
  isOnline?: boolean
}

/**
 * Component events
 */
interface Emits {
  /** Emitted when edit button is clicked */
  (e: 'edit'): void
  /** Emitted when change avatar button is clicked */
  (e: 'changeAvatar'): void
}

const props = withDefaults(defineProps<Props>(), {
  isOnline: false
})

const emit = defineEmits<Emits>()

/**
 * Computes user initials from nickname or username
 */
const initials = computed(() => {
  const name = props.profile.nickname || props.profile.username || '?'
  return name.charAt(0).toUpperCase()
})

/**
 * Returns filtered list of user roles
 */
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
