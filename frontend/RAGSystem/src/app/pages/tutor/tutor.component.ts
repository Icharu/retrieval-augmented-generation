import { Component, inject, signal, SecurityContext } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TutorService } from '../../services/tutor.service';
import { marked } from 'marked';
import markedKatex from 'marked-katex-extension';

export interface TutorMessage {
  text: string;
  isBot: boolean;
}

export interface Language {
  code: string;
  name: string;
  nativeName: string;
  flag: string;
}

@Component({
  selector: 'app-tutor-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './tutor.component.html',
  styleUrls: ['./tutor.component.css']
})
export class TutorPageComponent {
  private tutorService = inject(TutorService);
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

  messages = signal<TutorMessage[]>([]);
  isLoading = signal(false);
  currentQuery = '';

  languages: Language[] = [
    { code: 'en', name: 'English',    nativeName: 'English',    flag: '🇺🇸' },
    { code: 'es', name: 'Spanish',    nativeName: 'Español',    flag: '🇪🇸' },
    { code: 'fr', name: 'French',     nativeName: 'Français',   flag: '🇫🇷' },
    { code: 'pt', name: 'Portuguese', nativeName: 'Português',  flag: '🇧🇷' },
    { code: 'de', name: 'German',     nativeName: 'Deutsch',    flag: '🇩🇪' },
    { code: 'it', name: 'Italian',    nativeName: 'Italiano',   flag: '🇮🇹' },
    { code: 'ja', name: 'Japanese',   nativeName: '日本語',      flag: '🇯🇵' },
    { code: 'zh', name: 'Mandarin',   nativeName: '中文',        flag: '🇨🇳' },
    { code: 'ko', name: 'Korean',     nativeName: '한국어',      flag: '🇰🇷' },
    { code: 'ar', name: 'Arabic',     nativeName: 'العربية',     flag: '🇸🇦' },
  ];

  selectedLanguage = signal<Language>(this.languages[0]);

  selectLanguage(lang: Language) {
    this.selectedLanguage.set(lang);
  }

  async sendMessage() {
    if (!this.currentQuery.trim() || this.isLoading()) return;

    const query = this.currentQuery.trim();
    const targetLang = this.selectedLanguage().name;

    this.messages.update(m => [...m, { text: query, isBot: false }]);
    this.messages.update(m => [...m, { text: '', isBot: true }]);

    this.currentQuery = '';
    this.isLoading.set(true);

    try {
      const stream = this.tutorService.streamQuery(query, targetLang);
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
        newMsgs[newMsgs.length - 1].text = 'Sorry, the response stream was interrupted unexpectedly.';
        return newMsgs;
      });
      console.error('Streaming error:', err);
    } finally {
      this.isLoading.set(false);
    }
  }

  renderMarkdown(text: string): SafeHtml {
    try {
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
