import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class OceanHarbor extends Policy {

    public OceanHarbor(String fileLocation, boolean letter, boolean emailOnly) {
        super(fileLocation, letter, emailOnly);
    }

    @Override
    public void getInfoFromPolicy(String fileLocation) {
        PDFTextStripper pdfStripper = null;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        File policy = new File(fileLocation);
        try {
            RandomAccessRead randomAccessFile = new RandomAccessFile(policy, "r");
            PDFParser parser = new PDFParser(randomAccessFile);
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(5);
            String parsedText = pdfStripper.getText(pdDoc);
            System.out.println(parsedText);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Implement
    }

    @Override
    public String printCoverages() {
        return null;
    }

    @Override
    public int[] findCoverages(String policy) {
        return new int[0];
    }
}
