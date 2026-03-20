export const hasPermission = (permissions: string[], permission: string) => {
  if (permissions.includes('*')) return true
  return permissions.includes(permission)
}
