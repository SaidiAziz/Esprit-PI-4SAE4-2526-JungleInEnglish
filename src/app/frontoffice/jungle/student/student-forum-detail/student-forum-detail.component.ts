import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ForumService } from '../../../../core/services/forum.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ForumPostDetails, RecommendationsResponse, ForumReactionType } from '../../../../core/models/forum.model';

@Component({
  selector: 'app-student-forum-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="fd-page">
      <a routerLink="/student/forum" class="back">Back to forum</a>

      <div *ngIf="loading" class="banner info">Loading post...</div>
      <div *ngIf="error" class="banner error">{{ error }}</div>

      <article *ngIf="!loading && post" class="post">
        <div class="post-head">
          <div>
            <span class="pin" *ngIf="post.pinned">Pinned</span>
            <h2>{{ post.title }}</h2>
            <div class="meta">
              <span>By {{ post.authorName }}</span>
              <span>Updated {{ post.updatedAt | date:'medium' }}</span>
            </div>
            <div class="reactions">
              <button type="button" (click)="react('LIKE')" [disabled]="reacting">Like {{ post.likesCount }}</button>
              <button type="button" class="danger" (click)="react('DISLIKE')" [disabled]="reacting">Dislike {{ post.dislikesCount }}</button>
            </div>
          </div>
        </div>

        <p class="content">{{ post.content }}</p>

        <div class="tags" *ngIf="post.tags?.length">
          <span *ngFor="let tag of post.tags">{{ tag }}</span>
        </div>

        <section class="recommendations">
          <h3>Recommended for you</h3>
          <div *ngIf="recsLoading" class="banner info">Loading recommendations...</div>

          <div *ngIf="!recsLoading && recs" class="recs-grid">
            <div class="recs-card">
              <h4>Similar posts</h4>
              <a *ngFor="let similarPost of recs.similarPosts" class="recs-link" [routerLink]="['/student/forum', similarPost.id]">
                {{ similarPost.title }}
                <span class="mini">({{ similarPost.commentsCount }} comments)</span>
              </a>
              <div class="empty" *ngIf="recs.similarPosts.length === 0">No similar posts yet.</div>
            </div>

            <div class="recs-card">
              <h4>Suggested courses</h4>
              <div *ngFor="let course of recs.suggestedCourses" class="course-item">
                <div>
                  <div class="course-title">{{ course.title }}</div>
                  <div class="mini">{{ course.level }} · {{ course.durationHours }}h</div>
                </div>
                <div class="price">{{ course.price ?? '—' }}</div>
              </div>
              <div class="empty" *ngIf="recs.suggestedCourses.length === 0">No course suggestions.</div>
            </div>
          </div>
        </section>

        <section class="comments">
          <h3>Comments ({{ post.comments.length }})</h3>

          <div class="comment-form">
            <input [ngModel]="commentAuthorName" name="authorName" readonly>
            <textarea [(ngModel)]="commentContent" name="commentContent" rows="3" placeholder="Write a comment..."></textarea>
            <button type="button" (click)="addComment()" [disabled]="submitting">Send</button>
          </div>

          <div class="comment" *ngFor="let comment of post.comments">
            <div class="comment-top">
              <strong>{{ comment.authorName }}</strong>
              <span>{{ comment.createdAt | date:'medium' }}</span>
            </div>
            <p>{{ comment.content }}</p>
            <div class="comment-actions">
              <button type="button" class="ghost" (click)="acceptComment(comment.id)" [disabled]="moderating">
                Mark as accepted
              </button>
              <span class="accepted" *ngIf="post.acceptedCommentId === comment.id">Accepted answer</span>
            </div>
          </div>

          <div class="empty" *ngIf="post.comments.length === 0">
            No comments yet. Be the first to reply.
          </div>
        </section>
      </article>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .fd-page {
      max-width: 1180px;
      margin: 1.2rem auto 2.2rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      color: #0f172a;
    }

    .back {
      text-decoration: none;
      color: #065f46;
      font-weight: 800;
      width: fit-content;
      padding: 0.25rem 0.6rem;
      border-radius: 999px;
      background: rgba(6, 95, 70, 0.08);
    }

    .banner {
      padding: 0.78rem 0.95rem;
      border-radius: 16px;
      font-size: 0.92rem;
      border: 1px solid transparent;
    }

    .banner.info {
      background: rgba(8, 28, 49, 0.05);
      color: rgba(15, 23, 42, 0.85);
      border-color: rgba(8, 28, 49, 0.08);
    }

    .banner.error {
      background: rgba(185, 28, 28, 0.10);
      color: #b91c1c;
      border-color: rgba(185, 28, 28, 0.18);
    }

    .post,
    .recs-card,
    .comment-form,
    .comment {
      background: rgba(255, 255, 255, 0.92);
      border: 1px solid rgba(15, 23, 42, 0.08);
      border-radius: 22px;
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
    }

    .post {
      padding: 1.2rem 1.25rem;
    }

    .post-head h2 {
      margin: 0.35rem 0;
      font-size: 1.55rem;
    }

    .pin {
      display: inline-flex;
      font-size: 0.75rem;
      font-weight: 800;
      color: #aa8733;
      background: rgba(170, 135, 51, 0.12);
      padding: 0.2rem 0.6rem;
      border-radius: 999px;
      border: 1px solid rgba(170, 135, 51, 0.18);
    }

    .meta,
    .comment-top {
      display: flex;
      flex-wrap: wrap;
      gap: 0.8rem;
      color: rgba(15, 23, 42, 0.65);
      font-size: 0.84rem;
    }

    .reactions,
    .comment-actions {
      display: flex;
      gap: 0.7rem;
      flex-wrap: wrap;
      margin-top: 0.8rem;
    }

    .reactions button,
    .comment-actions button,
    .comment-form button {
      padding: 0.58rem 0.95rem;
      border-radius: 999px;
      border: none;
      background: #2d5757;
      color: #fff;
      font-weight: 700;
      cursor: pointer;
    }

    .reactions button.danger {
      background: #c84630;
    }

    .comment-actions button.ghost {
      background: rgba(61, 61, 96, 0.08);
      color: #3d3d60;
      border: 1px solid rgba(61, 61, 96, 0.12);
    }

    .content {
      margin: 1rem 0;
      line-height: 1.7;
      color: #334155;
      white-space: pre-wrap;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.45rem;
      margin-bottom: 1rem;
    }

    .tags span {
      font-size: 0.72rem;
      padding: 0.2rem 0.55rem;
      border-radius: 999px;
      background: rgba(45, 87, 87, 0.08);
      color: #2d5757;
    }

    .recommendations,
    .comments {
      margin-top: 1.4rem;
    }

    .recs-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 1rem;
      margin-top: 0.8rem;
    }

    .recs-card {
      padding: 1rem;
    }

    .recs-card h4,
    .comments h3,
    .recommendations h3 {
      margin: 0 0 0.75rem;
    }

    .recs-link {
      display: block;
      text-decoration: none;
      color: #2d5757;
      font-weight: 600;
      padding: 0.4rem 0;
    }

    .mini {
      font-size: 0.78rem;
      color: rgba(15, 23, 42, 0.6);
    }

    .course-item {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.5rem 0;
      border-bottom: 1px solid rgba(15, 23, 42, 0.06);
    }

    .course-item:last-child {
      border-bottom: none;
    }

    .course-title {
      font-weight: 700;
    }

    .price {
      font-weight: 700;
      color: #c84630;
    }

    .comment-form,
    .comment {
      padding: 1rem;
      margin-top: 0.8rem;
    }

    .comment-form {
      display: grid;
      gap: 0.7rem;
    }

    .comment-form input,
    .comment-form textarea {
      padding: 0.72rem 0.95rem;
      border-radius: 14px;
      border: 1px solid rgba(15, 23, 42, 0.14);
      background: #ffffff;
    }

    .accepted {
      display: inline-flex;
      align-items: center;
      padding: 0.28rem 0.68rem;
      border-radius: 999px;
      background: rgba(34, 197, 94, 0.14);
      color: #15803d;
      font-size: 0.78rem;
      font-weight: 700;
    }

    .empty {
      color: rgba(15, 23, 42, 0.65);
      font-size: 0.9rem;
    }

    @media (max-width: 860px) {
      .recs-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class StudentForumDetailComponent {
  private readonly forumService = inject(ForumService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  postId = Number(this.route.snapshot.paramMap.get('id') || 0);
  post: ForumPostDetails | null = null;
  recs: RecommendationsResponse | null = null;
  loading = false;
  recsLoading = false;
  error: string | null = null;
  commentContent = '';
  submitting = false;
  reacting = false;
  moderating = false;
  readonly commentAuthorName = this.buildAuthorName();

  constructor() {
    this.loadPost();
    this.loadRecommendations();
  }

  loadPost(): void {
    this.loading = true;
    this.error = null;
    this.forumService.getPost(this.postId).subscribe({
      next: (post) => {
        this.post = post;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load post details.';
        this.loading = false;
      }
    });
  }

  loadRecommendations(): void {
    this.recsLoading = true;
    this.forumService.getRecommendations(this.postId).subscribe({
      next: (recs) => {
        this.recs = recs;
        this.recsLoading = false;
      },
      error: () => {
        this.recsLoading = false;
      }
    });
  }

  react(type: ForumReactionType): void {
    this.reacting = true;
    this.forumService.reactToPost(this.postId, {
      voterName: this.commentAuthorName,
      type
    }).subscribe({
      next: (post) => {
        this.post = post;
        this.reacting = false;
      },
      error: () => {
        this.reacting = false;
      }
    });
  }

  addComment(): void {
    if (!this.commentContent.trim()) {
      return;
    }

    this.submitting = true;
    this.forumService.addComment(this.postId, {
      content: this.commentContent.trim(),
      authorName: this.commentAuthorName
    }).subscribe({
      next: () => {
        this.commentContent = '';
        this.submitting = false;
        this.loadPost();
      },
      error: () => {
        this.submitting = false;
      }
    });
  }

  acceptComment(commentId: number): void {
    this.moderating = true;
    this.forumService.acceptAnswer(this.postId, commentId).subscribe({
      next: (post) => {
        this.post = post;
        this.moderating = false;
      },
      error: () => {
        this.moderating = false;
      }
    });
  }

  private buildAuthorName(): string {
    const user = this.authService.getCurrentUser();
    if (!user) {
      return 'Student';
    }
    return `${user.firstName} ${user.lastName}`.trim();
  }
}
