package com.senierr.simple;

import java.io.Serializable;

/**
 * 测试实体类
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public class MyEntity implements Serializable {

    private static final long serialVersionUID = -6849794470754667710L;

    private String ip;
    private String country;
    private String city;
    private String isp;

    public MyEntity(String ip, String country, String city, String isp) {
        this.ip = ip;
        this.country = country;
        this.city = city;
        this.isp = isp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "ip='" + ip + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", isp='" + isp + '\'' +
                '}';
    }
}
