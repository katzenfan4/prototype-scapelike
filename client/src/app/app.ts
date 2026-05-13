import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { GameService, SessionInfo } from './services/game.service';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit, OnDestroy {
  public messages: string[] = [];
  public loggedIn: boolean = false;
  public tickSeq: number = 0;
  public tickCount: number = 0;
  public sessionInfo: SessionInfo | null = null;

  private messageSub!: Subscription;
  private tickSub!: Subscription;
  private sessionSub!: Subscription;

  constructor(public readonly game: GameService) {}

  ngOnInit(): void {
    this.messageSub = this.game.messages$.subscribe((msg) => this.messages.push(msg));
    this.tickSub = this.game.tick$.subscribe((seq) => {
      this.tickSeq = seq;
      this.tickCount++;
    });
    this.sessionSub = this.game.sessionInfo$.subscribe((session) => (this.sessionInfo = session));
  }

  public async onLogin(username: string): Promise<void> {
    if (!username.trim()) return;
    try {
      await this.game.login(username.trim());
      this.loggedIn = true;
    } catch (e) {
      console.error('Login failed', e);
    }
  }

  ngOnDestroy(): void {
    this.messageSub.unsubscribe();
    this.tickSub.unsubscribe();
    this.sessionSub.unsubscribe();
    this.game.disconnect();
  }
}
