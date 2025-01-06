<#macro score limit coinSymbol top100=false>
    <div class="card card500">
        <@coin_balance limit=limit coinSymbol=coinSymbol>
        <div class="card-header">
            ${coinRank.coin}富豪榜
            <#if !top100>
                <span class="pull-right"><a href="/top100">Top100</a></span>
            </#if>
        </div>
        <table class="table">

            <#if top100>
                <tr>
                    <th>用户名</th>
                    <th>${coinRank.coin}地址</th>
                    <th>${coinRank.coin}余额</th>
                    <th>打赏</th>
                </tr>
            </#if>
                <#list coinRank.users as user>
                    <tr>
                        <td><a href="/user/${user.username}">${user.username}</a></td>
                        <td><a href="https://chainz.cryptoid.info/ifc/address.dws?${coinRank.wallets[user_index].primaryAddress}.htm">详情</a></td>
                        <td>${coinRank.wallets[user_index].balance}</td>
                        <td>
                            <#if _user??>
                            <button type="button" onclick="openSendCoin(false,'${coinRank.coin}','${user.username}','${coinRank.wallets[user_index].primaryAddress}',1000,'${_user.username}','${_user.token!}')">打赏
                            </button>
                            </#if>
                        </td>
                    </tr>
                </#list>
        </table>
        </@coin_balance>
    </div>
</#macro>
