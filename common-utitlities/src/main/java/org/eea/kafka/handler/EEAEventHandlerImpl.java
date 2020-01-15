package org.eea.kafka.handler;

import java.util.HashSet;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.EEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.factory.EEAEventCommandFactory;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class EEAEventHandlerImpl.
 */
@Component
public class EEAEventHandlerImpl implements EEAEventHandler {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(EEAEventHandlerImpl.class);

  /** The eea eent command factory. */
  @Autowired
  private EEAEventCommandFactory eeaEentCommandFactory;

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }

  /**
   * Process message.
   *
   * @param message the message
   * @throws EEAException the EEA exception
   */
  @Override
  public void processMessage(EEAEventVO message) throws EEAException {
    EEAEventHandlerCommand command = eeaEentCommandFactory.getEventCommand(message);
    if (null != command) {
      String user = "null";
      if (message.getData().containsKey("user")) {
        user = String.valueOf(message.getData().get("user"));
        ThreadPropertiesManager.setVariable("user", user);
      }
      if (message.getData().containsKey("token")) {
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(user, new HashSet<String>()),
                String.valueOf(message.getData().get("token")), null));
      }
      command.execute(message);
    }
  }

}
