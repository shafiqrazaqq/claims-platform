import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { ClaimDetail, ClaimPage, NewClaim, Summary } from './models';

@Injectable({ providedIn: 'root' })
export class ClaimsApi {
  private readonly http = inject(HttpClient);

  list(page: number, status = '') {
    const params: Record<string, string | number> = status ? { page, status } : { page };
    return this.http.get<ClaimPage>('/api/claims', { params });
  }

  get(id: number) {
    return this.http.get<ClaimDetail>(`/api/claims/${id}`);
  }

  submit(claim: NewClaim) {
    return this.http.post<ClaimDetail>('/api/claims', claim);
  }

  answerInfo(id: number, message: string) {
    return this.http.post<ClaimDetail>(`/api/claims/${id}/info-response`, { message });
  }

  pickUp(id: number) {
    return this.http.post<ClaimDetail>(`/api/claims/${id}/pick-up`, null);
  }

  requestInfo(id: number, message: string) {
    return this.http.post<ClaimDetail>(`/api/claims/${id}/info-request`, { message });
  }

  changeReserve(id: number, amount: number) {
    return this.http.put<ClaimDetail>(`/api/claims/${id}/reserve`, { amount });
  }

  approve(id: number, amount: number) {
    return this.http.post<ClaimDetail>(`/api/claims/${id}/approve`, { amount });
  }

  reject(id: number, message: string) {
    return this.http.post<ClaimDetail>(`/api/claims/${id}/reject`, { message });
  }

  settle(id: number) {
    return this.http.post<ClaimDetail>(`/api/claims/${id}/settle`, null);
  }

  summary() {
    return this.http.get<Summary>('/api/summary');
  }
}

export function errorText(error: HttpErrorResponse): string {
  return error.error?.detail ?? 'Cannot reach the server. Try again.';
}
