package org.eea.datalake.service.impl;

import com.opencsv.CSVWriter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.Preconditions;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.eea.datalake.service.S3ConvertService;
import org.eea.datalake.service.model.ParquetStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3ConvertServiceImpl implements S3ConvertService {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConvertServiceImpl.class);

    @Override
    public void convertParquetToCSV(File parquetFile, File csvOutputFile) throws IOException {
        Preconditions.checkArgument(parquetFile.getName().endsWith(".parquet"),
            "parquet file should have .parquet extension");
        Preconditions.checkArgument(csvOutputFile.getName().endsWith(".csv"),
            "csv file should have .csv extension");
        Preconditions.checkArgument(!csvOutputFile.exists(),
            "Output file " + csvOutputFile.getAbsolutePath() + " already exists");

        LOG.info("Converting {} to {}", parquetFile.getName(), csvOutputFile.getName());

        try (InputStream inputStream = new FileInputStream(parquetFile);
            CSVWriter csvWriter = new CSVWriter(new FileWriter(csvOutputFile),
                CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            ParquetStream parquetStream = new ParquetStream(inputStream);
            ParquetReader<GenericRecord> r = AvroParquetReader
                .<GenericRecord>builder(parquetStream)
                .disableCompatibility()
                .build();

            int counter = 0;
            int size = 0;
            GenericRecord record;
            while ((record = r.read()) != null) {

                if (counter == 0 ) {
                    size = record.getSchema().getFields().size();
                    List<String> headers = record.getSchema().getFields().stream()
                        .map(Schema.Field::name)
                        .collect(Collectors.toList());
                    csvWriter.writeNext(headers.toArray(String[]::new), false);
                    counter++;
                } else {
                    String[] columns = new String[size];
                    for (int i = 0; i < size; i++) {
                        columns[i] = record.get(i).toString();

                    }
                    csvWriter.writeNext(columns, false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
