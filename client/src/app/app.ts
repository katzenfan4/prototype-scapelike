import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { GameService } from './services/game.service';

const wsUrl = 'ws://localhost:7070/ws/game';

@Component({
  selector: 'app-root',
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit, OnDestroy {
  public messages: string[] = [];
  private messageSub!: Subscription;

  constructor(public readonly game: GameService) {}

  ngOnInit(): void {
    this.messageSub = this.game.messages$.subscribe((msg) => this.messages.push(msg));
    this.game.connect(wsUrl);
  }

  ngOnDestroy(): void {
    this.messageSub.unsubscribe();
    this.game.disconnect();
  }
}
