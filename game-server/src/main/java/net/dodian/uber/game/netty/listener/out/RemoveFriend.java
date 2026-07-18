package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

/**
 * The Tarnish client removes the entry locally when it emits its remove-friend
 * request, so no server-to-client packet is required.
 */
public class RemoveFriend implements OutgoingPacket {

    public RemoveFriend(long name) {
        // Kept as a source-compatible command; Tarnish removes the friend locally.
    }

    @Override
    public void send(Client client) {
        // Intentionally empty: opcode 51 is not a Tarnish server packet.
    }
}
