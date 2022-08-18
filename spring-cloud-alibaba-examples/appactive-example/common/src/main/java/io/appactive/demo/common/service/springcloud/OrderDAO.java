package io.appactive.demo.common.service.springcloud;

import io.appactive.demo.common.entity.ResultHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Service
public class OrderDAO {

    @Autowired
    private OrderService orderService;

    public ResultHolder<String> buy(String rId, String pId, Integer number){
        return orderService.buy(rId, pId, number);
    }

    @FeignClient(name = "storage")
    public interface OrderService {

        @GetMapping("/buy/")
        ResultHolder<String> buy(@RequestParam(name = "rId") String rId,
                                 @RequestParam(name = "id") String id,
                                 @RequestParam(name = "number") Integer number);
    }
}
