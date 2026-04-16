export interface SweetAlertResult {
  isConfirmed: boolean;
  isDismissed: boolean;
  isDenied: boolean;
}

export interface SweetAlertOptions {
  icon?: 'success' | 'error' | 'warning' | 'info' | 'question';
  title?: string;
  text?: string;
  showCancelButton?: boolean;
  confirmButtonText?: string;
  cancelButtonText?: string;
  confirmButtonColor?: string;
  cancelButtonColor?: string;
  timer?: number;
  timerProgressBar?: boolean;
}

const Swal = {
  fire(options: SweetAlertOptions): Promise<SweetAlertResult> {
    const message = [options.title, options.text].filter(Boolean).join('\n');
    const confirmed = options.showCancelButton ? window.confirm(message || 'Confirm?') : (window.alert(message || ''), true);
    return Promise.resolve({
      isConfirmed: confirmed,
      isDismissed: !confirmed,
      isDenied: false
    });
  }
};

export default Swal;

