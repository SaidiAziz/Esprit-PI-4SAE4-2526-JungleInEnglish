import { Component, Input, NgModule } from '@angular/core';

@Component({
  selector: 'p-radioButton',
  standalone: true,
  template: ''
})
export class RadioButtonComponent {
  @Input() name?: string;
  @Input() value?: unknown;
  @Input() disabled?: boolean;
}

@NgModule({
  imports: [RadioButtonComponent],
  exports: [RadioButtonComponent]
})
export class RadioButtonModule {}

