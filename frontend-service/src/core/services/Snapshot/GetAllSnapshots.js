export const GetAllSnapshots = ({ snapshotRepository }) => async dataSetId => snapshotRepository.all(dataSetId);
