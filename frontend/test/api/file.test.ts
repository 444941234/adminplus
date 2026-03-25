import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  uploadManagedFile,
  getMyFiles,
  getFilesByDirectory,
  getFileById,
  deleteManagedFile
} from '@/api/file'

// Mock the request module
vi.mock('@/api/request', () => ({
  get: vi.fn(),
  del: vi.fn(),
  upload: vi.fn()
}))

import { get, del, upload } from '@/api/request'

describe('File API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('uploadManagedFile', () => {
    it('should upload a file successfully', async () => {
      const mockFile = new File(['content'], 'test.txt', { type: 'text/plain' })
      const mockUploadedFile = {
        id: '1',
        fileName: 'test.txt',
        fileSize: 7,
        directory: 'files',
        url: '/files/test.txt'
      }
      vi.mocked(upload).mockResolvedValue({ code: 200, message: 'success', data: mockUploadedFile })

      const result = await uploadManagedFile(mockFile, 'files')

      expect(upload).toHaveBeenCalledWith(
        '/files/upload?directory=files',
        mockFile
      )
      expect(result.data).toEqual(mockUploadedFile)
    })

    it('should upload a file to custom directory', async () => {
      const mockFile = new File(['content'], 'test.txt', { type: 'text/plain' })
      const mockUploadedFile = {
        id: '1',
        fileName: 'test.txt',
        fileSize: 7,
        directory: 'custom',
        url: '/files/custom/test.txt'
      }
      vi.mocked(upload).mockResolvedValue({ code: 200, message: 'success', data: mockUploadedFile })

      const result = await uploadManagedFile(mockFile, 'custom')

      expect(upload).toHaveBeenCalledWith(
        '/files/upload?directory=custom',
        mockFile
      )
      expect(result.data.directory).toBe('custom')
    })
  })

  describe('getMyFiles', () => {
    it('should fetch user files', async () => {
      const mockFiles = [
        { id: '1', fileName: 'file1.txt', fileSize: 100, url: '/files/file1.txt' },
        { id: '2', fileName: 'file2.txt', fileSize: 200, url: '/files/file2.txt' }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockFiles })

      const result = await getMyFiles()

      expect(get).toHaveBeenCalledWith('/files/my')
      expect(result.data).toEqual(mockFiles)
    })
  })

  describe('getFilesByDirectory', () => {
    it('should fetch files by directory', async () => {
      const mockFiles = [
        { id: '1', fileName: 'file1.txt', fileSize: 100, directory: 'documents', url: '/files/documents/file1.txt' }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockFiles })

      const result = await getFilesByDirectory('documents')

      expect(get).toHaveBeenCalledWith('/files/directory/documents')
      expect(result.data).toEqual(mockFiles)
    })

    it('should encode directory name properly', async () => {
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: [] })

      await getFilesByDirectory('my documents/files')

      expect(get).toHaveBeenCalledWith('/files/directory/my%20documents%2Ffiles')
    })
  })

  describe('getFileById', () => {
    it('should fetch file by id', async () => {
      const mockFile = {
        id: '1',
        fileName: 'test.txt',
        fileSize: 100,
        directory: 'files',
        url: '/files/test.txt'
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockFile })

      const result = await getFileById('1')

      expect(get).toHaveBeenCalledWith('/files/1')
      expect(result.data).toEqual(mockFile)
    })
  })

  describe('deleteManagedFile', () => {
    it('should delete a file', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteManagedFile('1')

      expect(del).toHaveBeenCalledWith('/files/1')
      expect(result.code).toBe(200)
    })
  })
})
