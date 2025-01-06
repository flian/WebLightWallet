<div class="card" id="_sendCoinFormDiv" style="display: none;">
    <div class="card-body">
        <form onsubmit="return;" class="form-horizontal">
            <div class="form-group row">
                <label  class="col-sm-2 control-label">币种</label>
                <div class="col-sm-10">
                    <div class="input-group">
                        <span id="_sendCoin">IFC</span>
                    </div>
                </div>
            </div>
            <div class="form-group row">
                <label for="_sendCoinToUser" class="col-sm-2 control-label">接收人</label>
                <div class="col-sm-10">
                    <div class="input-group">
                        <span><input type="text" name="toUser" id="_sendCoinToUser" class="form-control" placeholder="接收人"/></span>
                    </div>
                </div>
            </div>
            <div class="form-group row">
                <label for="_sendCoinToAddress" class="col-sm-2 control-label">地址</label>
                <div class="col-sm-10">
                    <div class="input-group">
                        <span><input type="text" name="toAddress" id="_sendCoinToAddress" class="form-control" placeholder="地址" style="width: 400px"/></span>
                    </div>
                </div>
            </div>
            <div class="form-group row">
                <label for="_sendCoinAmount" class="col-sm-2 control-label">金额</label>
                <div class="col-sm-10">
                    <div class="input-group">
                        <span><input type="text" name="amount" id="_sendCoinAmount" class="form-control" placeholder="金额" value="1001"/></span>
                        <span id="_totalAmount"></span>
                    </div>
                </div>
            </div>
            <div class="form-group row">
                <label for="_sendCoinPassword" class="col-sm-2 control-label">密码</label>
                <div class="col-sm-10">
                    <input type="password" name="password" id="_sendCoinPassword" class="form-control" placeholder="密码"/>
                    <input type="hidden" name="sendCoinUuid" id="_sendCoinUuid" class="form-control" placeholder="uuid"/>
                </div>
            </div>

        </form>
    </div>
</div>