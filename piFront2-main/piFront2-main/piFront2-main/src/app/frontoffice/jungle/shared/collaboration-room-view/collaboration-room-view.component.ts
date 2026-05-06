import { Component, inject, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom, forkJoin, Subscription } from 'rxjs';
import {
  Challenge,
  ChallengeAnswerResponse,
  ChallengeSubmission,
  PeerCorrection,
  Room,
  RoomMessage,
  RoomParticipant
} from '../../../../core/models/collaboration.model';
import { AuthService } from '../../../../core/services/auth.service';
import { AppNotificationService } from '../../../../core/services/app-notification.service';
import { CollaborationService } from '../../../../core/services/collaboration.service';
import { RoomCallService } from '../../../../core/services/room-call.service';
import { UserDirectoryService } from '../../../../core/services/user-directory.service';
import { DASHBOARD_MATERIAL_IMPORTS } from '../../../shared/dashboard-material.imports';

@Component({
  selector: 'app-collaboration-room-view',
  standalone: true,
  imports: [...DASHBOARD_MATERIAL_IMPORTS],
  template: `
    <section class="fd-page-shell" *ngIf="room">
      <header class="fd-page-hero">
        <div>
          <p class="fd-eyebrow">Practice Room</p>
          <h1>{{ room.title }}</h1>
          <p class="fd-muted">{{ room.topic }} | {{ room.level }} | {{ room.type }}</p>
        </div>
        <div class="fd-chip-row">
          <mat-chip>{{ roomMemberCount }} people</mat-chip>
          <mat-chip highlighted>{{ challenges.length }} challenges</mat-chip>
          <ng-container *ngIf="!isTutor; else tutorRoomState">
            <button mat-stroked-button type="button" (click)="joinRoom()">Join</button>
            <button mat-button type="button" (click)="leaveRoom()">Leave</button>
          </ng-container>
          <ng-template #tutorRoomState>
            <mat-chip>Tutor view</mat-chip>
            <mat-chip>{{ room.status }}</mat-chip>
          </ng-template>
        </div>
      </header>

      <mat-card class="fd-surface-card" *ngIf="supportsVideoCall">
        <div class="fd-section-head">
          <div>
            <h2>{{ isVoiceOnlyRoom ? 'Voice Room Meeting' : 'Mixed Practice Meeting' }}</h2>
            <p>{{ isVoiceOnlyRoom ? 'This room is call-first. Join the meeting to practice live with the participants below.' : 'Join the meeting to practice live, while keeping the study chat available underneath.' }}</p>
          </div>
          <div class="fd-chip-row">
            <span class="fd-pill">{{ callStateLabel }}</span>
            <span class="fd-pill">{{ roomMemberCount }} in room</span>
            <span class="fd-pill" *ngIf="inCall">{{ callParticipantCount }} in call</span>
            <span class="fd-pill" *ngIf="handRaised">Hand raised</span>
            <button mat-flat-button color="primary" type="button" *ngIf="!inCall" (click)="joinCall()">Join call</button>
          </div>
        </div>

        <div class="fd-meeting-layout">
          <div>
            <div class="fd-video-grid" *ngIf="inCall; else callHint">
              <article class="fd-video-tile fd-video-tile--local">
                <video id="local-video" autoplay muted playsinline></video>
                <div class="fd-video-tile__label">
                  You
                  <span *ngIf="screenSharing"> | Sharing screen</span>
                  <span *ngIf="handRaised"> | Hand raised</span>
                </div>
              </article>
              <article class="fd-video-tile" *ngFor="let remote of remoteParticipants">
                <video [id]="'remote-video-' + remote.userId" autoplay playsinline></video>
                <div class="fd-video-tile__label">
                  {{ participantName(remote.userId) }}
                  <span *ngIf="isHandRaised(remote.userId)"> | Hand raised</span>
                </div>
              </article>
            </div>

            <ng-template #callHint>
              <div class="fd-empty-state">Join the call when you want live speaking/video practice.</div>
            </ng-template>

            <div class="fd-call-controls" *ngIf="inCall">
              <button mat-mini-fab type="button" (click)="toggleMicrophone()" [class.fd-call-control--active]="microphoneEnabled" [attr.aria-label]="microphoneEnabled ? 'Mute microphone' : 'Unmute microphone'">
                <span class="fd-call-button__icon" [innerHTML]="callControlIcon(microphoneEnabled ? 'mic-on' : 'mic-off')"></span>
                <span class="fd-call-button__text">{{ microphoneEnabled ? 'Mic' : 'Muted' }}</span>
              </button>
              <button mat-mini-fab type="button" (click)="toggleCamera()" [class.fd-call-control--active]="cameraEnabled" [attr.aria-label]="cameraEnabled ? 'Turn camera off' : 'Turn camera on'">
                <span class="fd-call-button__icon" [innerHTML]="callControlIcon(cameraEnabled ? 'cam-on' : 'cam-off')"></span>
                <span class="fd-call-button__text">{{ cameraEnabled ? 'Camera' : 'Camera off' }}</span>
              </button>
              <button mat-mini-fab type="button" (click)="toggleScreenShare()" [class.fd-call-control--active]="screenSharing" [attr.aria-label]="screenSharing ? 'Stop screen sharing' : 'Share your screen'">
                <span class="fd-call-button__icon" [innerHTML]="callControlIcon(screenSharing ? 'share-stop' : 'share-start')"></span>
                <span class="fd-call-button__text">{{ screenSharing ? 'Stop share' : 'Share' }}</span>
              </button>
              <button mat-mini-fab type="button" (click)="toggleRaiseHand()" [class.fd-call-control--active]="handRaised" [attr.aria-label]="handRaised ? 'Lower hand' : 'Raise hand'">
                <span class="fd-call-button__icon" [innerHTML]="callControlIcon(handRaised ? 'hand-down' : 'hand-up')"></span>
                <span class="fd-call-button__text">{{ handRaised ? 'Lower hand' : 'Raise hand' }}</span>
              </button>
              <button mat-mini-fab type="button" color="warn" (click)="leaveCall()" aria-label="Leave call">
                <span class="fd-call-button__icon" [innerHTML]="callControlIcon('leave')"></span>
                <span class="fd-call-button__text">Leave</span>
              </button>
            </div>
          </div>

          <aside class="fd-meeting-roster">
            <div class="fd-section-head fd-section-head--embedded">
              <div>
                <h2>Meeting Members</h2>
                <p>See who is in the room before joining the call.</p>
              </div>
            </div>
            <div class="fd-room-stack">
              <article class="fd-room-row" *ngFor="let participant of participants">
                <div>
                  <strong>{{ participantLabel(participant) }}</strong>
                  <p>{{ participant.role }}</p>
                </div>
                <div class="fd-room-meta">
                  <span class="fd-pill" *ngIf="isParticipantInCall(participant.userId)">In call</span>
                  <span class="fd-pill" *ngIf="isHandRaised(participant.userId)">Hand raised</span>
                </div>
              </article>
              <div class="fd-empty-state" *ngIf="!participants.length">No room member yet.</div>
            </div>
          </aside>
        </div>
      </mat-card>

      <div class="fd-chat-layout" *ngIf="supportsStudyChat">
        <mat-card class="fd-surface-card fd-chat-shell">
          <div class="fd-chat-shell__header">
            <div>
              <h2>{{ isTutor ? 'Live room conversation' : 'Room conversation' }}</h2>
              <p>{{ isTutor ? 'Reply in chat for normal help. Open a student message only when you want to write a correction.' : 'Send your message, ask for translation or correction if needed, then check the side panel for teacher feedback.' }}</p>
            </div>
            <span class="fd-feedback" *ngIf="feedback">{{ feedback }}</span>
          </div>

          <div class="fd-chat-stream">
            <article
              class="fd-chat-bubble"
              [class.fd-chat-bubble--mine]="isOwnMessage(message)"
              [class.fd-chat-bubble--teacher]="!isOwnMessage(message) && isTeacherMessage(message)"
              [class.fd-chat-bubble--selected]="selectedMessage?.id === message.id"
              *ngFor="let message of messages">
              <div class="fd-chat-bubble__meta">
                <strong>{{ senderLabel(message) }}</strong>
                <span>{{ formatTime(message.sentAt) }}</span>
              </div>
              <p>{{ message.content }}</p>
              <div class="fd-chip-row">
                <span class="fd-pill" *ngIf="message.translationRequest">Translation requested</span>
                <span class="fd-pill" *ngIf="!message.translationRequest && message.autoTranslation === 'Translation seen'">Translation seen</span>
                <span class="fd-pill" *ngIf="message.correctionRequest">Correction requested</span>
                <span class="fd-pill" *ngIf="message.language">{{ message.language }}</span>
              </div>
              <small *ngIf="message.translationRequest">Ask for translation first, then press Seen after you read the teacher help.</small>
              <div class="fd-chat-bubble__actions">
                <button mat-button type="button" (click)="openCorrections(message)">{{ isOwnMessage(message) ? 'View corrections' : 'Correct this message' }}</button>
                <button mat-button type="button" *ngIf="isOwnMessage(message) && !message.translationRequest" (click)="requestTranslation(message)">Ask translation</button>
                <button mat-button type="button" *ngIf="canConfirmTranslation(message)" (click)="confirmTranslation(message)">Seen</button>
                <button mat-button type="button" *ngIf="isOwnMessage(message) && !message.correctionRequest" (click)="requestCorrection(message)">Ask correction</button>
                <button mat-button type="button" *ngIf="isOwnMessage(message)" (click)="deleteMessage(message.id)">Delete</button>
              </div>
            </article>
            <div class="fd-empty-state" *ngIf="!messages.length">No message yet. Start the discussion here.</div>
          </div>

          <div class="fd-chat-composer">
            <div class="fd-chat-composer__fields">
              <mat-form-field appearance="outline">
                <mat-label>{{ isTutor ? 'Reply to the room' : 'Write your message' }}</mat-label>
                <textarea matInput rows="3" [(ngModel)]="messageForm.content" name="content"></textarea>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Language</mat-label>
                <input matInput [(ngModel)]="messageForm.language" name="language">
              </mat-form-field>
            </div>
            <div class="fd-chat-composer__actions">
              <div class="fd-chip-row" *ngIf="!isTutor">
                <mat-checkbox [(ngModel)]="messageForm.translationRequest" name="translationRequest">Ask translation</mat-checkbox>
                <mat-checkbox [(ngModel)]="messageForm.correctionRequest" name="correctionRequest">Ask teacher correction</mat-checkbox>
              </div>
              <button mat-flat-button color="primary" type="button" (click)="sendMessage()">Send message</button>
            </div>
          </div>
        </mat-card>

        <div class="fd-chat-sidebar">
          <mat-card class="fd-surface-card" *ngIf="isTutor">
            <div class="fd-section-head">
              <div>
                <h2>Teacher Console</h2>
                <p>Translation requests are handled in normal chat. Use this panel mainly for student correction requests.</p>
              </div>
            </div>

            <div class="fd-room-stack">
              <article class="fd-highlight-panel" *ngFor="let message of messagesNeedingHelp">
                <div class="fd-section-inline">
                  <div>
                    <strong>{{ senderLabel(message) }}</strong>
                    <p>{{ message.content }}</p>
                  </div>
                  <span class="fd-pill">{{ message.correctionRequest ? 'Correction needed' : 'Translation asked' }}</span>
                </div>
                <div class="fd-chip-row">
                  <button mat-button type="button" (click)="focusMessage(message)">Open in chat</button>
                  <button mat-button type="button" *ngIf="message.correctionRequest" (click)="openCorrections(message)">Correct this message</button>
                </div>
              </article>
              <div class="fd-empty-state" *ngIf="!messagesNeedingHelp.length">No pending student help request right now.</div>
            </div>
          </mat-card>

          <mat-card class="fd-surface-card">
            <div class="fd-section-head">
              <div>
                <h2>{{ selectedMessage ? 'Corrections for selected message' : 'Corrections' }}</h2>
                <p>{{ selectedMessage ? 'Review or add a correction for the selected chat message.' : 'Select a message in chat to work on it.' }}</p>
              </div>
            </div>

            <div *ngIf="selectedMessage; else correctionHint" class="fd-room-stack">
              <div class="fd-highlight-panel">
                <strong>{{ senderLabel(selectedMessage) }}</strong>
                <p>{{ selectedMessage.content }}</p>
                <p class="fd-muted-inline" *ngIf="selectedMessage.translationRequest">This student asked for translation help. Reply here, then the student can click Seen.</p>
              </div>

              <form class="fd-form-grid" *ngIf="selectedMessage.correctionRequest && selectedMessage.senderId !== currentUserId" (ngSubmit)="submitCorrection()">
                <mat-form-field appearance="outline">
                  <mat-label>Better version of the message</mat-label>
                  <textarea matInput rows="2" [(ngModel)]="correctionForm.correctedText" name="correctedText"></textarea>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Explain the mistake</mat-label>
                  <textarea matInput rows="2" [(ngModel)]="correctionForm.explanation" name="explanation"></textarea>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Type of mistake</mat-label>
                  <mat-select [(ngModel)]="correctionForm.errorType" name="errorType">
                    <mat-option *ngFor="let type of errorTypes" [value]="type">{{ type }}</mat-option>
                  </mat-select>
                </mat-form-field>
                <button mat-flat-button color="primary" type="submit">Save correction</button>
              </form>

              <div class="fd-empty-state" *ngIf="!selectedMessage.correctionRequest && selectedMessage.senderId !== currentUserId">
                <div *ngIf="selectedMessage.translationRequest; else tutorNoAction">
                  <form class="fd-form-grid" (ngSubmit)="sendTranslationReply()">
                    <mat-form-field appearance="outline">
                      <mat-label>Reply to the translation request</mat-label>
                      <textarea matInput rows="2" [(ngModel)]="translationReplyText" name="translationReplyText"></textarea>
                    </mat-form-field>
                    <button mat-flat-button color="primary" type="submit">Send reply</button>
                  </form>
                </div>
                <ng-template #tutorNoAction>
                  This message does not need a formal correction.
                </ng-template>
              </div>

              <div class="fd-empty-state" *ngIf="selectedMessage.senderId === currentUserId && !corrections.length">
                You cannot correct your own message. Peer corrections will appear here once submitted.
              </div>

              <div class="fd-room-stack">
                <article class="fd-room-row" *ngFor="let correction of corrections">
                  <div>
                    <div class="fd-chip-row">
                      <strong>{{ correction.errorType }}</strong>
                      <span class="fd-pill">{{ correctionAuthorLabel(correction) }}</span>
                    </div>
                    <p>{{ correction.correctedText }}</p>
                    <small>{{ correction.explanation }}</small>
                  </div>
                  <div class="fd-room-meta" style="justify-content: space-between; align-items: center;">
                    <div style="display: flex; gap: 8px;">
                      <span class="fd-status-pill fd-status-pill--accepted" *ngIf="correction.accepted">Accepted</span>
                      <span *ngIf="correction.helpfulVotes > 0" style="color: var(--color-primary-600); font-size: 0.8rem; font-weight: 500;">👍 {{ correction.helpfulVotes }} Helpful</span>
                    </div>
                    <div>
                      <button mat-button type="button" *ngIf="canVoteCorrection(correction)" (click)="voteCorrection(correction.id)">Vote Helpful</button>
                      <button mat-button type="button" *ngIf="canAcceptCorrection(correction)" (click)="acceptCorrection(correction.id)">Seen</button>
                      <button mat-button type="button" color="warn" *ngIf="canAcceptCorrection(correction)" (click)="refuseCorrection(correction.id)">Refuse</button>
                      <button mat-button type="button" *ngIf="canDeleteCorrection(correction)" (click)="deleteCorrection(correction.id)">Delete</button>
                    </div>
                  </div>
                </article>
              </div>
            </div>

            <ng-template #correctionHint>
              <div class="fd-empty-state">Select a chat message first, then write or review corrections here.</div>
            </ng-template>
          </mat-card>

          <mat-card class="fd-surface-card">
            <div class="fd-section-head">
              <div>
                <h2>Challenge</h2>
                <p>{{ isTutor ? 'Create challenges from the Challenges page, then review answers here.' : 'Answer the active room challenge here.' }}</p>
              </div>
            </div>

            <div class="fd-room-stack" *ngIf="challenges.length; else noChallenge">
              <article class="fd-highlight-panel" *ngFor="let challenge of challenges">
                <div class="fd-section-inline">
                  <div>
                    <strong>{{ challenge.type }}</strong>
                    <p>{{ challenge.prompt }}</p>
                  </div>
                  <span class="fd-pill">{{ challenge.difficulty }}</span>
                </div>
                <div class="fd-chip-row" *ngIf="isTutor">
                  <button mat-button type="button" (click)="openChallengeSubmissions(challenge)">Review student answers</button>
                </div>
                <div class="fd-form-grid fd-form-grid--compact" *ngIf="!isTutor && challenge.status === 'OPEN'; else closedChallenge">
                  <mat-form-field appearance="outline">
                    <mat-label>Write your answer</mat-label>
                    <input matInput [(ngModel)]="challengeAnswers[challenge.id]" [name]="'answer-' + challenge.id">
                  </mat-form-field>
                  <button mat-flat-button color="primary" type="button" (click)="submitAnswer(challenge)">Send answer</button>
                </div>
                <ng-template #closedChallenge>
                  <p class="fd-muted-inline">
                    {{ isTutor ? 'Review student answers from here or from the Challenges page.' : 'This challenge is ' + challenge.status.toLowerCase() + '.' }}
                  </p>
                </ng-template>

                <div class="fd-room-stack" *ngIf="isTutor && selectedChallenge?.id === challenge.id">
                  <p class="fd-muted-inline" *ngIf="!selectedChallengeSubmissions.length">No student answer yet.</p>
                  <article class="fd-room-row" *ngFor="let submission of selectedChallengeSubmissions">
                    <div>
                      <div class="fd-chip-row">
                        <strong>{{ submissionLabel(submission) }}</strong>
                        <span class="fd-status-pill" [class.fd-status-pill--accepted]="submission.correct">
                          {{ submission.correct ? 'Correct' : 'Needs retry' }}
                        </span>
                      </div>
                      <p>{{ submission.answer }}</p>
                      <small>{{ submission.feedback }}</small>
                    </div>
                    <div class="fd-room-meta">
                      <span>{{ submission.pointsAwarded }} pts</span>
                    </div>
                  </article>
                </div>
              </article>
            </div>

            <ng-template #noChallenge>
              <div class="fd-empty-state">No challenge yet. The tutor can create one from the Challenges page.</div>
            </ng-template>

            <div class="fd-result-banner" *ngIf="answerFeedback" [class.fd-result-banner--success]="lastChallengeResult === 'correct'" [class.fd-result-banner--warning]="lastChallengeResult === 'retry'">
              <strong>{{ lastChallengeResult === 'correct' ? 'Challenge completed' : 'Keep trying' }}</strong>
              <p>{{ answerFeedback }}</p>
            </div>
          </mat-card>

          <mat-card class="fd-surface-card">
            <div class="fd-section-head">
              <div>
                <h2>People In The Room</h2>
                <p>See who is active in this room.</p>
              </div>
            </div>
            <div class="fd-room-stack">
              <article class="fd-room-row" *ngFor="let participant of participants">
                <div>
                  <strong>{{ participantLabel(participant) }}</strong>
                  <p>{{ participant.role }} | {{ participant.reputationScore }} rep</p>
                </div>
                <div class="fd-room-meta">
                  <span>{{ participant.messagesCount }} msgs</span>
                  <span>{{ participant.correctionsGiven }} fixes</span>
                  <button mat-button type="button" *ngIf="isTutor && participant.userId !== currentUserId" (click)="kick(participant.userId)">Remove</button>
                </div>
              </article>
              <div class="fd-empty-state" *ngIf="!participants.length">No participant found in this room yet.</div>
            </div>
          </mat-card>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .fd-page-hero {
      align-items: flex-start;
    }

    .fd-page-hero h1 {
      margin-bottom: 0.35rem;
    }

    .fd-page-hero .fd-chip-row {
      align-items: flex-start;
      justify-content: flex-end;
      min-width: 300px;
    }

    .fd-surface-card {
      padding: 1rem !important;
    }

    .fd-meeting-layout {
      display: grid;
      grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.75fr);
      gap: 1rem;
      align-items: start;
    }

    .fd-video-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 0.9rem;
    }

    .fd-video-tile {
      position: relative;
      overflow: hidden;
      min-height: 210px;
      border-radius: 16px;
      border: 1px solid rgba(212, 196, 184, 0.82);
      background: linear-gradient(180deg, #1f2937, #111827);
      box-shadow: 0 18px 36px rgba(15, 23, 42, 0.14);
    }

    .fd-video-tile video {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .fd-video-tile__label {
      position: absolute;
      left: 0.8rem;
      right: 0.8rem;
      bottom: 0.8rem;
      padding: 0.45rem 0.65rem;
      border-radius: 10px;
      background: rgba(15, 23, 42, 0.72);
      color: #f8fafc;
      font-size: 0.82rem;
      backdrop-filter: blur(8px);
    }

    .fd-meeting-roster {
      border: 1px solid rgba(212, 196, 184, 0.8);
      border-radius: 16px;
      background: linear-gradient(180deg, #fffdf9, #f8f0e8);
      padding: 1rem;
    }

    .fd-call-controls {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem;
      margin-top: 1rem;
    }

    .fd-call-controls button[mat-mini-fab] {
      width: auto;
      min-width: 68px;
      height: auto;
      padding: 0.7rem 0.8rem;
      border-radius: 14px;
      display: inline-flex;
      flex-direction: column;
      gap: 0.35rem;
      box-shadow: none;
      border: 1px solid rgba(212, 196, 184, 0.82);
      background: #fffdf9;
      color: #43515b;
    }

    .fd-call-controls .fd-call-control--active {
      background: #f8f0e8;
      color: #0f172a;
    }

    .fd-call-button__icon {
      width: 20px;
      height: 20px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto;
    }

    .fd-call-button__icon svg {
      width: 20px;
      height: 20px;
    }

    .fd-call-button__text {
      font-size: 0.73rem;
      font-weight: 700;
      line-height: 1;
    }

    .fd-chat-layout {
      display: grid;
      grid-template-columns: minmax(0, 1.3fr) minmax(300px, 0.9fr);
      gap: 1rem;
      align-items: start;
    }

    .fd-chat-shell {
      min-height: 760px;
      display: grid;
      grid-template-rows: auto 1fr auto;
      gap: 1rem;
    }

    .fd-chat-shell__header h2,
    .fd-section-head h2 {
      margin: 0;
    }

    .fd-chat-stream {
      display: grid;
      gap: 0.9rem;
      max-height: 920px;
      overflow: auto;
      padding-right: 0.2rem;
    }

    .fd-chat-bubble {
      padding: 0.95rem 1rem;
      border-radius: 16px;
      border: 1px solid rgba(212, 196, 184, 0.72);
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
    }

    .fd-chat-bubble--mine {
      background: linear-gradient(180deg, #fff7eb, #f8ebd9);
    }

    .fd-chat-bubble--teacher {
      border-color: rgba(182, 126, 36, 0.28);
      background: linear-gradient(180deg, #fffdf9, #f6ead5);
    }

    .fd-chat-bubble--selected {
      box-shadow: 0 0 0 2px rgba(183, 121, 31, 0.18);
    }

    .fd-chat-bubble__meta,
    .fd-chat-bubble__actions,
    .fd-section-inline,
    .fd-room-meta,
    .fd-chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem;
      align-items: center;
    }

    .fd-chat-bubble__meta {
      justify-content: space-between;
      margin-bottom: 0.45rem;
      color: #61717d;
      font-size: 0.8rem;
    }

    .fd-chat-bubble p,
    .fd-room-row p,
    .fd-highlight-panel p {
      margin: 0;
      line-height: 1.5;
      color: #334155;
    }

    .fd-chat-bubble__actions {
      margin-top: 0.7rem;
    }

    .fd-chat-composer {
      border-top: 1px solid rgba(212, 196, 184, 0.7);
      padding-top: 1rem;
      display: grid;
      gap: 0.85rem;
    }

    .fd-chat-composer__fields {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(160px, 220px);
      gap: 0.85rem;
    }

    .fd-chat-composer__actions {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .fd-chat-sidebar {
      display: grid;
      gap: 1rem;
    }

    .fd-room-stack {
      display: grid;
      gap: 0.85rem;
    }

    .fd-room-row,
    .fd-highlight-panel {
      padding: 0.95rem 1rem;
      border-radius: 14px;
      border: 1px solid rgba(212, 196, 184, 0.74);
      background: linear-gradient(180deg, #ffffff, #fcf8f2);
    }

    .fd-room-row small,
    .fd-muted-inline {
      color: #61717d;
      line-height: 1.45;
    }

    .fd-progress-inline {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
      align-items: center;
    }

    .fd-form-grid--compact {
      grid-template-columns: minmax(0, 1fr) auto;
      align-items: start;
    }

    @media (max-width: 1100px) {
      .fd-meeting-layout,
      .fd-chat-layout {
        grid-template-columns: 1fr;
      }

      .fd-chat-shell {
        min-height: 0;
      }
    }

    @media (max-width: 720px) {
      .fd-page-hero .fd-chip-row {
        min-width: 0;
        justify-content: flex-start;
      }

      .fd-chat-composer__fields,
      .fd-form-grid--compact {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class CollaborationRoomViewComponent implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly collaborationService = inject(CollaborationService);
  private readonly authService = inject(AuthService);
  private readonly userDirectoryService = inject(UserDirectoryService);
  private readonly roomCallService = inject(RoomCallService);
  private readonly notificationService = inject(AppNotificationService);
  private liveUpdatesSubscription?: Subscription;
  private remoteStreamsSubscription?: Subscription;
  private callStateSubscription?: Subscription;
  private raisedHandsSubscription?: Subscription;
  private debugStateSubscription?: Subscription;
  private readonly userNames = new Map<number, string>();
  private lastSeenMessageId = 0;

  roomId = 1;
  room: Room | null = null;
  participants: RoomParticipant[] = [];
  messages: RoomMessage[] = [];
  challenges: Challenge[] = [];
  selectedChallenge: Challenge | null = null;
  selectedChallengeSubmissions: ChallengeSubmission[] = [];
  corrections: PeerCorrection[] = [];
  selectedMessage: RoomMessage | null = null;
  feedback = '';
  answerFeedback = '';
  lastChallengeResult: 'correct' | 'retry' | null = null;
  activeTabIndex = 0;
  inCall = false;
  microphoneEnabled = true;
  cameraEnabled = true;
  callStateLabel = 'Call idle';
  localStream: MediaStream | null = null;
  remoteParticipants: Array<{ userId: number; stream: MediaStream }> = [];
  handRaised = false;
  screenSharing = false;
  raisedHands = new Set<number>();

  readonly currentUserId = this.authService.getCurrentUser()?.id ?? 1;
  readonly isTutor = this.authService.getUserRole() === 'TUTOR';
  readonly errorTypes: PeerCorrection['errorType'][] = ['GRAMMAR', 'VOCABULARY', 'PRONUNCIATION', 'SPELLING'];
  readonly challengeAnswers: Record<number, string> = {};

  readonly messageForm = {
    content: '',
    language: 'English',
    translationRequest: false,
    correctionRequest: false,
    mediaUrl: null as string | null
  };

  readonly correctionForm = {
    correctedText: '',
    explanation: '',
    errorType: 'GRAMMAR' as PeerCorrection['errorType']
  };
  translationReplyText = '';

  constructor() {
    this.roomId = Number(this.route.snapshot.paramMap.get('id')) || 1;
    this.loadRoom();
    this.listenToRoomUpdates();
    this.bindCallState();
  }

  loadRoom(): void {
    this.collaborationService.getRoom(this.roomId).subscribe(room => this.room = room);
    this.collaborationService.getRoomParticipants(this.roomId).subscribe(participants => {
      this.participants = participants;
      this.resolveUserNames(participants.map(participant => participant.userId));
    });
    this.collaborationService.getRoomMessages(this.roomId).subscribe(messages => {
      const newestMessageId = messages.at(-1)?.id ?? 0;
      if (this.messages.length && newestMessageId > this.lastSeenMessageId) {
        const newestMessage = messages.at(-1);
        if (newestMessage && newestMessage.senderId !== this.currentUserId) {
          this.playIncomingMessageSound();
        }
      }
      this.messages = messages;
      this.lastSeenMessageId = newestMessageId;
      this.resolveUserNames(messages.map(message => message.senderId));
    });
    this.collaborationService.getRoomChallenges(this.roomId).subscribe(challenges => this.challenges = challenges);
  }

  ngOnDestroy(): void {
    this.liveUpdatesSubscription?.unsubscribe();
    this.remoteStreamsSubscription?.unsubscribe();
    this.callStateSubscription?.unsubscribe();
    this.raisedHandsSubscription?.unsubscribe();
    this.debugStateSubscription?.unsubscribe();
    this.roomCallService.leaveCall();
  }

  joinRoom(): void {
    this.collaborationService.joinRoom(this.roomId).subscribe(() => {
      this.feedback = 'You joined the room. You can now chat, correct messages, or answer the challenge.';
      this.notify('success', 'Room joined', 'You can now practice in this room.');
    });
  }

  leaveRoom(): void {
    this.collaborationService.leaveRoom(this.roomId).subscribe(() => {
      this.feedback = 'You left the room.';
      this.notify('info', 'Room left', 'You left the practice room.');
    });
  }

  sendMessage(): void {
    this.collaborationService.joinRoom(this.roomId).subscribe({
      next: () => this.executeMessageSend(),
      error: () => this.executeMessageSend()
    });
  }

  private executeMessageSend(): void {
    this.collaborationService.sendMessage(this.roomId, this.messageForm).subscribe(() => {
      this.feedback = 'Message sent.';
      this.messageForm.content = '';
      this.messageForm.translationRequest = false;
      this.messageForm.correctionRequest = false;
      this.notify('info', 'Message sent', 'Your message is now visible in the room.');
      this.loadRoom();
    });
  }

  requestTranslation(message: RoomMessage): void {
    this.collaborationService.requestTranslation(this.roomId, message.id).subscribe(() => {
      this.feedback = `Translation requested for message #${message.id}.`;
      this.notify('info', 'Translation requested', 'Wait for the teacher reply, then confirm with Seen.');
      this.loadRoom();
    });
  }

  confirmTranslation(message: RoomMessage): void {
    this.collaborationService.confirmTranslation(this.roomId, message.id).subscribe(() => {
      this.feedback = 'Translation seen. Your listening progress was updated.';
      this.notify('success', 'Listening improved', 'You confirmed the translation help.');
      this.loadRoom();
    });
  }

  requestCorrection(message: RoomMessage): void {
    this.collaborationService.requestCorrection(this.roomId, message.id).subscribe(() => {
      this.feedback = `Teacher correction requested for message #${message.id}.`;
      this.notify('info', 'Correction requested', 'The teacher can now review your message.');
      this.loadRoom();
    });
  }

  deleteMessage(messageId: number): void {
    this.collaborationService.deleteMessage(this.roomId, messageId).subscribe(() => {
      this.feedback = `Message #${messageId} deleted.`;
      this.notify('warning', 'Message deleted', 'Your message was removed from the room.');
      this.loadRoom();
    });
  }

  openCorrections(message: RoomMessage, silent = false): void {
    this.selectedMessage = message;
    this.collaborationService.getRoomCorrections(message.id).subscribe(corrections => {
      this.corrections = [...corrections].sort((left, right) => Number(right.accepted) - Number(left.accepted));
    });
    this.activeTabIndex = 1;
    this.feedback = 'You are now in the correction area for this message.';
    if (!silent) {
      this.notify('info', 'Correction panel', 'Review the feedback for this message.');
    }
  }

  submitCorrection(): void {
    if (!this.selectedMessage) {
      return;
    }

    this.collaborationService.joinRoom(this.roomId).subscribe({
      next: () => this.executeCorrectionSubmit(),
      error: () => this.executeCorrectionSubmit()
    });
  }

  private executeCorrectionSubmit(): void {
    if (!this.selectedMessage) {
      return;
    }

    this.collaborationService.submitCorrection(this.selectedMessage.id, this.correctionForm).subscribe(() => {
      this.feedback = 'Correction submitted.';
      this.correctionForm.correctedText = '';
      this.correctionForm.explanation = '';
      this.notify('success', 'Correction sent', 'The student can now review your correction.');
      this.openCorrections(this.selectedMessage as RoomMessage, true);
    });
  }

  sendTranslationReply(): void {
    const content = this.translationReplyText.trim();
    if (!content) {
      this.notify('warning', 'Reply required', 'Write a translation reply first.');
      return;
    }

    this.collaborationService.sendMessage(this.roomId, {
      content,
      language: this.selectedMessage?.language ?? 'English',
      translationRequest: false,
      correctionRequest: false,
      mediaUrl: null
    }).subscribe(() => {
      this.translationReplyText = '';
      this.feedback = 'Translation reply sent.';
      this.notify('success', 'Translation reply sent', 'The student can now confirm the translation.');
      this.loadRoom();
    });
  }

  acceptCorrection(correctionId: number): void {
    this.collaborationService.acceptCorrection(correctionId).subscribe(() => {
      this.feedback = 'Correction seen. Your progress was updated.';
      this.notify('success', 'Grammar improved', 'Correction seen. Your grammar stat improved.');
      if (this.selectedMessage) {
        this.openCorrections(this.selectedMessage, true);
      }
    });
  }

  refuseCorrection(correctionId: number): void {
    this.collaborationService.refuseCorrection(correctionId).subscribe(() => {
      this.feedback = 'Correction refused. The corrector lost 1 reputation point.';
      this.notify('warning', 'Correction refused', 'This correction was rejected.');
      if (this.selectedMessage) {
        this.openCorrections(this.selectedMessage, true);
      }
    });
  }

  voteCorrection(correctionId: number): void {
    this.collaborationService.voteCorrection(correctionId).subscribe(() => {
      this.feedback = 'Helpful vote recorded.';
      this.notify('success', 'Reputation updated', 'The correction author gained reputation points.');
      if (this.selectedMessage) {
        this.openCorrections(this.selectedMessage, true);
      }
    });
  }

  deleteCorrection(correctionId: number): void {
    this.collaborationService.deleteCorrection(correctionId).subscribe(() => {
      this.feedback = 'Correction deleted.';
      this.notify('warning', 'Correction deleted', 'This correction was removed.');
      if (this.selectedMessage) {
        this.openCorrections(this.selectedMessage, true);
      }
    });
  }

  submitAnswer(challenge: Challenge): void {
    this.collaborationService.submitChallengeAnswer(challenge.id, {
      answer: this.challengeAnswers[challenge.id] ?? ''
    }).subscribe((response: ChallengeAnswerResponse) => {
      this.answerFeedback = response.feedback;
      this.lastChallengeResult = response.correct ? 'correct' : 'retry';
      this.notify(
        response.correct ? 'achievement' : 'info',
        response.correct ? 'Challenge completed' : 'Answer submitted',
        response.correct ? 'You earned challenge points and reputation.' : 'Your answer was sent. Try again if needed.'
      );
      if (this.isTutor && this.selectedChallenge?.id === challenge.id) {
        this.openChallengeSubmissions(challenge);
      }
    });
  }

  openChallengeSubmissions(challenge: Challenge, silent = false): void {
    this.selectedChallenge = challenge;
    this.collaborationService.getChallengeSubmissions(challenge.id).subscribe(submissions => {
      this.selectedChallengeSubmissions = submissions;
      this.resolveUserNames(submissions.map(submission => submission.userId));
      if (!silent) {
        this.notify('info', 'Answers loaded', 'Student challenge submissions are ready to review.');
      }
    });
  }

  kick(userId: number): void {
    this.collaborationService.kickParticipant(this.roomId, userId).subscribe(() => {
      this.feedback = `User #${userId} removed from room.`;
      this.notify('warning', 'Participant removed', 'The participant was removed from this room.');
      this.loadRoom();
    });
  }

  isOwnMessage(message: RoomMessage): boolean {
    return message.senderId === this.currentUserId;
  }

  senderLabel(message: RoomMessage): string {
    const participant = this.participants.find(item => item.userId === message.senderId);
    if (message.senderId === this.currentUserId) {
      return this.isTutor ? 'You (Teacher)' : 'You';
    }
    if (participant?.role === 'HOST') {
      return this.userNames.get(message.senderId) ?? 'Teacher';
    }
    return this.userNames.get(message.senderId) ?? 'Student';
  }

  isTeacherMessage(message: RoomMessage): boolean {
    const participant = this.participants.find(item => item.userId === message.senderId);
    return participant?.role === 'HOST';
  }

  participantLabel(participant: RoomParticipant): string {
    if (participant.userId === this.currentUserId) {
      return this.isTutor ? 'You (Teacher)' : 'You';
    }
    if (participant.role === 'HOST') {
      return this.userNames.get(participant.userId) ?? 'Teacher';
    }
    return this.userNames.get(participant.userId) ?? 'Student';
  }

  correctionAuthorLabel(correction: PeerCorrection): string {
    if (correction.correctorId === this.currentUserId) {
      return this.isTutor ? 'By you (Tutor)' : 'By you';
    }

    const participant = this.participants.find(item => item.userId === correction.correctorId);
    if (participant?.role === 'HOST') {
      return `By ${this.userNames.get(correction.correctorId) ?? 'Teacher'}`;
    }
    return `By ${this.userNames.get(correction.correctorId) ?? 'Student'}`;
  }

  canAcceptCorrection(correction: PeerCorrection): boolean {
    return !this.isTutor && this.selectedMessage?.senderId === this.currentUserId && !correction.accepted;
  }

  canConfirmTranslation(message: RoomMessage): boolean {
    return this.isOwnMessage(message) && message.translationRequest && this.hasTeacherReplyAfter(message);
  }

  canVoteCorrection(correction: PeerCorrection): boolean {
    return correction.correctorId !== this.currentUserId;
  }

  canDeleteCorrection(correction: PeerCorrection): boolean {
    return correction.correctorId === this.currentUserId;
  }

  get messagesNeedingHelp(): RoomMessage[] {
    return this.messages.filter(message =>
      message.senderId !== this.currentUserId && (message.translationRequest || message.correctionRequest)
    );
  }

  focusMessage(message: RoomMessage): void {
    this.selectedMessage = message;
    this.activeTabIndex = 0;
    this.feedback = `Student message #${message.id} is ready for review in chat.`;
  }

  private hasTeacherReplyAfter(message: RoomMessage): boolean {
    const messageTime = new Date(message.sentAt).getTime();
    return this.messages.some(candidate =>
      candidate.senderId !== this.currentUserId &&
      this.isTeacherMessage(candidate) &&
      new Date(candidate.sentAt).getTime() > messageTime
    );
  }

  submissionLabel(submission: ChallengeSubmission): string {
    return submission.userId === this.currentUserId ? 'You' : (this.userNames.get(submission.userId) ?? 'Student');
  }

  get supportsVideoCall(): boolean {
    return this.room?.type === 'VOICE' || this.room?.type === 'MIXED';
  }

  get supportsStudyChat(): boolean {
    return this.room?.type !== 'VOICE';
  }

  get isVoiceOnlyRoom(): boolean {
    return this.room?.type === 'VOICE';
  }

  participantName(userId: number): string {
    return this.userNames.get(userId) ?? `Participant ${userId}`;
  }

  get callParticipantCount(): number {
    const remoteIds = new Set(this.remoteParticipants.map(participant => participant.userId));
    return this.inCall ? remoteIds.size + 1 : 0;
  }

  get roomMemberCount(): number {
    return new Set(this.participants.map(participant => participant.userId)).size;
  }

  isHandRaised(userId: number): boolean {
    return this.raisedHands.has(userId);
  }

  isParticipantInCall(userId: number): boolean {
    return userId === this.currentUserId ? this.inCall : this.remoteParticipants.some(participant => participant.userId === userId);
  }

  formatTime(value: string): string {
    return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  callControlIcon(kind: 'mic-on' | 'mic-off' | 'cam-on' | 'cam-off' | 'share-start' | 'share-stop' | 'hand-up' | 'hand-down' | 'leave'): string {
    if (kind === 'mic-on') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><path d="M12 19v3"/><path d="M8 22h8"/></svg>';
    }
    if (kind === 'mic-off') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4l16 16"/><path d="M9 9v3a3 3 0 0 0 5.12 2.12"/><path d="M12 3a3 3 0 0 1 3 3v3"/><path d="M19 10v2a7 7 0 0 1-11.36 5.46"/><path d="M12 19v3"/><path d="M8 22h8"/></svg>';
    }
    if (kind === 'cam-on') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="6" width="13" height="12" rx="2"/><path d="M16 10l5-3v10l-5-3z"/></svg>';
    }
    if (kind === 'cam-off') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4l16 16"/><path d="M10.6 6H5a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h11a2 2 0 0 0 1.4-.6"/><path d="M16 10l5-3v10l-5-3z"/></svg>';
    }
    if (kind === 'share-start') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="12" rx="2"/><path d="M8 20h8"/><path d="M12 16v4"/><path d="M12 8V6"/><path d="M9.5 8.5L12 6l2.5 2.5"/></svg>';
    }
    if (kind === 'share-stop') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="12" rx="2"/><path d="M8 20h8"/><path d="M12 16v4"/><path d="M4 4l16 16"/></svg>';
    }
    if (kind === 'hand-up') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 11V5a1 1 0 0 1 2 0v6"/><path d="M11 11V4a1 1 0 0 1 2 0v7"/><path d="M15 11V6a1 1 0 0 1 2 0v7"/><path d="M19 12a2 2 0 0 1 2 2v1a7 7 0 0 1-14 0V9a1 1 0 0 1 2 0v2"/></svg>';
    }
    if (kind === 'hand-down') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 11V5a1 1 0 0 1 2 0v6"/><path d="M11 11V4a1 1 0 0 1 2 0v7"/><path d="M15 11V6a1 1 0 0 1 2 0v7"/><path d="M19 12a2 2 0 0 1 2 2v1a7 7 0 0 1-14 0V9a1 1 0 0 1 2 0v2"/><path d="M12 3v4"/><path d="M9.5 4.5L12 7l2.5-2.5"/></svg>';
    }
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5l-7 7 7 7"/><path d="M2 12h9a8 8 0 0 0 8-8"/></svg>';
  }

  private listenToRoomUpdates(): void {
    this.liveUpdatesSubscription = this.collaborationService.subscribeToRoomEvents(this.roomId).subscribe({
      next: eventType => {
        if (eventType === 'connected') {
          return;
        }
        this.notifyLiveEvent(eventType);
        this.loadRoom();
        if (this.selectedMessage) {
          this.openCorrections(this.selectedMessage, true);
        }
        if (this.selectedChallenge) {
          this.openChallengeSubmissions(this.selectedChallenge, true);
        }
      },
      error: () => {
        this.notify('warning', 'Live updates paused', 'Refresh the room if updates stop appearing.');
      }
    });
  }

  private resolveUserNames(userIds: number[]): void {
    const uniqueIds = [...new Set(userIds.filter(id => !!id))].filter(id => !this.userNames.has(id) && id !== this.currentUserId);
    if (!uniqueIds.length) {
      return;
    }

    const lookups = uniqueIds.map(userId => this.userDirectoryService.getUserById(userId));
    forkJoin(lookups).subscribe(users => {
      users.forEach(user => {
        if (!user) {
          return;
        }
        const fullName = `${user.firstName} ${user.lastName}`.trim();
        this.userNames.set(user.id, fullName || user.email);
      });
    });
  }

  private playIncomingMessageSound(): void {
    try {
      const AudioContextCtor = window.AudioContext || (window as typeof window & { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
      if (!AudioContextCtor) {
        return;
      }

      const context = new AudioContextCtor();
      const oscillator = context.createOscillator();
      const gain = context.createGain();
      oscillator.type = 'sine';
      oscillator.frequency.setValueAtTime(720, context.currentTime);
      gain.gain.setValueAtTime(0.0001, context.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.05, context.currentTime + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, context.currentTime + 0.18);
      oscillator.connect(gain);
      gain.connect(context.destination);
      oscillator.start();
      oscillator.stop(context.currentTime + 0.18);
      oscillator.onended = () => {
        void context.close();
      };
    } catch {
      // Ignore audio failures and keep the room usable.
    }
  }

  async joinCall(): Promise<void> {
    const token = this.authService.getToken();
    if (!token) {
      this.notify('warning', 'Login required', 'You must be logged in to join the call.');
      return;
    }

    try {
      await firstValueFrom(this.collaborationService.joinRoom(this.roomId)).catch(() => undefined);
      this.localStream = await this.roomCallService.joinCall(this.roomId, this.currentUserId, token);
      this.inCall = true;
      this.attachLocalStream();
      this.remoteStreamsSubscription?.unsubscribe();
      this.remoteStreamsSubscription = this.roomCallService.remoteStreams$.subscribe(remoteParticipants => {
        this.remoteParticipants = remoteParticipants;
        this.resolveUserNames(remoteParticipants.map(participant => participant.userId));
        queueMicrotask(() => this.attachRemoteStreams());
      });
      this.bindRaisedHands();
      this.notify('success', 'Call joined', 'You are now live in the meeting.');
    } catch {
      this.notify('warning', 'Camera or microphone blocked', 'Allow device access to join the call.');
    }
  }

  leaveCall(): void {
    this.roomCallService.leaveCall();
    this.remoteStreamsSubscription?.unsubscribe();
    this.remoteParticipants = [];
    this.localStream = null;
    this.inCall = false;
    this.microphoneEnabled = true;
    this.cameraEnabled = true;
    this.handRaised = false;
    this.screenSharing = false;
    this.raisedHands = new Set();
    this.playLeaveCallSound();
    this.notify('info', 'Call left', 'You left the meeting.');
  }

  toggleMicrophone(): void {
    this.microphoneEnabled = !this.microphoneEnabled;
    this.roomCallService.toggleAudio(this.microphoneEnabled);
  }

  toggleCamera(): void {
    this.cameraEnabled = !this.cameraEnabled;
    this.roomCallService.toggleVideo(this.cameraEnabled);
  }

  async toggleScreenShare(): Promise<void> {
    try {
      if (this.screenSharing) {
        this.roomCallService.stopScreenShare();
        this.screenSharing = false;
      } else {
        await this.roomCallService.startScreenShare();
        this.screenSharing = true;
      }
      this.attachLocalStream();
    } catch {
      this.notify('warning', 'Screen share failed', 'Screen sharing could not start.');
    }
  }

  private notify(kind: 'info' | 'success' | 'warning' | 'achievement', title: string, message: string): void {
    if (kind === 'achievement') {
      this.notificationService.achievement(title, message);
      return;
    }
    if (kind === 'success') {
      this.notificationService.success(title, message);
      return;
    }
    if (kind === 'warning') {
      this.notificationService.warning(title, message);
      return;
    }
    this.notificationService.info(title, message);
  }

  private notifyLiveEvent(eventType: string): void {
    if (eventType === 'correction-submitted' && !this.isTutor) {
      this.notificationService.success('Message corrected', 'A teacher or peer corrected a message in this room.');
    } else if (eventType === 'correction-accepted' && !this.isTutor) {
      this.notificationService.success('Progress updated', 'A correction was seen and a learner stat improved.');
    } else if (eventType === 'message-sent' && this.isTutor) {
      this.notificationService.info('New student activity', 'A new message arrived in the room.');
    } else if (eventType === 'translation-seen' && this.isTutor) {
      this.notificationService.success('Translation confirmed', 'A student reviewed your translation help and improved listening.');
    }
  }

  toggleRaiseHand(): void {
    this.handRaised = this.roomCallService.toggleHandRaise();
  }

  private bindCallState(): void {
    this.callStateSubscription = this.roomCallService.callState$.subscribe(state => {
      this.callStateLabel = state === 'idle' ? 'Call idle' : state === 'joining' ? 'Joining call...' : 'Call connected';
      this.inCall = state !== 'idle';
    });
  }

  private attachLocalStream(): void {
    this.attachVideoElement('local-video', this.roomCallService.getPreviewStream(), true);
  }

  private attachRemoteStreams(): void {
    this.remoteParticipants.forEach(participant => {
      this.attachVideoElement(`remote-video-${participant.userId}`, participant.stream, false);
    });
  }

  private bindRaisedHands(): void {
    this.raisedHandsSubscription?.unsubscribe();
    this.raisedHandsSubscription = this.roomCallService.raisedHands$.subscribe(raisedHands => {
      this.raisedHands = new Set(raisedHands);
    });
  }

  formatDebugTime(value: string): string {
    return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  private attachVideoElement(elementId: string, stream: MediaStream | null, muted: boolean, attempt = 0): void {
    if (!stream) {
      return;
    }

    const videoElement = document.getElementById(elementId) as HTMLVideoElement | null;
    if (!videoElement) {
      if (attempt < 12) {
        setTimeout(() => this.attachVideoElement(elementId, stream, muted, attempt + 1), 120);
      }
      return;
    }

    videoElement.autoplay = true;
    videoElement.playsInline = true;
    videoElement.muted = muted;

    if (videoElement.srcObject !== stream) {
      videoElement.srcObject = stream;
    }

    videoElement.onloadedmetadata = () => {
      void videoElement.play().catch(() => undefined);
    };

    void videoElement.play().catch(() => {
      if (attempt < 12) {
        setTimeout(() => this.attachVideoElement(elementId, stream, muted, attempt + 1), 120);
      }
    });
  }

  private playLeaveCallSound(): void {
    try {
      const AudioContextCtor = window.AudioContext || (window as typeof window & { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
      if (!AudioContextCtor) {
        return;
      }

      const context = new AudioContextCtor();
      const oscillator = context.createOscillator();
      const gain = context.createGain();
      oscillator.type = 'triangle';
      oscillator.frequency.setValueAtTime(420, context.currentTime);
      oscillator.frequency.exponentialRampToValueAtTime(240, context.currentTime + 0.22);
      gain.gain.setValueAtTime(0.0001, context.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.045, context.currentTime + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, context.currentTime + 0.24);
      oscillator.connect(gain);
      gain.connect(context.destination);
      oscillator.start();
      oscillator.stop(context.currentTime + 0.24);
      oscillator.onended = () => {
        void context.close();
      };
    } catch {
      // Ignore audio failures and keep the room usable.
    }
  }
}
