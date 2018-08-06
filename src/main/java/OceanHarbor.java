import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OceanHarbor extends Policy {

    public OceanHarbor(File policyFile, boolean updateInQQ, boolean printMortgage, boolean mailToInsured, ApplicationWindow applicationWindow) {
        super(policyFile, updateInQQ, printMortgage, mailToInsured, applicationWindow);
    }

    @Override
    public String getOldPolicyNum(String currentPolicyNum) {
        String endingNumber = currentPolicyNum.substring(currentPolicyNum.length() - 2);
        endingNumber = (Integer.parseInt(endingNumber) - 1) + "";
        if(endingNumber.length() == 1) {
            endingNumber  = "0" + endingNumber;
        }
        return currentPolicyNum.substring(0,currentPolicyNum.length() - 2) + endingNumber;
    }

    @Override
    public void printMortgagee() throws IOException {
        PDDocument doc = PDDocument.load(this.policyFile);

        Splitter splitter = new Splitter();
        splitter.setStartPage(1);
        splitter.setEndPage(4);
        List<PDDocument> pages = splitter.split(doc);

        PDFMergerUtility merger = new PDFMergerUtility();

        PDDocument mortgagePDF = new PDDocument();
        for(PDDocument page : pages) {
             merger.appendDocument(mortgagePDF, page);
        }

        mortgagePDF.save(PRINT_FOLDER + this.policyNumber +"_Mortgage.pdf");

    }

    @Override
    public void getInfoFromPolicy() throws IOException {
        String pdfText = getPDFText(this.policyFile);
        String nameAndAddress = pdfText.substring(pdfText.indexOf("Insured\n") + 8);
        nameAndAddress = nameAndAddress.substring(0, nameAndAddress.indexOf("OCEAN HARBOR CASUALTY"));

        String[] nameAndAddressArr = nameAndAddress.split("\n", 2);
        this.name = nameAndAddressArr[0];
        this.address = nameAndAddressArr[1];
        this.dwelling = this.isDwelling(pdfText);

        this.policyNumber = pdfText.substring(pdfText.indexOf("POLICY NUMBER") - 13,pdfText.indexOf("POLICY NUMBER") - 1);

        this.coverages = this.dwelling ? this.findDwellingCoverages(pdfText) : this.findHOCoverages(pdfText);

        this.premium = Double.parseDouble(cutSection(pdfText, "Total Policy Premium: ", 8));

        this.deductible = Integer.parseInt(this.cutSection(pdfText, "otherwise\n", 6));

        String hurricaneDed = this.cutSection(pdfText, "Ded.: ", 2);
        try {
            this.hurricaneDeductible = Integer.parseInt(hurricaneDed.replace("%", ""));
        } catch(NumberFormatException ex) {
            this.hurricaneDeductible = 0;
        }


        int dateStartIndex = pdfText.indexOf("FROM TO");
        String[] dates = pdfText.substring(dateStartIndex + 9, pdfText.indexOf("\n", dateStartIndex + 9)).split(" ");
        this.effectiveDate = dates[0];
        this.expirationDate = dates[0];


    }

    private boolean isDwelling(String pdfText) {
        return pdfText.contains("DWELLING PROPERTY POLICY");
    }

    @Override
    public String printCoverages() {
        StringBuilder coverages = new StringBuilder();
        int i = 65;
        for(Integer cssValue : this.coverages.keySet()) {
            coverages.append("Coverage " + (char) (i) + ": $" + this.coverages.get(cssValue));
            i++;
        }
        return coverages.toString();
    }

    public HashMap<Integer, Double> findDwellingCoverages(String policy) {
        HashMap<Integer, Double> coverages = new HashMap<Integer, Double>();
        coverages.put(315, Double.parseDouble(this.cutToFrom(policy, "Dwelling $", "Fire")));
        coverages.put(683, Double.parseDouble(this.cutToFrom(policy, "Other Structure", "  ")));
        coverages.put(753, Double.parseDouble(this.cutToFrom(policy, "Personal Property","  " )));
        coverages.put(576 , Double.parseDouble(this.cutToFrom(policy, "Fair Rental Value", "\n")));
        coverages.put(576,  coverages.get(576) + Double.parseDouble(this.cutToFrom(policy, "Additional Living Expense", "\n")));
        coverages.put(732, Double.parseDouble(this.cutToFrom(policy, "Personal Liability", "\n")));
        coverages.put(602, Double.parseDouble(this.cutToFrom(policy, "Medical Payments to Others", "\n")));
        //coverages.put(728, Double.parseDouble(this.cutToFrom(policy, "Personal Injury",  "\n")));
        return coverages;
    }

    public HashMap<Integer, Double> findHOCoverages(String policy) {
        HashMap<Integer, Double> coverages = new HashMap<>();
        coverages.put(315, Double.parseDouble(this.cutSection(policy, "Building", 12)));
        coverages.put(683, Double.parseDouble(this.cutSection(policy, "Other Structure", 10)));
        coverages.put(753, Double.parseDouble(this.cutSection(policy, "Personal Property", 11)));
        coverages.put(576, Double.parseDouble(this.cutSection(policy, "Loss of Use", 11)));
        coverages.put(732, Double.parseDouble(this.cutSection(policy, "Personal Liability", 8)));
        coverages.put(602, Double.parseDouble(this.cutSection(policy, "Medical Payments to Others", 7)));
        return coverages;
    }
}
