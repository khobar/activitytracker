import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "../../services/authentication.service";
import {AlertService} from "../../services/alert.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  model: any = {};
  loading = false;
  returnUrl: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authenticationService: AuthenticationService,
    private alertService: AlertService) {
  }

  ngOnInit() {
    this.authenticationService.logout();
    // this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  login() {
    this.loading = true;
    this.authenticationService.login(this.model.apiKey, this.model.secret)
      .subscribe(
        data => {
          this.router.navigate(["/"]);
        },
        error => {
          this.alertService.error("Failed to verify API key");
          this.loading = false;
        });
  }


}
