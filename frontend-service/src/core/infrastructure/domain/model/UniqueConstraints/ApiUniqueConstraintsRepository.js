import { apiUniqueConstraints } from 'core/infrastructure/api/domain/model/UniqueConstraints/ApiUniqueConstraints';

const all = async () => apiUniqueConstraints.all();

export const ApiUniqueConstraintsRepository = { all };
