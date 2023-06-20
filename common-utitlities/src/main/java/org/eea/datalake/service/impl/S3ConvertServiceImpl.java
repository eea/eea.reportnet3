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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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

    @Override
    public void convertParquetToJSON(File parquetFile, File jsonOutputFile) {
        Preconditions.checkArgument(parquetFile.getName().endsWith(".parquet"),
            "parquet file should have .parquet extension");
        Preconditions.checkArgument(jsonOutputFile.getName().endsWith(".json"),
            "csv file should have .json extension");
        Preconditions.checkArgument(!jsonOutputFile.exists(),
            "Output file " + jsonOutputFile.getAbsolutePath() + " already exists");

        LOG.info("Converting {} to {}", parquetFile.getName(), jsonOutputFile.getName());

        try (InputStream inputStream = new FileInputStream(parquetFile);
            FileWriter fw = new FileWriter(jsonOutputFile, true);
            BufferedWriter bw = new BufferedWriter(fw)) {

            ParquetStream parquetStream = new ParquetStream(inputStream);
            ParquetReader<GenericRecord> r = AvroParquetReader
                .<GenericRecord>builder(parquetStream)
                .disableCompatibility()
                .build();

            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

            bw.write("{\"records\":[\n");
            GenericRecord record = r.read();
            List<String> headers = new ArrayList<>();
            int size = record.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                headers.add(record.get(i).toString());
            }
            int counter = 0;
            while ((record = r.read()) != null) {
                if (counter == 0 ) {
                    bw.write("{");
                    counter++;
                } else {
                    bw.write(",\n{");
                }
                for (int i = 0; i < size; i++) {
                    String recordValue = record.get(i).toString();
                    boolean isNumeric = pattern.matcher(recordValue).matches();
                    bw.write("\""+headers.get(i)+"\":");
                    if (isNumeric) {
                        bw.write(recordValue);
                    } else {
                        bw.write("\""+recordValue+"\"");
                    }
                    if (i < size - 1) {
                        bw.write(",");
                    }
                }
                bw.write("}");
            }
            bw.write("\n]}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertParquetToXML(File parquetFile, File xmlOutputFile) {
        Preconditions.checkArgument(parquetFile.getName().endsWith(".parquet"),
            "parquet file should have .parquet extension");
        Preconditions.checkArgument(xmlOutputFile.getName().endsWith(".xml"),
            "csv file should have .xml extension");
        Preconditions.checkArgument(!xmlOutputFile.exists(),
            "Output file " + xmlOutputFile.getAbsolutePath() + " already exists");

        LOG.info("Converting {} to {}", parquetFile.getName(), xmlOutputFile.getName());

        try (InputStream inputStream = new FileInputStream(parquetFile);
            FileWriter fw = new FileWriter(xmlOutputFile, true);
            BufferedWriter bw = new BufferedWriter(fw)) {

            ParquetStream parquetStream = new ParquetStream(inputStream);
            ParquetReader<GenericRecord> r = AvroParquetReader
                .<GenericRecord>builder(parquetStream)
                .disableCompatibility()
                .build();

            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            bw.write("<Records>\n");
            GenericRecord record = r.read();

            List<String> headers = new ArrayList<>();
            int size = record.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                headers.add(record.get(i).toString());
            }
            while ((record = r.read()) != null) {
                bw.write("<Record>");
                for (int i = 0; i < size; i++) {
                    String recordValue = record.get(i).toString();
                    bw.write("<"+headers.get(i)+">");
                    bw.write(recordValue);
                    bw.write("</"+headers.get(i)+">");
                }
                bw.write("</Record>\n");
            }
            bw.write("</Records>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
