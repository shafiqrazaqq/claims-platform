import { Routes } from '@angular/router';

import { roleGuard } from './auth';
import { Claimant } from './claimant';
import { Login } from './login';
import { Officer } from './officer';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'claimant', component: Claimant, canActivate: [roleGuard('CLAIMANT')] },
  { path: 'officer', component: Officer, canActivate: [roleGuard('OFFICER')] },
  { path: '**', redirectTo: 'login' },
];
