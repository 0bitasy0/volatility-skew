
import com.opencsv.bean.CsvBindByName;

public class Call {
    @CsvBindByName
    private String secid;

    @CsvBindByName
    private String date;

    @CsvBindByName
    private String days;

    @CsvBindByName
    private String impl_volatility;

    @CsvBindByName
    private String impl_strike;

    @CsvBindByName
    private String cp_flag;

    @CsvBindByName
    private String ticker;

    @CsvBindByName
    private String index_flag;

}
