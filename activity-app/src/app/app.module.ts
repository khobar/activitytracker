import {BrowserModule} from '@angular/platform-browser';
import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';


import {AddActivityComponent} from './components/add-activity/add-activity.component';
import {AlertComponent} from './directives/alert/alert.component';
import {AppComponent} from './app.component';
import {ChartComponent} from './components/chart/chart.component';
import {LoginComponent} from './components/login/login.component';
import {HomeComponent} from './components/home/home.component';
import {NonWorkingComponent} from './components/non-working/non-working.component';

import {AuthGuard} from "./guards/auth.guard";
import {AlertService} from "./services/alert.service";
import {AuthenticationService} from "./services/authentication.service";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {ActivitiesService} from "./services/activities.service";

import {routing} from "./app.routing";
import {FormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {NgHttpLoaderModule} from "ng-http-loader/ng-http-loader.module";
import {MDBBootstrapModule} from 'angular-bootstrap-md';
import {DatePipe} from "@angular/common";
import {MyDateRangePickerModule} from "mydaterangepicker";
import {MyDatePickerModule} from "mydatepicker";


@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    NgHttpLoaderModule,
    MDBBootstrapModule.forRoot(),
    MyDateRangePickerModule,
    MyDatePickerModule,
    routing,
  ],
  declarations: [
    AppComponent,
    LoginComponent,
    AlertComponent,
    HomeComponent,
    ChartComponent,
    NonWorkingComponent,
    AddActivityComponent
  ],
  providers: [
    AuthGuard,
    AlertService,
    ActivitiesService,
    AuthenticationService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    DatePipe],
  bootstrap: [AppComponent],
  schemas: [NO_ERRORS_SCHEMA]
})
export class AppModule {
}
