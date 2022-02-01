import { useRecoilCallback, useRecoilValue, useSetRecoilState } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import { dataStore, filterByStore, sortByStore } from 'views/_components/Filters/_functions/Stores/filterStore';

export const useApplyFilters = ({ filterByKeys = [], recoilId }) => {
  const sortBy = useRecoilValue(sortByStore(recoilId));

  const setData = useSetRecoilState(dataStore(recoilId));

  const getFilterBy = useRecoilCallback(
    ({ snapshot }) =>
      async () => {
        const response = await Promise.all(
          filterByKeys.map(key => snapshot.getPromise(filterByStore(`${key}_${recoilId}`)))
        );
        return Object.assign({}, ...response);
      },
    []
  );

  const isFiltered = async () => {
    const filterBy = await getFilterBy();

    if (isEmpty(filterBy)) {
      return false;
    }

    return Object.values(filterBy)
      .map(key => isEmpty(key))
      .includes(false);
  };

  const resetFilterState = () => {};

  return { getFilterBy, isFiltered: isFiltered(), resetFilterState, setData, sortBy };
};
