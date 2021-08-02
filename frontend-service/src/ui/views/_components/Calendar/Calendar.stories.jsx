import { storiesOf } from '../../../../../.storybook/storiesOf';
import { Calendar } from './Calendar';

storiesOf('Calendar', module).add('Default', () => (
  <Calendar monthNavigator={true} value={null} yearNavigator={true} yearRange="2010:2030" />
));
