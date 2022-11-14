package org.eea.orchestrator.scheduling;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.commands.DeleteProviderCommand;
import org.eea.axon.release.commands.RestoreDataFromSnapshotCommand;
import org.eea.axon.release.events.DatasetRunningStatusUpdatedEvent;
import org.eea.axon.release.events.DatasetStatusUpdatedEvent;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataset.InternalProcessVO;
import org.eea.interfaces.vo.dataset.PgStatActivityVO;
import org.eea.interfaces.vo.dataset.enums.InternalProcessTypeEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class JobForCheckingInternalProcess {

    private static final String COPY = "COPY";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForCheckingInternalProcess.class);

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private EmbeddedEventStore embeddedEventStore;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> checkInternalProcess(),
                new CronTrigger("0 */1 * * * *"));
    }

    /**
     * The job runs every 30 minutes. It finds internal processes and creates respective commands
     */
    public void checkInternalProcess() {
        LOG.info("Running scheduled task checkInternalProcess");
        try {
            List<InternalProcessVO> internalProcesses = dataSetControllerZuul.getInternalProcesses();
            Map<Pair<Long, Long>, List<InternalProcessVO>> processesByDataflowAndDataProvider = internalProcesses.stream().collect(groupingBy(JobForCheckingInternalProcess::getPairOfDataflowDataProvider));
            for (Pair<Long, Long> pair : processesByDataflowAndDataProvider.keySet()) {
                Map<String, List<InternalProcessVO>> processes = processesByDataflowAndDataProvider.get(pair).stream().collect(groupingBy(InternalProcessVO::getType));
                List<InternalProcessVO> internalProcessList;
                if (processes.keySet().size() > 1) {
                    internalProcessList = processes.get(InternalProcessTypeEnum.DELETE.getValue());
                } else {
                    Iterator<Map.Entry<String, List<InternalProcessVO>>> iterator = processes.entrySet().iterator();
                    internalProcessList = iterator.next().getValue();
                }

                boolean canSendCommand = true;
                for (InternalProcessVO process : internalProcessList) {
                    List<PgStatActivityVO> results = dataSetControllerZuul.getPgStatActivityResults();
                    for (PgStatActivityVO pgStatActivity : results) {
                        if (pgStatActivity.getQuery().contains(COPY + " " + String.format(LiteralConstants.DATASET_FORMAT_NAME, process.getDataCollectionId()))) {
                            canSendCommand = false;
                        }
                    }
                }
                InternalProcessVO processVO = internalProcessList.get(0);
                if (canSendCommand) {
                    DomainEventStream events = embeddedEventStore.readEvents(processVO.getAggregateId());
                    List<? extends DomainEventMessage<?>> eventMessages = events.asStream().collect(Collectors.toList());
                    if (processes.keySet().size() > 1 || processVO.getType().equals(InternalProcessTypeEnum.DELETE.getValue())) {
                        for (DomainEventMessage message : eventMessages) {
                            if (message.getPayload() instanceof DatasetStatusUpdatedEvent) {
                                MetaData metadata = message.getMetaData();
                                DatasetStatusUpdatedEvent event = (DatasetStatusUpdatedEvent) message.getPayload();
                                DeleteProviderCommand command = DeleteProviderCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                                        .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots())
                                        .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId())
                                        .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).dataCollectionForDeletion(event.getDataCollectionForDeletion()).datasetDataCollection(event.getDatasetDataCollection()).build();
                                commandGateway.send(GenericCommandMessage.asCommandMessage(command).withMetaData(metadata));
                                internalProcessList.forEach(intProc -> dataSetControllerZuul.removeInternalProcess(intProc.getId()));
                            }
                        }
                    } else if (processVO.getType().equals(InternalProcessTypeEnum.RESTORE.getValue())) {
                        for (DomainEventMessage message : eventMessages) {
                            if (message.getPayload() instanceof DatasetRunningStatusUpdatedEvent) {
                                MetaData metadata = message.getMetaData();
                                DatasetRunningStatusUpdatedEvent event = (DatasetRunningStatusUpdatedEvent) message.getPayload();
                                RestoreDataFromSnapshotCommand command = RestoreDataFromSnapshotCommand.builder().recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                                        .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).datasetDataCollection(event.getDatasetDataCollection())
                                        .releaseAggregateId(event.getReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).datasetReleaseAggregateId(event.getDatasetReleaseAggregateId())
                                        .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
                                commandGateway.send(GenericCommandMessage.asCommandMessage(command).withMetaData(metadata));
                                internalProcessList.forEach(intProc -> dataSetControllerZuul.removeInternalProcess(intProc.getId()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task checkInternalProcess " + e.getMessage());
        }
    }

    private static Pair<Long, Long> getPairOfDataflowDataProvider(InternalProcessVO internalProcessVO) {
        return Pair.of(internalProcessVO.getDataflowId(), internalProcessVO.getDataProviderId());
    }
}


















