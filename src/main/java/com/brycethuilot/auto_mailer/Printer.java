package com.brycethuilot.auto_mailer;

import javafx.concurrent.Task;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;


/**
 * static class created to print PDDocuments using PDFBox's printing classes
 * Uses the system printing dialog
 *
 * @author Bryce Thuilot
 * @version %I%, %G%
 * @since 1.0
 */
public class Printer {


    /**
     * Prints a given PDDocument with a given jobName
     * @param file PDDocuemnt to print
     * @param jobName Name to give printing job
     */
    static void printFile(PDDocument file, String jobName) {
        print(file, jobName);
    }

    /**
     *  Prints multiple PDDocument with multiple names, (i.e. toPrint.get(0) is printed with name.get(0) job name)
     * @param toPrint List of PDDocuments to print
     * @param name list of names to given jobs
     */
    static void printFiles(List<PDDocument> toPrint, List<String> name) {
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

                //Load PDF & create a com.brycethuilot.auto_mailer.Printer Job
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

    /**
     * Prints the given PDDoucment with the given job name
     * @param document PDDocument to print
     * @param name name to given job
     */
    private static void print(PDDocument document, String name) {

        //Create new Task
        Task task = new Task<Boolean>() {
            @Override
            public Boolean call() throws PrinterException {

                //Load PDF & create a com.brycethuilot.auto_mailer.Printer Job
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
