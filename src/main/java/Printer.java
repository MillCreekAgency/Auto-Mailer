import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;


public class Printer {

    public static void printFile(File file) {
        try {
            try {
                System.out.println(file.getAbsolutePath());
                PDDocument document = PDDocument.load(file);
                printFile(document);
            }catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException passwordEx) {
                System.out.println(passwordEx.getLocalizedMessage());
            }
        }catch (IOException ioex) {
            System.out.println(ioex.getMessage());
            System.out.println("HERE");
            //TODO fix "ERROR header doesnt contain version info"
        }
    }

    public static void printFile(PDDocument file) {
       try {
           print(file);
       }catch(PrinterException printerEx) {
           System.out.println(printerEx.getMessage());
       }
    }

    private static void print(PDDocument document) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        if (job.printDialog())
        {
            job.print();
        }
    }
}
