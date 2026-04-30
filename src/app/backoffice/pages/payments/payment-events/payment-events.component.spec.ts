import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { PaymentEventsComponent } from './payment-events.component';

describe('PaymentEventsComponent', () => {
  let component: PaymentEventsComponent;
  let fixture: ComponentFixture<PaymentEventsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentEventsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PaymentEventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
