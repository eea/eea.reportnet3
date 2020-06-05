import isNil from 'lodash/isNil';

import { apiIntegration } from 'core/infrastructure/api/domain/model/Integration/ApiIntegration';

import { Integration } from 'core/domain/model/Integration/Integration';

const all = async () => apiIntegration.all();
// const all = async integration => parseIntegrationsList(await apiIntegration.all(integration));

const deleteById = async integrationId => {
  return await apiIntegration.deleteById(integrationId);
};

const parseIntegration = integrationDTO => new Integration(integrationDTO);

const parseIntegrationsList = integrationsDTO => {
  if (!isNil(integrationsDTO)) {
    const integrations = [];
    integrationsDTO.forEach(integrationDTO => integrations.push(parseIntegration(integrationDTO)));
    return integrations;
  }
  return;
};

export const ApiIntegrationRepository = { all, deleteById };
