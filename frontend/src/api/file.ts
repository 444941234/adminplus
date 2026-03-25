import { del, get, upload } from '@/utils/request'
import type { FileRecord } from '@/types'

export function uploadManagedFile(file: File, directory = 'files') {
  return upload<FileRecord>(`/files/upload?directory=${encodeURIComponent(directory)}`, file)
}

export function getMyFiles() {
  return get<FileRecord[]>('/files/my')
}

export function getFilesByDirectory(directory: string) {
  return get<FileRecord[]>(`/files/directory/${encodeURIComponent(directory)}`)
}

export function getFileById(id: string) {
  return get<FileRecord>(`/files/${id}`)
}

export function deleteManagedFile(id: string) {
  return del<void>(`/files/${id}`)
}
