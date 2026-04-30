import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { EventDashboardFrontComponent } from './event-dashboard-front.component';

describe('EventDashboardFrontComponent', () => {
  let component: EventDashboardFrontComponent;
  let fixture: ComponentFixture<EventDashboardFrontComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventDashboardFrontComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(EventDashboardFrontComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
