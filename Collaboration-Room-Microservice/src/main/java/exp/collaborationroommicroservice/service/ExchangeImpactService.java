package exp.collaborationroommicroservice.service;

public interface ExchangeImpactService {

    void onTranslationRequested(Long roomId, Long userId);

    void onCorrectionApproved(Long roomId, Long learnerUserId);

    void onCallJoined(Long roomId, Long userId);

    void onCallLeft(Long roomId, Long userId);
}
