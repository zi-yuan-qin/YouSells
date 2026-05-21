export interface ScriptCategory {
  id: number;
  categoryCode: string;
  categoryName: string;
  sortOrder: number;
}

export interface ScriptItem {
  id: number;
  categoryId: number;
  categoryName: string;
  title: string;
  applicableScene: string | null;
  status: string;
  content: string;
  updatedAt: string;
}

export interface ScriptCreateRequest {
  categoryId: number;
  title: string;
  content: string;
  applicableScene?: string | null;
  status?: string;
}

export interface ScriptUpdateRequest {
  categoryId: number;
  title: string;
  content: string;
  applicableScene?: string | null;
  status?: string;
}
