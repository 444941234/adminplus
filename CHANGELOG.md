# Changelog

All notable changes to AdminPlus will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-03-20

### Added - Profile Page Redesign

#### Frontend Changes
- **Complete Profile Page Redesign**
  - New modern UI with gradient hero section
  - Responsive 3-column layout (desktop/tablet/mobile)
  - Inline editing for all profile fields
  - Activity dashboard with statistics
  - Quick settings toggles
  - Profile completion badge

- **New Components**
  - `ProfileHero.vue` - Hero section with avatar and user info
  - `ProfileInfo.vue` - Inline editable profile information
  - `InlineEditField.vue` - Reusable inline edit component
  - `ActivityDashboard.vue` - Activity statistics display
  - `CompletionBadge.vue` - Profile completion indicator
  - `QuickSettings.vue` - Quick access settings toggles
  - `ProfileSecurity.vue` - Security settings section

- **New Composable**
  - `useInlineEdit.ts` - Reusable inline editing logic

- **Features**
  - Click-to-edit interface with auto-save
  - Keyboard shortcuts (Enter to save, Escape to cancel)
  - Optimistic UI updates
  - Loading states and error handling
  - Real-time validation
  - Profile completion tracking (25% per field)

#### Backend Changes
- **New API Endpoints**
  - `GET /api/v1/profile/activity` - Get user activity statistics
  - `GET /api/v1/profile/settings` - Get user settings
  - `PUT /api/v1/profile/settings` - Update user settings

- **New DTOs**
  - `ActivityStatsResp` - Activity statistics response
  - `SettingsResp` - User settings response
  - `SettingsUpdateReq` - Settings update request

#### Documentation
- Added comprehensive Profile redesign documentation (`docs/profile-redesign.md`)
- Added JSDoc comments to all Profile components
- Updated README.md with new features
- Created CHANGELOG.md

### Changed
- Updated Profile page layout for better UX
- Improved responsive design across all breakpoints
- Enhanced error handling in profile updates
- Better accessibility with ARIA labels and keyboard navigation

### Fixed
- Layout issues on mobile devices
- State management bugs in profile updates
- API integration errors

## [1.0.0] - 2026-02-07

### Added
- Initial release of AdminPlus
- User management
- Role management
- Menu management
- Department management
- Dictionary management
- Log management
- File management
- System monitoring
- Basic profile page
- Authentication and authorization
- Dynamic routing
- Permission-based UI controls

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 2.0.0 | 2026-03-20 | Profile Page Redesign |
| 1.0.0 | 2026-02-07 | Initial Release |

---

## Upgrade Notes

### From 1.x to 2.0

**Breaking Changes:**
- Profile page component structure completely changed
- New API endpoints for activity and settings
- Updated type definitions for Profile and ActivityStats

**Migration Steps:**
1. Update imports for Profile components
2. Update API calls to use new endpoints
3. Update component props and events
4. Refer to `docs/profile-redesign.md` for detailed migration guide

---

**For more details, see:**
- [Profile Redesign Documentation](./docs/profile-redesign.md)
- [README.md](./README.md)
- [Development Standards](./@docs/开发规范.md)
