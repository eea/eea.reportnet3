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
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.model.ParquetStream;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.eea.utils.LiteralConstants.*;

@Service
public class S3ConvertServiceImpl implements S3ConvertService {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConvertServiceImpl.class);

    private final S3Helper s3Helper;
    private final SpatialDataHandling spatialDataHandling;

    public S3ConvertServiceImpl(SpatialDataHandling spatialDataHandling, S3Helper s3Helper) {
        this.s3Helper = s3Helper;
        this.spatialDataHandling = spatialDataHandling;
    }

    /**  The path export DL */
    @Value("${exportDLPath}")
    private String exportDLPath;

    @Override
    public void convertParquetToCSVinZIP(File csvFile, String tableName, ZipOutputStream out) {
        try (FileInputStream fis = new FileInputStream(csvFile)) {
            // Adding the csv file to the zip
            ZipEntry e = new ZipEntry(tableName + CSV_TYPE);
            out.putNextEntry(e);
            int length;
            byte[] buffer = new byte[1024];

            while ((length = fis.read(buffer)) != -1) {
                out.write(buffer,0, length);
            }
            out.closeEntry();
        } catch (Exception e) {
            LOG.error("Error in convert method for csvOutputFile {} and tableName {}", csvFile, tableName, e);
        }
    }

    @Override
    public File createCSVFile(List<S3Object> exportFilenames, String tableName, Long datasetId) {
        File csvFile = new File(new File(exportDLPath, "dataset-" + datasetId), tableName + CSV_TYPE);
        LOG.info("Creating file for export: {}", csvFile);

        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile),
            CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            convertParquetToCSV(exportFilenames, tableName, datasetId, csvWriter);
        } catch (Exception e) {
            LOG.error("Error in convert method for csvOutputFile {} and tableName {}", csvFile, tableName, e);
        }
        return csvFile;
    }

    @Override
    public File createEmptyCSVFile(String tableName, Long datasetId, List<String> headers) {
        File csvFile = new File(new File(exportDLPath, "dataset-" + datasetId), tableName + CSV_TYPE);
        LOG.info("Creating file for export: {}", csvFile);

        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile),
            CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvWriter.writeNext(headers.toArray(String[]::new), false);
        } catch (Exception e) {
            LOG.error("Error in convert method for csvOutputFile {} and tableName {}", csvFile, tableName, e);
        }
        return csvFile;
    }

    @Override
    public File createJsonFile(List<S3Object> exportFilenames, String tableName, Long datasetId) {
        File jsonFile = new File(new File(exportDLPath, "dataset-" + datasetId), tableName + JSON_TYPE);
        LOG.info("Creating file for export: {}", jsonFile);

        try (FileWriter fw = new FileWriter(jsonFile);
            BufferedWriter bw = new BufferedWriter(fw)) {
            convertParquetToJSON(exportFilenames, tableName, datasetId, bw);
        } catch (Exception e) {
            LOG.error("Error in convert method for jsonOutputFile {} and tableName {}", jsonFile, tableName, e);
        }
        return jsonFile;
    }

    private void convertParquetToCSV(List<S3Object> exportFilenames, String tableName, Long datasetId,
                                     CSVWriter csvWriter) throws IOException {
        int counter = 0;
        for (int i = 0; i < exportFilenames.size(); i++) {
            File parquetFile = s3Helper.getFileFromS3Export(exportFilenames.get(i).key(), tableName, exportDLPath, PARQUET_TYPE, datasetId);
            try (InputStream inputStream = new FileInputStream(parquetFile);
                ParquetReader<GenericRecord> r = AvroParquetReader.<GenericRecord>builder(new ParquetStream(inputStream)).disableCompatibility().build()) {
                GenericRecord record;

                while ((record = r.read()) != null) {
                    long size = record.getSchema().getFields().stream().map(Schema.Field::name).filter(t -> !t.equals("dir0")).count();
                    if (i == 0 && counter == 0) {
                      csvWriter.writeNext(record.getSchema().getFields().stream()
                          .map(Schema.Field::name).filter(t -> !t.equals("dir0")).toArray(String[]::new), false);
                        counter++;
                    }
                    String[] columns = new String[(int) size];
                    int index = 0;
                    var fields_2 = record.getSchema().getFields().stream().filter( t -> !t.name().equals("dir0")).collect(Collectors.toList());
                    for (Schema.Field field : fields_2) {
                        Object fieldValue = record.get(field.name());
                        if (fieldValue instanceof ByteBuffer) {
                            ByteBuffer byteBuffer = (ByteBuffer) fieldValue;
                            String modifiedJson = spatialDataHandling.decodeSpatialData(byteBuffer.array());
                            columns[index] = modifiedJson;
                        } else {
                            columns[index] = (fieldValue != null) ? fieldValue.toString() : "";
                        }
                        index++;
                    }
                    csvWriter.writeNext(columns, false);
                }
            } catch (ParseException e) {
                LOG.error("Invalid GeoJson!! Tried to decode from binary but failed", e);
            }
        }
    }

    @Override
    public void convertParquetToJSON(List<S3Object> exportFilenames, String tableName, Long datasetId, BufferedWriter bufferedWriter) {

        try {
            bufferedWriter.write("{\"tables\":[{\"records\":[");
            List<String> headers = new ArrayList<>();
            int headersSize = 0;
            int counter = 0;

            for (int j = 0; j < exportFilenames.size(); j++) {
                File parquetFile =
                    s3Helper.getFileFromS3Export(exportFilenames.get(j).key(), tableName,
                        exportDLPath, PARQUET_TYPE, datasetId);
                try (InputStream inputStream = new FileInputStream(parquetFile);
                     ParquetReader<GenericRecord> r = AvroParquetReader.<GenericRecord>builder(new ParquetStream(inputStream)).disableCompatibility().build()) {
                    GenericRecord record;
                    Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                    while ((record = r.read()) != null) {
                        if (counter == 0) {
                            headers = record.getSchema().getFields().stream().map(Schema.Field::name).filter(t -> !t.equals("dir0")).collect(Collectors.toList());
                            headersSize = headers.size();
                            bufferedWriter.write("{");
                            counter++;
                        } else {
                            bufferedWriter.write(",{");
                        }
                        int index = 0;
                        for (Schema.Field field : record.getSchema().getFields()) {
                            var containsDir0 = field.name().equals("dir0");
                            if (containsDir0) {
                                continue;
                            }

                            String recordValue = record.get(index).toString();
                            boolean isNumeric = pattern.matcher(recordValue).matches();
                            bufferedWriter.write("\"" + headers.get(index) + "\":");
                            if (isNumeric) {
                                bufferedWriter.write(recordValue);
                            } else {
                                Object fieldValue = record.get(index);
                                if (fieldValue instanceof ByteBuffer) {
                                    ByteBuffer byteBuffer = (ByteBuffer) fieldValue;
                                    String modifiedJson = spatialDataHandling.decodeSpatialData(byteBuffer.array());
                                    bufferedWriter.write(modifiedJson);
                                }else {
                                    bufferedWriter.write("\"" + recordValue + "\"");
                                }
                            }
                            if (index < headersSize - 1) {
                                bufferedWriter.write(",");
                            }
                            index++;
                        }
                        bufferedWriter.write("}");
                    }
                }
            }
            bufferedWriter.write("],\"tableName\":\"");
            bufferedWriter.write(tableName);
            bufferedWriter.write("\"}]}");
        } catch (Exception e) {
            LOG.error("Error in convert method for tableName {}", tableName, e);
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
