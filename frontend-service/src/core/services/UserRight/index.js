import { AllEditors } from './AllEditors';
import { AllReporters } from './AllReporters';
import { AllRequesters } from './AllRequesters';
import { DeleteEditor } from './DeleteEditor';
import { DeleteReporter } from './DeleteReporter';
import { DeleteRequester } from './DeleteRequester';
import { UpdateEditor } from './UpdateEditor';
import { UpdateReporter } from './UpdateReporter';
import { UpdateRequester } from './UpdateRequester';

import { userRightRepository } from 'core/domain/model/UserRight/UserRightRepository';

export const UserRightService = {
  allEditors: AllEditors({ userRightRepository }),
  allReporters: AllReporters({ userRightRepository }),
  allRequesters: AllRequesters({ userRightRepository }),
  deleteEditor: DeleteEditor({ userRightRepository }),
  deleteReporter: DeleteReporter({ userRightRepository }),
  deleteRequester: DeleteRequester({ userRightRepository }),
  updateEditor: UpdateEditor({ userRightRepository }),
  updateReporter: UpdateReporter({ userRightRepository }),
  updateRequester: UpdateRequester({ userRightRepository })
};
