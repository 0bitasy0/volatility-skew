
import com.opencsv.bean.CsvBindByName;

public class Call {
    public String getSecid() {
        return secid;
    }

    public String getDate() {
        return date;
    }

    public String getDays() {
        return days;
    }

    public String getImpl_volatility() {
        return impl_volatility;
    }

    public String getImpl_strike() {
        return impl_strike;
    }

    public String getCp_flag() {
        return cp_flag;
    }

    public String getTicker() {
        return ticker;
    }

    public String getIndex_flag() {
        return index_flag;
    }

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
