import { GetCountries } from './GetCountries';
import { GetIssues } from './GetIssues';
import { GetOrganizations } from './GetOrganizations';
import { GetOpened } from './GetOpened';
import { ObligationById } from './ObligationById';

import { obligationRepository } from 'core/domain/model/Obligation/ObligationRepository';

export const ObligationService = {
  getOrganizations: GetOrganizations({ obligationRepository }),
  getCountries: GetCountries({ obligationRepository }),
  getIssues: GetIssues({ obligationRepository }),
  obligationById: ObligationById({ obligationRepository }),
  opened: GetOpened({ obligationRepository })
};
