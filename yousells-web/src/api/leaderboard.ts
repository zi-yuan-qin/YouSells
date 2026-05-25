import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { LeaderboardItem } from '@/types/leaderboard'

export async function getLeaderboard(): Promise<LeaderboardItem[]> {
  const response = await request.get<ApiResponse<LeaderboardItem[]>>('/leaderboard')
  return response.data.data
}
