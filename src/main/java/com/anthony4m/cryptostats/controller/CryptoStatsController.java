package com.anthony4m.cryptostats.controller;

import com.anthony4m.cryptostats.model.CoinInfo;
import com.anthony4m.cryptostats.model.HistoryData;
import com.anthony4m.cryptostats.service.CoinsDataService;
import com.anthony4m.cryptostats.utils.Utility;
import io.github.dengliming.redismodule.redistimeseries.Sample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/coins")
@Slf4j
public class CryptoStatsController {

    @Autowired
    private CoinsDataService coinsDataService;

    @GetMapping("/all")
    public ResponseEntity<List<CoinInfo>> fetchAllCoins(){
        return ResponseEntity.ok()
                .body(coinsDataService.fetchAllCoins());
    }

    @GetMapping("/history/{symbol}/{timeperiod}")
    public List<HistoryData> fetchCoinHistory(@PathVariable String symbol,@PathVariable String timeperiod){
        List<Sample.Value> coinTimeData = coinsDataService.fetchCoinHistoryByTimeData(symbol,timeperiod);
        List<HistoryData> coinHistory = coinTimeData
                .stream()
                .map(coin ->
            new HistoryData(
                    Utility.convertUnixTimeToDate(coin.getTimestamp()),
                    Utility.round(coin.getValue(),2)
            )).collect(Collectors.toList());
        return coinHistory;
    }
}
