package exp.collaborationroommicroservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CollaborationRoomMicroserviceApplicationTests {

    @Test
    void applicationClassIsLoadable() {
        assertDoesNotThrow(() -> CollaborationRoomMicroserviceApplication.class.getDeclaredConstructor());
    }
}
