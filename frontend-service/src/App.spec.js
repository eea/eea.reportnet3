import React from 'react';
import { shallow } from 'enzyme';
import App from './App';
import Navigation from './components/Navigation/Navigation';
import Footer from './components/Layout/Footer/Footer';
import ReporterDataSet from './components/Pages/ReporterDataSet/ReporterDataSet';

describe('App', () => {
  let wrapper;
  beforeEach(() => wrapper = shallow(<App />));
  
  //Snapshot
  it('should render correctly', () => expect(wrapper).toMatchSnapshot());

  it('should render a <div />', () => {
    expect(wrapper.find('div').length).toEqual(1);
  });

  it('should render the Navigation Component', () => {
    expect(wrapper.containsMatchingElement(<Navigation />)).toEqual(true);
  });
  it('should render the ReporterDataSet Component', () => {
    expect(wrapper.containsMatchingElement(<ReporterDataSet />)).toEqual(true);
  });
  it('should render the Footer Component', () => {
    expect(wrapper.containsMatchingElement(<Footer />)).toEqual(true);
  });
});