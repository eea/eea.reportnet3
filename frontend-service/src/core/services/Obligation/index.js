import { GetClients } from './GetClients';
import { GetCountries } from './GetCountries';
import { GetIssues } from './GetIssues';
import { GetOpened } from './GetOpened';
import { ObligationById } from './ObligationById';

import { obligationRepository } from 'core/domain/model/Obligation/ObligationRepository';

export const ObligationService = {
  getClients: GetClients({ obligationRepository }),
  getCountries: GetCountries({ obligationRepository }),
  getIssues: GetIssues({ obligationRepository }),
  obligationById: ObligationById({ obligationRepository }),
  opened: GetOpened({ obligationRepository })
};
