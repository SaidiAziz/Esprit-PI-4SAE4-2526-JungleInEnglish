package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoomStatusRequest {

    @NotNull
    private RoomStatus status;
}
