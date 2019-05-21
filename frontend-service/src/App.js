import React from 'react';
import styles from './App.module.css';
import Navigation from './components/Navigation/Navigation';
import Footer from './components/Layout/Footer/Footer';
import ReporterDataSet from './components/Pages/ReporterDataSet/ReporterDataSet';

const App = () => {
  return (
    <div className={styles.App}>
      <Navigation />
      <ReporterDataSet />
      <Footer />
    </div>
  );
}

export default App;
