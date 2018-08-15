import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
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
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));

        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        attr.add(new JobName("Letter to insured", new Locale("en")));

        PDViewerPreferences vp = document.getDocumentCatalog().getViewerPreferences();
        if (vp != null && vp.getDuplex() != null)
        {
            String dp = vp.getDuplex();
            if (PDViewerPreferences.DUPLEX.DuplexFlipLongEdge.toString().equals(dp))
            {
                attr.add(Sides.TWO_SIDED_LONG_EDGE);
            }
            else if (PDViewerPreferences.DUPLEX.DuplexFlipShortEdge.toString().equals(dp))
            {
                attr.add(Sides.TWO_SIDED_SHORT_EDGE);
            }
            else if (PDViewerPreferences.DUPLEX.Simplex.toString().equals(dp))
            {
                attr.add(Sides.ONE_SIDED);
            }
        }

        if (job.printDialog(attr))
        {
            job.print(attr);
        }
    }
}
