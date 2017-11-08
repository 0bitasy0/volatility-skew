
public class Call {

    int date;
    double volatility;
    double strike;
    String company;

    static Call get(String[] data){
        try{
            Call call = new Call();
            call.date = Integer.parseInt(data[1]);
            call.volatility = Double.parseDouble(data[3]);
            call.strike = Double.parseDouble(data[4]);
            call.company = data[6];
            return call;
        } catch (NumberFormatException e){
            return null;
        }
    }
}
