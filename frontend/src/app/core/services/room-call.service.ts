import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

type SignalType = 'offer' | 'answer' | 'ice-candidate';

interface SignalMessage {
  type: string;
  roomId?: number;
  senderUserId?: number;
  targetUserId?: number;
  participantIds?: number[];
  payload?: Record<string, unknown>;
}

export interface RoomCallPeerDebugState {
  userId: number;
  connectionState: RTCPeerConnectionState | 'new';
  iceConnectionState: RTCIceConnectionState | 'new';
  iceGatheringState: RTCIceGatheringState | 'new';
  signalingState: RTCSignalingState | 'stable';
  hasRemoteStream: boolean;
}

export interface RoomCallDebugEntry {
  timestamp: string;
  level: 'info' | 'warn' | 'error';
  message: string;
}

export interface RoomCallDebugState {
  socketState: 'closed' | 'connecting' | 'open';
  joinedCall: boolean;
  lastError: string | null;
  peers: RoomCallPeerDebugState[];
  events: RoomCallDebugEntry[];
}

@Injectable({ providedIn: 'root' })
export class RoomCallService {
  private readonly peerConnections = new Map<number, RTCPeerConnection>();
  private readonly remoteStreams = new Map<number, MediaStream>();
  private readonly remoteStreamsSubject = new BehaviorSubject<Array<{ userId: number; stream: MediaStream }>>([]);
  private readonly callStateSubject = new BehaviorSubject<'idle' | 'joining' | 'connected'>('idle');
  private readonly raisedHandsSubject = new BehaviorSubject<Set<number>>(new Set());
  private readonly debugStateSubject = new BehaviorSubject<RoomCallDebugState>({
    socketState: 'closed',
    joinedCall: false,
    lastError: null,
    peers: [],
    events: []
  });
  private socket: WebSocket | null = null;
  private localStream: MediaStream | null = null;
  private screenStream: MediaStream | null = null;
  private roomId: number | null = null;
  private userId: number | null = null;
  private queuedSignals: string[] = [];
  private handRaised = false;

  readonly remoteStreams$ = this.remoteStreamsSubject.asObservable();
  readonly callState$ = this.callStateSubject.asObservable();
  readonly raisedHands$ = this.raisedHandsSubject.asObservable();
  readonly debugState$ = this.debugStateSubject.asObservable();

  getPreviewStream(): MediaStream | null {
    return this.screenStream ?? this.localStream;
  }

  async joinCall(roomId: number, userId: number, token: string): Promise<MediaStream> {
    this.roomId = roomId;
    this.userId = userId;
    this.callStateSubject.next('joining');
    this.logDebug('info', `Preparing local media for room ${roomId}.`);
    this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    this.logDebug(
      'info',
      `Local media ready (${this.localStream.getAudioTracks().length} audio, ${this.localStream.getVideoTracks().length} video).`
    );
    this.openSocket(token);
    return this.localStream;
  }

  leaveCall(): void {
    this.logDebug('info', 'Leaving call.');
    this.safeSocketSend(JSON.stringify({ type: 'leave-call' }));
    this.socket?.close();
    this.socket = null;
    this.peerConnections.forEach(connection => connection.close());
    this.peerConnections.clear();
    this.remoteStreams.clear();
    this.remoteStreamsSubject.next([]);
    this.raisedHandsSubject.next(new Set());
    this.localStream?.getTracks().forEach(track => track.stop());
    this.screenStream?.getTracks().forEach(track => track.stop());
    this.localStream = null;
    this.screenStream = null;
    this.callStateSubject.next('idle');
    this.roomId = null;
    this.userId = null;
    this.handRaised = false;
    this.queuedSignals = [];
    this.debugStateSubject.next({
      socketState: 'closed',
      joinedCall: false,
      lastError: null,
      peers: [],
      events: []
    });
  }

  toggleAudio(enabled: boolean): void {
    this.localStream?.getAudioTracks().forEach(track => track.enabled = enabled);
  }

  toggleVideo(enabled: boolean): void {
    this.localStream?.getVideoTracks().forEach(track => track.enabled = enabled);
  }

  async startScreenShare(): Promise<void> {
    if (!navigator.mediaDevices?.getDisplayMedia) {
      throw new Error('Screen sharing is not supported in this browser');
    }

    this.screenStream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: false });
    const sharedTrack = this.screenStream.getVideoTracks()[0];
    this.replaceOutgoingVideoTrack(sharedTrack);
    sharedTrack.onended = () => this.stopScreenShare();
  }

  stopScreenShare(): void {
    const cameraTrack = this.localStream?.getVideoTracks()[0];
    if (cameraTrack) {
      this.replaceOutgoingVideoTrack(cameraTrack);
    }
    this.screenStream?.getTracks().forEach(track => track.stop());
    this.screenStream = null;
  }

  isScreenSharing(): boolean {
    return !!this.screenStream;
  }

  toggleHandRaise(): boolean {
    this.handRaised = !this.handRaised;
    const nextRaisedHands = new Set(this.raisedHandsSubject.value);
    if (this.userId != null) {
      if (this.handRaised) {
        nextRaisedHands.add(this.userId);
      } else {
        nextRaisedHands.delete(this.userId);
      }
      this.raisedHandsSubject.next(nextRaisedHands);
    }
    this.safeSocketSend(JSON.stringify({
      type: 'hand-state',
      roomId: this.roomId,
      payload: { raised: this.handRaised }
    }));
    this.logDebug('info', this.handRaised ? 'Hand raised.' : 'Hand lowered.');
    return this.handRaised;
  }

  private openSocket(token: string): void {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const socketUrl = `${protocol}//${window.location.host}/api/collaboration/ws/call?token=${encodeURIComponent(token)}`;
    this.socket = new WebSocket(socketUrl);
    this.patchDebugState({ socketState: 'connecting', lastError: null });
    this.logDebug('info', `Opening signaling socket for room ${this.roomId}.`);

    this.socket.onopen = () => {
      this.patchDebugState({ socketState: 'open' });
      this.logDebug('info', 'Signaling socket connected.');
      this.flushQueuedSignals();
      this.safeSocketSend(JSON.stringify({ type: 'join-call', roomId: this.roomId }));
    };

    this.socket.onmessage = async event => {
      const message = JSON.parse(event.data) as SignalMessage;
      await this.handleSignal(message);
    };

    this.socket.onerror = () => {
      this.patchDebugState({ lastError: 'Signaling socket error.' });
      this.logDebug('error', 'Signaling socket error.');
    };

    this.socket.onclose = () => {
      this.callStateSubject.next('idle');
      this.patchDebugState({ socketState: 'closed', joinedCall: false });
      this.logDebug('warn', 'Signaling socket closed.');
    };
  }

  private async handleSignal(message: SignalMessage): Promise<void> {
    this.logDebug('info', `Signal received: ${message.type}${message.senderUserId ? ` from ${message.senderUserId}` : ''}.`);
    switch (message.type) {
      case 'joined-call':
        this.callStateSubject.next('connected');
        this.patchDebugState({ joinedCall: true });
        this.logDebug('info', `Joined call. Existing participants: ${(message.participantIds ?? []).join(', ') || 'none'}.`);
        for (const participantId of message.participantIds ?? []) {
          await this.createOffer(participantId);
        }
        break;
      case 'participant-joined':
        if (message.senderUserId) {
          this.logDebug('info', `Participant ${message.senderUserId} joined the call.`);
        }
        break;
      case 'participant-left':
        if (message.senderUserId) {
          this.logDebug('warn', `Participant ${message.senderUserId} left the call.`);
          this.removeParticipant(message.senderUserId);
        }
        break;
      case 'hand-state':
        if (message.senderUserId) {
          this.updateRaisedHand(message.senderUserId, Boolean(message.payload?.['raised']));
        }
        break;
      case 'offer':
        if (message.senderUserId && message.payload?.['sdp']) {
          await this.handleOffer(message.senderUserId, String(message.payload['sdp']));
        }
        break;
      case 'answer':
        if (message.senderUserId && message.payload?.['sdp']) {
          await this.handleAnswer(message.senderUserId, String(message.payload['sdp']));
        }
        break;
      case 'ice-candidate':
        if (message.senderUserId && message.payload?.['candidate']) {
          await this.handleCandidate(message.senderUserId, message.payload['candidate'] as RTCIceCandidateInit);
        }
        break;
      case 'error':
        this.patchDebugState({ lastError: String(message.payload?.['message'] ?? 'Unknown signaling error') });
        this.logDebug('error', `Server signaling error: ${String(message.payload?.['message'] ?? 'Unknown error')}.`);
        break;
      default:
        break;
    }
  }

  private async createOffer(targetUserId: number): Promise<void> {
    const connection = this.ensurePeerConnection(targetUserId);
    this.logDebug('info', `Creating offer for participant ${targetUserId}.`);
    const offer = await connection.createOffer();
    await connection.setLocalDescription(offer);
    this.updatePeerDebug(targetUserId, connection);
    this.sendSignal('offer', targetUserId, { sdp: offer.sdp ?? '' });
  }

  private async handleOffer(senderUserId: number, sdp: string): Promise<void> {
    const connection = this.ensurePeerConnection(senderUserId);
    this.logDebug('info', `Applying remote offer from participant ${senderUserId}.`);
    await connection.setRemoteDescription({ type: 'offer', sdp });
    const answer = await connection.createAnswer();
    await connection.setLocalDescription(answer);
    this.updatePeerDebug(senderUserId, connection);
    this.sendSignal('answer', senderUserId, { sdp: answer.sdp ?? '' });
  }

  private async handleAnswer(senderUserId: number, sdp: string): Promise<void> {
    const connection = this.ensurePeerConnection(senderUserId);
    this.logDebug('info', `Applying remote answer from participant ${senderUserId}.`);
    await connection.setRemoteDescription({ type: 'answer', sdp });
    this.updatePeerDebug(senderUserId, connection);
  }

  private async handleCandidate(senderUserId: number, candidate: RTCIceCandidateInit): Promise<void> {
    const connection = this.ensurePeerConnection(senderUserId);
    try {
      await connection.addIceCandidate(candidate);
      this.logDebug('info', `ICE candidate added for participant ${senderUserId}.`);
      this.updatePeerDebug(senderUserId, connection);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown ICE error';
      this.patchDebugState({ lastError: message });
      this.logDebug('error', `Failed to add ICE candidate for participant ${senderUserId}: ${message}.`);
    }
  }

  private ensurePeerConnection(remoteUserId: number): RTCPeerConnection {
    if (this.peerConnections.has(remoteUserId)) {
      return this.peerConnections.get(remoteUserId)!;
    }

    const connection = new RTCPeerConnection({
      iceServers: [
        { urls: 'stun:stun.l.google.com:19302' }
      ]
    });

    this.localStream?.getTracks().forEach(track => {
      connection.addTrack(track, this.localStream as MediaStream);
    });
    this.logDebug('info', `Peer connection created for participant ${remoteUserId}.`);
    this.updatePeerDebug(remoteUserId, connection);

    connection.onicecandidate = event => {
      if (event.candidate) {
        this.logDebug('info', `Local ICE candidate generated for participant ${remoteUserId}.`);
        this.sendSignal('ice-candidate', remoteUserId, { candidate: event.candidate.toJSON() });
      }
    };

    connection.ontrack = event => {
      const [stream] = event.streams;
      if (!stream) {
        return;
      }
      this.remoteStreams.set(remoteUserId, stream);
      this.logDebug('info', `Remote media track received from participant ${remoteUserId}.`);
      this.updatePeerDebug(remoteUserId, connection, true);
      this.publishRemoteStreams();
    };

    connection.onconnectionstatechange = () => {
      this.logDebug('info', `Peer ${remoteUserId} connection state: ${connection.connectionState}.`);
      this.updatePeerDebug(remoteUserId, connection);
      if (['failed', 'disconnected', 'closed'].includes(connection.connectionState)) {
        this.removeParticipant(remoteUserId);
      }
    };

    connection.oniceconnectionstatechange = () => {
      this.logDebug('info', `Peer ${remoteUserId} ICE connection: ${connection.iceConnectionState}.`);
      this.updatePeerDebug(remoteUserId, connection);
    };

    connection.onicegatheringstatechange = () => {
      this.logDebug('info', `Peer ${remoteUserId} ICE gathering: ${connection.iceGatheringState}.`);
      this.updatePeerDebug(remoteUserId, connection);
    };

    connection.onsignalingstatechange = () => {
      this.logDebug('info', `Peer ${remoteUserId} signaling state: ${connection.signalingState}.`);
      this.updatePeerDebug(remoteUserId, connection);
    };

    this.peerConnections.set(remoteUserId, connection);
    return connection;
  }

  private sendSignal(type: SignalType, targetUserId: number, payload: Record<string, unknown>): void {
    this.logDebug('info', `Sending ${type} to participant ${targetUserId}.`);
    this.safeSocketSend(JSON.stringify({
      type,
      roomId: this.roomId,
      targetUserId,
      payload
    }));
  }

  private removeParticipant(userId: number): void {
    this.peerConnections.get(userId)?.close();
    this.peerConnections.delete(userId);
    this.remoteStreams.delete(userId);
    this.removePeerDebug(userId);
    this.publishRemoteStreams();
  }

  private publishRemoteStreams(): void {
    this.remoteStreamsSubject.next(
      [...this.remoteStreams.entries()].map(([userId, stream]) => ({ userId, stream }))
    );
  }

  private replaceOutgoingVideoTrack(nextTrack: MediaStreamTrack): void {
    this.peerConnections.forEach(connection => {
      const sender = connection.getSenders().find(item => item.track?.kind === 'video');
      if (sender) {
        void sender.replaceTrack(nextTrack);
      }
    });
    this.logDebug('info', `Outgoing video track replaced with ${nextTrack.label || nextTrack.kind}.`);
  }

  private updateRaisedHand(userId: number, raised: boolean): void {
    const nextRaisedHands = new Set(this.raisedHandsSubject.value);
    if (raised) {
      nextRaisedHands.add(userId);
    } else {
      nextRaisedHands.delete(userId);
    }
    this.raisedHandsSubject.next(nextRaisedHands);
  }

  private safeSocketSend(payload: string): void {
    if (!this.socket) {
      this.logDebug('warn', 'Tried to send a signaling message without a socket.');
      return;
    }
    if (this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(payload);
      return;
    }
    if (this.socket.readyState === WebSocket.CONNECTING) {
      this.queuedSignals.push(payload);
      this.logDebug('warn', 'Queued signaling message while socket is still connecting.');
      return;
    }
    this.logDebug('warn', 'Dropped signaling message because the socket is closed.');
  }

  private flushQueuedSignals(): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
    if (this.queuedSignals.length) {
      this.logDebug('info', `Flushing ${this.queuedSignals.length} queued signaling message(s).`);
    }
    while (this.queuedSignals.length) {
      const payload = this.queuedSignals.shift();
      if (payload) {
        this.socket.send(payload);
      }
    }
  }

  private updatePeerDebug(remoteUserId: number, connection: RTCPeerConnection, hasRemoteStream?: boolean): void {
    const current = this.debugStateSubject.value;
    const nextPeers = [...current.peers];
    const index = nextPeers.findIndex(peer => peer.userId === remoteUserId);
    const previous = index >= 0 ? nextPeers[index] : undefined;
    const nextPeer: RoomCallPeerDebugState = {
      userId: remoteUserId,
      connectionState: connection.connectionState || previous?.connectionState || 'new',
      iceConnectionState: connection.iceConnectionState || previous?.iceConnectionState || 'new',
      iceGatheringState: connection.iceGatheringState || previous?.iceGatheringState || 'new',
      signalingState: connection.signalingState || previous?.signalingState || 'stable',
      hasRemoteStream: hasRemoteStream ?? previous?.hasRemoteStream ?? this.remoteStreams.has(remoteUserId)
    };

    if (index >= 0) {
      nextPeers[index] = nextPeer;
    } else {
      nextPeers.push(nextPeer);
    }

    this.patchDebugState({ peers: nextPeers });
  }

  private removePeerDebug(remoteUserId: number): void {
    this.patchDebugState({
      peers: this.debugStateSubject.value.peers.filter(peer => peer.userId !== remoteUserId)
    });
  }

  private patchDebugState(patch: Partial<RoomCallDebugState>): void {
    this.debugStateSubject.next({
      ...this.debugStateSubject.value,
      ...patch
    });
  }

  private logDebug(level: 'info' | 'warn' | 'error', message: string): void {
    const current = this.debugStateSubject.value;
    const nextEvents = [
      {
        timestamp: new Date().toISOString(),
        level,
        message
      },
      ...current.events
    ].slice(0, 40);

    this.debugStateSubject.next({
      ...current,
      events: nextEvents,
      lastError: level === 'error' ? message : current.lastError
    });
  }
}
