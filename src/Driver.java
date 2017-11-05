import com.opencsv.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Driver {
    private static final String CALL_FILE = "data/CallSample.csv";
    private static final String PRICE_FILE = "data/PriceSampleData.csv";
    //private static final String PRICE_FILE = "data/PRICEDATASP500.csv";

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
        calls.next(); //Skip secid
        int currentDate = Integer.parseInt(calls.next());
        int currentYear = currentDate/10000;
        int date = currentDate;
        int rowBeforeYear = 1;
        int currentRow;
        year:
        while(calls.hasNext()) {
            company:
            for(int j = 1; j < numCompanies; j++){
                String currentCompany = prices.get(0)[j];//TODO: Add checker to make sure next company has any entries in a given year (otherwise skip), make sure no data is entered for callVolatilities if min is -1 at the end (currently puts 0 inside).
                String company = currentCompany;
                currentRow = rowBeforeYear;
                    while(company.equals(currentCompany)) {
                    double min = -1;
                    double minVol = 0;
                    while (currentDate == date) {
                        calls.next(); //Skip days
                        double volatility = calls.nextDouble();
                        double strike = calls.nextDouble();
                        double diff = Math.abs(Double.parseDouble(prices.get(currentRow)[j]) - strike);
                        if(min == -1 && diff < min){
                            min = diff;
                            minVol = volatility;
                        }
                        calls.next(); //Skip flag
                        company = calls.next();
                        calls.next(); //Skip index
                        calls.next(); //Skip secid
                        date = calls.nextInt();
                    }
                    callVolatilities[currentRow][j] = minVol+"";
                    if (date / 10000 != currentYear) {
                        rowBeforeYear = currentRow;
                        currentYear = date / 10000;
                        while(Integer.parseInt(prices.get(currentRow)[0]) != date) currentRow++;
                        continue year;
                    } else if (!company.equals(currentCompany)) {
                        continue company;
                    }
                    while(Integer.parseInt(prices.get(currentRow)[0]) != date) currentRow++;

                }
            }
        }


    }

    private static void loopingStrategy() {
        try {
            Scanner callSampleScanner = new Scanner(new File(CALL_FILE));
            callSampleScanner.useDelimiter(",");
            Scanner priceSampleScanner = new Scanner(new File(PRICE_FILE));
            priceSampleScanner.useDelimiter(",");

            int rows = getNumRows(new File(CALL_FILE));
            String[][] callSample = loadData(callSampleScanner, rows);
            rows = getNumRows(new File(PRICE_FILE));
            String[][] priceSampleData = loadData(priceSampleScanner, rows);

            double[][] answers = new double[priceSampleData.length][priceSampleData[0].length];

            for(int i = 1; i < priceSampleData.length; i++){
                for(int j = 1; j < priceSampleData[0].length; j++){
                    double min = -1;
                    double volatility = -1;
                    for (String[] aCallSample : callSample) {
                        if (priceSampleData[i][0].equals(aCallSample[1]) && priceSampleData[0][j].equals(aCallSample[6])) {
                            double diff = Math.abs(Double.parseDouble(priceSampleData[i][j]) - Double.parseDouble(aCallSample[4]));
                            if (min == -1 || diff < min) {
                                min = diff;
                                volatility = Double.parseDouble(aCallSample[3]);
                            }
                        }
                    }
                    answers[i][j] =volatility;
                }
            }

            System.out.println(answers[0]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getNumRows(File file) {
        int count = 0;
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file");
            throw new RuntimeException(e.getMessage());
        }
        while(scanner.hasNextLine()){
            count++;
            scanner.nextLine();
        }
        scanner.close();
        return count;
    }

    private static String[][] loadData(Scanner scanner, int numRows) {
        String firstLine = scanner.nextLine();
        String[] firstLineArray = split(firstLine);
        String[][] ret = new String[numRows][firstLineArray.length];
        System.arraycopy(firstLineArray, 0, ret[0], 0, ret[0].length);
        for(int i = 1; i < numRows; i++){
            String line = scanner.nextLine();
            String[] lineArray = new String[firstLineArray.length];
            String[] split = split(line);
            System.arraycopy(split, 0, lineArray, 0, lineArray.length);
            System.arraycopy(lineArray, 0, ret[i], 0, ret[0].length);
        }

        return ret;
    }

    private static String[] split(String string){
        int count = string.length() - string.replace(",", "").length();
        String[] ret = new String[count+1];
        int index = 0;
        StringBuilder sb = new StringBuilder();
        for(char c : string.toCharArray()){
            if(c == ','){
                if(sb.toString().equals("")) sb.append("-1");
                ret[index] = sb.toString();
                sb = new StringBuilder();
                index++;
                continue;
            }
            sb.append(c);
        }
        if(ret[ret.length-1] == null) sb.append("-1");
        ret[index] = sb.toString();
        return ret;
    }



}
