import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ParticipationListComponent } from './participation-list.component';

describe('ParticipationListComponent', () => {
  let component: ParticipationListComponent;
  let fixture: ComponentFixture<ParticipationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParticipationListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
