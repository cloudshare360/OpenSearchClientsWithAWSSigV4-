import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Add any authentication headers here if needed
    const authReq = request.clone({
      setHeaders: {
        'Content-Type': 'application/json'
      }
    });
    return next.handle(authReq);
  }
}
