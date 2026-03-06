/**
 * Icon Compatibility Layer for UI Package
 * Provides simple text/emoji based icons to replace Element Plus icons
 */

// Simple icon mapping using emoji/text alternatives
const iconMap: Record<string, string> = {
  // Navigation
  'ArrowDown': '↓',
  'ArrowLeft': '←',
  'ArrowRight': '→',
  'ArrowUp': '↑',
  'Back': '←',
  'Right': '→',
  'Expand': '⇲',
  'Fold': 'ⱸ',

  // Actions
  'Plus': '+',
  'Edit': '✏️',
  'Delete': '🗑️',
  'Search': '🔍',
  'Refresh': '🔄',
  'Close': '×',
  'CloseBold': '✕',
  'Check': '✓',
  'Checked': '✓',
  'ZoomIn': '🔍+',
  'ZoomOut': '🔍-',

  // Files and folders
  'Folder': '📁',
  'FolderOpened': '📂',
  'Document': '📄',
  'Files': '📋',
  'Upload': '↑',
  'Download': '↓',
  'Notebook': '📓',

  // User and auth
  'User': '👤',
  'UserFilled': '👤',
  'Avatar': '👤',
  'Lock': '🔒',
  'Unlock': '🔓',
  'SwitchButton': '🔄',

  // System
  'Setting': '⚙️',
  'Tools': '🛠️',
  'Menu': '☰',
  'Grid': '⊞',
  'List': '☰',
  'FullScreen': '⛶',

  // Status and info
  'InfoFilled': 'ℹ️',
  'SuccessFilled': '✅',
  'Warning': '⚠️',
  'WarningFilled': '⚠️',
  'CircleCheck': '✓',
  'CircleClose': '✕',
  'CirclePlus': '+',
  'Notification': '🔔',

  // Data and charts
  'DataAnalysis': '📊',
  'Monitor': '📈',
  'PieChart': '🥧',
  'Histogram': '📊',
  'TrendCharts': '📈',
  'DataLine': '📈',

  // Misc
  'MagicStick': '✨',
  'Bell': '🔔',
  'Message': '💬',
  'ChatDotRound': '💬',
  'Calendar': '📅',
  'Clock': '🕐',
  'Timer': '⏱️',
  'Star': '☆',
  'StarFilled': '★',
  'HomeFilled': '🏠',
  'OfficeBuilding': '🏢',
  'Management': '👥',
  'Operation': '⚙️',
};

// Create a simple Vue component for each icon
export const createIconComponent = (iconText: string) => {
  return {
    name: 'IconCompat',
    template: `<span class="icon-compat">{{ iconText }}</span>`,
    props: ['iconText'],
    style: `
      .icon-compat {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-style: normal;
        line-height: 1;
      }
    `
  };
};

// Export all icon components
const icons: Record<string, any> = {};
Object.keys(iconMap).forEach(key => {
  icons[key] = createIconComponent(iconMap[key]);
});

// Default export
export default icons;

// Named exports for individual icons
export const HomeFilled = icons['HomeFilled'];
export const Setting = icons['Setting'];
export const User = icons['User'];
export const UserFilled = icons['UserFilled'];
export const Menu = icons['Menu'];
export const Document = icons['Document'];
export const Tools = icons['Tools'];
export const DataAnalysis = icons['DataAnalysis'];
export const Monitor = icons['Monitor'];
export const Avatar = icons['Avatar'];
export const ArrowDown = icons['ArrowDown'];
export const ArrowUp = icons['ArrowUp'];
export const ArrowLeft = icons['ArrowLeft'];
export const ArrowRight = icons['ArrowRight'];
export const FullScreen = icons['FullScreen'];
export const CloseBold = icons['CloseBold'];
export const SwitchButton = icons['SwitchButton'];
export const Edit = icons['Edit'];
export const Delete = icons['Delete'];
export const Search = icons['Search'];
export const Lock = icons['Lock'];
export const Unlock = icons['Unlock'];
export const View = icons['View'];
export const Hide = icons['Hide'];
export const Check = icons['Check'];
export const Checked = icons['Checked'];
export const Close = icons['Close'];
export const Folder = icons['Folder'];
export const FolderOpened = icons['FolderOpened'];
export const Files = icons['Files'];
export const DataLine = icons['DataLine'];
export const Management = icons['Management'];
export const Bell = icons['Bell'];
export const Message = icons['Message'];
export const ChatDotRound = icons['ChatDotRound'];
export const Calendar = icons['Calendar'];
export const Clock = icons['Clock'];
export const Timer = icons['Timer'];
export const Warning = icons['Warning'];
export const InfoFilled = icons['InfoFilled'];
export const SuccessFilled = icons['SuccessFilled'];
export const CircleCheck = icons['CircleCheck'];
export const CircleClose = icons['CircleClose'];
export const CirclePlus = icons['CirclePlus'];
export const ZoomIn = icons['ZoomIn'];
export const ZoomOut = icons['ZoomOut'];
export const Refresh = icons['Refresh'];
export const Download = icons['Download'];
export const Upload = icons['Upload'];
export const Star = icons['Star'];
export const StarFilled = icons['StarFilled'];
export const Grid = icons['Grid'];
export const List = icons['List'];
export const Operation = icons['Operation'];
export const TrendCharts = icons['TrendCharts'];
export const PieChart = icons['PieChart'];
export const Histogram = icons['Histogram'];
export const WarningFilled = icons['WarningFilled'];
export const Plus = icons['Plus'];
export const Expand = icons['Expand'];
export const Fold = icons['Fold'];
export const Notebook = icons['Notebook'];
export const Notification = icons['Notification'];
export const OfficeBuilding = icons['OfficeBuilding'];