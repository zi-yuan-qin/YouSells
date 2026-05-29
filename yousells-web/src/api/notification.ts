import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { NotificationItem } from '@/types/notification'

export async function getNotifications(page = 1, pageSize = 20): Promise<PageResponse<NotificationItem>> {
  const response = await request.get<ApiResponse<PageResponse<NotificationItem>>>('/notifications', {
    params: { page, pageSize }
  })
  return response.data.data
}

export async function getUnreadCount(): Promise<number> {
  const response = await request.get<ApiResponse<number>>('/notifications/unread-count')
  return response.data.data
}

export async function markRead(id: number): Promise<void> {
  await request.put<ApiResponse<void>>(`/notifications/${id}/read`)
}

export async function markAllRead(): Promise<void> {
  await request.put<ApiResponse<void>>('/notifications/read-all')
}

export async function deleteNotification(id: number): Promise<void> {
  await request.delete<ApiResponse<void>>(`/notifications/${id}`)
}

export async function getTrashNotifications(page = 1, pageSize = 20): Promise<PageResponse<NotificationItem>> {
  const response = await request.get<ApiResponse<PageResponse<NotificationItem>>>('/notifications/trash', {
    params: { page, pageSize }
  })
  return response.data.data
}

export async function restoreNotification(id: number): Promise<void> {
  await request.put<ApiResponse<void>>(`/notifications/${id}/restore`)
}

export async function permanentDeleteNotification(id: number): Promise<void> {
  await request.delete<ApiResponse<void>>(`/notifications/${id}/permanent`)
}

export async function permanentDeleteAllNotifications(): Promise<void> {
  await request.delete<ApiResponse<void>>('/notifications/trash')
}
