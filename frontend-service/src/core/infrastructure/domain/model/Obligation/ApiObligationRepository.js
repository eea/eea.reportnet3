import { apiObligation } from 'core/infrastructure/api/domain/model/Obligation';
import { Obligation } from 'core/domain/model/Obligation/Obligation';

const opened = async () => {
  const openedObligationsDTO = await apiObligation.openedObligations();

  return openedObligationsDTO.map(openedObligation => new Obligation(openedObligation));
};

export const ApiObligationRepository = {
  opened
};
