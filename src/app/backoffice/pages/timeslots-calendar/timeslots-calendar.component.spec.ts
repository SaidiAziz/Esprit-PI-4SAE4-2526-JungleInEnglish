import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { TimeslotsCalendarComponent } from './timeslots-calendar.component';

describe('TimeslotsCalendarComponent', () => {
  let component: TimeslotsCalendarComponent;
  let fixture: ComponentFixture<TimeslotsCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeslotsCalendarComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimeslotsCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
