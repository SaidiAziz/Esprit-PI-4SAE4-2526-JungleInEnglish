import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ParticipateSessionComponent } from './participate-session.component';

describe('ParticipateSessionComponent', () => {
  let component: ParticipateSessionComponent;
  let fixture: ComponentFixture<ParticipateSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipateSessionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipateSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
