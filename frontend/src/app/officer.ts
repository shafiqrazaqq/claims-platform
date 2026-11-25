import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

import { Auth } from './auth';
import { ClaimInfo } from './claim-info';
import { ClaimsApi, errorText } from './claims-api';
import { ClaimDetail, ClaimPage, STATUSES, Summary } from './models';

@Component({
  selector: 'app-officer',
  imports: [ReactiveFormsModule, ClaimInfo, DecimalPipe],
  templateUrl: './officer.html',
})
export class Officer {
  readonly api = inject(ClaimsApi);
  readonly auth = inject(Auth);
  private readonly fb = inject(NonNullableFormBuilder);
  readonly statuses = STATUSES;
  readonly claims = signal<ClaimPage | null>(null);
  readonly summary = signal<Summary | null>(null);
  readonly selected = signal<ClaimDetail | null>(null);
  readonly filter = signal('');
  readonly error = signal('');
  readonly busy = signal(false);

  readonly message = this.fb.control('', [Validators.required, Validators.maxLength(2000)]);
  readonly amount = this.fb.control(0, [Validators.required, Validators.min(0.01)]);

  constructor() {
    this.load(0);
  }

  load(page: number) {
    this.api.list(page, this.filter()).subscribe({ next: (p) => this.claims.set(p), error: this.fail });
    this.api.summary().subscribe({ next: (s) => this.summary.set(s), error: this.fail });
  }

  changeFilter(status: string) {
    this.filter.set(status);
    this.load(0);
  }

  open(id: number) {
    this.api.get(id).subscribe({ next: this.show, error: this.fail });
  }

  refresh() {
    this.load(this.claims()?.page.number ?? 0);
    const current = this.selected();
    if (current) {
      this.open(current.claim.id);
    }
  }

  /** Runs one officer action, then reloads the list and summary. */
  run(action: Observable<ClaimDetail>) {
    this.busy.set(true);
    action.subscribe({
      next: (detail) => {
        this.show(detail);
        this.load(this.claims()?.page.number ?? 0);
      },
      error: this.fail,
    });
  }

  private readonly show = (detail: ClaimDetail) => {
    this.busy.set(false);
    this.error.set('');
    this.message.reset();
    this.amount.setValue(detail.claim.reserveAmount ?? 0);
    this.selected.set(detail);
  };

  private readonly fail = (e: HttpErrorResponse) => {
    this.busy.set(false);
    this.error.set(errorText(e));
  };
}
