package org.eea.communication.configuration;

import java.security.Principal;
import org.eea.security.jwt.utils.JwtTokenProvider;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  private final Logger logger = LoggerFactory.getLogger(WebSocketConfiguration.class);

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/queue", "/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/communication/reportnet-websocket");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor.getUser() != null) {
          return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          try {
            accessor.setUser(new StompPrincipal(((AccessToken) jwtTokenProvider
                .retrieveToken(accessor.getFirstNativeHeader("token"))).getPreferredUsername()));
            return message;
          } catch (VerificationException ex) {
            logger.error("Security token is not valid: {}", ex.getMessage());
          }
        }

        return null;
      }
    });
  }
}


class StompPrincipal implements Principal {
  String name;

  StompPrincipal(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
