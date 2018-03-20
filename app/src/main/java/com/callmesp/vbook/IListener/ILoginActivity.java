package com.callmesp.vbook.IListener;

/**
 * Created by Administrator on 2018/1/9.
 */

public interface ILoginActivity {
    void showProgressBar();
    void hideProgressBar();
    void makeToast(String str);
    void loginSuccess(String uuid);
    void loginFail();
}
