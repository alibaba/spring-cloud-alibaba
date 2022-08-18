package io.appactive.demo.common.service.springcloud;

import io.appactive.demo.common.RPCType;
import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ProductDAO {

    @Autowired
    private ProductService productService;

    public ResultHolder<List<Product>> list(){
        return productService.list();
    }

    public ResultHolder<Product> detail(String rId, String pId){
        return productService.detail(rId, pId);
    }

    public ResultHolder<Product> detailHidden(String pId){
        return productService.detailHidden(pId);
    }

    public ResultHolder<String> buy(String rId, String pId, Integer number){
        return productService.buy(RPCType.SpringCloud.name(), rId, pId, number);
    }

    @Autowired(required = false)
    RestTemplate restTemplate;
    public ResultHolder<List<Product>> listTemplate(){
        if (restTemplate !=null ){
            return restTemplate.getForObject("http://product/list", ResultHolder.class);
        }
        return productService.list();
    }
    public ResultHolder<Product> detailTemplate(String rId, String pId){
        if (restTemplate !=null ){
            Map<String, String> params = new HashMap<>(2);
            params.put("rId", rId);
            params.put("pId", pId);
            return restTemplate.getForObject("http://product/detail", ResultHolder.class, params);
        }
        return productService.detail(rId, pId);
    }

    @FeignClient(name = "product")
    public interface ProductService {

        @RequestMapping(value = "/list/")
        ResultHolder<List<Product>>  list();

        @RequestMapping("/detail/")
        ResultHolder<Product> detail(@RequestParam(name = "rId") String rId,
                                     @RequestParam(name = "pId") String pId
        );

        @RequestMapping("/detailHidden/")
        ResultHolder<Product> detailHidden(@RequestParam(name = "pId") String pId
        );

        @RequestMapping("/buy/")
        ResultHolder<String> buy(@RequestParam(name = "rpcType") String rpcType,
                                 @RequestParam(name = "rId") String rId,
                                  @RequestParam(name = "pId") String pId,
                                  @RequestParam(name = "number") Integer number
        );

    }
}
