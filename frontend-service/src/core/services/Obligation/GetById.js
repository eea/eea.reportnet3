export const GetById = ({ obligationRepository }) => async obligationId =>
  obligationRepository.getObligationById(obligationId);
