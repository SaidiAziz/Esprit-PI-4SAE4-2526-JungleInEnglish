// availability.component.ts
import { Component, OnInit, ViewChild, TemplateRef, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AvailabilityService } from './services/availability.service';
import { AuthService } from '../../../core/services/auth.service';
import { Availability } from '../../../core/models/Availability';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-availability',
  templateUrl: './availability.component.html',
  styleUrls: ['./availability.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class AvailabilityComponent implements OnInit {

  availabilities: Availability[] = [];
  isLoading = false;
  editMode = false;
  tutorId = 0;
  tutorName = '';

  weekDays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  timeSlots = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00'];

  currentSlot: Availability = this.emptySlot();
  modalRef: NgbModalRef | null = null;

  @ViewChild('slotModal') slotModal!: TemplateRef<any>;

  constructor(
    private availabilityService: AvailabilityService,
    private authService: AuthService,
    private modalService: NgbModal,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    // Récupérer le tutorId depuis l'authentification
    const user = this.authService.getCurrentUser();
    if (user?.id) {
      this.tutorId = user.id;
      this.tutorName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Tutor';
    } else {
      console.error('User not found or not authenticated');
      return;
    }
    this.loadAvailabilities();
  }

  loadAvailabilities(): void {
    if (!this.tutorId) return;
    
    this.isLoading = true;
    this.availabilityService.getByTutor(this.tutorId).subscribe({
      next: (data) => {
        this.availabilities = data || [];
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading availabilities:', error);
        this.isLoading = false;
      }
    });
  }

  openModal(): void {
    this.editMode = false;
    this.currentSlot = this.emptySlot();
    this.modalRef = this.modalService.open(this.slotModal);
  }

  editSlot(slot: Availability): void {
    this.editMode = true;
    this.currentSlot = { ...slot };
    this.modalRef = this.modalService.open(this.slotModal);
  }

  closeModal(): void {
    this.modalRef?.dismiss();
    this.modalRef = null;
  }

  saveSlot(): void {
    // Validation de base
    if (!this.currentSlot.dayOfWeek || !this.currentSlot.startTime || !this.currentSlot.endTime) {
      alert('Please fill in all required fields');
      return;
    }

    // Validation du temps
    if (this.currentSlot.startTime >= this.currentSlot.endTime) {
      alert('Start time must be before end time');
      return;
    }

    this.isLoading = true;
    this.currentSlot.tutorId = this.tutorId;

    const action = this.editMode
      ? this.availabilityService.update(this.currentSlot.id!, this.currentSlot)
      : this.availabilityService.add(this.currentSlot);

    action.subscribe({
      next: () => {
        this.isLoading = false;
        this.loadAvailabilities();
        this.closeModal();
      },
      error: (error) => {
        console.error('Error saving slot:', error);
        alert('Error saving slot. Please try again.');
        this.isLoading = false;
      }
    });
  }

  toggleSlot(slot: Availability): void {
    if (!slot.id) return;
    
    this.availabilityService.toggle(slot.id).subscribe({
      next: () => {
        this.loadAvailabilities();
      },
      error: (error) => {
        console.error('Error toggling slot:', error);
        alert('Error updating slot availability.');
      }
    });
  }

  deleteSlot(id: number | undefined): void {
    if (!id) return;
    
    if (confirm('Are you sure you want to delete this availability slot?')) {
      this.availabilityService.delete(id).subscribe({
        next: () => {
          this.loadAvailabilities();
        },
        error: (error) => {
          console.error('Error deleting slot:', error);
          alert('Error deleting slot. Please try again.');
        }
      });
    }
  }

  hasSlot(day: string, hour: string): boolean {
    return this.availabilities.some(a =>
      a.dayOfWeek === day &&
      a.available &&
      a.startTime <= hour &&
      a.endTime > hour
    );
  }

  getSlot(day: string, hour: string): Availability {
    return this.availabilities.find(a =>
      a.dayOfWeek === day && a.startTime <= hour && a.endTime > hour
    )!;
  }

  emptySlot(): Availability {
    return {
      tutorId: this.tutorId,
      dayOfWeek: 'MONDAY',
      startTime: '09:00',
      endTime: '10:00',
      available: true,
      availabilityType: 'RECURRING',
      specificDate: ''
    };
  }
}