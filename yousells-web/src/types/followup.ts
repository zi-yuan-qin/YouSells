export interface FollowUpRecord {
  id: number;
  customerId: number;
  followType: string;
  communicatedContent: string;
  customerFeedback: string | null;
  currentConcern: string | null;
  nextAction: string | null;
  nextFollowAt: string | null;
  operatorDisplayName: string;
  ownerDisplayName: string;
  createdAt: string;
}

export interface FollowUpCreateRequest {
  customerId: number;
  followType: string;
  communicatedContent: string;
  customerFeedback?: string | null;
  currentConcern?: string | null;
  nextAction?: string | null;
  nextFollowAt?: string | null;
}
