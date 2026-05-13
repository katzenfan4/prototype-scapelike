import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GameService {
  private socket: WebSocket | null = null;
  public readonly messages$ = new Subject<string>();
  public connected: boolean = false;

  public connect(url: string): void {
    this.socket = new WebSocket(url);
    this.socket.onopen = () => (this.connected = true);
    this.socket.onclose = () => (this.connected = false);
    this.socket.onmessage = (event) => this.messages$.next(event.data as string);
    this.socket.onerror = (event) => console.error('WebSocket error', event);
  }

  public send(msg: string): void {
    this.socket?.send(msg);
  }

  public disconnect(): void {
    this.socket?.close();
  }
}
