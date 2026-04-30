import { Injectable, TemplateRef } from '@angular/core';

export class NgbModalRef {
  close(_result?: unknown): void {}
  dismiss(_reason?: unknown): void {}
}

@Injectable({ providedIn: 'root' })
export class NgbModal {
  open(_content: TemplateRef<unknown> | unknown): NgbModalRef {
    return new NgbModalRef();
  }
}

