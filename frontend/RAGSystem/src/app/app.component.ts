import { Component } from '@angular/core';
import { ChatPageComponent } from './pages/chat/chat.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ChatPageComponent],
  template: `<app-chat-page class="block w-full h-screen relative"></app-chat-page>`
})
export class AppComponent {
  title = 'Leia.AI';
}
