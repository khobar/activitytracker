import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';


import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {AlertComponent} from './directives/alert/alert.component';
import {HomeComponent} from './components/home/home.component';
import {routing} from "./app.routing";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {AuthGuard} from "./guards/auth.guard";
import {AlertService} from "./services/alert.service";
import {AuthenticationService} from "./services/authentication.service";


@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    routing
  ],
  declarations: [
    AppComponent,
    LoginComponent,
    AlertComponent,
    HomeComponent
  ],
  providers: [
    AuthGuard,
    AlertService,
    AuthenticationService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
