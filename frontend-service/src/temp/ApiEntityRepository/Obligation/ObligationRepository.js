import { ApiObligationRepository } from 'repositories/_temp/model/Obligation/ApiObligationRepository';

export const ObligationRepository = {
  getCountries: () => Promise.reject('[ObligationRepository#getCountries] must be implemented'),
  getIssues: () => Promise.reject('[ObligationRepository#getIssues] must be implemented'),
  getOrganizations: () => Promise.reject('[ObligationRepository#getOrganizations] must be implemented'),
  obligationById: () => Promise.reject('[ObligationRepository#obligationById] must be implemented'),
  opened: () => Promise.reject('[ObligationRepository#opened] must be implemented')
};

export const obligationRepository = Object.assign({}, ObligationRepository, ApiObligationRepository);
