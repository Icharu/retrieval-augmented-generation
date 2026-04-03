import { Component, inject, signal, ViewChild, ElementRef, SecurityContext } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ChatService } from '../../services/chat.service';
import { DocumentService } from '../../services/document.service';
import { marked, Lexer, Parser } from 'marked';
import markedKatex from 'marked-katex-extension';

export interface ChatMessage {
  text: string;
  isBot: boolean;
}

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatPageComponent {
  private chatService = inject(ChatService);
  private documentService = inject(DocumentService);
  private sanitizer = inject(DomSanitizer);

  constructor() {
    marked.use(markedKatex({
      throwOnError: false,
      displayMode: false
    }));

    marked.use({
      breaks: true,
      gfm: true,
      pedantic: false
    });
  }

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
    this.messages.update(m => [...m, { text: query, isBot: false }]);
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

  renderMarkdown(text: string): SafeHtml {
    try {
      // Use the synchronous lexer + parser pipeline — works reliably in all marked v7+ versions
      // without relying on the deprecated { async: false } option.
      const tokens = marked.lexer(text);
      const rawHtml = marked.parser(tokens);
      return this.sanitizer.bypassSecurityTrustHtml(rawHtml);
    } catch (e) {
      console.error('Markdown parsing error', e);
      const fallback = text.replace(/\n/g, '<br>');
      return this.sanitizer.bypassSecurityTrustHtml(fallback);
    }
  }
}