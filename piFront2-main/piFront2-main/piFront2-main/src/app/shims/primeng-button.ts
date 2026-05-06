import { Directive, NgModule } from '@angular/core';

@Directive({ selector: '[pButton]', standalone: true })
export class PButtonDirective {}

@NgModule({
  imports: [PButtonDirective],
  exports: [PButtonDirective]
})
export class ButtonModule {}

