package org.eea.interfaces.vo.dremio;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DremioJobStatusResponse {

    DremioJobStatusEnum jobState;
    Long rowCount;
    String errorMessage;
    String startedAt;
    String endedAt;
    String queryType;
    String queueName;
    String queueId;
    String resourceSchedulingStartedAt;
    String resourceSchedulingEndedAt;
    String cancellationReason;
}
