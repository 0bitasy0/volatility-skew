import com.opencsv.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Driver {
    public static final String DATA_PUT_OUTPUT_CSV = "data/PutOutput.csv";
    private static final String CALL_FILE = "data/putsdata.csv";
    private static final String PRICE_FILE = "data/pricedata.csv";

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

        loop:
        for(int i = 1; i < companies.length; i++){
            currentRow = getCurrentRow(1, current, prices);
            String currentCompany = current.company;
            if(!prices.get(0)[i].equals(currentCompany)) continue;
            while(current.company.equals(currentCompany)){
                int currentDate = current.date;
                double min = -1;
                double minVol = 0;
                String priceString;
                if((priceString = prices.get(currentRow)[i]).equals("")){
                    do {
                        current = nextCall(calls);
                    } while(current == null);
                    currentRow = getCurrentRow(currentRow, current, prices);
                    continue;
                }
                double price = Double.parseDouble(priceString)*0.95;
                while (current.date == currentDate) {
                    double diff = Math.abs(price - current.strike);
                    if(min == -1 || diff < min){
                        min = diff;
                        minVol = current.volatility;
                    }
                    do {
                        if(!calls.hasNext()) break loop;
                        current = nextCall(calls);
                    } while(current == null);
                }
                callVolatilities[currentRow][i] = min == -1 ? "" : minVol+"";
                currentRow = getCurrentRow(currentRow, current, prices);
                if(currentRow == -1)
                    break;
            }
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(DATA_PUT_OUTPUT_CSV));
            for(String[] row : callVolatilities)
                writer.writeNext(row);
            writer.close();
        } catch (IOException e) {
            System.out.println("Failure");
            e.printStackTrace();
        }
    }

    private static int getCurrentRow(int start, Call call, List<String[]> prices){
        int row = start;

        while(Integer.parseInt(prices.get(row)[0]) != call.date){
            row++;
            if(row >= prices.size()) return -1;
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
