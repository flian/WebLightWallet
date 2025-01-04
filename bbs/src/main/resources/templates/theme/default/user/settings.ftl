<#include "../layout/layout.ftl"/>
<@html page_title="设置" page_tab="settings">
    <div class="row">
        <div class="col-md-9">
            <#if !user.active>
                <div class="alert alert-danger">
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                    <strong>你的帐号还没有激活，请进入邮箱点击激活邮箱中的链接进行激活 或者 <a href="javascript:;" id="sendActiveEmail">重新发送</a>
                        激活链接</strong>
                </div>
            </#if>
            <div class="card">
                <div class="card-header">设置</div>
                <div class="card-body">
                    <br>
                    <form class="form-horizontal" onsubmit="return;">
                        <div class="form-group row">
                            <label for="username" class="col-sm-2 control-label">用户名</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="username" name="username" disabled
                                       value="${user.username}"
                                       placeholder="用户名">
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="telegramName" class="col-sm-2 control-label">Telegram用户名</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="telegramName" name="telegramName"
                                       value="${user.telegramName!}"
                                       placeholder="Telegram用户名">
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="website" class="col-sm-2 control-label">个人主页</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="website" name="website"
                                       value="${user.website!}"
                                       placeholder="个人主页">
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="bio" class="col-sm-2 control-label">个人简介</label>
                            <div class="col-sm-10">
                                <textarea name="bio" id="bio" rows="3" class="form-control" placeholder="个人简介">${user.bio!?html}</textarea>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-sm-2 col-sm-10">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" id="emailNotification"
                                               <#if user.emailNotification>checked</#if>>
                                        有新消息发送邮件
                                    </label>
                                </div>
                                <#--<div class="checkbox">
                                  <label>
                                    <input type="checkbox" id="telegramNotification"> 有新消息发送Telegram通知
                                  </label>
                                </div>-->
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-sm-2 col-sm-10">
                                <button type="button" id="settings_btn" class="btn btn-info">提交</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div class="card">
                <div class="card-header">IFC钱包设置</div>
                <div class="card-body">
                    <#if coinWalletMap['INFINITE_COIN']??>
                        <table class="table">
                            <tr>
                                <th>IFC</th>
                                <th>地址</th>
                                <th>余额</th>
                                <th>刷新余额</th>
                            </tr>
                            <tr>
                                <td>IFC</td>
                                <td>${coinWalletMap['INFINITE_COIN'].primaryAddress}</td>
                                <td>${coinWalletMap['INFINITE_COIN'].balance}</td>
                                <td><button type="button" id="ifcRefreshBalanceBtn" class="btn btn-info">刷新</button></td>
                            </tr>
                        </table>
                    </#if>
                    <form onsubmit="return;" class="form-horizontal">
                        <#if coinWalletMap['INFINITE_COIN']??>
                        <div class="form-group row">
                            <label for="ifcWalletOldPassword" class="col-sm-2 control-label">原密码</label>
                            <div class="col-sm-10">
                                <div class="input-group">
                                    <input type="password" name="ifcWalletOldPassword" id="ifcWalletOldPassword" class="form-control"  placeholder="原密码"/>
                                </div>
                            </div>
                        </div>
                        </#if>
                        <div class="form-group row">
                            <label for="ifcWalletPassword" class="col-sm-2 control-label">新密码</label>
                            <div class="col-sm-10">
                                <div class="input-group">
                                    <input type="password" name="ifcWalletPassword" id="ifcWalletPassword" class="form-control"  placeholder="新密码"/>
                                </div>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="ifcWalletConfirmPassword" class="col-sm-2 control-label">确认密码</label>
                            <div class="col-sm-10">
                                <input type="password" name="ifcWalletConfirmPassword" id="ifcWalletConfirmPassword" class="form-control" placeholder="确认密码"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-sm-2 col-sm-10">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" name="ifcRememberPassword" id="ifcRememberPassword">记住密码。
                                        <br/>
                                        系统会记住密码，后续操作会比较方便，但是不太安全。不记住密码，忘记密码会比较麻烦：）请权衡后勾选！！！
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-sm-2 col-sm-10">
                                <button type="button" id="ifcWalletSubmit" class="btn btn-info">开通IFC钱包/修改IFC钱包密码</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <div class="card">
                <div class="card-header">修改邮箱</div>
                <div class="card-body">
                    <form onsubmit="return;" class="form-horizontal">
                        <div class="form-group row">
                            <label for="email" class="col-sm-2 control-label">邮箱</label>
                            <div class="col-sm-10">
                                <div class="input-group">
                                    <input type="email" name="email" id="email" class="form-control" value="${user.email!}" placeholder="邮箱"/>
                                    <span class="input-group-append">
                                        <button type="button" id="sendEmailCode" class="btn btn-info" autocomplete="off" data-loading-text="发送中...">发送验证码</button>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="code" class="col-sm-2 control-label">验证码</label>
                            <div class="col-sm-10">
                                <input type="text" name="code" id="code" class="form-control" placeholder="验证码"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-sm-2 col-sm-10">
                                <button type="button" id="settings_email_btn" class="btn btn-info">更改邮箱</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div class="card">
                <div class="card-header">修改头像</div>
                <div class="card-body">
                    <form onsubmit="return;" class="form-horizontal">
                        <div class="form-group row">
                            <label for="" class="col-sm-2 control-label" style="vertical-align: middle">当前头像</label>
                            <div class="col-sm-10">
                                <img src="${user.avatar!"/static/theme/default/images/ifc.jpeg"}" class="avatar avatar-lg" alt="avatar"/>&nbsp;&nbsp;
                                <img src="${user.avatar!"/static/theme/default/images/ifc.jpeg"}" class="avatar" style="vertical-align: bottom" alt="avatar"/>&nbsp;&nbsp;
                                <img src="${user.avatar!"/static/theme/default/images/ifc.jpeg"}" class="avatar avatar-sm" style="vertical-align: bottom" alt="avatar"/>
                            </div>
                            <div class="offset-sm-2 col-sm-10" style="margin-top: 10px;">
                                <a href="javascript:;" id="selectAvatar">上传新头像</a>
                                <input type="file" class="d-none" name="file" id="file"/>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div class="card">
                <div class="card-header">修改密码</div>
                <div class="card-body">
                    <form onsubmit="return;" class="form-horizontal">
                        <div class="form-group row">
                            <label for="oldPassword" class="col-sm-2 control-label">旧密码</label>
                            <div class="col-sm-10">
                                <input type="password" name="oldPassword" id="oldPassword" class="form-control" placeholder="旧密码"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label for="newPassword" class="col-sm-2 control-label">新密码</label>
                            <div class="col-sm-10">
                                <input type="password" name="newPassword" id="newPassword" class="form-control" placeholder="新密码"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-sm-2 col-sm-10">
                                <button type="button" id="settings_pwd_btn" class="btn btn-info">更改密码</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <div class="col-md-3 hidden-xs">
            <#include "../components/user_info.ftl"/>
            <#include "../components/token.ftl"/>
        </div>
    </div>
    <script>
        $(function () {
            $("#settings_btn").click(function () {
                var telegramName = $("#telegramName").val();
                var website = $("#website").val();
                var bio = $("#bio").val();
                var emailNotification = $("#emailNotification").is(":checked");
                req("put", "/api/settings", {
                    telegramName: telegramName,
                    website: website,
                    bio: bio,
                    emailNotification: emailNotification,
                }, "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("更新个人资料成功");
                        setTimeout(function () {
                            window.location.reload();
                        }, 700);
                    } else {
                        err(data.description);
                    }
                });
            });

            // 上传头像
            $("#selectAvatar").click(function () {
                $("#file").click();
            });
            $("#file").change(function () {
                var fd = new FormData();
                fd.append("file", document.getElementById("file").files[0]);
                fd.append("type", "avatar");
                fd.append("token", "${_user.token!}");
                $.post({
                    url: "/api/upload",
                    data: fd,
                    dataType: 'json',
                    headers: {
                        'token': '${_user.token!}'
                    },
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.code === 200) {
                            if (data.detail.errors.length === 0) {
                                suc("修改头像成功");
                                $.each($(".avatar "), function (i, v) {
                                    $(v).attr("src", data.detail.urls[0]);
                                })
                            } else {
                                err(data.detail.errors[0]);
                            }
                        } else {
                            err(data.description);
                        }
                    }
                })
            });

            //refresh balance
            $("#ifcRefreshBalanceBtn").on("click",function (){
                req("post","/api/coin/${user.username}/refresh/INFINITE_COIN",{},"${_user.token!}",function (refreshResult){
                    if(refreshResult.code === 200){
                        suc("成功");
                        setTimeout(function () {
                            window.location.reload();
                        }, 700);
                    } else {
                        err("失败");
                    }
                });
            });

            //IFC初始/修改密码
            $("#ifcWalletSubmit").on("click",function (){
                let oldPassword = $("#ifcWalletOldPassword").val();
                let password = $("#ifcWalletPassword").val();
                let confirmPassword = $("#ifcWalletConfirmPassword").val();
                let ifcRememberPassword = $("#ifcRememberPassword").is(":checked");
                if(!password || !(password === confirmPassword)){
                    err("密码为空或密码和确认密码不匹配。");
                }else {
                    req("get","/api/coin/public/key/rsa",{},{},function (data){
                        let resPubKey = '';
                        if(data.code === 200){
                            resPubKey = data.detail;
                            let encryptedOldPassword = null;
                            if(oldPassword){
                                //if contain old password,means change password.
                                encryptedOldPassword = RSAEncrypt(resPubKey.publicKey,oldPassword);
                            }
                            let encryptedPassword = RSAEncrypt(resPubKey.publicKey,password);
                            let idxKey = resPubKey.idxKey;
                            req("post","/api/coin/${user.username}/init/coin"
                                ,{"coinSymbol":"INFINITE_COIN","encryptedOldPassword":encryptedOldPassword,"encryptedPassword":encryptedPassword,
                                    "pubIdxKey":idxKey,"saveEncryptedPasswordForThisWallet":ifcRememberPassword},"${_user.token!}",function (changeResult){
                                    if(changeResult.code === 200){
                                        suc("初始化/修改IFC钱包密码成功");
                                        setTimeout(function () {
                                            window.location.reload();
                                        }, 700);
                                    }else {
                                        err(changeResult.description);
                                    }
                            });
                        }else {
                            err(data.description);
                        }
                    });

                }
            });
            // 发送激活邮件
            $("#sendActiveEmail").on("click", function () {
                req("get", "/api/settings/sendActiveEmail", "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("发送成功");
                    } else {
                        err(data.description);
                    }
                });
            })

            // 更改邮箱
            $("#sendEmailCode").on("click", function () {
                var loadingBtn = $(this).button("loading");
                var email = $("#email").val();
                req("get", "/api/settings/sendEmailCode", {email}, "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("发送成功");
                    } else {
                        err(data.description);
                    }
                    loadingBtn.button("reset");
                });
            })
            $("#settings_email_btn").click(function () {
                var email = $("#email").val();
                var code = $("#code").val();
                req("put", "/api/settings/updateEmail", {email, code}, "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("更改成功");
                        setTimeout(function () {
                            window.location.reload();
                        }, 700);
                    } else {
                        err(data.description);
                    }
                });
            })

            // 更改密码
            $("#settings_pwd_btn").click(function () {
                var oldPassword = $("#oldPassword").val();
                var newPassword = $("#newPassword").val();
                if (!oldPassword) {
                    err("请输入旧密码");
                    return;
                }
                if (!newPassword) {
                    err("请输入新密码");
                    return;
                }
                req("put", "/api/settings/updatePassword", {oldPassword, newPassword}, "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("修改密码成功");
                        setTimeout(function () {
                            window.location.reload();
                        }, 700);
                    } else {
                        err(data.description);
                    }
                });
            });
        })
    </script>
</@html>
