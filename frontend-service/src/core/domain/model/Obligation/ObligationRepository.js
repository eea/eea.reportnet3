import { ApiObligationRepository } from 'core/infrastructure/domain/model/Obligation/ApiObligationRepository';

export const ObligationRepository = {
  obligationById: () => Promise.reject('[ObligationRepository#obligationById] must be implemented'),
  opened: () => Promise.reject('[ObligationRepository#opened] must be implemented')
};

export const obligationRepository = Object.assign({}, ObligationRepository, ApiObligationRepository);
