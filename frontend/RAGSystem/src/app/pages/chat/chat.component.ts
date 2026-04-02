import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { DocumentService } from '../../services/document.service';

export interface ChatMessage {
  text: string;
  isBot: boolean;
}

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatPageComponent {
  private chatService = inject(ChatService);
  private documentService = inject(DocumentService);
  
  messages = signal<ChatMessage[]>([]);
  isLoading = signal(false);
  currentQuery = '';

  selectedFile = signal<File | null>(null);
  isUploading = signal(false);
  uploadMessage = signal<string>('');

  @ViewChild('fileInput') fileInput!: ElementRef;

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile.set(input.files[0]);
      this.uploadMessage.set('');
    }
  }

  upload() {
    const file = this.selectedFile();
    if (!file) return;

    this.isUploading.set(true);
    this.uploadMessage.set('Uploading & Learning...');

    this.documentService.uploadDocument(file).subscribe({
      next: () => {
        this.isUploading.set(false);
        this.uploadMessage.set('Knowledge integrated successfully!');
        this.selectedFile.set(null);
      },
      error: (err) => {
        this.isUploading.set(false);
        this.uploadMessage.set('Error uploading document.');
        console.error(err);
      }
    });
  }

  async sendMessage() {
    if (!this.currentQuery.trim() || this.isLoading()) return;

    const query = this.currentQuery.trim();
    // Add user query immediately
    this.messages.update(m => [...m, { text: query, isBot: false }]);
    
    // Add empty bot structure to be appended progressively
    this.messages.update(m => [...m, { text: '', isBot: true }]);
    
    this.currentQuery = '';
    this.isLoading.set(true);

    try {
      const stream = this.chatService.streamQuery(query);
      
      let isFirstChunk = true;

      for await (const chunk of stream) {
        if (isFirstChunk) {
          this.isLoading.set(false);
          isFirstChunk = false;
        }
        
        // Append streamed text cleanly by updating the very last array item
        this.messages.update(msgs => {
          const newMsgs = [...msgs];
          const lastMsg = newMsgs[newMsgs.length - 1];
          lastMsg.text += chunk;
          return newMsgs;
        });
      }
    } catch (err) {
      this.messages.update(m => {
        const newMsgs = [...m];
        newMsgs[newMsgs.length - 1].text = 'Sorry, my response stream interrupted unexpectedly.';
        return newMsgs;
      });
      console.error('Streaming error:', err);
    } finally {
      this.isLoading.set(false);
    }
  }
}
