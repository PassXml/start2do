package org.start2do.pdf.util;


import com.lowagie.text.FontFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public final class PDFUtil {

    protected JasperReport jasperReport;

    public PDFUtil(InputStream jrxmlFileInputSteam) {
        System.setProperty("java.awt.headless", "true");
        try {
            this.jasperReport = JasperCompileManager.compileReport(jrxmlFileInputSteam);
        } catch (JRException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void registerFont(String basePath, String fontFile, String fontName, InputStream inputStream) {
        if (basePath == null || basePath.length() < 1) {
            basePath = System.getProperty("java.io.tmpdir");
        }
        String fontPath = basePath + fontFile;
        File file = Paths.get(fontPath).toFile();
        if (!file.exists() && file.length() < 1) {
            try {
                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(fontPath);
                    byte[] buffer = new byte[1024];
                    int n;
                    while (-1 != (n = inputStream.read(buffer))) {
                        outputStream.write(buffer, 0, n);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FontFactory.register(fontPath, fontName);
    }

    public JasperPrint fillReport(Map<String, Object> parameters) {
        try {
            return JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
        } catch (JRException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public JasperPrint fillReport(JRBeanCollectionDataSource datasoure) {
        try {
            return JasperFillManager.fillReport(jasperReport, null, datasoure);
        } catch (JRException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public JasperPrint fillReport(Map<String, Object> parameters, JRBeanCollectionDataSource datasoure) {
        try {
            return JasperFillManager.fillReport(jasperReport, parameters, datasoure);
        } catch (JRException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void exportReportToPdfStream(Map<String, Object> parameters, OutputStream outputStream) {
        exportReportToPdfStream(fillReport(parameters), outputStream);
    }

    public void exportReportToPdfStream(JasperPrint jasperPrint, OutputStream outputStream) {
        try {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> JRBeanCollectionDataSource getJRBeanCollectionDataSource(T... t) {
        return new JRBeanCollectionDataSource(Arrays.asList(t));
    }
}
