package com.codecool.tarokkgame.model.dto.restdto;

/**
 * Player feedback (bug report / suggestion). Not persisted – it is forwarded
 * straight to the maintainer's inbox.
 *
 * @param message  the player's message (required)
 * @param userAgent the browser / device string sent by the frontend (optional, helps debugging)
 */
public record FeedbackDTO(String message, String userAgent) {
}
