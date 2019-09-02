import { dataFlowRepository } from 'core/domain/model/DataFlow/DataFlowRepository';
import { GetPendingDataFlows } from './GetPendingDataFlows';
import { GetAcceptedDataFlows } from './GetAcceptedDataFlows';
import { GetCompletedDataFlows } from './GetCompletedDataFlows';
import { GetReportingDataFlow } from './GetReportingDataFlow';
import { AcceptDataFlow } from './AcceptDataFlow';
import { RejectDataFlow } from './RejectDataFlow';
import { GetAllDataFlows } from './GetAllDataFlows';

export const DataFlowService = {
  all: GetAllDataFlows({ dataFlowRepository }),
  accepted: GetAcceptedDataFlows({ dataFlowRepository }),
  pending: GetPendingDataFlows({ dataFlowRepository }),
  completed: GetCompletedDataFlows({ dataFlowRepository }),
  reporting: GetReportingDataFlow({ dataFlowRepository }),
  accept: AcceptDataFlow({ dataFlowRepository }),
  reject: RejectDataFlow({ dataFlowRepository })
};
