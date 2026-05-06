import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ForumService } from '../../../../core/services/forum.service';
import { AuthService } from '../../../../core/services/auth.service';
import { CourseCatalogService } from '../../../../core/services/course-catalog.service';
import { CourseCatalogItem } from '../../../../core/models/course.model';

@Component({
  selector: 'app-student-forum-new',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="new-page">
      <a routerLink="/student/forum" class="back">Back to forum</a>

      <div class="hero">
        <div>
          <h2>Create a new post</h2>
          <p>Write a clear question or useful tip so other learners can respond quickly.</p>
        </div>
      </div>

      <div *ngIf="error" class="banner error">{{ error }}</div>

      <form class="form" (ngSubmit)="submit()">
        <div class="field">
          <label for="title">Title</label>
          <input id="title" [(ngModel)]="title" name="title" placeholder="Example: How can I improve speaking confidence?">
        </div>

        <div class="field">
          <label for="category">Category</label>
          <select id="category" [(ngModel)]="category" name="category">
            <option value="GRAMMAR">Grammar</option>
            <option value="VOCABULARY">Vocabulary</option>
            <option value="SPEAKING">Speaking</option>
            <option value="LISTENING">Listening</option>
            <option value="WRITING">Writing</option>
            <option value="PRONUNCIATION">Pronunciation</option>
            <option value="IELTS">IELTS</option>
            <option value="BUSINESS_ENGLISH">Business English</option>
            <option value="GENERAL">General</option>
          </select>
        </div>

        <div class="field">
          <label for="courseId">Related course</label>
          <select id="courseId" [(ngModel)]="selectedCourseId" name="courseId">
            <option [ngValue]="null">No linked course</option>
            <option *ngFor="let course of courses" [ngValue]="course.id">{{ course.title }} · {{ course.level }}</option>
          </select>
        </div>

        <div class="field">
          <label for="tags">Tags</label>
          <input id="tags" [(ngModel)]="tagsText" name="tagsText" placeholder="example: pronunciation, tenses, ielts">
        </div>

        <div class="field">
          <label for="author">Posted by</label>
          <input id="author" [ngModel]="authorName" name="authorName" readonly>
        </div>

        <div class="field">
          <label for="content">Content</label>
          <textarea id="content" [(ngModel)]="content" name="content" rows="7" placeholder="Write your question or message..."></textarea>
        </div>

        <div class="actions">
          <button type="submit" [disabled]="submitting">Publish</button>
          <a routerLink="/student/forum" class="ghost">Cancel</a>
        </div>
      </form>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .new-page {
      max-width: 980px;
      margin: 1.2rem auto 2rem;
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

    .hero {
      background:
        radial-gradient(900px 220px at 10% 0%, rgba(246, 189, 96, 0.18), transparent 55%),
        radial-gradient(700px 220px at 85% 0%, rgba(45, 87, 87, 0.14), transparent 60%),
        linear-gradient(135deg, #0b1120 0%, #111827 55%, #0b1120 100%);
      color: #e5e7eb;
      border-radius: 22px;
      padding: 1.25rem 1.35rem;
      box-shadow: 0 18px 55px rgba(15, 23, 42, 0.35);
      border: 1px solid rgba(255, 255, 255, 0.08);
    }

    .hero h2 {
      margin: 0 0 0.25rem;
      font-size: 1.45rem;
    }

    .hero p {
      margin: 0;
      color: rgba(203, 213, 245, 0.92);
    }

    .banner.error {
      background: rgba(185, 28, 28, 0.10);
      color: #b91c1c;
      padding: 0.78rem 0.95rem;
      border-radius: 16px;
      border: 1px solid rgba(185, 28, 28, 0.18);
    }

    .form {
      background: rgba(255, 255, 255, 0.92);
      border: 1px solid rgba(15, 23, 42, 0.08);
      border-radius: 22px;
      padding: 1.2rem 1.25rem;
      box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
      display: grid;
      gap: 0.95rem;
    }

    .field {
      display: grid;
      gap: 0.35rem;
    }

    .field label {
      font-size: 0.85rem;
      font-weight: 800;
      color: rgba(15, 23, 42, 0.85);
    }

    .field input,
    .field select,
    .field textarea {
      padding: 0.72rem 0.95rem;
      border-radius: 14px;
      border: 1px solid rgba(15, 23, 42, 0.14);
      background: #ffffff;
    }

    .actions {
      display: flex;
      gap: 0.65rem;
      align-items: center;
      flex-wrap: wrap;
    }

    .actions button {
      padding: 0.62rem 1.1rem;
      border-radius: 999px;
      border: 1px solid rgba(255, 255, 255, 0.14);
      background: linear-gradient(135deg, #f6bd60 0%, #c84630 100%);
      color: #fdfcfc;
      font-weight: 800;
      cursor: pointer;
      letter-spacing: 0.03em;
      text-transform: uppercase;
      font-size: 0.82rem;
      box-shadow: 0 12px 26px rgba(200, 70, 48, 0.22);
    }

    .actions .ghost {
      padding: 0.62rem 1.1rem;
      border-radius: 999px;
      border: 1px solid rgba(15, 23, 42, 0.08);
      text-decoration: none;
      color: #0f172a;
      font-weight: 800;
      background: rgba(255, 255, 255, 0.86);
    }
  `]
})
export class StudentForumNewComponent {
  private readonly forumService = inject(ForumService);
  private readonly authService = inject(AuthService);
  private readonly courseCatalogService = inject(CourseCatalogService);
  private readonly router = inject(Router);

  title = '';
  content = '';
  category = 'GENERAL';
  selectedCourseId: number | null = null;
  tagsText = '';
  courses: CourseCatalogItem[] = [];
  submitting = false;
  error: string | null = null;

  readonly authorName = this.buildAuthorName();

  constructor() {
    this.courseCatalogService.getCourses().subscribe({
      next: (courses) => {
        this.courses = courses.filter((course) => course.active);
      }
    });
  }

  submit(): void {
    this.error = null;
    if (!this.title.trim() || !this.content.trim() || !this.authorName.trim()) {
      this.error = 'Please fill the title and content before publishing.';
      return;
    }

    this.submitting = true;
    this.forumService.createPost({
      title: this.title.trim(),
      content: this.content.trim(),
      authorName: this.authorName.trim(),
      category: this.category,
      courseId: this.selectedCourseId,
      tags: this.tagsText
        .split(',')
        .map((value) => value.trim().toLowerCase())
        .filter(Boolean)
    }).subscribe({
      next: (post) => {
        this.submitting = false;
        this.router.navigate(['/student/forum', post.id]);
      },
      error: () => {
        this.submitting = false;
        this.error = 'Failed to create post.';
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
