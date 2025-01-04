package co.yiiu.pybbs.directive;

import co.yiiu.pybbs.service.IUserWalletService;
import co.yiiu.pybbs.service.vo.CoinRank;
import freemarker.core.Environment;
import freemarker.template.*;
import org.lotus.webwallet.base.api.enums.SupportedCoins;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@Component
public class CoinBalanceDirective implements TemplateDirectiveModel {


    @Resource
    private IUserWalletService userWalletService;

    @Override
    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody
            templateDirectiveBody) throws TemplateException, IOException {
        Integer limit = Integer.parseInt(map.get("limit").toString());
        SupportedCoins coin = SupportedCoins.valueOf(map.get("coinSymbol").toString());
        if (limit > 100) limit = 100;
        CoinRank coinRank = userWalletService.listTopUsers(coin, limit);
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28);
        environment.setVariable("coinRank", builder.build().wrap(coinRank));
        templateDirectiveBody.render(environment.getOut());
    }
}
