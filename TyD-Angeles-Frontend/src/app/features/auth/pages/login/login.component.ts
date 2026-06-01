import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  effect,
  inject,
  signal,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthStore } from '../../auth.store';
import {NgOptimizedImage} from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, NgOptimizedImage],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent implements OnInit {

  // ── Dependencias ──────────────────────────────────────────────────────────
  readonly store = inject(AuthStore);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  // ── Estado local ──────────────────────────────────────────────────────────
  readonly currentYear = new Date().getFullYear();
  readonly mostrarPassword = signal(false);
  readonly sesionExpirada = signal(false);
  readonly tiempoRestante = signal('--:--');

  // ── Formulario ────────────────────────────────────────────────────────────
  readonly form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required]],
  });

  // ── Countdown ─────────────────────────────────────────────────────────────
  private countdownTimer: ReturnType<typeof setInterval> | null = null;

  constructor() {
    effect(() => {
      const hasta = this.store.bloqueadoHasta();

      if (hasta) {
        this.iniciarCountdown(hasta);
      } else {
        this.detenerCountdown();
        this.tiempoRestante.set('--:--');
      }
    });

    this.destroyRef.onDestroy(() => this.detenerCountdown());
  }

  // ── Ciclo de vida ─────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.sesionExpirada.set(!!params['sesionExpirada']);
    });
  }

  // ── Validaciones ──────────────────────────────────────────────────────────
  get usernameInvalido(): boolean {
    const c = this.form.get('username')!;
    return c.invalid && c.touched;
  }

  get passwordInvalido(): boolean {
    const c = this.form.get('password')!;
    return c.invalid && c.touched;
  }

  // ── Acciones ──────────────────────────────────────────────────────────────
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.store.cargando() || this.store.bloqueadoHasta()) return;

    this.store.login({
      username: this.form.value.username!.trim(),
      password: this.form.value.password!,
    });
  }

  togglePassword(): void {
    this.mostrarPassword.update(v => !v);
  }

  // ── Countdown ─────────────────────────────────────────────────────────────
  private iniciarCountdown(bloqueadoHastaIso: string): void {
    this.detenerCountdown();

    const tick = () => {
      const diff =
        new Date(bloqueadoHastaIso).getTime() - Date.now();

      if (diff <= 0) {
        this.tiempoRestante.set('00:00');
        this.detenerCountdown();
        this.store.limpiarError();
        return;
      }

      const mins = Math.floor(diff / 60_000);
      const secs = Math.floor((diff % 60_000) / 1000);

      this.tiempoRestante.set(
        `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
      );
    };

    tick();

    this.countdownTimer = setInterval(tick, 1000);
  }

  private detenerCountdown(): void {
    if (this.countdownTimer !== null) {
      clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    }
  }
}
