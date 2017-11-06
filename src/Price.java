
public class Price {

    int date;
    double volatility;
    double strike;
    String company;

    Price(String[] data){
        date = Integer.parseInt(data[1]);
        volatility = Double.parseDouble(data[3]);
        strike = Double.parseDouble(data[4]);
        company = data[6];
    }
}
