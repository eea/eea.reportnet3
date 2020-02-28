export const Create = ({ validationRepository }) => async validationRule =>
  validationRepository.deleteById(validationRule);
