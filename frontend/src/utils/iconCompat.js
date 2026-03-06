/**
 * Element Plus Icons Compatibility Layer
 * Provides text/emoji based icons to replace Element Plus icons
 */

// Simple icon mapping using emoji/text alternatives
const iconMap = {
  // Navigation
  'ArrowDown': '↓',
  'ArrowLeft': '←',
  'ArrowRight': '→',
  'ArrowUp': '↑',
  'Back': '←',
  'Right': '→',

  // Actions
  'Plus': '+',
  'Edit': '✏️',
  'Delete': '🗑️',
  'Search': '🔍',
  'Refresh': '🔄',
  'Close': '×',
  'CloseBold': '✕',
  'Check': '✓',
  'ZoomIn': '🔍+',
  'ZoomOut': '🔍-',

  // Files and folders
  'Folder': '📁',
  'FolderOpened': '📂',
  'Document': '📄',
  'Files': '📋',
  'Upload': '↑',
  'Download': '↓',

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

  // Data and charts
  'DataAnalysis': '📊',
  'Monitor': '📈',
  'PieChart': '🥧',
  'Histogram': '📊',
  'TrendCharts': '📈',

  // Misc
  'MagicStick': '✨',
  'Bell': '🔔',
  'Message': '💬',
  'Calendar': '📅',
  'Clock': '🕐',
  'Star': '☆',
  'StarFilled': '★',
  'HomeFilled': '🏠',
  'Expand': '⇲',
  'Fold': 'ⱸ',
};

// Create a simple Vue component for each icon
export const createIconComponent = (iconText) => {
  return {
    name: 'IconCompat',
    template: `<span class="icon-compat">{{ '${iconText}' }}</span>`,
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
const icons = {};
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
export const Close = icons['Close'];
export const ArrowRight = icons['ArrowRight'];
export const ArrowLeft = icons['ArrowLeft'];
export const ArrowUp = icons['ArrowUp'];
export const Folder = icons['Folder'];
export const FolderOpened = icons['FolderOpened'];
export const Files = icons['Files'];
export const DataLine = icons['DataLine'];
export const Management = icons['Management'];
export const Bell = icons['Bell'];
export const Message = icons['Message'];
export const ChatLineSquare = icons['ChatLineSquare'];
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
export const RefreshRight = icons['RefreshRight'];
export const RefreshLeft = icons['RefreshLeft'];
export const Download = icons['Download'];
export const Upload = icons['Upload'];
export const Share = icons['Share'];
export const More = icons['More'];
export const MoreFilled = icons['MoreFilled'];
export const Star = icons['Star'];
export const StarFilled = icons['StarFilled'];
export const EditPen = icons['EditPen'];
export const DeleteFilled = icons['DeleteFilled'];
export const MagicStick = icons['MagicStick'];
export const Grid = icons['Grid'];
export const List = icons['List'];
export const Operation = icons['Operation'];
export const Position = icons['Position'];
export const Tickets = icons['Tickets'];
export const Wallet = icons['Wallet'];
export const ShoppingCart = icons['ShoppingCart'];
export const Goods = icons['Goods'];
export const SoldOut = icons['SoldOut'];
export const Present = icons['Present'];
export const Box = icons['Box'];
export const Discount = icons['Discount'];
export const TrendCharts = icons['TrendCharts'];
export const PieChart = icons['PieChart'];
export const Histogram = icons['Histogram'];
export const Odometer = icons['Odometer'];
export const WarningFilled = icons['WarningFilled'];
export const Plus = icons['Plus'];
export const Expand = icons['Expand'];
export const Fold = icons['Fold'];
