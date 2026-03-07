export interface MenuItem {
  id: string | number
  name: string
  path: string
  icon?: string
  children?: MenuItem[]
}

export interface UserInfo {
  nickname: string
  avatar?: string
}