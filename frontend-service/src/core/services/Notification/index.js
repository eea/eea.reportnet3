import { notificationRepository } from 'core/domain/model/Notification/NotificationRepository';
import { All } from './All';
import { Parse } from './Parse';
import { ReadAll } from './ReadAll';
import { ReadById } from './ReadById';
import { RemoveAll } from './RemoveAll';
import { RemoveById } from './RemoveById';

export const NotificationService = {
  all: All({ notificationRepository }),
  parse: Parse({ notificationRepository }),
  readAll: ReadAll({ notificationRepository }),
  readById: ReadById({ notificationRepository }),
  removeAll: RemoveAll({ notificationRepository }),
  removeById: RemoveById({ notificationRepository })
};
