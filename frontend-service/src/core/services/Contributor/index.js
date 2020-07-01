import { AllEditors } from './AllEditors';
import { AllReporters } from './AllReporters';
import { DeleteEditor } from './DeleteEditor';
import { DeleteReporter } from './DeleteReporter';
import { UpdateEditor } from './UpdateEditor';
import { UpdateReporter } from './UpdateReporter';

import { contributorRepository } from 'core/domain/model/Contributor/ContributorRepository';

export const ContributorService = {
  allEditors: AllEditors({ contributorRepository }),
  allReporters: AllReporters({ contributorRepository }),
  deleteEditor: DeleteEditor({ contributorRepository }),
  deleteReporter: DeleteReporter({ contributorRepository }),
  updateEditor: UpdateEditor({ contributorRepository }),
  updateReporter: UpdateReporter({ contributorRepository })
};
