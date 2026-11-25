import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ClaimInfo } from './claim-info';
import { ClaimsApi, errorText } from './claims-api';
import { ClaimDetail, ClaimPage } from './models';

@Component({
  selector: 'app-claimant',
  imports: [ReactiveFormsModule, ClaimInfo],
  templateUrl: './claimant.html',
})
export class Claimant {
  private readonly api = inject(ClaimsApi);
  private readonly fb = inject(NonNullableFormBuilder);
  readonly claims = signal<ClaimPage | null>(null);
  readonly selected = signal<ClaimDetail | null>(null);
  readonly error = signal('');
  readonly busy = signal(false);

  readonly form = this.fb.group({
    market: ['MY', [Validators.required, Validators.pattern('[A-Z]{2}')]],
    type: ['MOTOR', Validators.required],
    policyNumber: ['', [Validators.required, Validators.maxLength(50)]],
    incidentDate: ['', Validators.required],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    currency: ['MYR', [Validators.required, Validators.pattern('[A-Z]{3}')]],
    estimatedAmount: [0, [Validators.required, Validators.min(0.01)]],
  });
  readonly answer = this.fb.control('', [Validators.required, Validators.maxLength(2000)]);

  constructor() {
    this.load(0);
  }

  load(page: number) {
    this.api.list(page).subscribe({ next: (p) => this.claims.set(p), error: this.fail });
  }

  open(id: number) {
    this.answer.reset();
    this.api.get(id).subscribe({ next: this.show, error: this.fail });
  }

  refresh() {
    this.load(this.claims()?.page.number ?? 0);
    const current = this.selected();
    if (current) {
      this.open(current.claim.id);
    }
  }

  submit() {
    this.busy.set(true);
    this.api.submit(this.form.getRawValue()).subscribe({
      next: (detail) => {
        this.form.reset();
        this.show(detail);
        this.load(0);
      },
      error: this.fail,
    });
  }

  sendAnswer(id: number) {
    this.busy.set(true);
    this.api.answerInfo(id, this.answer.value).subscribe({
      next: (detail) => {
        this.answer.reset();
        this.show(detail);
        this.load(this.claims()?.page.number ?? 0);
      },
      error: this.fail,
    });
  }

  private readonly show = (detail: ClaimDetail) => {
    this.busy.set(false);
    this.error.set('');
    this.selected.set(detail);
  };

  private readonly fail = (e: HttpErrorResponse) => {
    this.busy.set(false);
    this.error.set(errorText(e));
  };
}
