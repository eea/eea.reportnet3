import { apiObligation } from 'core/infrastructure/api/domain/model/Obligation';
import { Obligation } from 'core/domain/model/Obligation/Obligation';

const opened = async () => {
  const openedObligationsDTO = await apiObligation.openedObligations();

  return openedObligationsDTO.map(openedObligation => new Obligation(openedObligation));
};

const getObligationById = async obligationId => {
  const obligationByIdDto = await apiObligation.getObligationByID(obligationId);
  const obligationById = new Obligation(obligationByIdDto);

  return obligationById;
};

export const ApiObligationRepository = {
  opened,
  getObligationById
};
