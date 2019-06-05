import React, {useState} from 'react';
import styles from './App.module.css';
import Navigation from './components/Navigation/Navigation';
import Footer from './components/Layout/Footer/Footer';
import ReporterDataSet from './components/Pages/ReporterDataSet/ReporterDataSet';
import ResourcesContext from './components/Context/ResourcesContext';
import langResources from './conf/messages.en.json';
import iconsResources from './conf/prime.icons.json';

const App = () => {
  const [resources] = useState({...langResources, ...iconsResources});
  console.log(resources)
  return (
    <div className={styles.App}>
    <ResourcesContext.Provider value={resources}>
      <Navigation />
      <ReporterDataSet />
      <Footer />
      </ResourcesContext.Provider>
    </div>
  );
}

export default App;
