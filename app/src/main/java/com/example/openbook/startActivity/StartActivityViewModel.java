package com.example.openbook.startActivity;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.function.Function;

public class StartActivityViewModel extends ViewModel {

    String TAG = "StartActivityViewModelTAG";

    StartActivityModel model = new StartActivityModel();
    private Function<LoginResponseModel, Void> callbackLogin = null;
    private Function<String, Void> callbackIdDuplication = null;

    private MutableLiveData<Boolean> checkLoginBlank;
    private MutableLiveData<String> signUpIdWarning;

    public MutableLiveData<String> getSignUpIdWarning(){
        if(signUpIdWarning == null){
            signUpIdWarning = new MutableLiveData<>();
        }
        return signUpIdWarning;
    }

    public MutableLiveData<Boolean> getCheckLoginBlank() {
        if (checkLoginBlank == null) {
            checkLoginBlank = new MutableLiveData<>();
        }
        return checkLoginBlank;
    }

    private MutableLiveData<LoginResponseModel> loginModel;

    public MutableLiveData<LoginResponseModel> getLoginModel() {
        if(loginModel == null){
            loginModel = new MutableLiveData<>();
        }
        return loginModel;
    }

    public void checkLoginBlank(String id, String password) {
        if(id.isEmpty() || password.isEmpty()){
            checkLoginBlank.setValue(false);
        }else{
            checkLoginBlank.setValue(true);
            requestLogin(id, String.valueOf(password.hashCode()));
        }
    }


    public void requestLogin(String id, String password) {

        callbackLogin = new Function<LoginResponseModel, Void>() {
            @Override
            public Void apply(LoginResponseModel loginResponseModel) {
                Log.d(TAG, "callbackLogin: " + loginResponseModel);

                if(loginResponseModel.result == "success"){
                    loginModel.setValue(loginResponseModel);
                }

                return null;
            }
        };

        model.requestLogin(id, password, callbackLogin);

    }

    public void checkIdDuplication(String id){

        if(id.isEmpty()){
            signUpIdWarning.setValue("아이디를 입력해주세요.");
        }else{
            signUpIdWarning.setValue("");
            requestIdDuplication(id);
        }
    }

    public void requestIdDuplication(String id){
        Log.d(TAG, "requestIdDuplication: " + id);
        callbackIdDuplication = new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Log.d(TAG, "Duplication callback: " + s);
                return apply(s);
            }
        };

        model.requestIdDuplication(id, callbackIdDuplication);
    }


}
