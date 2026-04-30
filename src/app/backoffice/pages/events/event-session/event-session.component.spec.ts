import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { EventSessionComponent } from './event-session.component';

describe('EventSessionComponent', () => {
  let component: EventSessionComponent;
  let fixture: ComponentFixture<EventSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventSessionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
