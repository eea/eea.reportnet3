import { notificationRepository } from 'entities/Notification/NotificationRepository';
import { All } from './All';
import { Parse } from './Parse';
import { ParseHidden } from './ParseHidden';
import { ReadAll } from './ReadAll';
import { ReadById } from './ReadById';
import { RemoveAll } from './RemoveAll';
import { RemoveById } from './RemoveById';

export const NotificationService = {
  all: All({ notificationRepository }),
  parse: Parse({ notificationRepository }),
  parseHidden: ParseHidden({ notificationRepository }),
  readAll: ReadAll({ notificationRepository }),
  readById: ReadById({ notificationRepository }),
  removeAll: RemoveAll({ notificationRepository }),
  removeById: RemoveById({ notificationRepository })
};
