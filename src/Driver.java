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
    public static final double CALL_PUT_MULTIPLIER = 0.95;

    public static void main(String[] args){
        Scanner calls;
        List<String[]> prices;

        /*
        Setup the scanner for calls/puts and create the table of prices.
         */
        try {
            calls = new Scanner(new File(CALL_FILE)).useDelimiter("[\\n,]");
            CSVReader reader = new CSVReaderBuilder(new FileReader(PRICE_FILE)).withCSVParser(new RFC4180Parser()).build();
            prices = reader.readAll();
        } catch (IOException e) {
            System.out.println("Couldn't find files");
            throw new RuntimeException(e.getMessage());
        }

        calls.nextLine(); //Skip call/put headers
        String[] companies = prices.get(0); //Get the top row of the prices file, which contains the companies
        String[][] callVolatilities = new String[prices.size()][companies.length]; //Create the array which will be written to the output file.

        /*
        Add the first row or the prices table (the companies) to the output array
         */
        System.arraycopy(companies, 0, callVolatilities[0], 0, companies.length);
        for(int i = 0; i < prices.size(); i++){
            callVolatilities[i][0] = prices.get(i)[0];
        }

        Call current = null;
        int currentRow = 0;

        /*
        Get to the first existing call/put entry.
         */
        while(current == null){
            current = nextCall(calls);
        }

        loop:
        for(int i = 1; i < companies.length; i++){ //For every company...
            currentRow = getCurrentRow(1, current, prices); //Find out which row of the output we're on
            String currentCompany = current.company; //Get the company of the current entry
            if(!prices.get(0)[i].equals(currentCompany)) continue; //If the company we're on doesn't match the entry's, skip to the next company. This happens when an entire company has no valid entries.
            while(current.company.equals(currentCompany)){ //While the next entry is still one from the same company we're on...
                int currentDate = current.date;
                double min = -1;
                double minVol = 0;
                String priceString;
                /*
                Search for a valid entry that has a matching valid price associated with it.
                 */
                if((priceString = prices.get(currentRow)[i]).equals("")){
                    do {
                        current = nextCall(calls);
                    } while(current == null);
                    currentRow = getCurrentRow(currentRow, current, prices);
                    continue;
                }
                double price = Double.parseDouble(priceString)* CALL_PUT_MULTIPLIER;
                /*
                Look through every entry with the same date and keep track of the minimum strike entry and its associated volatility.
                 */
                while (current.date == currentDate) { //Potential bug: If one company's last entry is on the same date as the next company's first entry, this loop will treat those separate sections as one.
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
                callVolatilities[currentRow][i] = min == -1 ? "" : minVol+""; //We've reached the end of the section, so record the volatility in the output array.
                currentRow = getCurrentRow(currentRow, current, prices); //Get ready to look at the next entry.
                if(currentRow == -1) //This occurs when we hit a new company. The loop breaks out of the day-by-day cycle to the next company.
                    break;
            }
        }

        /*
        Write the output array to a file.
         */
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
