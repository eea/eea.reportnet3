package org.eea.communication.configuration;

import org.eea.security.jwt.utils.JwtTokenProvider;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The Class CustomChannelInterceptor.
 */
@Component
public class WebsocketChannelInterceptor implements ChannelInterceptor {

  /**
   * The client outbound channel.
   */
  @Autowired
  @Qualifier("clientOutboundChannel")
  private MessageChannel clientOutboundChannel;

  /**
   * The jwt token provider.
   */
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(WebsocketChannelInterceptor.class);

  /**
   * Pre send.
   *
   * @param message the message
   * @param channel the channel
   *
   * @return the message
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    Assert.notNull(accessor, "Cannot get accessor from message");

    if (isLogged(accessor)) {
      logMessage(accessor);
      return message;
    }

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      if (doLogin(accessor)) {
        return message;
      }
      return null;
    }

    sendErrorCommand(accessor.getSessionId(), "Login needed");
    return null;
  }

  /**
   * Checks if is logged.
   *
   * @param accessor the accessor
   *
   * @return true, if is logged
   */
  private boolean isLogged(StompHeaderAccessor accessor) {
    return accessor.getUser() != null;
  }

  /**
   * Log message.
   *
   * @param accessor the accessor
   */
  private void logMessage(StompHeaderAccessor accessor) {
    if (!accessor.isHeartbeat()) {
      logger.info("Message received: User={}, SessionId={}, Command={}", accessor.getUser(),
          accessor.getSessionId(), accessor.getCommand());
    }
  }

  /**
   * Do login.
   *
   * @param accessor the accessor
   *
   * @return true, if successful
   */
  private boolean doLogin(StompHeaderAccessor accessor) {
    try {
      accessor.setUser(new StompPrincipal(
          ((AccessToken) jwtTokenProvider.retrieveToken(accessor.getFirstNativeHeader("token")))
              .getPreferredUsername()));
      logger.info("Message received: User={}, SessionId={}, Command={}", accessor.getUser(),
          accessor.getSessionId(), accessor.getCommand());
    } catch (VerificationException e) {
      logger.error("Security token not valid; {}", e.getMessage());
      sendErrorCommand(accessor.getSessionId(), "Token validation failed");
      return false;
    }
    return true;
  }

  /**
   * Send error command.
   *
   * @param sessionId the session id
   * @param message the message
   */
  private void sendErrorCommand(String sessionId, String message) {
    StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
    errorAccessor.setSessionId(sessionId);
    errorAccessor.setMessage(message);
    clientOutboundChannel
        .send(MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders()));
  }
}
