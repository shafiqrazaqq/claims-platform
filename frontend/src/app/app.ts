import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { Auth } from './auth';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    <header>
      <h1>Claims platform</h1>
      @if (auth.user(); as user) {
        <span>{{ user.username }} ({{ user.role }})</span>
        <button type="button" (click)="logout()">Log out</button>
      }
    </header>
    <main><router-outlet /></main>
  `,
})
export class App {
  readonly auth = inject(Auth);
  private readonly router = inject(Router);

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
