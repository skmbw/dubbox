package com.cnblogs.yjmyzz.demo.service.api.dubbo;

import org.codehaus.jackson.map.annotate.JsonFilter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangjunming on 2016/11/2.
 */
public class User implements Serializable {

    private int userId;

    private String userName;

//    @JsonField
    private Date birthday;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}
