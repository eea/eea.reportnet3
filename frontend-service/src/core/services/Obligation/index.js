import { GetById } from './GetById';
import { GetClients } from './GetClient';
import { GetCountries } from './GetCountries';
import { GetIssues } from './GetIssues';
import { GetOpened } from './GetOpened';
import { obligationRepository } from 'core/domain/model/Obligation/ObligationRepository';

export const ObligationService = {
  getClients: GetClients({ obligationRepository }),
  getCountries: GetCountries({ obligationRepository }),
  getIssues: GetIssues({ obligationRepository }),
  getObligationById: GetById({ obligationRepository }),
  opened: GetOpened({ obligationRepository })
};
