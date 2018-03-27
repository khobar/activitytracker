import {Injectable} from '@angular/core';
import {User} from "../models/user";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import 'rxjs/Rx';



@Injectable()
export class AuthenticationService {

  constructor(private http: HttpClient) {
  }


  logout() {
    localStorage.removeItem('currentUser');
  }

  loginHttp(username: any, password: any) {
    console.log("Calling " + environment.baseURL + 'user');
    return this.http.post<any>(environment.baseURL + 'user', {apiKey: username, secret: password})
      .map(user => {
        if (user) {
          localStorage.setItem('currentUser', JSON.stringify(user));
        }
        return user;
      });
  }


  login(apiKey: any, secret: any) {
    if (apiKey === 'user' && secret === 'password') {
      let user = new User(apiKey, secret);
      localStorage.setItem('currentUser', JSON.stringify(user));
      return user
    }
  }

  loggedIn() {
    return (localStorage.getItem('currentUser'))
  }
}
