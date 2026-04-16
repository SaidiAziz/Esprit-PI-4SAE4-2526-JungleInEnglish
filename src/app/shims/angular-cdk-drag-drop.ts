import { Directive, EventEmitter, Input, NgModule, Output } from '@angular/core';

export interface CdkDragDrop<T = any> {
  previousIndex: number;
  currentIndex: number;
  item?: unknown;
  container?: { data: T };
  previousContainer?: { data: T };
}

export function moveItemInArray<T>(array: T[], fromIndex: number, toIndex: number): void {
  if (!array.length || fromIndex === toIndex) return;
  const item = array.splice(fromIndex, 1)[0];
  array.splice(toIndex, 0, item);
}

@Directive({ selector: '[cdkDrag]', standalone: true })
export class CdkDragDirective {}

@Directive({ selector: '[cdkDropList]', standalone: true })
export class CdkDropListDirective<T = any> {
  @Input() cdkDropListData?: T;
  @Output() cdkDropListDropped = new EventEmitter<CdkDragDrop<T>>();
}

@NgModule({
  imports: [CdkDragDirective, CdkDropListDirective],
  exports: [CdkDragDirective, CdkDropListDirective]
})
export class DragDropModule {}
