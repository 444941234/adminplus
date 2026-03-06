# BigModel Style Frontend Redesign - Design Document

**Date:** 2026-03-06
**Status:** Approved
**Approach:** Full Custom Components (Approach 1)

---

## Overview

Complete visual overhaul of AdminPlus frontend to match [BigModel](https://bigmodel.cn/usercenter/settings/account) design style. This involves creating a full custom component library to replace Element Plus.

---

## 1. Color System

```scss
// BigModel Style Colors
--bg-page: #f5f7fa;        // Light gray page background
--bg-white: #ffffff;        // White card/surface
--bg-hover: #f8f9fb;        // Hover state
--bg-active: #eff0f5;       // Active state

--text-primary: #1d2129;    // Main text (near black)
--text-secondary: #4e5969;  // Secondary text
--text-tertiary: #86909c;   // Placeholder/hint text
--text-disabled: #c9cdd4;   // Disabled text

--primary: #165dff;         // Primary accent (subtle blue)
--primary-hover: #4080ff;
--primary-active: #0e42d2;

--border: #e5e6eb;          // Subtle borders
--border-light: #f2f3f5;

--success: #00b42a;         // Green for success
--warning: #ff7d00;         // Orange for warning
--danger: #f53f3f;          // Red for danger
```

---

## 2. Typography System

```scss
--font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
               'Helvetica Neue', Arial, 'Noto Sans', sans-serif;

--font-size-xs: 12px;
--font-size-sm: 13px;
--font-size-base: 14px;
--font-size-md: 16px;
--font-size-lg: 18px;
--font-size-xl: 20px;

--font-weight-normal: 400;
--font-weight-medium: 500;
--font-weight-semibold: 600;
```

---

## 3. Layout Structure

```
┌─────────────────────────────────────────────────────┐
│  Sidebar (200px)     │  Main Content Area            │
│                      │                               │
│  ┌────────────────┐  │  ┌─────────────────────────┐ │
│  │ Logo           │  │  │  Top Header             │ │
│  ├────────────────┤  │  ├─────────────────────────┤ │
│  │ Menu Group 1   │  │  │                         │ │
│  │  ├ Item 1      │  │  │  Page Content           │ │
│  │  ├ Item 2      │  │  │  ┌─────────────────────┤ │
│  │  └ Item 3      │  │  │  │ Card 1              │ │
│  ├────────────────┤  │  │  ├─────────────────────┤ │
│  │ Menu Group 2   │  │  │  │ Card 2              │ │
│  │  ├ Item 4      │  │  │  └─────────────────────┘ │
│  │  └ Item 5      │  │  │                         │ │
│  └────────────────┘  │  └─────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

**Layout specifications:**
- Sidebar: 200px width, collapsible to 64px
- Header: 56px height
- Content padding: 24px
- Card gap: 16px
- Card border-radius: 8px
- Card shadow: `0 1px 2px rgba(0,0,0,0.05)`

---

## 4. Component Specifications

### BmSidebar
- Collapsible menu groups with icons
- Active state highlighting
- Hover effects
- Mobile responsive with overlay

### BmCard
- White background
- 8px border-radius
- Subtle shadow
- Optional header/footer slots

### BmButton
- Variants: primary, default, text, danger
- Sizes: sm, md, lg
- Loading state
- Disabled state

### BmInput
- Prefix/suffix icons
- Focus states
- Error states
- Disabled state

---

## 5. File Structure

```
frontend/packages/ui-vue/src/
├── components/
│   ├── bigmodel/                    # New BigModel components
│   │   ├── layout/
│   │   │   ├── BmSidebar.vue
│   │   │   ├── BmHeader.vue
│   │   │   └── BmLayout.vue
│   │   ├── card/
│   │   │   └── BmCard.vue
│   │   ├── button/
│   │   │   └── BmButton.vue
│   │   ├── form/
│   │   │   ├── BmInput.vue
│   │   │   ├── BmSelect.vue
│   │   │   ├── BmCheckbox.vue
│   │   │   ├── BmRadio.vue
│   │   │   ├── BmSwitch.vue
│   │   │   └── BmForm.vue
│   │   ├── data/
│   │   │   ├── BmTable.vue
│   │   │   └── BmPagination.vue
│   │   ├── feedback/
│   │   │   ├── BmModal.vue
│   │   │   ├── BmToast.vue
│   │   │   └── BmConfirm.vue
│   │   └── other/
│   │       ├── BmAvatar.vue
│   │       ├── BmBadge.vue
│   │       ├── BmTag.vue
│   │       └── BmIcon.vue
│   └── ...
├── styles/
│   ├── themes/
│   │   └── bigmodel.scss
│   ├── components/
│   │   ├── layout.scss
│   │   ├── card.scss
│   │   ├── button.scss
│   │   ├── form.scss
│   │   └── table.scss
│   └── index.scss
```

---

## 6. Migration Strategy

### Phase 1: Foundation
- Create theme variables
- Build core layout components
- Create base components

### Phase 2: Form & Data
- Build form components
- Build data components

### Phase 3: Feedback & Other
- Build feedback components
- Build utility components

### Phase 4: Page Migration
- Migrate all pages to new components

### Phase 5: Polish
- Refine animations
- Add responsive adjustments
- Performance optimization

---

## 7. Technical Decisions

| Decision | Rationale |
|----------|-----------|
| Drop Element Plus | Full control over styling |
| Vue 3 Composition API | Already in use |
| SCSS for styles | Already in project |
| Minimalist icons | Fewer dependencies |
| TypeScript support | Maintain type safety |

---

## 8. Scope

- **All pages**: Login, Dashboard, User, Role, Menu, Dept, Config, Log, Profile, etc.
- **All components**: Complete custom component library
- **Color scheme**: BigModel colors exactly
- **Responsive**: Mobile-first approach

---

## 9. Next Steps

1. Create implementation plan using `writing-plans` skill
2. Set up BigModel theme variables
3. Begin Phase 1: Foundation components
