package org.example;

import java.io.Serializable;

public class Good implements Serializable {
    private static final long serialVersionUID = 1L;
    String goodName;
    String goodId;
    String producer;
    String produceTime;
    String type;
    double purchasePrice;
    double retailPrice;
    int number;
    public Good(){}

    public Good(String goodName,String goodNumber,String produceTime,double retailPrice,int number) {
        this.goodName = goodName;
        this.goodId = goodNumber;
        this.produceTime = produceTime;
        this.retailPrice = retailPrice;
        this.number = number;
    }

    public Good(String goodName,String goodId,String producer,String produceTime,String type,double purchasePrice,double retailPrice,int number) {
        this.goodName = goodName;
        this.goodId = goodId;
        this.producer = producer;
        this.produceTime = produceTime;
        this.type = type;
        this.purchasePrice = purchasePrice;
        this.retailPrice = retailPrice;
        this.number = number;
    }
}
