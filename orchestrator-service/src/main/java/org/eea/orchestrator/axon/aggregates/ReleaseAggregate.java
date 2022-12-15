package org.eea.orchestrator.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.CreateReleaseStartNotificationCommand;
import org.eea.axon.release.commands.SetJobCancelledCommand;
import org.eea.axon.release.commands.SetJobFinishedCommand;
import org.eea.axon.release.commands.SetJobInProgressCommand;
import org.eea.axon.release.events.*;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class ReleaseAggregate {

    @AggregateIdentifier
    private String releaseAggregate;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private Long jobId;

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseAggregate.class);

    public ReleaseAggregate() {

    }

    @CommandHandler
    public ReleaseAggregate(CreateReleaseStartNotificationCommand command) {
        ReleaseStartNotificationCreatedEvent event = new ReleaseStartNotificationCreatedEvent();
        BeanUtils.copyProperties(command, event);
        apply(event);
    }

    @EventSourcingHandler
    public void on(ReleaseStartNotificationCreatedEvent event) {
        this.releaseAggregate = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.jobId = event.getJobId();
    }

    @CommandHandler
    public void handle(SetJobInProgressCommand command, MetaData metaData, JobService jobService) {
        try {
            jobService.updateJobStatus(command.getJobId(), JobStatusEnum.IN_PROGRESS);

            JobSetInProgressEvent event = new JobSetInProgressEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while setting release job status to IN_PROGRESS for dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(SetJobFinishedCommand command, MetaData metaData, JobService jobService) {
        try {
            jobService.updateJobStatus(command.getJobId(), JobStatusEnum.FINISHED);

            JobFinishedEvent event = new JobFinishedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while setting release job status to FINISHED for dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(SetJobCancelledCommand command, JobService jobService) {
        try {
            jobService.updateJobStatus(command.getJobId(), JobStatusEnum.CANCELLED);

            JobCancelledEvent event = new JobCancelledEvent();
            BeanUtils.copyProperties(command, event);
            apply(event);
        } catch (Exception e) {
            LOG.error("Error while setting release job status to CANCELLED for dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

}













