function suc(msg) {
    layer.msg(msg, {icon: 1, anim: 5, offset: 't', time: 20000});
}

function err(msg) {
    layer.msg(msg, {icon: 2, anim: 6, offset: 't', time: 20000});
}

function tip(msg) {
    layer.msg(msg, {offset: 't'});
}

function openSendCoin(coin,toUser,toAddress,amount){
    layer.open({
        type:1,
        title:'打赏',
        area:['800px','600px'],
        content:$("#_sendCoinFormDiv"),
        btn:['发送','取消'],
        yes:()=>{
            suc("haha");
        }
    });
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