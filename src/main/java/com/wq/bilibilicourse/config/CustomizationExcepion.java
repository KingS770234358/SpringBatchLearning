package com.wq.bilibilicourse.config;

/**
 * 重试 Step 捕捉的异常
 */
public class CustomizationExcepion extends Exception{
    public CustomizationExcepion(){
        super();
    }
    public CustomizationExcepion(String msg){
        super(msg);
    }

}
