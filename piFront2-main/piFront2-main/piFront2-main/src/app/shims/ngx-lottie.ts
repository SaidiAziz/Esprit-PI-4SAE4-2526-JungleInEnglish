import { Component, Input } from '@angular/core';

export interface AnimationOptions {
  path?: string;
  loop?: boolean;
  autoplay?: boolean;
}

@Component({
  selector: 'ng-lottie',
  standalone: true,
  template: ''
})
export class LottieComponent {
  @Input() options?: AnimationOptions;
  @Input() width?: string;
  @Input() height?: string;
}

