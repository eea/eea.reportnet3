import { GetOpened } from './GetOpened';
import { obligationRepository } from 'core/domain/model/Obligation/ObligationRepository';

export const ObligationService = {
  opened: GetOpened({ obligationRepository })
};
