import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ForumService } from '../../../../core/services/forum.service';
import { ForumPostListItem } from '../../../../core/models/forum.model';

@Component({
  selector: 'app-student-forum',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="forum-page">
      <div class="forum-hero">
        <div>
          <h2>Student Forum</h2>
          <p>Ask questions, share tips, and help each other learn English in one shared community space.</p>
        </div>
        <div class="hero-side">
          <div class="hero-pill">Forum Integration</div>
          <a routerLink="/student/forum/new" class="new-post-btn">New post</a>
        </div>
      </div>

      <div class="forum-toolbar">
        <input [(ngModel)]="q" placeholder="Search by title, content, author...">
        <button type="button" (click)="search()">Search</button>
        <button type="button" class="ghost" (click)="clear()">Reset</button>
      </div>

      <div *ngIf="loading" class="banner info">Loading posts...</div>
      <div *ngIf="error" class="banner error">{{ error }}</div>

      <div class="forum-grid" *ngIf="!loading && !error">
        <a class="post-card" *ngFor="let post of posts" [routerLink]="['/student/forum', post.id]">
          <div class="post-top">
            <span class="pin" *ngIf="post.pinned">Pinned</span>
            <span class="meta">{{ post.commentsCount }} comments</span>
          </div>
          <h3>{{ post.title }}</h3>
          <div class="reactions">
            <span>Like {{ post.likesCount }}</span>
            <span>Dislike {{ post.dislikesCount }}</span>
          </div>
          <div class="tags" *ngIf="post.tags?.length">
            <span *ngFor="let tag of post.tags">{{ tag }}</span>
          </div>
          <div class="post-bottom">
            <span class="author">By {{ post.authorName }}</span>
            <span class="date">{{ post.updatedAt | date:'medium' }}</span>
          </div>
        </a>

        <div class="empty" *ngIf="posts.length === 0">
          <h3>No posts found</h3>
          <p>Try another search term. Detail, new post, and reporting screens can be integrated next.</p>
        </div>
      </div>

      <div class="pager" *ngIf="!loading && !error && totalPages > 1">
        <button type="button" (click)="prev()" [disabled]="page === 0">Prev</button>
        <span>Page {{ page + 1 }} / {{ totalPages }}</span>
        <button type="button" (click)="next()" [disabled]="page >= totalPages - 1">Next</button>
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .forum-page {
      max-width: 1180px;
      margin: 1.2rem auto 2.2rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      color: #0f172a;
    }

    .forum-hero {
      display: grid;
      grid-template-columns: 1.2fr 0.8fr;
      align-items: center;
      gap: 1rem;
      padding: 1.3rem 1.35rem;
      border-radius: 22px;
      background:
        radial-gradient(900px 220px at 10% 0%, rgba(246, 189, 96, 0.24), transparent 55%),
        radial-gradient(700px 220px at 85% 0%, rgba(45, 87, 87, 0.18), transparent 60%),
        linear-gradient(135deg, #0b1120 0%, #111827 55%, #0b1120 100%);
      color: #e5e7eb;
      box-shadow: 0 18px 55px rgba(15, 23, 42, 0.35);
      border: 1px solid rgba(255, 255, 255, 0.08);
    }

    .forum-hero h2 {
      margin: 0 0 0.35rem;
      font-size: 1.55rem;
      letter-spacing: -0.02em;
    }

    .forum-hero p,
    .hero-side p {
      margin: 0;
      color: rgba(203, 213, 245, 0.92);
      font-size: 0.95rem;
    }

    .hero-side {
      padding: 1rem 1.05rem;
      border-radius: 16px;
      background: rgba(247, 237, 226, 0.08);
      border: 1px solid rgba(247, 237, 226, 0.18);
    }

    .hero-pill {
      display: inline-block;
      margin-bottom: 0.65rem;
      padding: 0.22rem 0.7rem;
      border-radius: 999px;
      background: #f6bd60;
      color: #3d3d60;
      font-size: 0.68rem;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      font-weight: 700;
    }

    .new-post-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0.62rem 1rem;
      border-radius: 999px;
      text-decoration: none;
      font-weight: 800;
      font-size: 0.82rem;
      letter-spacing: 0.03em;
      text-transform: uppercase;
      color: #fdfcfc;
      background: linear-gradient(135deg, #f6bd60 0%, #c84630 100%);
      box-shadow: 0 12px 26px rgba(200, 70, 48, 0.24);
    }

    .forum-toolbar {
      display: grid;
      grid-template-columns: 1fr auto auto;
      gap: 0.7rem;
      align-items: center;
    }

    .forum-toolbar input {
      width: 100%;
      padding: 0.7rem 0.95rem;
      border-radius: 999px;
      border: 1px solid rgba(15, 23, 42, 0.08);
      background: #ffffff;
    }

    .forum-toolbar button {
      padding: 0.65rem 1rem;
      border-radius: 999px;
      border: none;
      background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
      color: #fff;
      font-weight: 800;
      cursor: pointer;
      font-size: 0.82rem;
    }

    .forum-toolbar button.ghost {
      background: transparent;
      border: 1px solid rgba(15, 23, 42, 0.08);
      color: #0f172a;
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

    .forum-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 1rem;
    }

    .post-card {
      display: block;
      background: rgba(255, 255, 255, 0.92);
      border: 1px solid rgba(15, 23, 42, 0.08);
      border-radius: 22px;
      padding: 1.05rem 1.05rem 0.95rem;
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
      text-decoration: none;
      color: inherit;
      backdrop-filter: blur(8px);
    }

    .post-top,
    .post-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.6rem;
    }

    .post-top {
      margin-bottom: 0.55rem;
    }

    .pin {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      font-size: 0.75rem;
      font-weight: 800;
      color: #aa8733;
      background: rgba(170, 135, 51, 0.12);
      padding: 0.2rem 0.6rem;
      border-radius: 999px;
      border: 1px solid rgba(170, 135, 51, 0.18);
    }

    .meta,
    .post-bottom {
      font-size: 0.82rem;
      color: rgba(15, 23, 42, 0.65);
    }

    .post-card h3 {
      margin: 0 0 0.65rem;
      font-size: 1.04rem;
      color: #0f172a;
      line-height: 1.35;
    }

    .reactions {
      display: flex;
      gap: 0.75rem;
      font-size: 0.86rem;
      font-weight: 800;
      color: rgba(15, 23, 42, 0.86);
      margin-bottom: 0.6rem;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 0.45rem;
      margin-bottom: 0.6rem;
    }

    .tags span {
      font-size: 0.72rem;
      padding: 0.2rem 0.55rem;
      border-radius: 999px;
      background: rgba(45, 87, 87, 0.08);
      color: #2d5757;
    }

    .pager {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.9rem;
      margin-top: 0.2rem;
    }

    .pager button {
      padding: 0.45rem 0.9rem;
      border-radius: 999px;
      border: 1px solid rgba(15, 23, 42, 0.08);
      background: #fff;
      cursor: pointer;
      font-weight: 800;
    }

    .empty {
      grid-column: 1 / -1;
      text-align: center;
      background: rgba(255, 255, 255, 0.92);
      border: 1px solid rgba(15, 23, 42, 0.08);
      border-radius: 22px;
      padding: 1.6rem 1.2rem;
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
    }

    @media (max-width: 1024px) {
      .forum-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .forum-hero {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 700px) {
      .forum-toolbar {
        grid-template-columns: 1fr;
      }

      .forum-toolbar button {
        width: 100%;
      }
    }

    @media (max-width: 640px) {
      .forum-grid {
        grid-template-columns: minmax(0, 1fr);
      }
    }
  `]
})
export class StudentForumComponent {
  private readonly forumService = inject(ForumService);

  q = '';
  loading = false;
  error: string | null = null;
  page = 0;
  size = 6;
  totalPages = 1;
  posts: ForumPostListItem[] = [];

  constructor() {
    this.load();
  }

  load(page: number = this.page): void {
    this.loading = true;
    this.error = null;
    this.forumService.listPosts({ q: this.q || undefined, page, size: this.size }).subscribe({
      next: (response) => {
        this.posts = response.content;
        this.page = response.number;
        this.totalPages = response.totalPages || 1;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load forum posts.';
        this.loading = false;
      }
    });
  }

  search(): void {
    this.page = 0;
    this.load(0);
  }

  clear(): void {
    this.q = '';
    this.page = 0;
    this.load(0);
  }

  prev(): void {
    if (this.page <= 0) {
      return;
    }
    this.load(this.page - 1);
  }

  next(): void {
    if (this.page >= this.totalPages - 1) {
      return;
    }
    this.load(this.page + 1);
  }
}
