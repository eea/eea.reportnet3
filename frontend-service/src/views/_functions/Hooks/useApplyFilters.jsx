import { useRecoilCallback, useRecoilValue, useSetRecoilState } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import { dataStore, filterByStore, sortByStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByAllKeys } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

export const useApplyFilters = recoilId => {
  const sortBy = useRecoilValue(sortByStore(recoilId));

  const setData = useSetRecoilState(dataStore(recoilId));

  const getFilterBy = useRecoilCallback(
    ({ snapshot }) =>
      async () => {
        const filterByKeys = await snapshot.getPromise(filterByAllKeys(recoilId));
        const response = await Promise.all(
          filterByKeys.map(key => snapshot.getPromise(filterByStore(`${key}_${recoilId}`)))
        );

        return Object.assign({}, ...response);
      },
    [recoilId]
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

  const resetFilterState = useRecoilCallback(
    ({ snapshot, reset }) =>
      async () => {
        const filterByKeys = await snapshot.getPromise(filterByAllKeys(recoilId));

        await Promise.all(filterByKeys.map(key => reset(filterByStore(`${key}_${recoilId}`))));
      },
    [recoilId]
  );

  return { getFilterBy, isFiltered: isFiltered(), resetFilterState, setData, sortByOptions: sortBy };
};
