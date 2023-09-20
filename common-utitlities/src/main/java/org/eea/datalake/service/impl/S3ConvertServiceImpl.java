package org.eea.datalake.service.impl;

import com.opencsv.CSVWriter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.Preconditions;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.eea.datalake.service.S3ConvertService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.model.ParquetStream;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

@Service
public class S3ConvertServiceImpl implements S3ConvertService {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConvertServiceImpl.class);

    /** The big data dataset service */
    @Autowired
    private S3Helper s3Helper;

    /**
     * The path export DL.
     */
    @Value("${exportDLPath}")
    private String exportDLPath;

    @Override
    public void convertParquetToCSV(S3PathResolver s3PathResolver, String nameDataset) {

        File csvFile = new File(exportDLPath + nameDataset + CSV_TYPE);

        try (CSVWriter csvWriter =
            new CSVWriter(new FileWriter(csvFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            List<S3Object> exportFilenames = s3Helper.getFilenamesFromTableNames(s3PathResolver);
            LOG.info("Exported dataset data for S3PathResolver {} with exportFilenames {}", s3PathResolver, exportFilenames);
            int size = 0;
            int counter = 0;

            for (int i = 0; i < exportFilenames.size(); i++) {
                File parquetFile = s3Helper.getFileFromS3(exportFilenames.get(i).key(), nameDataset, exportDLPath, LiteralConstants.PARQUET_TYPE);
                InputStream inputStream = new FileInputStream(parquetFile);
                ParquetStream parquetStream = new ParquetStream(inputStream);
                ParquetReader<GenericRecord> r =
                    AvroParquetReader.<GenericRecord>builder(parquetStream).disableCompatibility().build();

                GenericRecord record;
                while ((record = r.read()) != null) {
                    if (i == 0 && counter == 0) {
                        size = record.getSchema().getFields().size();
                        List<String> headers =
                            record.getSchema().getFields().stream().map(Schema.Field::name).collect(Collectors.toList());
                        csvWriter.writeNext(headers.toArray(String[]::new), false);
                        counter++;
                    }
                    String[] columns = new String[size];
                    for (int j = 0; j < size; j++) {
                        columns[j] = record.get(j).toString();
                    }
                    csvWriter.writeNext(columns, false);
                }
            }
        } catch (Exception e) {
            LOG.error("Error in convert method for csvOutputFile {}, s3PathResolver {} and nameDataset {}", csvFile, s3PathResolver, nameDataset);
        }
    }

    @Override
    public void convertParquetToJSON(File parquetFile, File jsonOutputFile) {
        validateFileFormat(parquetFile, jsonOutputFile, JSON_TYPE);

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
            do {
                if (counter == 0) {
                    bw.write("{");
                    counter++;
                } else {
                    bw.write(",\n{");
                }
                for (int i = 0; i < size; i++) {
                    String recordValue = record.get(i).toString();
                    boolean isNumeric = pattern.matcher(recordValue).matches();
                    bw.write("\"" + headers.get(i) + "\":");
                    if (isNumeric) {
                        bw.write(recordValue);
                    } else {
                        bw.write("\"" + recordValue + "\"");
                    }
                    if (i < size - 1) {
                        bw.write(",");
                    }
                }
                bw.write("}");
            } while ((record = r.read()) == null);

            bw.write("\n]}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertParquetToXML(File parquetFile, File xmlOutputFile) {
        validateFileFormat(parquetFile, xmlOutputFile, XML_TYPE);

        try (InputStream inputStream = new FileInputStream(parquetFile);
            FileWriter fw = new FileWriter(xmlOutputFile, true);
            BufferedWriter bw = new BufferedWriter(fw)) {

            ParquetStream parquetStream = new ParquetStream(inputStream);
            ParquetReader<GenericRecord> r = AvroParquetReader
                .<GenericRecord>builder(parquetStream)
                .disableCompatibility()
                .build();

            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            bw.write("<Records>\n");
            GenericRecord record = r.read();

            List<String> headers = new ArrayList<>();
            int size = record.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                headers.add(record.get(i).toString());
            }

            do {
                bw.write("<Record>");
                for (int i = 0; i < size; i++) {
                    String recordValue = record.get(i).toString();
                    bw.write("<" + headers.get(i) + ">");
                    bw.write(recordValue);
                    bw.write("</" + headers.get(i) + ">");
                }
                bw.write("</Record>\n");
            } while ((record = r.read()) == null);

            bw.write("</Records>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertParquetToXLSX(File parquetFile, File excelOutputFile) {
        validateFileFormat(parquetFile, excelOutputFile, XLSX_TYPE);

        try (InputStream inputStream = new FileInputStream(parquetFile)) {
            ParquetStream parquetStream = new ParquetStream(inputStream);
            ParquetReader<GenericRecord> r = AvroParquetReader
                .<GenericRecord>builder(parquetStream)
                .disableCompatibility()
                .build();

            SXSSFWorkbook workbook = new SXSSFWorkbook(1);
            Sheet sheet = workbook.createSheet();

            int counter = 0;
            Row row = sheet.createRow(counter++);
            GenericRecord record = r.read();

            int size = record.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                row.createCell(i).setCellValue(record.get(i).toString());
            }

            while ((record = r.read()) != null) {
                LOG.info("record: {}", record.toString());
                row = sheet.createRow(counter++);
                for (int i = 0; i < size; i++) {
                    LOG.info("record.get(i).toString(): {}",record.get(i).toString());
                    row.createCell(i).setCellValue(record.get(i).toString());
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(excelOutputFile)) {
                workbook.write(outputStream);
            }
            workbook.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void validateFileFormat(File parquetFile, File csvOutputFile, String type) {
        Preconditions.checkArgument(parquetFile.getName().endsWith(PARQUET_TYPE),
            "parquet file should have .parquet extension");
        Preconditions.checkArgument(csvOutputFile.getName().endsWith(type),
            "csv file should have .csv extension");

        LOG.info("Converting {} to {}", parquetFile.getName(), csvOutputFile.getName());
    }
}
