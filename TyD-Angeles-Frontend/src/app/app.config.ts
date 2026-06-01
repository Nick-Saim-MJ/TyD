import {
  ApplicationConfig,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { jwtInterceptor, errorInterceptor } from './core/interceptors/interceptors';

export const appConfig: ApplicationConfig = {
  providers: [
    // Coalescencia de eventos: reduce detecciones de cambio innecesarias
    provideZoneChangeDetection({ eventCoalescing: true }),

    // Router: binding automático + transiciones suaves entre rutas
    provideRouter(
      routes,
      withComponentInputBinding(),
      withViewTransitions(), // Angular 19: animaciones nativas de navegación
    ),

    // HttpClient: Fetch API (más eficiente que XHR) + interceptores funcionales
    provideHttpClient(withFetch(), withInterceptors([jwtInterceptor, errorInterceptor])),
  ],
};
