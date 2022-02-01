import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import { filterByAllKeys, filterByKeySearchStore } from '../Stores/filterKeysStore';
import { searchByStore } from '../Stores/filterStore';

export const useSearch = ({ onFilterData, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));
  const setFilterBySearchKeys = useSetRecoilState(filterByKeySearchStore(recoilId));

  const [searchBy, setSearchBy] = useRecoilState(searchByStore(recoilId));

  useEffect(() => {
    setFilterBySearchKeys(prevState => ({ keys: option.searchBy, nestedKey: prevState?.nestedKey }));
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  const onSearch = async value => {
    setSearchBy(value);
    await onFilterData({ key: option.key, searchValue: value, type: 'SEARCH' });
  };

  return { searchBy, onSearch };
};
