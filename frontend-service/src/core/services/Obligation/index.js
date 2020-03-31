import { GetOpened } from './GetOpened';
import { ObligationById } from './ObligationById';

import { obligationRepository } from 'core/domain/model/Obligation/ObligationRepository';

export const ObligationService = {
  obligationById: ObligationById({ obligationRepository }),
  opened: GetOpened({ obligationRepository })
};
