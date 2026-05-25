export interface TaskBoardItem {
  id: number;
  taskTitle: string;
  direction: string;
  status: string;
  priority: string;
  ownerUserId: number;
  ownerDisplayName: string;
  dueAt: string | null;
}

export interface TaskBoardColumn {
  status: string;
  title: string;
  items: TaskBoardItem[];
}

export interface TaskCreateRequest {
  taskTitle: string;
  taskDescription?: string | null;
  direction: string;
  ownerUserId: number;
  priority?: string;
  dueAt?: string | null;
}

export interface TaskUpdateRequest {
  taskTitle: string;
  status?: string;
  taskDescription?: string | null;
  priority?: string;
  ownerUserId: number;
  dueAt?: string | null;
}

export interface TaskStatusUpdateRequest {
  status: string;
}
