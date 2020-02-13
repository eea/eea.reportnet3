import React from 'react';

import logo from 'assets/images/logo.png';

import { Document, Image, Page, StyleSheet, Text, View } from '@react-pdf/renderer';

const ConfirmationReceipt = ({ dataflowData }) => {
  const styles = StyleSheet.create({
    image: { width: '30vmin', height: '30vmin' },
    page: { backgroundColor: 'white' },
    section: { textAlign: 'center', margin: 30 }
  });

  return (
    <Document>
      <Page size="A4" style={styles.page}>
        <View style={[styles.section, { color: 'black' }]}>
          <Image style={styles.image} src={logo} />
          <Text>REPORTNET 3</Text>
          <Text>Confirmation receipt - {dataflowData.description}</Text>
          <Text>{dataflowData.name}</Text>
          {/* {dataflowData.datasets.map(dataset => (
            <Text style={{ color: 'teal', textAlign: 'left' }}>{dataset.datasetName}</Text>
          ))}  */}
        </View>
      </Page>
    </Document>
  );
};

export { ConfirmationReceipt };
