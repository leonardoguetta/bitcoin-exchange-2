package at.outdated.bitcoin.exchange.mtgox;

import at.outdated.bitcoin.exchange.api.ExchangeApiClient;
import at.outdated.bitcoin.exchange.api.account.AccountInfo;
import at.outdated.bitcoin.exchange.api.account.Wallet;
import at.outdated.bitcoin.exchange.api.account.WalletTransaction;
import at.outdated.bitcoin.exchange.api.currency.Currency;
import at.outdated.bitcoin.exchange.api.market.MarketDepth;
import at.outdated.bitcoin.exchange.api.market.TickerValue;
import at.outdated.bitcoin.exchange.mtgox.auth.Nonce;
import at.outdated.bitcoin.exchange.mtgox.auth.RequestAuth;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: ebirn
 * Date: 03.05.13
 * Time: 11:18
 * To change this template use File | Settings | File Templates.
 */

public class MtGoxClient extends ExchangeApiClient {

    private final String API_BASE_URL = "https://data.mtgox.com/api/2/";

    //Paths
    private final String API_GET_INFO = "money/info";
    private final String API_GET_WALLET_HISTORY = "money/wallet/history";
    private final String API_TICKER_USD = "BTCUSD/MONEY/TICKER";
    private final String API_TICKER_EUR = "BTCEUR/MONEY/TICKER";
    private final String API_TICKER_FAST_USD = "BTCUSD/MONEY/TICKER_FAST";
    private final String API_TICKER_FAST_EUR = "BTCEUR/MONEY/TICKER_FAST";

    private final String API_WITHDRAW = "MONEY/BITCOIN/SEND_SIMPLE";
    private final String API_LAG = "MONEY/ORDER/LAG";
    private final String API_ADD_ORDER = "BTCUSD/MONEY/ORDER/ADD";

    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";

    private WebTarget apiBaseResource;

    static {
        log = LoggerFactory.getLogger("client.mtgox");
    }

    public MtGoxClient() {
        client= ClientBuilder.newBuilder().register(MtGoxJSONResolver.class).build();
        apiBaseResource = client.target(API_BASE_URL);
    }

    @Override
    public AccountInfo getAccountInfo() {


        Response res = signedRequest(API_GET_INFO, "");

        if(res == null) {
            log.warn("failed to get account info");
            return null;
        }

        MtGoxAccountInfo accountInfo = res.readEntity(ApiAccountInfo.class).getData();

        // fix up wallte structure, load transaction data
        MtGoxWallets wallets = accountInfo.getWallets();
        for(Currency c : wallets.getCurrencies()) {
            Wallet w = wallets.getWallet(c);

            MtGoxWalletHistory history = this.getWalletHistory(c);
            w.setTransactions(history.getTransactions());

            accountInfo.setWallet(w);
        }

        return accountInfo;
    }


    @Override
    public MarketDepth getMarketDepth(Currency base, Currency quote) {
        return null;
    }

    @Override
    public TickerValue getTicker(Currency currency) {

        TickerValue ticker = null;

        String uri = "BTC" + currency.name() + "/money/ticker";
        WebTarget webResource = client.target(API_BASE_URL + uri);
        ApiTickerResponse tickerResponse = simpleGetRequest(webResource, ApiTickerResponse.class);

        if(tickerResponse != null && tickerResponse.getData() != null) {
            tickerResponse.getData().setInCurrency(currency);
            tickerResponse.getData().setItemCurrency(Currency.BTC);
            ticker = tickerResponse.getData().getTickerValue();
        }

        return ticker;
    }


    public TickerValue getFastTicker(Currency currency) {

        TickerValue ticker = null;
        WebTarget webResource = client.target(API_BASE_URL + API_TICKER_FAST_EUR);
        ApiTickerResponse res =  simpleGetRequest(webResource, ApiTickerResponse.class);

        if(res != null) ticker = res.getData().getTickerValue();

        return ticker;
    }


    @Override
    public Number getLag() {

        WebTarget webResource = client.target(API_BASE_URL + API_LAG);
        ApiLagResponse lagResponse = simpleGetRequest(webResource, ApiLagResponse.class);

        if(lagResponse == null) {
            log.warn("no response from api for trading lag");
            return null;
        }

        return lagResponse.getData().getSeconds();
    }

    @Override
    protected Invocation.Builder setupProtectedResource(WebTarget res) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Response signedRequest(String path, String data) {

        try {
            long nonce = Nonce.next()*1000;

            String payload = "nonce="+nonce;

            if(data != null && data.isEmpty() == false) {
                payload += ("&"+data);
            }

            RequestAuth auth = new RequestAuth();
            String signature = auth.hmac(path, payload, getSecret("mtgox"));

            Date requestDate = new Date();

            Invocation.Builder builder = apiBaseResource.path(path).request("application/json");
            Response res = builder.header("Rest-Key", getUserId("mtgox"))
                                        .header("Rest-Sign", signature)
                                        .post(Entity.entity(payload, MediaType.APPLICATION_FORM_URLENCODED_TYPE));



            updateApiLag(requestDate);

            if(res.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL)
                return res;
        }
        //catch(UniformInterfaceException  e) {
        //    handleApiError(e);
        //}
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public MtGoxWalletHistory getWalletHistory(Currency currency) {

        Date requestDate = new Date();
        Response res = signedRequest(API_GET_WALLET_HISTORY, "currency="+currency.name());
        updateApiLag(requestDate);

        if(res == null) return null;

        MtGoxWalletHistory mtGoxWalletHistory = res.readEntity(ApiWalletHistory.class).getData();

        // TODO: fix this somwhere else? remove empty/null Transaction
        Iterator<WalletTransaction> it = mtGoxWalletHistory.getTransactions().iterator();
        while(it.hasNext()) {
            WalletTransaction trans = it.next();
            if(trans.getValue() == null) it.remove();
        }

        return mtGoxWalletHistory;
    }




}
