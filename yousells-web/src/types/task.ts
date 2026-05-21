export interface TaskBoardItem {
  id: number;
  taskTitle: string;
  taskType: string | null;
  status: string;
  priority: string;
  ownerDisplayName: string;
  assistantDisplayName: string | null;
  dueAt: string | null;
  nextAction: string | null;
}

export interface TaskBoardColumn {
  status: string;
  title: string;
  items: TaskBoardItem[];
}

export interface TaskCreateRequest {
  taskTitle: string;
  taskType?: string | null;
  taskDescription?: string | null;
  priority?: string;
  ownerUserId: number;
  assistantUserId?: number | null;
  dueAt?: string | null;
}

export interface TaskUpdateRequest {
  taskTitle: string;
  status: string;
  taskDescription?: string | null;
  priority?: string;
  ownerUserId: number;
  assistantUserId?: number | null;
  dueAt?: string | null;
  nextAction?: string | null;
}
