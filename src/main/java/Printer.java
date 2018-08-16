import javafx.concurrent.Task;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;


public class Printer {


    public static void printFile(PDDocument file, String jobName) {
        print(file, jobName);
    }

    public static void printFiles(List<PDDocument> toPrint, List<String> name) {
        printMutliple(toPrint, name);
    }

    private static void printMutliple(List<PDDocument> toPrint, List<String> names) {
        //Create new Task
        if(toPrint.isEmpty()) {
            return;
        }

        Task task = new Task<Boolean>() {
            @Override
            public Boolean call() throws PrinterException {

                //Load PDF & create a Printer Job
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(toPrint.get(0)));
                if(!names.isEmpty()) {
                    job.setJobName(names.get(0));
                    names.remove(0);
                }

                //Show native print dialog & wait for user to hit "print"
                if (job.printDialog()) {
                    job.print();
                }

                toPrint.remove(0);

                return true;
            }

            @Override
            protected void succeeded() {
                printMutliple(toPrint, names);
            }
        };
        //Run task on new thread
        new Thread(task).start();
    }

    private static void print(PDDocument document, String name) {

        //Create new Task
        Task task = new Task<Boolean>() {
            @Override
            public Boolean call() throws PrinterException {

                //Load PDF & create a Printer Job
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(document));
                job.setJobName(name);

                //Show native print dialog & wait for user to hit "print"
                if (job.printDialog()) {
                    job.print();
                }

                return true;
            }
        };
        //Run task on new thread
        new Thread(task).start();
    }
}
