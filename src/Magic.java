import java.io.File;
import java.util.Scanner;

public class Magic {
    private static final String CALL_SAMPLE = "data/CallSample.csv";
    private static final String PRICE_SAMPLE = "data/PriceSampleData.csv";

    public static void main(String[] args){
        try {
            Scanner callSampleScanner = new Scanner(new File(CALL_SAMPLE));
            callSampleScanner.useDelimiter(",");
            Scanner priceSampleScanner = new Scanner(new File(PRICE_SAMPLE));
            priceSampleScanner.useDelimiter(",");

            int rows = getNumRows(new Scanner(new File(CALL_SAMPLE)));
            String[][] callSample = loadData(callSampleScanner, rows);
            rows = getNumRows(new Scanner(new File(PRICE_SAMPLE)));
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

    private static int getNumRows(Scanner scanner) {
        int count = 0;
        while(scanner.hasNextLine()){
            count++;
            scanner.nextLine();
        }
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
