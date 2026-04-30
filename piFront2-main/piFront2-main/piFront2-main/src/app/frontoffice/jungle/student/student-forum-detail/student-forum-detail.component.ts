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
    <div class="fd-page">
      <a routerLink="/student/forum" class="back">← Back to forum</a>

      <div *ngIf="loading" class="banner info">Loading post...</div>
      <div *ngIf="error" class="banner error">{{ error }}</div>

      <article *ngIf="!loading && post" class="post">
        <div class="post-head">
          <div>
            <span class="pin" *ngIf="post.pinned">📌 Pinned</span>
            <h2>{{ post.title }}</h2>
            <div class="meta">
              <span>By {{ post.authorName }}</span>
              <span>Updated {{ post.updatedAt | date:'medium' }}</span>
            </div>
            <div class="reactions">
              <button type="button" (click)="react('LIKE')" [disabled]="reacting">👍 {{ post.likesCount }}</button>
              <button type="button" class="danger" (click)="react('DISLIKE')" [disabled]="reacting">👎 {{ post.dislikesCount }}</button>
              <a
                class="danger link-btn"
                [routerLink]="['/forum/report']"
                [queryParams]="{ targetType: 'POST', targetId: post.id, postId: post.id }"
              >
                🚩 Report Post
              </a>
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
            <input
              [ngModel]="commentAuthorName"
              [ngModelOptions]="{standalone: true}"
              name="authorName"
              readonly
            />
            <textarea
              [(ngModel)]="commentContent"
              [ngModelOptions]="{standalone: true}"
              name="commentContent"
              rows="3"
              placeholder="Write a comment..."
            ></textarea>
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
                ✅ Accept
              </button>
              <a
                class="ghost danger link-btn"
                [routerLink]="['/forum/report']"
                [queryParams]="{ targetType: 'COMMENT', targetId: comment.id, postId: postId }"
              >
                🚩 Report
              </a>
              <span class="accepted" *ngIf="post.acceptedCommentId === comment.id">Accepted answer</span>
            </div>
          </div>

          <div class="empty" *ngIf="post.comments.length === 0">
            No comments yet. Be the first to reply.
          </div>
        </section>
      </article>
    </div>
  `,
  styles: [`
    /* Premium post details theme (aligned with Courses / Student Courses) */
    :host {
      --ink: #0f172a;
      --muted: rgba(15, 23, 42, 0.65);
      --line: rgba(15, 23, 42, 0.08);
      --brand: #081c31;
      --accent: #aa8733;
      --accent2: rgba(170, 135, 51, 0.95);
      --radius: 22px;
      --shadow-soft: 0 10px 24px rgba(15, 23, 42, 0.08);
      display: block;
      min-height: calc(100vh - 140px);
      padding: 0.75rem 0 1.25rem;
      background:
        radial-gradient(1200px 280px at 15% 0%, rgba(170, 135, 51, 0.10), transparent 60%),
        radial-gradient(900px 260px at 85% 0%, rgba(8, 28, 49, 0.08), transparent 60%),
        linear-gradient(180deg, rgba(15, 23, 42, 0.02) 0%, rgba(15, 23, 42, 0.00) 35%, rgba(15, 23, 42, 0.02) 100%);
    }

    .fd-page {
      max-width: 1180px;
      margin: 1.2rem auto 2.2rem;
      padding: 0 1rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      color: var(--ink);
    }

    .back {
      text-decoration: none;
      color: #065f46;
      font-weight: 900;
      width: fit-content;
      padding: 0.25rem 0.6rem;
      border-radius: 999px;
      background: rgba(6, 95, 70, 0.08);
    }

    .back:hover {
      background: rgba(6, 95, 70, 0.14);
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

    .post {
      background: rgba(255, 255, 255, 0.86);
      border: 1px solid var(--line);
      border-radius: var(--radius);
      padding: 1.3rem 1.35rem;
      box-shadow: var(--shadow-soft);
      backdrop-filter: blur(8px);
      position: relative;
      overflow: hidden;
    }

    .post::before {
      content: "";
      position: absolute;
      inset: -2px;
      background: radial-gradient(800px 140px at 15% 0%, rgba(170, 135, 51, 0.18), transparent 60%);
      opacity: 0.9;
      pointer-events: none;
    }

    .post > * { position: relative; }

    .pin {
      display: inline-block;
      margin-bottom: 0.4rem;
      font-size: 0.75rem;
      font-weight: 900;
      color: var(--accent);
      background: rgba(170, 135, 51, 0.12);
      padding: 0.18rem 0.55rem;
      border-radius: 999px;
      border: 1px solid rgba(170, 135, 51, 0.18);
    }

    .post h2 {
      margin: 0 0 0.45rem;
      font-size: 1.48rem;
      color: var(--ink);
      letter-spacing: -0.02em;
      line-height: 1.25;
    }

    .meta {
      display: flex;
      gap: 0.9rem;
      flex-wrap: wrap;
      font-size: 0.85rem;
      color: var(--muted);
    }

    .reactions {
      margin-top: 0.6rem;
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .reactions button {
      padding: 0.46rem 0.92rem;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.92);
      cursor: pointer;
      font-weight: 900;
      font-size: 0.82rem;
      transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
    }

    .reactions button.danger {
      border-color: rgba(185, 28, 28, 0.18);
      color: #b91c1c;
      background: rgba(185, 28, 28, 0.08);
    }

    .reactions button:disabled {
      opacity: 0.6;
      cursor: default;
    }

    .link-btn {
      text-decoration: none;
      padding: 0.46rem 0.92rem;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.92);
      color: var(--ink);
      cursor: pointer;
      font-weight: 900;
      font-size: 0.82rem;
      transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
    }

    .link-btn.danger {
      border-color: rgba(185, 28, 28, 0.18);
      color: #b91c1c;
      background: rgba(185, 28, 28, 0.08);
    }

    .reactions button:hover:not(:disabled),
    .link-btn:hover {
      transform: translateY(-1px);
      box-shadow: 0 12px 26px rgba(15, 23, 42, 0.10);
      border-color: rgba(170, 135, 51, 0.22);
    }

    .content {
      margin: 1rem 0 1.1rem;
      color: rgba(15, 23, 42, 0.92);
      line-height: 1.7;
      white-space: pre-wrap;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.45rem;
      margin: 0.2rem 0 1rem;
    }

    .tags span {
      font-size: 0.72rem;
      padding: 0.2rem 0.55rem;
      border-radius: 999px;
      background: rgba(170, 135, 51, 0.10);
      color: var(--accent);
      border: 1px solid rgba(170, 135, 51, 0.16);
      font-weight: 800;
    }

    .recommendations {
      margin: 1rem 0 1.25rem;
    }

    .recommendations h3 {
      margin: 0 0 0.6rem;
    }

    .recs-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 1rem;
    }

    .recs-card {
      background: rgba(255, 255, 255, 0.86);
      border: 1px solid var(--line);
      border-radius: var(--radius);
      padding: 0.95rem 1rem;
      box-shadow: var(--shadow-soft);
      backdrop-filter: blur(8px);
    }

    .recs-card h4 {
      margin: 0 0 0.6rem;
      font-size: 0.9rem;
    }

    .recs-link {
      display: block;
      text-decoration: none;
      color: var(--ink);
      font-weight: 900;
      padding: 0.45rem 0.35rem;
      border-radius: 10px;
    }

    .recs-link:hover {
      background: rgba(8, 28, 49, 0.04);
    }

    .mini {
      font-size: 0.78rem;
      color: var(--muted);
      font-weight: 600;
    }

    .course-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.7rem;
      padding: 0.45rem 0.2rem;
      border-radius: 10px;
    }

    .course-title {
      font-weight: 900;
      color: var(--ink);
    }

    .price {
      font-weight: 900;
      color: var(--ink);
    }

    @media (max-width: 720px) {
      .recs-grid { grid-template-columns: 1fr; }
    }

    .comments h3 {
      margin: 0 0 0.6rem;
    }

    .comment-form {
      display: grid;
      gap: 0.6rem;
      padding: 1rem 1.05rem;
      border-radius: var(--radius);
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.86);
      box-shadow: var(--shadow-soft);
      backdrop-filter: blur(8px);
      margin-bottom: 0.9rem;
    }

    .comment-form input,
    .comment-form textarea {
      padding: 0.72rem 0.95rem;
      border-radius: 14px;
      border: 1px solid rgba(15, 23, 42, 0.14);
      background: #ffffff;
      transition: box-shadow 0.18s ease, border-color 0.18s ease;
    }

    .comment-form input:focus,
    .comment-form textarea:focus {
      outline: none;
      border-color: rgba(170, 135, 51, 0.65);
      box-shadow: 0 0 0 4px rgba(170, 135, 51, 0.14);
    }

    .comment-form button {
      justify-self: start;
      padding: 0.62rem 1.1rem;
      border-radius: 999px;
      border: 1px solid rgba(255, 255, 255, 0.14);
      background: linear-gradient(135deg, var(--accent) 0%, var(--accent2) 100%);
      color: #fff;
      font-weight: 900;
      cursor: pointer;
      letter-spacing: 0.03em;
      text-transform: uppercase;
      font-size: 0.82rem;
      box-shadow: 0 12px 26px rgba(170, 135, 51, 0.22);
      transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
    }

    .comment-form button:hover {
      transform: translateY(-1px);
      background: var(--brand);
      box-shadow: 0 18px 40px rgba(8, 28, 49, 0.35);
    }

    .comment {
      padding: 0.95rem 1rem;
      border-radius: 18px;
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.86);
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
      backdrop-filter: blur(8px);
      margin-bottom: 0.6rem;
    }

    .comment-actions {
      margin-top: 0.5rem;
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      align-items: center;
    }

    .comment-actions .ghost {
      padding: 0.42rem 0.85rem;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.92);
      cursor: pointer;
      font-weight: 900;
      font-size: 0.82rem;
      transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
    }

    .comment-actions .ghost.danger {
      border-color: rgba(185, 28, 28, 0.18);
      color: #b91c1c;
      background: rgba(185, 28, 28, 0.08);
    }

    .comment-actions .link-btn {
      padding: 0.42rem 0.85rem;
      font-size: 0.82rem;
      background: rgba(255, 255, 255, 0.92);
    }

    .accepted {
      padding: 0.22rem 0.62rem;
      border-radius: 999px;
      background: rgba(6, 95, 70, 0.12);
      color: #065f46;
      font-weight: 900;
      font-size: 0.75rem;
      border: 1px solid rgba(6, 95, 70, 0.16);
    }

    .comment-top {
      display: flex;
      justify-content: space-between;
      gap: 0.8rem;
      font-size: 0.85rem;
      color: rgba(15, 23, 42, 0.82);
      margin-bottom: 0.4rem;
    }

    .comment p {
      margin: 0;
      color: rgba(15, 23, 42, 0.92);
    }

    .empty {
      text-align: center;
      padding: 1.2rem 1rem;
      color: var(--muted);
      border-radius: var(--radius);
      border: 1px solid var(--line);
      background: rgba(255, 255, 255, 0.82);
      box-shadow: var(--shadow-soft);
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