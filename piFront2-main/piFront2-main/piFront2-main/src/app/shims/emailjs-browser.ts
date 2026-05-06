const emailjs = {
  send(
    serviceID: string,
    templateID: string,
    params: Record<string, unknown>,
    publicKey: string
  ): Promise<{ status: number; text: string }> {
    return fetch('https://api.emailjs.com/api/v1.0/email/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        service_id: serviceID,
        template_id: templateID,
        user_id: publicKey,
        template_params: params
      })
    }).then(r => ({ status: r.status, text: r.statusText }));
  }
};

export default emailjs;
