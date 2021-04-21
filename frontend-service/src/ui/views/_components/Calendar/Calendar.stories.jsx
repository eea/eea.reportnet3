import { storiesOf } from '../../../../../.storybook/storiesOf';
import { Calendar } from './Calendar';

storiesOf('Calendar', module).add('Default', () => (
  <Calendar value={null} monthNavigator={true} yearNavigator={true} yearRange="2010:2030" />
));
