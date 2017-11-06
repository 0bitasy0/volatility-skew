import com.opencsv.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Driver {
    private static final String CALL_FILE = "data/855530b3ea9531b8.csv";
    private static final String PRICE_FILE = "data/PRICEDATASP500.csv";

    public static void main(String[] args){
        Scanner calls;
        List<String[]> prices;
        try {
            calls = new Scanner(new File(CALL_FILE)).useDelimiter("[\\n,]");
            CSVReader reader = new CSVReaderBuilder(new FileReader(PRICE_FILE)).withCSVParser(new RFC4180Parser()).build();
            prices = reader.readAll();
        } catch (IOException e) {
            System.out.println("Couldn't find files");
            throw new RuntimeException(e.getMessage());
        }

        calls.nextLine(); //Skip headers
        String[] companies = prices.get(0);
        String[][] callVolatilities = new String[prices.size()][companies.length];

        System.arraycopy(companies, 0, callVolatilities[0], 0, companies.length);
        for(int i = 0; i < prices.size(); i++){
            callVolatilities[i][0] = prices.get(i)[0];
        }

        int numCompanies = companies.length;
        Price current = new Price(args(calls));
        int currentDate = current.date;
        int currentYear = currentDate/10000;
        int rowBeforeYear = 1;
        int currentRow;

        try {
            year:
            while(calls.hasNext()) {
                company:
                for(int j = 1; j < numCompanies; j++){
                    String currentCompany = prices.get(0)[j];//TODO: Add checker to make sure next company has any entries in a given year (otherwise skip), make sure no data is entered for callVolatilities if min is -1 at the end (currently puts 0 inside).
                    if(!currentCompany.equals(current.company)) continue;
                    currentRow = rowBeforeYear;
                    while(Integer.parseInt(prices.get(currentRow)[0]) != current.date) currentRow++;
                    while(current.company.equals(currentCompany)) {
                        double min = -1;
                        double minVol = 0;
                        currentDate = current.date;
                        while (current.date == currentDate) {
                            double diff = Math.abs(Double.parseDouble(prices.get(currentRow)[j]) - current.strike);
                            if(min == -1 || diff < min){
                                min = diff;
                                minVol = current.volatility;
                            }

                            current = new Price(args(calls));
                        }
                        callVolatilities[currentRow][j] = minVol+"";
                        if (current.date / 10000 != currentYear) {
                            rowBeforeYear = currentRow;
                            currentYear = current.date / 10000;
                            while(Integer.parseInt(prices.get(currentRow)[0]) != current.date) currentRow++;
                            continue year;
                        } else if (!current.company.equals(currentCompany)) {
                            continue company;
                        }
                        while(Integer.parseInt(prices.get(currentRow)[0]) != current.date) currentRow++;

                    }
                }
            }
        } finally { //After all rows have been parsed
            try {
                CSVWriter writer = new CSVWriter(new FileWriter("out/sampleCallOutput.csv"));
                for(String[] row : callVolatilities)
                    writer.writeNext(row);
                writer.close();
            } catch (IOException e) {
                System.out.println("Failure");
                e.printStackTrace();
            }
        }
    }

    private static String[] args(Scanner scanner){

        try {
            return new String[]{
                    scanner.next(),//secid
                    scanner.next(),//date
                    scanner.next(),//days
                    scanner.next(),//volatility
                    scanner.next(),//strike
                    scanner.next(),//flag
                    scanner.next(),//company
                    scanner.next()//index
            };
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
