import { notificationRepository } from 'core/domain/model/Notification/NotificationRepository';
import { All } from './All';
import { Parse } from './Parse';
import { ReadedAll } from './ReadedAll';
import { ReadedById } from './ReadedById';
import { RemoveAll } from './RemoveAll';
import { RemoveById } from './RemoveById';

export const NotificationService = {
  all: All({ notificationRepository }),
  parse: Parse({ notificationRepository }),
  readedAll: ReadedAll({ notificationRepository }),
  readedById: ReadedById({ notificationRepository }),
  removeAll: RemoveAll({ notificationRepository }),
  removeById: RemoveById({ notificationRepository })
};
