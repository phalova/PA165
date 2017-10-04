package cz.fi.muni.pa165.currency;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrencyConvertorImplTest {


    @Mock
    private ExchangeRateTable exchangeRate; // smenny kurz

    private CurrencyConvertor convertor;
    private static Currency CZK = Currency.getInstance("CZK");
    private static Currency EUR = Currency.getInstance("EUR");

    /*
        softly - kolektor kdyz dam jenom assertThat, tak mi to skonci hned
        na prvnim assertu, u softly mi to ukaze, jak dopadly vsechny asserty
    */
    private SoftAssertions softly = new SoftAssertions();

    @Before
    public void setup() {
        exchangeRate = mock(ExchangeRateTable.class);
        convertor = new CurrencyConvertorImpl(exchangeRate);
    }

    @Test
    public void testConvert() throws ExternalServiceFailureException {
        //sets value of exchangeRate
        when(exchangeRate.getExchangeRate(CZK, EUR)).thenReturn(new BigDecimal("0.1"));

        // round half-even
        softly.assertThat(convertor.convert(CZK, EUR, new BigDecimal("15.050"))).isEqualTo("1.50");
        //round up
        softly.assertThat(convertor.convert(CZK, EUR, new BigDecimal("15.051"))).isEqualTo("1.51");
        //round down
        softly.assertThat(convertor.convert(CZK, EUR, new BigDecimal("15.149"))).isEqualTo("1.51");
        //proc jeste toto?
        softly.assertThat(convertor.convert(CZK, EUR, new BigDecimal("15.150"))).isEqualTo("1.52");
        softly.assertAll();
    }


    /*
             * @throws IllegalArgumentException when sourceCurrency, targetCurrency or
             * sourceAmount is null
             * @throws UnknownExchangeRateException when the exchange rate is not known,
             * because the lookup failed or information about given currencies pair is
             * not available
    */

    @Test
    public void testConvertWithNullSourceCurrency() {
        assertThatThrownBy(()->convertor.convert(null, EUR, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("One of the required parameters is not set");
    }

    @Test
    public void testConvertWithNullTargetCurrency() {
        assertThatThrownBy(()->convertor.convert(CZK, null, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("One of the required parameters is not set");
    }

    @Test
    public void testConvertWithNullSourceAmount() {
        assertThatThrownBy(()->convertor.convert(CZK, EUR, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("One of the required parameters is not set");
    }

    @Test
    public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
        when(exchangeRate.getExchangeRate(CZK, EUR)).thenReturn(null);
        assertThatThrownBy(()->convertor.convert(CZK, EUR, BigDecimal.ZERO))
                .isInstanceOf(UnknownExchangeRateException.class);
    }

    @Test
    public void testConvertWithExternalServiceFailure() throws ExternalServiceFailureException {
        //Kdyz volani metody getExchangeRate vyhodi vyjimku,
        // chci, aby program zareagoval - vyhodil vyjimku UnknownExchangeRateException
        doThrow(ExternalServiceFailureException.class).when(exchangeRate).getExchangeRate(CZK,EUR);
        assertThatThrownBy(()->convertor.convert(CZK, EUR, BigDecimal.ZERO))
                .isInstanceOf(UnknownExchangeRateException.class);
    }

}
