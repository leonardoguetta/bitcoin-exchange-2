package at.outdated.bitcoin.exchange.btce;

import at.outdated.bitcoin.exchange.api.ExchangeApiClient;
import at.outdated.bitcoin.exchange.api.account.AccountInfo;
import at.outdated.bitcoin.exchange.api.currency.Currency;
import at.outdated.bitcoin.exchange.api.market.TickerValue;
import com.sun.jersey.api.client.WebResource;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 26.05.13
 * Time: 23:43
 * To change this template use File | Settings | File Templates.
 */
public class BtcEApiClient extends ExchangeApiClient {

    @Override
    public AccountInfo getAccountInfo() {



        return new BtcEAccountInfo();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected <R> R simpleRequest(WebResource resource, Class<R> resultClass, String httpMethod, Object payload) {

        R result = null;

        String resultStr = super.simpleRequest(resource, String.class, httpMethod, payload);
        result = BtcEJSONResolver.convertFromJson(resultStr, resultClass);

        return result;
    }

    @Override
    public TickerValue getTicker(Currency currency) {

        // https://btc-e.com/api/2/btc_usd/ticker

        WebResource tickerResource = client.resource("https://btc-e.com/api/2/btc_"+currency.name().toLowerCase()+"/ticker");

        TickerResponse response = simpleGetRequest(tickerResource, TickerResponse.class);

        BtcETickerValue btcETickerValue = response.getTicker();

        TickerValue value = btcETickerValue.getTickerValue();
        value.setCurrency(currency);

        return value;
    }

    @Override
    public Number getLag() {
        return 0.12345678910;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected WebResource.Builder setupProtectedResource(WebResource res) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}