import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import { filterByAllKeys, filterByKeySearchStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';
import { searchByStore } from 'views/_components/Filters/_functions/Stores/filterStore';

export const useSearch = ({ hasCustomSort, onFilterData, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));
  const setFilterBySearchKeys = useSetRecoilState(filterByKeySearchStore(recoilId));

  const [searchBy, setSearchBy] = useRecoilState(searchByStore(recoilId));

  useEffect(() => {
    setFilterBySearchKeys(prevState => ({ keys: option.searchBy, nestedKey: prevState?.nestedKey }));
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  const onSearch = async value => {
    setSearchBy(value);

    if (!hasCustomSort) {
      await onFilterData({ key: option.key, searchValue: value, type: 'SEARCH' });
    }
  };

  return { searchBy, onSearch };
};
