import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Locale;


public class Printer {


    public static void printFile(PDDocument file) {
       try {
           print(file);
       }catch(PrinterException printerEx) {
           System.out.println(printerEx.getMessage());
       }
    }

    private static void print(PDDocument document) throws PrinterException {
        System.out.println("Help");
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));

        if (job.printDialog())
        {
            job.print();
        }
    }
}
