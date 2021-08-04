import { GetCountries } from './GetCountries';
import { GetIssues } from './GetIssues';
import { GetOpened } from './GetOpened';
import { GetOrganizations } from './GetOrganizations';
import { ObligationById } from './ObligationById';

import { obligationRepository } from 'entities/Obligation/ObligationRepository';

export const ObligationService = {
  getCountries: GetCountries({ obligationRepository }),
  getIssues: GetIssues({ obligationRepository }),
  getOrganizations: GetOrganizations({ obligationRepository }),
  obligationById: ObligationById({ obligationRepository }),
  opened: GetOpened({ obligationRepository })
};
