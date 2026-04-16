import { Component, NgModule } from '@angular/core';

@Component({
  selector: 'p-toast',
  standalone: true,
  template: ''
})
export class ToastComponent {}

@NgModule({
  imports: [ToastComponent],
  exports: [ToastComponent]
})
export class ToastModule {}

