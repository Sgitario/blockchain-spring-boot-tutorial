package org.sgitario.lottery.rest;

import java.io.IOException;

import org.sgitario.lottery.blockchain.model.Balance;
import org.sgitario.lottery.blockchain.model.Player;
import org.sgitario.lottery.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LotteryController {

    private final LotteryService lotteryService;

    @Autowired
    public LotteryController(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }

    @GetMapping("/lottery/balance")
    public Balance getLotteryBalance() throws IOException {
        return new Balance(lotteryService.getBalance());
    }

    @PostMapping("/lottery/join")
    public void joinLottery(@RequestBody Player player) throws Exception {
        lotteryService.join(player);
    }
}
