import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pw  = group.get('newPassword')?.value;
  const cpw = group.get('confirmPassword')?.value;
  return pw && cpw && pw !== cpw ? { mismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent implements OnInit {
  form!: FormGroup;
  submitted = false;
  loading = false;
  successMessage = '';
  errorMessage = '';
  token = '';
  tokenMissing = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.tokenMissing = true;
    }

    this.form = this.fb.group(
      {
        newPassword:     ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required]
      },
      { validators: passwordsMatch }
    );
  }

  get f() { return this.form.controls; }

  onSubmit(): void {
    this.submitted = true;
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid || this.tokenMissing) return;

    this.loading = true;
    this.form.disable();

    this.authService.resetPassword({ token: this.token, newPassword: this.form.value.newPassword }).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMessage = res.message;
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (err) => {
        this.loading = false;
        this.form.enable();
        if (err.status === 400) {
          this.errorMessage = 'Reset link is invalid or has expired. Please request a new one.';
        } else {
          this.errorMessage = 'Something went wrong. Please try again.';
        }
      }
    });
  }
}

