export type Status = 'SUBMITTED' | 'UNDER_REVIEW' | 'INFO_REQUESTED' | 'APPROVED' | 'REJECTED' | 'SETTLED';

export const STATUSES: Status[] = ['SUBMITTED', 'UNDER_REVIEW', 'INFO_REQUESTED', 'APPROVED', 'REJECTED', 'SETTLED'];

export interface Me {
  username: string;
  role: 'CLAIMANT' | 'OFFICER';
}

export interface NewClaim {
  market: string;
  type: string;
  policyNumber: string;
  incidentDate: string;
  description: string;
  currency: string;
  estimatedAmount: number;
}

export interface Claim extends NewClaim {
  id: number;
  reference: string;
  claimant: string;
  reserveAmount: number | null;
  approvedAmount: number | null;
  status: Status;
  assignedOfficer: string | null;
  decisionReason: string | null;
}

export interface ClaimDetail {
  claim: Claim;
  timeline: { type: string; actor: string; message: string | null; createdAt: string }[];
}

export interface ClaimPage {
  content: Claim[];
  page: { number: number; totalPages: number; totalElements: number };
}

export interface Summary {
  byStatus: { status: Status; count: number }[];
  byOfficer: { officer: string; status: Status; count: number }[];
  outstanding: { currency: string; amount: number }[];
}
