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

  login(username: any, password: any) {
    console.log("Calling " + environment.baseURL + 'user');
    return this.http.post<any>(environment.baseURL + 'user', {apiKey: username, secret: password})
      .map(user => {
        if (user) {
          let currentUser = JSON.stringify(new User(user.apiKey, user.secret));
          localStorage.setItem('currentUser', currentUser);
        }
        return user;
      });
  }

  loggedIn() {
    return (localStorage.getItem('currentUser'))
  }
}
