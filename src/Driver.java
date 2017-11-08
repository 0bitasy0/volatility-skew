import com.opencsv.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Driver {
    private static final String CALL_FILE = "data/calldata.csv";
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

        Call current = null;
        int currentRow = 0;

        while(current == null){
            current = nextCall(calls);
        }
        currentRow = getCurrentRow(0, current, prices);
        String currentCompany = current.company;

        for(int i = 1; i < companies.length; i++){
            while(current.company.equals(currentCompany)){
                int currentDate = current.date;
                double min = -1;
                double minVol = 0;
                while (current.date == currentDate) {
                    double diff = Math.abs(Double.parseDouble(prices.get(currentRow)[i]) - current.strike);
                    if(min == -1 || diff < min){
                        min = diff;
                        minVol = current.volatility;
                    }
                    do {
                        current = nextCall(calls);
                    } while(current == null);
                }
                callVolatilities[currentRow][i] = min == -1 ? "" : minVol+"";
                currentRow = getCurrentRow(currentRow, current, prices);
            }
        }

        //optimizedLoops(calls, prices, companies, callVolatilities);
    }

    private static int getCurrentRow(int start, Call call, List<String[]> prices){
        int row = start;
        while(Integer.parseInt(prices.get(row)[0]) != call.date){
            row++;
        }
        return row;
    }

    private static Call nextCall(Scanner scanner){
        try {
            return Call.get(new String[]{
                    scanner.next(),//secid
                    scanner.next(),//date
                    scanner.next(),//days
                    scanner.next(),//volatility
                    scanner.next(),//strike
                    scanner.next(),//flag
                    scanner.next(),//company
                    scanner.next()//index
            });
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
