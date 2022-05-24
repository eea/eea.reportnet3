import { useRecoilCallback, useRecoilValue, useSetRecoilState } from 'recoil';

import {
  dataStore,
  filterByCustomFilterStore,
  filterByStore,
  filteredDataStore,
  isFilteredStore,
  searchByStore,
  sortByStore
} from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByAllKeys } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

export const useApplyFilters = recoilId => {
  const isFiltered = useRecoilValue(isFilteredStore(recoilId));
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

  const resetFilterState = useRecoilCallback(
    ({ snapshot, reset }) =>
      async () => {
        const filterByKeys = await snapshot.getPromise(filterByAllKeys(recoilId));

        reset(filterByCustomFilterStore(recoilId));
        reset(filteredDataStore(recoilId));
        reset(isFilteredStore(recoilId));
        reset(searchByStore(recoilId));
        reset(sortByStore(recoilId));
        await Promise.all(filterByKeys.map(key => reset(filterByStore(`${key}_${recoilId}`))));
      },
    [recoilId]
  );

  return { getFilterBy, isFiltered, resetFilterState, setData, sortByOptions: sortBy };
};
