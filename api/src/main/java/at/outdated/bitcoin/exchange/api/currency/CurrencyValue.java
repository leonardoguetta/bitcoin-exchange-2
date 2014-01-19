package at.outdated.bitcoin.exchange.api.currency;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 02.05.13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class CurrencyValue {

    public static final MathContext CURRENCY_MATH_CONTEXT = new MathContext(7, RoundingMode.HALF_UP);


    private BigDecimal value = new BigDecimal("0.000000000");
    private Currency currency = null;

    public CurrencyValue() {

    }

    @Deprecated
    public CurrencyValue(double value, Currency curr) {
        this.value = new BigDecimal(value, CURRENCY_MATH_CONTEXT);
        //this.value_int = (long)(value * curr.getDivide());
        currency = curr;
    }

    public CurrencyValue(BigDecimal value, Currency curr) {
        this.value = value;
        currency = curr;
    }

    public CurrencyValue(Currency curr) {
        this.value = new BigDecimal(0.0, CURRENCY_MATH_CONTEXT);
        //this.value_int = (long)(value * curr.getDivide());
        currency = curr;
    }

    public CurrencyValue(CurrencyValue value) {
        this.currency = value.currency;
        this.value = value.value;
    }

    public double doubleValue() {
        return value.doubleValue();
    }


    public Currency getCurrency() {
        return currency;
    }

    public CurrencyValue add(CurrencyValue other) {
        checkArgument(other);
        this.value = this.value.add(other.value, CURRENCY_MATH_CONTEXT);

        return this;
    }

    public CurrencyValue add(BigDecimal other) {
        this.value = this.value.add(other, CURRENCY_MATH_CONTEXT);

        return this;
    }

    public CurrencyValue subtract(CurrencyValue other) {

        checkArgument(other);
        this.value = this.value.subtract(other.value, CURRENCY_MATH_CONTEXT);
        return this;
    }

    public CurrencyValue subtract(BigDecimal other) {
        this.value = this.value.subtract(other, CURRENCY_MATH_CONTEXT);
        return this;
    }

    public CurrencyValue multiply(BigDecimal mul) {
        value = this.value.multiply(mul, CURRENCY_MATH_CONTEXT);

        return this;
    }

    public CurrencyValue multiply(CurrencyValue other) {
        checkArgument(other);
        this.value = this.value.multiply(other.value, CURRENCY_MATH_CONTEXT);

        return this;
    }

    public CurrencyValue divide(CurrencyValue other) {
        checkArgument(other);
        this.value = this.value.divide(other.value, CURRENCY_MATH_CONTEXT);
        return this;
    }
    public CurrencyValue divide(BigDecimal div) {
        this.value = this.value.divide(div, CURRENCY_MATH_CONTEXT);
        return this;
    }

    private void checkArgument(CurrencyValue other) {
        if(this.currency != other.currency) {
            throw new IllegalArgumentException("currency mismatch");
        }
    }

    public boolean isMoreThan(CurrencyValue other) {
        return value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(CurrencyValue other) {
        return value.compareTo(other.value) < 0;
    }

    public String toString() {
        return valueToString() + " " + currency.name();
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNonNegative() {
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String valueToString() {
        return value.toPlainString();
    }
}
