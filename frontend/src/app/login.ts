import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Auth } from './auth';
import { errorText } from './claims-api';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: `
    <form class="card narrow" [formGroup]="form" (ngSubmit)="login()">
      <h2>Log in</h2>
      <label>Username <input formControlName="username" autocomplete="username" /></label>
      <label>Password <input type="password" formControlName="password" autocomplete="current-password" /></label>
      @if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      }
      <button [disabled]="form.invalid">Log in</button>
    </form>
  `,
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  readonly error = signal('');
  readonly form = inject(NonNullableFormBuilder).group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  login() {
    const { username, password } = this.form.getRawValue();
    this.auth.login(username, password).subscribe({
      next: (me) => this.router.navigateByUrl(me.role === 'OFFICER' ? '/officer' : '/claimant'),
      error: (e) => this.error.set(e.status === 401 ? 'Wrong username or password' : errorText(e)),
    });
  }
}
