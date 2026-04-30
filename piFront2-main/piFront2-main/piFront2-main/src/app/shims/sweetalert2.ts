export interface SweetAlertResult {
  isConfirmed: boolean;
  isDismissed: boolean;
  isDenied: boolean;
}

export interface SweetAlertOptions {
  icon?: 'success' | 'error' | 'warning' | 'info' | 'question';
  title?: string;
  text?: string;
  html?: string;
  showCancelButton?: boolean;
  confirmButtonText?: string;
  cancelButtonText?: string;
  confirmButtonColor?: string;
  cancelButtonColor?: string;
  timer?: number;
  timerProgressBar?: boolean;
  toast?: boolean;
  position?: string;
  showConfirmButton?: boolean;
  target?: EventTarget | null;
  acceptButtonStyleClass?: string;
  message?: string;
  header?: string;
  accept?: () => void;
}

type SwalInstance = {
  fire(options: SweetAlertOptions): Promise<SweetAlertResult>;
  fire(title: string, message?: string, icon?: string): Promise<SweetAlertResult>;
  mixin(opts: SweetAlertOptions): SwalInstance;
};

function _fire(optionsOrTitle: SweetAlertOptions | string, message?: string, icon?: string): Promise<SweetAlertResult> {
  let title: string | undefined;
  let text: string | undefined;
  let showCancel = false;

  if (typeof optionsOrTitle === 'string') {
    title = optionsOrTitle;
    text = message;
  } else {
    title = optionsOrTitle.title;
    text = optionsOrTitle.text ?? optionsOrTitle.html;
    showCancel = optionsOrTitle.showCancelButton ?? false;
  }

  const msg = [title, text].filter(Boolean).join('\n');
  const confirmed = showCancel
    ? window.confirm(msg || 'Confirm?')
    : (window.alert(msg || ''), true);

  return Promise.resolve({ isConfirmed: confirmed, isDismissed: !confirmed, isDenied: false });
}

const Swal: SwalInstance = {
  fire: _fire as SwalInstance['fire'],
  mixin(opts: SweetAlertOptions): SwalInstance {
    return {
      fire: (optionsOrTitle: SweetAlertOptions | string, message?: string, icon?: string) => {
        const merged = typeof optionsOrTitle === 'object'
          ? { ...opts, ...optionsOrTitle }
          : optionsOrTitle;
        return _fire(merged as any, message, icon);
      },
      mixin: Swal.mixin.bind(Swal)
    };
  }
};

export default Swal;
