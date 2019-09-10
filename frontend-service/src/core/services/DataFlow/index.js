import { dataFlowRepository } from 'core/domain/model/DataFlow/DataFlowRepository';
import { GetPending } from './GetPending';
import { GetAccepted } from './GetAccepted';
import { GetCompleted } from './GetCompleted';
import { GetReporting } from './GetReporting';
import { Accept } from './Accept';
import { Reject } from './Reject';
import { GetAll } from './GetAll';

export const DataFlowService = {
  all: GetAll({ dataFlowRepository }),
  accepted: GetAccepted({ dataFlowRepository }),
  pending: GetPending({ dataFlowRepository }),
  completed: GetCompleted({ dataFlowRepository }),
  reporting: GetReporting({ dataFlowRepository }),
  accept: Accept({ dataFlowRepository }),
  reject: Reject({ dataFlowRepository })
};
