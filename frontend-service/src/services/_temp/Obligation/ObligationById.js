export const ObligationById = ({ obligationRepository }) => async obligationId =>
  obligationRepository.obligationById(obligationId);
