import { AllReporters } from './AllReporters';
import { AllRequesters } from './AllRequesters';
import { DeleteReporter } from './DeleteReporter';
import { DeleteRequester } from './DeleteRequester';
import { UpdateReporter } from './UpdateReporter';
import { UpdateRequester } from './UpdateRequester';
import { userRightRepository } from 'core/domain/model/UserRight/UserRightRepository';

export const UserRightService = {
  allReporters: AllReporters({ userRightRepository }),
  allRequesters: AllRequesters({ userRightRepository }),
  deleteReporter: DeleteReporter({ userRightRepository }),
  deleteRequester: DeleteRequester({ userRightRepository }),
  updateReporter: UpdateReporter({ userRightRepository }),
  updateRequester: UpdateRequester({ userRightRepository })
};
