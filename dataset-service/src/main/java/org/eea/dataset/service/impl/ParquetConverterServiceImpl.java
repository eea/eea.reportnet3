package org.eea.dataset.service.impl;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.eea.dataset.service.ParquetConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParquetConverterServiceImpl implements ParquetConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetConverterServiceImpl.class);

    @Override
    public Map<String, String> convertCsvFilesToParquetFiles(List<File> csvFiles){
        Map<String, String> parquetFileNamesAndPaths = new HashMap<>();
        for(File csvFile: csvFiles){
            String parquetFilePath = csvFile.getPath().replace(".csv",".parquet");
            String parquetFileName = csvFile.getName().replace(".csv",".parquet");
            convertCsvToParquet(csvFile.getPath(), parquetFilePath);
            parquetFileNamesAndPaths.put(parquetFileName, parquetFilePath);
        }
        return parquetFileNamesAndPaths;
    }

    @Override
    public void convertCsvToParquet(String csvFilePath, String parquetFilePath){


        // Check that the parquet file exists, if so delete it
        if (Files.exists(Paths.get(parquetFilePath))) {
            try {
                Files.delete(Paths.get(parquetFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFilePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())) {
            headers = new ArrayList<>();
            headers.add("recordId");
            headers.addAll(csvParser.getHeaderMap().keySet());
            Integer recordCounter = 0;
            for (CSVRecord csvRecord : csvParser) {
                recordCounter++;
                List<String> row = new ArrayList<>();
                for (String header : headers) {
                    if(header.equals("recordId")){
                        row.add(recordCounter.toString());
                    }
                    else {
                        row.add(csvRecord.get(header));
                    }
                }
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Defining schema
        List<Schema.Field> fields = new ArrayList<>();
        for (String header : headers) {
            fields.add(new Schema.Field(header, Schema.create(Schema.Type.STRING), null, null));
        }
        Schema schema = Schema.createRecord("Data", null, null, false, fields);

        // Write all data to a single Parquet file
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(new Path(parquetFilePath))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withPageSize(4 * 1024)
                .withRowGroupSize(16 * 1024)
                .build()) {
            for (List<String> row : data) {
                GenericRecord record = new GenericData.Record(schema);
                for (int i = 0; i < headers.size(); i++) {
                    record.put(headers.get(i), row.get(i));
                }
                writer.write(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("Finished writing to Parquet file: {}", parquetFilePath);




    }
}
