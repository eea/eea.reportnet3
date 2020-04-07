export const GetOpened = ({ obligationRepository }) => async filterData => obligationRepository.opened(filterData);
