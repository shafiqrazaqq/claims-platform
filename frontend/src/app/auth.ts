import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { tap } from 'rxjs';

import { Me } from './models';

// The login is kept in memory only, so a page reload logs the user out.
@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);
  readonly user = signal<Me | null>(null);
  header: string | null = null;

  login(username: string, password: string) {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    const header = 'Basic ' + btoa(String.fromCharCode(...bytes));
    return this.http.get<Me>('/api/me', { headers: { Authorization: header } }).pipe(
      tap((me) => {
        this.header = header;
        this.user.set(me);
      }),
    );
  }

  logout() {
    this.header = null;
    this.user.set(null);
  }
}

/** Adds the login to every API call. */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const header = inject(Auth).header;
  if (!header || !request.url.startsWith('/api/')) {
    return next(request);
  }
  return next(request.clone({ setHeaders: { Authorization: header } }));
};

/** Hides screens from the wrong role. The server makes the real check. */
export function roleGuard(role: Me['role']): CanActivateFn {
  return () => inject(Auth).user()?.role === role || inject(Router).parseUrl('/login');
}
