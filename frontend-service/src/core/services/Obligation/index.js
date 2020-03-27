import { GetOpened } from './GetOpened';
import { GetById } from './GetById';
import { obligationRepository } from 'core/domain/model/Obligation/ObligationRepository';

export const ObligationService = {
  opened: GetOpened({ obligationRepository }),
  getObligationById: GetById({ obligationRepository })
};
