package com.anthony4m.cryptostats.service;

import com.anthony4m.cryptostats.model.CoinData;
import com.anthony4m.cryptostats.model.CoinInfo;
import com.anthony4m.cryptostats.model.CoinPriceHistory;
import com.anthony4m.cryptostats.model.CoinPriceHistoryExchangeRate;
import com.anthony4m.cryptostats.model.Coins;
import com.anthony4m.cryptostats.utils.HttpUtils;
import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import io.github.dengliming.redismodule.redistimeseries.DuplicatePolicy;
import io.github.dengliming.redismodule.redistimeseries.RedisTimeSeries;
import io.github.dengliming.redismodule.redistimeseries.Sample;
import io.github.dengliming.redismodule.redistimeseries.TimeSeriesOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class CoinsDataService {
    private static final String GET_COINS_API = "https://coinranking1.p.rapidapi.com/coins?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=24h&tiers%5B0%5D=1&orderBy=marketCap&orderDirection=desc&limit=50&offset=0";
    private static final String GET_COIN_HISTORY_API = "https://coinranking1.p.rapidapi.com/coin/";
    private static final String COIN_HISTORY_TIME_PERIOD_PARAM = "/history?timePeriod=";
    private static final List <String> timePeriods = List.of("24h","7d","30d","3m","1y","3y","5y");
    private static final String REDIS_COINS_KEY = "coins";
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisJSON redisJSON;

    @Autowired
    private RedisTimeSeries redisTimeSeries;

    public void fetchCoins(){
        log.info("inside Fetch coins");
       ResponseEntity<Coins> coinsResponseEntity = restTemplate.exchange(GET_COINS_API,
                HttpMethod.GET,
                HttpUtils.getHttpEntity(),
                Coins.class);
       storeCoinsToRedisJSON(coinsResponseEntity.getBody());
    }

    private void storeCoinsToRedisJSON(Coins coins) {
        redisJSON.set(REDIS_COINS_KEY, SetArgs.Builder.create(".", GsonUtils.toJson(coins)));
    }

    public void fetchCoinHistory(){
        log.info("Fetch Coins History");
        List<CoinInfo> allCoins = getAllCoinsFromRedisJSON();
        allCoins.forEach(coinInfo -> {
            timePeriods.forEach(timePeriod ->{
              fetchCoinHistoryForTimePeriod(coinInfo,timePeriod);
            });
        });
    }

    private void fetchCoinHistoryForTimePeriod(CoinInfo coinInfo, String timePeriod) {
        log.info("Fetching History of Coin {} for Time Period {}",coinInfo.getName(),timePeriod);
        String url = GET_COIN_HISTORY_API + coinInfo.getUuid() + COIN_HISTORY_TIME_PERIOD_PARAM + timePeriod;
        ResponseEntity<CoinPriceHistory> coinPriceHistoryResponseEntity =  restTemplate.exchange(url,
                HttpMethod.GET,
                HttpUtils.getHttpEntity(),
                CoinPriceHistory.class);
        log.info("Data fetched from Api for coin history of {} for time period {}", coinInfo.getName(),timePeriod);
        storeCoinHistoryToRedisTimeSeries(coinPriceHistoryResponseEntity.getBody(), coinInfo.getSymbol(), timePeriod);
    }

    private void storeCoinHistoryToRedisTimeSeries(CoinPriceHistory coinPriceHistory, String symbol, String timePeriod) {
        log.info("Storing Coin history of {} for time period {} into redis time series",symbol,timePeriod);
        List<CoinPriceHistoryExchangeRate> coinPriceHistoryExchangeRates = coinPriceHistory.getData().getHistory();
        //How key is stored
        //Symbol:Timeperiod
        //BTC:24h, BTC:1y ETH:3y
        coinPriceHistoryExchangeRates.stream().filter(coinPriceHistoryExchangeRate -> coinPriceHistoryExchangeRate.getPrice() != null && coinPriceHistoryExchangeRate.getTimestamp() != null)
                .forEach(coinPriceHistoryExchangeRate -> {redisTimeSeries.add(
                        new Sample(symbol + ":" + timePeriod,Sample.Value.of(Long.parseLong(coinPriceHistoryExchangeRate.getTimestamp()),
                                Double.parseDouble(coinPriceHistoryExchangeRate.getPrice())
                        )),
                new TimeSeriesOptions()
                        .unCompressed()
                        .duplicatePolicy(DuplicatePolicy.LAST));
                });
        log.info("Complete: Stored coin into time series");
    }

    private List<CoinInfo> getAllCoinsFromRedisJSON() {
        CoinData coinData = redisJSON.get(REDIS_COINS_KEY, CoinData.class,new GetArgs().path(".data").indent("\t").newLine("\n").space(" "));
        return coinData.getCoins();
    }
}
