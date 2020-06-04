import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

const all = async () => apiIntegration.all();

export const ApiIntegrationRepository = { all };
