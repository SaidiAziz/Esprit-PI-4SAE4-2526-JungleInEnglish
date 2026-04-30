import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ForumService } from '../../../core/services/forum.service';

@Component({
  selector: 'app-forum-report',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forum-report.component.html',
  styleUrl: './forum-report.component.css'
})
export class ForumReportComponent {
  targetType: 'POST' | 'COMMENT' = 'COMMENT';
  targetId = 0;
  postId = 0;

  reporterName = '';
  reason = '';
  submitting = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private forumService: ForumService,
  ) {
    const t = (this.route.snapshot.queryParamMap.get('targetType') || 'COMMENT').toUpperCase();
    this.targetType = t === 'POST' ? 'POST' : 'COMMENT';
    this.targetId = Number(this.route.snapshot.queryParamMap.get('targetId') || 0);
    this.postId = Number(this.route.snapshot.queryParamMap.get('postId') || this.targetId || 0);
  }

  submit(): void {
    this.error = null;
    if (!this.reporterName.trim() || !this.reason.trim() || !this.targetId) {
      this.error = 'Please fill all fields.';
      return;
    }
    this.submitting = true;
    const payload = {
      reporterName: this.reporterName.trim(),
      reason: this.reason.trim()
    };

    const request$ = this.targetType === 'POST'
      ? this.forumService.reportPost(this.targetId, payload)
      : this.forumService.reportComment(this.targetId, payload);

    request$.subscribe({
      next: () => {
        this.submitting = false;
        if (this.postId) {
          this.router.navigate(['/forum', this.postId], { queryParams: { reported: '1' } });
        } else {
          this.router.navigate(['/forum']);
        }
      },
      error: () => {
        this.submitting = false;
        this.error = 'Failed to submit report.';
      }
    });
  }
}

