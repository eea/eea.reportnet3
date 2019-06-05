import React from 'react';
import { shallow } from 'enzyme';
import Title from './Title';

describe('Title', () => {
    let wrapper;
    beforeEach(() => wrapper = shallow(<Title />));
    
    //Snapshot
    it('should render correctly', () => expect(wrapper).toMatchSnapshot());
  
    it('should render a <div />', () => {
      expect(wrapper.find('div').length).toEqual(1);
    });

  });