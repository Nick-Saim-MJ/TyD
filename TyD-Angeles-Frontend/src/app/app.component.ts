import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthStore } from './features/auth/auth.store';

@Component({
  selector:   'app-root',
  standalone: true,
  imports:    [RouterOutlet],
  template:   `<router-outlet />`
})
export class AppComponent implements OnInit {
  private readonly authStore = inject(AuthStore);

  ngOnInit(): void {
    // Restaurar sesión desde localStorage al arrancar la app
    this.authStore.inicializarDesdeStorage();
  }
}
