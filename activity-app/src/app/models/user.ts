export class User {
  apiKey: string;
  auth: string;

  constructor(apiKey: string, secret: string) {
    this.apiKey = apiKey;
    this.auth = btoa(apiKey + ":" + secret);
  }
}
