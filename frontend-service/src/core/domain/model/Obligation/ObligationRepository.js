import { ApiObligationRepository } from 'core/infrastructure/domain/model/Obligation/ApiObligationRepository';

export const ObligationRepository = {
  getClients: () => Promise.reject('[ObligationRepository#getClients] must be implemented'),
  getCountries: () => Promise.reject('[ObligationRepository#getCountries] must be implemented'),
  getIssues: () => Promise.reject('[ObligationRepository#getIssues] must be implemented'),
  getObligationById: () => Promise.reject('[ObligationRepository#getObligationById] must be implemented'),
  opened: () => Promise.reject('[ObligationRepository#opened] must be implemented')
};

export const obligationRepository = Object.assign({}, ObligationRepository, ApiObligationRepository);
