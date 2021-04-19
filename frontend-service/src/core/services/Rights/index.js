import { AllEditors } from './AllEditors';
import { AllReporters } from './AllReporters';
import { AllRequesters } from './AllRequesters';
import { DeleteEditor } from './DeleteEditor';
import { DeleteReporter } from './DeleteReporter';
import { DeleteRequester } from './DeleteRequester';
import { UpdateEditor } from './UpdateEditor';
import { UpdateReporter } from './UpdateReporter';
import { UpdateRequester } from './UpdateRequester';

import { rightsRepository } from 'core/domain/model/Rights/RightsRepository';

export const RightsService = {
  allEditors: AllEditors({ rightsRepository }),
  allReporters: AllReporters({ rightsRepository }),
  allRequesters: AllRequesters({ rightsRepository }),
  deleteEditor: DeleteEditor({ rightsRepository }),
  deleteReporter: DeleteReporter({ rightsRepository }),
  deleteRequester: DeleteRequester({ rightsRepository }),
  updateEditor: UpdateEditor({ rightsRepository }),
  updateReporter: UpdateReporter({ rightsRepository }),
  updateRequester: UpdateRequester({ rightsRepository })
};
