import { dataFlowRepository } from 'core/domain/model/DataFlow/DataFlowRepository';
import { GetPendingDataFlows } from './GetPendingDataFlows';
import { GetAcceptedDataFlows } from './GetAcceptedDataFlows';
import { GetCompletedDataFlows } from './GetCompletedDataFlows';
import { GetReportingDataFlow } from './GetReportingDataFlow';

export const DataFlowService = {
  accepted: GetAcceptedDataFlows({ dataFlowRepository }),
  completed: GetCompletedDataFlows({ dataFlowRepository }),
  pending: GetPendingDataFlows({ dataFlowRepository }),
  reporting: GetReportingDataFlow({ dataFlowRepository })
};
