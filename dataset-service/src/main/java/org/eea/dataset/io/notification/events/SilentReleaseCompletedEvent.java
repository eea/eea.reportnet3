package org.eea.dataset.io.notification.events;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SilentReleaseCompletedEvent extends AbstractEEAEventHandlerCommand {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SilentReleaseCompletedEvent.class);


    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.SILENT_RELEASE_COMPLETED_EVENT;
    }

    /**
     * Execute.
     *
     * @param eeaEventVO the eea event VO
     * @throws EEAException the EEA exception
     */
    @Override
    public void execute(EEAEventVO eeaEventVO) throws EEAException {
        LOG.info("Inside SilentReleaseCompletedEvent {}", eeaEventVO);
    }
}
