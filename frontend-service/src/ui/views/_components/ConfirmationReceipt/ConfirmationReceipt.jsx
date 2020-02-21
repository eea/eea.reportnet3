import React, { useContext } from 'react';
import { isUndefined } from 'lodash';
import moment from 'moment';

import logo from 'assets/images/logo.png';

import { Page, View, Document, StyleSheet, Image, Font } from '@react-pdf/renderer';
import styled from '@react-pdf/styled-components';

export const ConfirmationReceipt = ({ receiptData, resources }) => {
  const hyphenationCallback = word => {
    return [word];
  };

  Font.registerHyphenationCallback(hyphenationCallback);

  const Header = styled.View`
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    margin-bottom: 24px;
  `;

  const Date = styled.Text`
    font-size: 12px;
    padding: 1px;
  `;

  const RepresentativeName = styled.Text`
    font-size: 12px;
    padding: 1px;
  `;

  const Title = styled.Text`
    text-transform: uppercase;
    font-size: 24px;
  `;

  const SubTitle = styled.Text`
    padding-top: 5px;
    font-size: 14px;
  `;

  const DataTableWrapper = styled.View`
    margin-top: 16px;
  `;

  const TableTitles = styled.View`
    margin-top: 20px;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
  `;

  const RowWrapper = styled.View`
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    border-bottom: 1 solid black;
  `;

  const DatasetsTitle = styled.Text`
    width: 72%;
    padding-top: 5px;
    font-size: 14px;
  `;

  const DateTitle = styled.Text`
    width: 28%;
    padding-top: 5px;
    font-size: 14px;
  `;

  const RowDataName = styled.Text`
    padding-top: 5px;
    padding-bottom: 5px;
    font-size: 16px;
    max-width: 70%;
  `;

  const RowDataDate = styled.Text`
    padding-top: 5px;
    padding-bottom: 5px;
    font-size: 16px;
    max-width: 30%;
  `;

  const Footer = styled.View`
    position: absolute;
    bottom: 64px;
    right: 0;
  `;

  const BottomLogo = styled.Image`
    width: 50px;
  `;

  const styles = StyleSheet.create({
    pageBackground: {
      position: 'absolute',
      width: '125%',
      height: '150%',
      right: '-65%',
      top: '-30%',
      opacity: 0.08
    },

    mainContent: { margin: 30, height: '100%' }
  });

  return (
    <Document style={styles.document}>
      <Page size="A5" ruler={false} rulerSteps={16} orientation="landscape" wrap>
        {!isUndefined(receiptData) && (
          <View style={styles.mainContent} height="100%">
            <Image src={logo} style={styles.pageBackground} fixed={true} />

            <Header fixed>
              <Date>
                {resources.messages['date']} : {moment().format('YYYY-MM-DD HH:mm:ss')}
              </Date>
              <RepresentativeName>
                {resources.messages['representative']}: {receiptData.representative}
              </RepresentativeName>
            </Header>

            <Title hyphenationCallback="1000">{resources.messages['confirmationReceipt']}</Title>
            <SubTitle>{receiptData.dataflowName}</SubTitle>
            <TableTitles>
              <DatasetsTitle> {resources.messages['datasets']}</DatasetsTitle>
              <DateTitle> {resources.messages['date']}</DateTitle>
            </TableTitles>
            <DataTableWrapper>
              {receiptData.datasets.map(dataset => (
                <>
                  <RowWrapper>
                    <RowDataName>{dataset.name}</RowDataName>
                    <RowDataDate>{moment(dataset.releaseDate).format('YYYY-MM-DD HH:mm:ss')}</RowDataDate>
                  </RowWrapper>
                </>
              ))}
            </DataTableWrapper>
            <Footer fixed={true}>
              <BottomLogo src={logo} fixed={true} />
            </Footer>
          </View>
        )}
      </Page>
    </Document>
  );
};
