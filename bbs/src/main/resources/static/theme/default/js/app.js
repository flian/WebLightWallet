function suc(msg) {
    layer.msg(msg, {icon: 1, anim: 5, offset: 't', time: 20000});
}

function err(msg) {
    layer.msg(msg, {icon: 2, anim: 6, offset: 't', time: 20000});
}

function tip(msg) {
    layer.msg(msg, {offset: 't'});
}

function openSendCoin(send,coin,toUser,toAddress,amount,me,token){
    $("#_sendCoin").text(coin);
    $("#_coinName").val(coin);
    $("#_sendCoinToUser").val(toUser);
    $("#_sendCoinToAddress").val(toAddress);
    $("#_sendCoinAmount").val(amount);
    $("#_sendCoinUuid").val(uuid(64,16));
    let coinLetInfo = {};
    req('get','/api/coin/'+me+'/coins',{},function (coinDetail){
        if(coinDetail.code === 200){
            coinDetail.detail.forEach(function (cc,index) {
               if(cc.coin === coin){
                   $("#_totalAmount").text('可用余额:'+cc.availableAmount);
               }
            });
        }
    });
    req('get','/api/coin/'+coin+'/netInfo',{},{},function(coinNetInfoData){
        if(coinNetInfoData.code === 200){
            coinLetInfo = coinNetInfoData.detail;
            $("#_sendCoin").text(coinLetInfo.coinDesc+",["+coinLetInfo.currentNet+":"+coinLetInfo.currentNetDesc+"]");
        }
    });
    layer.open({
        type:1,
        title:send?'转账':'打赏',
        area:['800px','600px'],
        content:$("#_sendCoinFormDiv"),
        btn:['发送','取消'],
        yes:()=>{
            checkAndSendCoin(me,token);
        }
    });
}
function checkAndSendCoin(me,token){
    let coin = $("#_coinName").val();
    let send2User= $("#_sendCoinToUser").val();
    let send2Address = $("#_sendCoinToAddress").val();
    let passwordIn = $("#_sendCoinPassword").val();
    if(!send2User&&!send2Address){
        err("转账用户和地址不能同时为空!!!");
        return false;
    }
    if(!passwordIn){
        err("密码不能为空！！！");
        return false;
    }
    let amt = $("#_sendCoinAmount").val();
    let uuid = $("#_sendCoinUuid").val();
    let rsaKey = getOneRsaKey();
    let encryptedPassword = RSAEncrypt(getRsaPublicKey(rsaKey),passwordIn);
    let idxKey = getRsaIdxKey(rsaKey);
    let postUrl = "/api/coin/"+me+"/"+coin+"/transfer";
    console.log(postUrl);
    let requestJson={"coinSymbol":coin,"toUserName":send2User,"toAddress":send2Address,"amount":amt,"encryptedPassword":encryptedPassword,"pubIdxKey":idxKey,"uuid":uuid};
    req("post",postUrl,requestJson,token,function (trsResult){
        //reset password filed after one call
        $("#_sendCoinPassword").val('');
        if(trsResult.code === 200){
            suc("发送硬币成功~");
            return true;
        }else {
            err(trsResult.description);
            return false;
        }
    });
}

function uuid(len, radix) {
    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    var uuid = [], i;
    radix = radix || chars.length;

    if (len) {
        // Compact form
        for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random()*radix];
    } else {
        // rfc4122, version 4 form
        var r;

        // rfc4122 requires these characters
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
        uuid[14] = '4';

        // Fill in random data.  At i==19 set the high bits of clock sequence as
        // per rfc4122, sec. 4.1.5
        for (i = 0; i < 36; i++) {
            if (!uuid[i]) {
                r = 0 | Math.random()*16;
                uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
            }
        }
    }

    return uuid.join('');
}

function getOneRsaKey(){
    let rsaPubKey = {};
    req("get","/api/coin/public/key/rsa",{},{},function (data){
        if(data.code === 200) {
            rsaPubKey = data.detail;
        }
    });
    return rsaPubKey;
}
function getRsaPublicKey(rsaPubKey){
    return rsaPubKey.publicKey;
}
function getRsaIdxKey(rsaPubKey){
    return rsaPubKey.idxKey;
}

function RSAEncrypt(pubKey,data){
     let encrypt = new JSEncrypt();
     encrypt.setPublicKey(pubKey);
     return encrypt.encrypt(data);
}

function req(method, url, body, token, cb) {
    let setup = {
        cache: false,
        async: false,
        dataType: 'json',
        contentType: 'application/json'
    };
    let _token = typeof arguments[2] === "string" ? arguments[2]
        : typeof arguments[3] === "string" ? arguments[3]
            : typeof arguments[4] === "string" ? arguments[4] : undefined;
    if (_token) {
        setup.headers = {
            'token': _token
        }
    }
    $.ajaxSetup(setup);
    let reqObj;
    if (method.toUpperCase() === "GET" || method.toUpperCase() === "POST" || method.toUpperCase() === "PUT" || method.toUpperCase() === "DELETE") {
        if (typeof arguments[2] === "object") {
            let data = JSON.stringify(body);
            if (method.toUpperCase() === "GET") {
                data = $.param(body);
            }
            reqObj = $.ajax({url, method, data});
        } else {
            reqObj = $.ajax({url, method});
        }
    } else {
        throw new Error("request method not support!");
    }
    let _cb = typeof arguments[2] === "function" ? arguments[2]
        : typeof arguments[3] === "function" ? arguments[3]
            : typeof arguments[4] === "function" ? arguments[4] : undefined;
    if (_cb) reqObj.done(_cb);
    reqObj.fail(err => console.error(err));
}