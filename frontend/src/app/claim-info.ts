import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { ClaimDetail } from './models';

/** Claim facts and timeline, used by both screens. */
@Component({
  selector: 'app-claim-info',
  imports: [DatePipe, DecimalPipe],
  template: `
    @let c = detail().claim;
    <h2>{{ c.reference }} <span class="status">{{ c.status }}</span></h2>
    <p>{{ c.type }} claim, {{ c.market }}, policy {{ c.policyNumber }}, incident on {{ c.incidentDate }}</p>
    <p>{{ c.description }}</p>
    <p>
      Estimate: {{ c.currency }} {{ c.estimatedAmount | number: '1.2-2' }}
      @if (c.reserveAmount !== null) {
        | Reserve: {{ c.currency }} {{ c.reserveAmount | number: '1.2-2' }}
      }
      @if (c.approvedAmount !== null) {
        | Approved: {{ c.currency }} {{ c.approvedAmount | number: '1.2-2' }}
      }
    </p>
    <p>Officer: {{ c.assignedOfficer ?? 'not assigned yet' }}</p>
    @if (c.decisionReason) {
      <p>Reason: {{ c.decisionReason }}</p>
    }
    <h3>Timeline</h3>
    <ol>
      @for (a of detail().timeline; track $index) {
        <li>
          {{ a.createdAt | date: 'd MMM y, HH:mm' }}: {{ a.type }} by {{ a.actor }}
          @if (a.message) {
            <br />{{ a.message }}
          }
        </li>
      }
    </ol>
  `,
})
export class ClaimInfo {
  readonly detail = input.required<ClaimDetail>();
}
