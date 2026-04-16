package exp.collaborationroommicroservice.websocket;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RoomSignalMessage {

    private String type;
    private Long roomId;
    private Long senderUserId;
    private Long targetUserId;
    private List<Long> participantIds;
    private Map<String, Object> payload;
}
