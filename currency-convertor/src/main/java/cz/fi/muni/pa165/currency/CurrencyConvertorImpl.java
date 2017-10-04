package cz.fi.muni.pa165.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;


/**
 * This is base implementation of {@link CurrencyConvertor}.
 *
 * @author petr.adamek@embedit.cz
 */
public class CurrencyConvertorImpl implements CurrencyConvertor {

    private final ExchangeRateTable exchangeRateTable;
    private final Logger logger = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) throws ExternalServiceFailureException {
        logger.trace("Method convert is called.");
        if (sourceCurrency == null || targetCurrency == null || sourceAmount == null) {
            throw new IllegalArgumentException("One of the required parameters is not set");
        }

        BigDecimal rate;
        try{
            rate = exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency);
        }catch(ExternalServiceFailureException e){
            logger.error("Conversion failure due to ExternalServiceException.");
            throw new UnknownExchangeRateException("Unknown exchange rate");
        }
        if (rate == null){
            logger.warn("Missing exchange rate for given currencies.");
            throw new UnknownExchangeRateException("Unknown exchange rate");
        }
        return rate.multiply(sourceAmount).setScale(2, RoundingMode.HALF_EVEN);
    }
}
