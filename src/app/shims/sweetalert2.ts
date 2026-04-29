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
  showConfirmButton?: boolean;
  confirmButtonText?: string;
  cancelButtonText?: string;
  confirmButtonColor?: string;
  cancelButtonColor?: string;
  allowOutsideClick?: boolean;
  toast?: boolean;
  position?: 'top-end' | 'top-start' | 'top' | 'center' | 'bottom-end' | 'bottom-start' | 'bottom';
  timer?: number;
  timerProgressBar?: boolean;
}

const Swal = {
  fire(options: SweetAlertOptions): Promise<SweetAlertResult> {
    const message = [options.title, options.text].filter(Boolean).join('\n');
    let confirmed = true;
    if (options.showCancelButton) {
      confirmed = window.confirm(message || 'Confirm?');
    } else {
      window.alert(message || '');
    }
    return Promise.resolve({
      isConfirmed: confirmed,
      isDismissed: !confirmed,
      isDenied: false
    });
  }
};

export default Swal;

