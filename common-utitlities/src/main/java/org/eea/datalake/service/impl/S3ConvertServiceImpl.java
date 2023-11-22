package org.eea.datalake.service.impl;

import com.opencsv.CSVWriter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.utils.LiteralConstants;
import org.json.JSONWriter;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.eea.utils.LiteralConstants.*;

@Service
public class S3ConvertServiceImpl implements S3ConvertService {

    private static final Logger LOG = LoggerFactory.getLogger(S3ConvertServiceImpl.class);

    /** The big data dataset service */
    @Autowired
    private S3Helper s3Helper;

    /**  The path export DL */
    @Value("${exportDLPath}")
    private String exportDLPath;

    @Override
    public void convertParquetToCSV(List<S3Object> exportFilenames, String tableName, Long datasetId) {
        createCSVFile(exportFilenames, tableName, datasetId);
    }

    @Override
    public void convertParquetToJSON(List<S3Object> exportFilenames, String tableName, Long datasetId) {
        createJsonFile(exportFilenames, tableName, datasetId);
    }

    @Override
    public void convertParquetToCSVinZIP(List<S3Object> exportFilenames, String tableName, Long datasetId, ZipOutputStream out) {
        File csvFile = createCSVFile(exportFilenames, tableName, datasetId);

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

    private File createCSVFile(List<S3Object> exportFilenames, String tableName, Long datasetId) {
        File csvFile = new File(new File(exportDLPath, "dataset-" + datasetId), tableName + CSV_TYPE);
        LOG.info("Creating file for export: {}", csvFile);

        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile),
            CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvConvertionFromParquet(exportFilenames, tableName, datasetId, csvWriter);

        } catch (Exception e) {
            LOG.error("Error in convert method for csvOutputFile {} and tableName {}", csvFile, tableName, e);
        }
        return csvFile;
    }

    private File createJsonFile(List<S3Object> exportFilenames, String tableName, Long datasetId) {
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

    private void csvConvertionFromParquet(List<S3Object> exportFilenames, String tableName, Long datasetId,
        CSVWriter csvWriter) throws IOException {
        int size = 0;
        int counter = 0;
        for (int i = 0; i < exportFilenames.size(); i++) {
            File parquetFile = s3Helper.getFileFromS3Export(exportFilenames.get(i).key(), tableName, exportDLPath, PARQUET_TYPE,
                datasetId);
            InputStream inputStream = new FileInputStream(parquetFile);
            ParquetStream parquetStream = new ParquetStream(inputStream);
            ParquetReader<GenericRecord> r = AvroParquetReader.<GenericRecord>builder(parquetStream).disableCompatibility().build();
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
    }

    @Override
    public void convertParquetToJSON(List<S3Object> exportFilenames, String tableName, Long datasetId, BufferedWriter bufferedWriter) {

        try {
            LOG.info("exportFilenames size {}", exportFilenames.size());
            bufferedWriter.write("{\"records\":[\n");

            for (int j = 0; j < exportFilenames.size(); j++) {
                File parquetFile =
                    s3Helper.getFileFromS3Export(exportFilenames.get(j).key(), tableName,
                        exportDLPath, PARQUET_TYPE, datasetId);
                InputStream inputStream = new FileInputStream(parquetFile);
                ParquetStream parquetStream = new ParquetStream(inputStream);
                ParquetReader<GenericRecord> r =
                    AvroParquetReader.<GenericRecord>builder(parquetStream).disableCompatibility().build();

                Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                GenericRecord record = r.read();
                int size = record.getSchema().getFields().size();
                List<String> headers = new ArrayList<>();

                while ((record = r.read()) != null) {
                    if (j == 0) {
                        headers = record.getSchema().getFields().stream().map(Schema.Field::name).collect(Collectors.toList());
                        bufferedWriter.write("{");
                    } else {
                        bufferedWriter.write(",\n{");
                    }
                    for (int i = 0; i < size; i++) {
                        String recordValue = record.get(i).toString();
                        boolean isNumeric = pattern.matcher(recordValue).matches();
                        bufferedWriter.write("\"" + headers.get(i) + "\":");
                        if (isNumeric) {
                            bufferedWriter.write(recordValue);
                        } else {
                            bufferedWriter.write("\"" + recordValue + "\"");
                        }
                        if (i < size - 1) {
                            bufferedWriter.write(",");
                        }
                    }
                    bufferedWriter.write("}");
                }
            }
            bufferedWriter.write("\n]}");
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
