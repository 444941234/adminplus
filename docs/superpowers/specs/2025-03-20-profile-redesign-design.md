# Profile Page Redesign Design Document

**Date:** 2025-03-20
**Status:** Approved
**Designer:** Claude Code

## Overview

Redesign the Profile page to be more humanized with a Clean & Modern visual style, incorporating three main feature areas: Quick Edit Experience, Activity Dashboard, and Settings Hub.

## Visual Style: Clean & Modern

- **Color Palette**: Primary blue (#3b82f6), neutral grays, white backgrounds
- **Design Language**: Card-based layout with subtle shadows, rounded corners (12-16px)
- **Typography**: Clear hierarchy, 13-16px body text, semibold headings
- **Spacing**: Generous padding (16-24px), consistent gaps (8-12px)

## Layout Structure

### Hero Section (Top)
- Gradient background: blue gradient (#3b82f6 to #1d4ed8)
- Avatar: 96x96px white rounded card with user initial
- Status indicator: Green dot in bottom-right of avatar
- User info: Name (24px semibold), username badge, department, contact info
- Action buttons: "Edit Profile" (white), "Change Avatar" (transparent)

### Three-Column Grid

#### Left Column (2fr) - Profile Details
**Profile Information Card:**
- Inline editable fields (Name, Email, Phone, Department)
- Click-to-edit interaction with auto-save on blur
- Visual feedback on hover (edit icon appears)
- Roles displayed as colored badges

**Security Card:**
- Password section with last changed date
- Two-Factor Auth status and manage button
- Change password action

#### Middle Column (1fr) - Activity Dashboard
**Activity Stats:**
- Days Active (blue card)
- Total Actions (green card)
- Recent Activity timeline with colored dots
- Last login info with IP address

#### Right Column (1fr) - Settings Hub
**Quick Settings Toggles:**
- Notifications (on/off)
- Dark Mode (on/off)
- Email Updates (on/off)
- Language selector

**Completion Badge:**
- Yellow gradient background
- Trophy emoji + "Profile Complete!" message
- Percentage indicator

## Interactions

### Inline Editing
1. User clicks any profile field
2. Field becomes editable (input or textarea)
3. User types changes
4. On blur or Enter, changes auto-save via API
5. Success toast notification
6. On error, field reverts with error message

### Quick Settings
- Toggle switches animate on change
- Settings save immediately via API
- Visual feedback (loading state during save)

### Avatar Upload
- Click "Change Avatar" button
- File picker opens
- Image preview before upload
- Upload with progress indicator
- Avatar updates in real-time

## Components

### ProfileHero
```vue
<ProfileHero
  :user="profile"
  :status="onlineStatus"
  @edit="startEdit"
  @change-avatar="openAvatarUpload"
/>
```

### InlineEditField
```vue
<InlineEditField
  v-model="value"
  :label="Display Name"
  :placeholder="Enter name"
  @save="handleSave"
/>
```

### ActivityDashboard
```vue
<ActivityDashboard
  :stats="activityStats"
  :recent-activity="activityLog"
/>
```

### QuickSettings
```vue
<QuickSettings
  v-model="settings"
  :options="settingOptions"
/>
```

## API Requirements

### Existing (Reuse)
- `GET /api/profile` - Get profile data
- `PUT /api/profile` - Update profile
- `POST /api/profile/password` - Change password
- `POST /api/profile/avatar` - Upload avatar

### New (Add)
- `GET /api/profile/activity` - Get activity stats and recent actions
- `GET /api/profile/settings` - Get user preferences
- `PUT /api/profile/settings` - Update preferences

## Data Structures

### Activity Stats
```typescript
interface ActivityStats {
  daysActive: number
  totalActions: number
  lastLogin: string
  lastLoginIp: string
  recentActivity: Array<{
    id: string
    action: string
    timestamp: string
    type: 'update' | 'create' | 'delete' | 'login'
  }>
}
```

### User Settings
```typescript
interface UserSettings {
  notifications: boolean
  darkMode: boolean
  emailUpdates: boolean
  language: string
}
```

## Responsive Design

### Desktop (>1024px)
- Full three-column layout
- Hero section full width

### Tablet (768-1024px)
- Two-column: Main content + sidebar
- Activity and Settings stack in sidebar

### Mobile (<768px)
- Single column stack
- Hero simplified (avatar smaller, buttons stacked)
- Cards stack vertically
- Settings collapse to accordion

## Accessibility

- All interactive elements keyboard accessible
- Toggle switches have ARIA labels
- Inline edit fields have proper focus states
- Color contrast meets WCAG AA standards
- Status indicators have text alternatives

## Success Criteria

1. User can edit profile fields inline without leaving the page
2. Activity dashboard shows meaningful engagement metrics
3. Quick settings allow immediate preference changes
4. Page loads in under 1 second
5. All interactions provide clear feedback
6. Design works on desktop, tablet, and mobile

## Implementation Notes

- Reuse existing shadcn/ui components where possible
- Build new composable for inline editing
- Use optimistic updates for better perceived performance
- Implement proper loading states for all async actions
- Add error boundaries for graceful failure handling
