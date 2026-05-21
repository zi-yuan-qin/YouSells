export interface CustomerDetail {
  id: number;
  customerCode: string;
  customerType: string;
  nickname: string;
  contactValue: string;
  sourcePlatform: string;
  expectedMajor: string | null;
  baseLevel: string | null;
  interestDirection: string | null;
  intentLevel: string;
  currentStage: string;
  currentConcern: string | null;
  latestFeedback: string | null;
  lastContactAt: string | null;
  nextFollowAction: string | null;
  nextFollowAt: string | null;
  ownerDisplayName: string;
  assistantDisplayName: string | null;
  tags: string[];
  remarks: string | null;
}

export interface CustomerUpdateRequest {
  nickname: string;
  contactValue: string;
  sourcePlatform: string;
  expectedMajor?: string | null;
  baseLevel?: string | null;
  intentLevel?: string;
  currentStage?: string;
  currentConcern?: string | null;
  latestFeedback?: string | null;
  ownerUserId: number;
  assistantUserId?: number | null;
  remarks?: string | null;
}

export interface CustomerTagVo {
  id: number;
  tagName: string;
  tagType: string;
  tagColor: string;
}

export interface CustomerNextFollowRequest {
  nextAction: string;
  nextFollowAt: string;
}
