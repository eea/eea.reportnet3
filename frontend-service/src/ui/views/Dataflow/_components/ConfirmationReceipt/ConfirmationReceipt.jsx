import React, { useEffect, useState } from 'react';

import { isUndefined } from 'lodash';
import { Document, Image, Page, StyleSheet, Text, View } from '@react-pdf/renderer';

import logo from 'assets/images/logo.png';

import { ConfirmationReceiptService } from 'core/services/ConfirmationReceipt';

const ConfirmationReceipt = ({ dataflowId, dataProviderId }) => {
  const [receiptData, setReceiptData] = useState();

  useEffect(() => {
    onLoadReceiptData();
  }, []);

  const onLoadReceiptData = async () => {
    setReceiptData(await ConfirmationReceiptService.get(dataflowId, dataProviderId));
  };

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
          <Text>Confirmation receipt</Text>
          {!isUndefined(receiptData) && (
            <>
              <Text>{receiptData.dataflowName}</Text>
              {receiptData.datasets.map(dataset => (
                <>
                  <Text style={{ color: 'teal', textAlign: 'left' }}>{dataset.name}</Text>
                  <Text style={{ color: 'teal', textAlign: 'left' }}>
                    {new Date(dataset.releaseDate).getTime() / 1000}
                  </Text>
                </>
              ))}
            </>
          )}
        </View>
      </Page>
    </Document>
  );
};

export { ConfirmationReceipt };
