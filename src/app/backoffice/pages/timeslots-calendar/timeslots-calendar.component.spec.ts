import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeslotsCalendarComponent } from './timeslots-calendar.component';

describe('TimeslotsCalendarComponent', () => {
  let component: TimeslotsCalendarComponent;
  let fixture: ComponentFixture<TimeslotsCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeslotsCalendarComponent]
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
