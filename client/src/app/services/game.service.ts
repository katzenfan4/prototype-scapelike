import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

const wsBaseUrl = 'ws://localhost:7070/ws/game';
const authUrl = 'http://localhost:7070/auth/login';

export interface SessionInfo {
  playerId: number;
}

@Injectable({ providedIn: 'root' })
export class GameService {
  private socket: WebSocket | null = null;
  public readonly messages$ = new Subject<string>();
  public readonly tick$ = new Subject<number>();
  public readonly sessionInfo$ = new Subject<SessionInfo>();
  public connected: boolean = false;

  public async login(username: string): Promise<void> {
    const response = await fetch(authUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username }),
    });
    if (!response.ok) {
      throw new Error(`Login failed: ${response.status}`);
    }
    const data = (await response.json()) as { token: string };
    this.connect(`${wsBaseUrl}?token=${data.token}`);
  }

  private connect(url: string): void {
    this.socket = new WebSocket(url);
    this.socket.onopen = () => (this.connected = true);
    this.socket.onclose = () => (this.connected = false);
    this.socket.onmessage = (event) => this.handleMessage(event.data as string);
    this.socket.onerror = (event) => console.error('WebSocket error', event);
  }

  private handleMessage(raw: string): void {
    try {
      const msg = JSON.parse(raw) as { type: string; [key: string]: unknown };
      if (msg.type === 'tick') {
        this.tick$.next(msg['seq'] as number);
      } else if (msg.type === 'session') {
        this.sessionInfo$.next({ playerId: msg['playerId'] as number });
      } else {
        this.messages$.next(raw);
      }
    } catch {
      this.messages$.next(raw);
    }
  }

  public send(msg: string): void {
    this.socket?.send(msg);
  }

  public disconnect(): void {
    this.socket?.close();
  }
}
