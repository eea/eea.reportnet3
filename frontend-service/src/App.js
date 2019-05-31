import React, {useState} from 'react';
import styles from './App.module.css';
import Navigation from './components/Navigation/Navigation';
import Footer from './components/Layout/Footer/Footer';
import ReporterDataSet from './components/Pages/ReporterDataSet/ReporterDataSet';
import LangContext from './components/Context/LanguageContext';
import langResources from './conf/messages.en.json';

const App = () => {
  const [langMessages, setLangMessages] = useState(langResources);
  return (
    <div className={styles.App}>
    <LangContext.Provider value={langMessages}>
      <Navigation />
      <ReporterDataSet />
      <Footer />
      </LangContext.Provider>
    </div>
  );
}

export default App;
